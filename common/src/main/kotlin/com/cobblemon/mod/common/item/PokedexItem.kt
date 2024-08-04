/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawText
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.ui.PokedexUIPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.MapUpdatePacket
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Math.clamp
import javax.imageio.ImageIO

class PokedexItem(val type: String) : CobblemonItem(Item.Properties()) {

    @JvmField
    var zoomLevel: Double = 1.0
    var isScanning = false
    var attackKeyHeldTicks = 0 // to help with detecting pressed and held states of the attack button
    var usageTicks = 0
    var transitionTicks = 0
    var focusTicks = 0
    var gracePeriod = 0
    var dexActive = false
    var innerRingRotation = 0
    var pokemonInFocus: PokemonEntity? = null
    var lastPokemonInFocus: PokemonEntity? = null
    var pokemonBeingScanned: PokemonEntity? = null
    var scanningProgress: Int = 0
    var pokedexUser: ServerPlayer? = null
    var originalHudHidden: Boolean = false
    var bufferImageSnap:  Boolean = false

    override fun getUseDuration(stack: ItemStack?, user: LivingEntity?): Int {
        return 72000 // (vanilla bows use 72000 ticks -> 1 hour of hold time)
    }

    override fun hurtEnemy(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?): Boolean {
        return !isScanning && super.hurtEnemy(stack, target, attacker)
    }

    override fun mineBlock(stack: ItemStack?, world: Level?, state: BlockState?, pos: BlockPos?, miner: LivingEntity?): Boolean {
        return !isScanning && super.mineBlock(stack, world, state, pos, miner)
    }

    override fun canAttackBlock(state: BlockState?, world: Level?, pos: BlockPos?, miner: Player?): Boolean {
        return !isScanning && super.canAttackBlock(state, world, pos, miner)
    }

    override fun use(world: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        println()
        val itemStack = player.getItemInHand(usedHand)
        dexActive = false

        if (player !is ServerPlayer) return InteractionResultHolder.success(itemStack) else pokedexUser = player

        player.startUsingItem(usedHand) // Start using the item

        /*if (player.isSneaking) {
            isScanning = true
            //return TypedActionResult.consume(itemStack)
        }
        else {
            openPokdexGUI(player)
        }*/

        return InteractionResultHolder.fail(itemStack)


        /*// Check if the player is interacting with a Pokémon
        val entity = player.world
                .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
                .filter { player.isLookingAt(it, stepDistance = 0.1F) }
                .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {
                PokedexUIPacket(type).sendToPlayer(player)
            }
            player.playSoundToPlayer(CobblemonSounds.POKEDEX_SHOW, SoundCategory.PLAYERS, 1F, 1F)
        } else {
            inUse = true
            zoomLevel = 1.0
            changeFOV(70.0)
            player.setCurrentHand(usedHand) // Start using the item
            return TypedActionResult.consume(itemStack)
        }*/

        //return TypedActionResult.success(itemStack)
    }

    fun openPokdexGUI(player: ServerPlayer) {
        // Check if the player is interacting with a Pokémon
        /*val entity = player.world
                .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
                .filter { player.isLookingAt(it, stepDistance = 0.1F) }
                .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                playSound(CobblemonSounds.POKEDEX_SCAN)
                //player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {*/
        PokedexUIPacket(type).sendToPlayer(player)
            //}
        playSound(CobblemonSounds.POKEDEX_OPEN)
        //player.playSoundToPlayer(CobblemonSounds.POKEDEX_OPEN, SoundCategory.PLAYERS, 1F, 1F)
        //}
    }

    override fun inventoryTick(stack: ItemStack?, world: Level?, entity: Entity?, slot: Int, selected: Boolean) {
        if (!isScanning) {
            if (focusTicks > 0) focusTicks--
            if (transitionTicks > 0) transitionTicks--
        }
    }

    override fun onUseTick(world: Level?, user: LivingEntity?, stack: ItemStack?, remainingUseTicks: Int) {
        if (user is Player) {
            // if the item has been used for more than 1 second activate scanning mode
            if (getUseDuration(stack, user) - remainingUseTicks > 3 && !dexActive) {
                usageTicks++
                if (transitionTicks < 12) transitionTicks++
                innerRingRotation = (if (pokemonInFocus != null) (innerRingRotation + 10) else (innerRingRotation + 1)) % 360

                // play the Scanner Open sound only once
                if (!isScanning) {
                    playSound(CobblemonSounds.POKEDEX_SCAN_OPEN)
                    val client = Minecraft.getInstance()

                    // Hide the HUD during scan mode
                    originalHudHidden = client.options.hideGui

                    client.options.hideGui = true
                }

               // isScanning = true

                // todo get it constantly scanning outwards to detect pokemon in focus
                Minecraft.getInstance().player?.let {
                    detectPokemon(it.level(), it, InteractionHand.MAIN_HAND, getUseDuration(stack, user) - remainingUseTicks)
                }

                // if there was a mouse click last tick and overlay is now down
                if (bufferImageSnap) {
                    Minecraft.getInstance().execute {
                        Minecraft.getInstance().player?.let {
                            //println("You have taken a picture")
                            playSound(CobblemonSounds.POKEDEX_SNAP_PICTURE)
                            //detectPokemon(it.world, it, Hand.MAIN_HAND)

                            // Todo create a "shotgun" ray cast to determine all Pokemon in the picture to be used for later

                            // take picture while overlay is down this tick
                            snapPicture(it)
                        }
                    }

                    // bring back overlay next tick
                    bufferImageSnap = false
                }

                isScanning = true
                // todo try to make it so that the player is able to walk normal speed while in scanner mode
                //user.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 3, 1, true, false, false)) // Remove slowness effect
            }
        }

        super.onUseTick(world, user, stack, remainingUseTicks)
    }

    override fun releaseUsing(stack: ItemStack?, world: Level, entity: LivingEntity, timeLeft: Int) {
        // todo if less than a second then open GUI
        if (entity is ServerPlayer && (getUseDuration(stack, entity) - timeLeft) <= 3) {
            dexActive = true
            openPokdexGUI(entity)
        } else if (entity !is ServerPlayer && (getUseDuration(stack, entity) - timeLeft) > 3){ // any other amount of time assume scanning mode was active
            playSound(CobblemonSounds.POKEDEX_SCAN_CLOSE)

            // todo if solution is found to boost player speed during scanning mode, we might need to end it here
            //entity.removeStatusEffect(StatusEffects.SPEED) // Remove slowness effect
        }

        // reset all overlay values to make sure nothing gets stuck
        resetOverlay()

        super.releaseUsing(stack, world, entity, timeLeft)
    }

    // for resetting certain values of overlay related things
    fun resetOverlay() {
        val client = Minecraft.getInstance()
        // Restore the HUD
        client.options.hideGui = originalHudHidden
        isScanning = false
        bufferImageSnap = false
        dexActive = false
        zoomLevel = 1.0
        attackKeyHeldTicks = 0
        //usageTicks = 0
        changeFOV(70.0)
    }


    // todo idk if we ever will need this since it is only fired when maxUseTime is reached whcich for this item is currently at 3 hours
    /*override fun finishUsing(stack: ItemStack?, world: Level?, entity: LivingEntity?): ItemStack? {
        *//*if (timeLeft < 20) {
            // todo if less than a second then open GUI
            openPokdexGUI(entity as ServerPlayer)
        }*//*

        if (world != null) {
            if (world.isClient && entity is Player) {
                inUse = false
                isScanning = false
                zoomLevel = 1.0
                changeFOV(70.0)
            }
        }

        super.finishUsing(stack, world, entity)
    }*/

    // todo maybe add a feature to scan objects like blocks for more info?
    /*override fun useOnBlock() {

    }*/

    fun changeFOV(fov: Double) {
        val client = Minecraft.getInstance()
        val oldFov = fov.toInt()
        val newFov = (fov / zoomLevel).coerceIn(30.0, 110.0).toInt()

        if (newFov != oldFov) {
            playSound(CobblemonSounds.POKEDEX_ZOOM_INCREMENT)
        }

        // logging for testing
        //println("Setting FOV to: $newFov with zoomLevel: $zoomLevel")

        client.options.fov().set(newFov)
    }

    // todo render detected pokemon details
    @Environment(EnvType.CLIENT)
    fun renderPokemonDetails() {

    }

    // todo render pokemon scan progress
    /*@Environment(EnvType.CLIENT)
    fun renderScanProgress(GuiGraphics: GuiGraphics, progress: Int) {
        val client = Minecraft.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val centerX = screenWidth / 2.0
        val centerY = screenHeight / 2.0
        val radius = 50.0  // Radius of the circle
        val segments = 100  // Number of segments in the circle

        val progressAngle = 360.0 * (progress / 100.0)  // Calculate the angle for the current progress

        // Prepare rendering
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)

        // Start drawing
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)

        // Center vertex
        bufferBuilder.vertex(centerX.toFloat(), centerY.toFloat(), 0.0f).color(0.0f, 1.0f, 1.0f, 1.0f)

        // Circle vertices
        for (i in 0..segments) {
            val angle = progressAngle * (i / segments.toDouble()) * (Math.PI / 180.0)
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            bufferBuilder.vertex(x.toFloat(), y.toFloat(), 0.0f).color(0.0f, 1.0f, 1.0f, 1.0f)
        }

        // Finish drawing
        val builtBuffer = bufferBuilder.end()
        BufferRenderer.draw(builtBuffer)

        // Restore rendering state
        RenderSystem.disableBlend()
    }*/

    /*// todo suffer greatly and try (and probably fail) to make a crappy rotating scan progress meter
    @Environment(EnvType.CLIENT)
    fun renderScanProgress(GuiGraphics: GuiGraphics, progress: Int) {
        val client = Minecraft.getInstance()
        val textureManager = client.textureManager
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val progressTexture = cobblemonResource("textures/gui/pokedex/pokedex_scanner_scan_progress.png")
        val maskTexture = cobblemonResource("textures/gui/pokedex/pokedex_scanner_scan_progress_mask.png")

        val textureWidth = 345  // Adjust to your texture's width
        val textureHeight = 207  // Adjust to your texture's height
        val centerX = (screenWidth - textureWidth) / 2
        val centerY = (screenHeight - textureHeight) / 2

        // Bind and draw the progress texture
        textureManager.bindTexture(progressTexture)
        GuiGraphics.drawTexture(progressTexture, centerX, centerY, centerX + textureWidth, centerY + textureHeight, textureWidth, textureHeight)

        // Calculate mask rotation based on progress
        val rotationDegrees = (360.0 * (progress / 100.0)).toFloat()

        // Setup matrix stack for rotation
        val matrices = GuiGraphics.getMatrices()
        matrices.push()
        matrices.translate((centerX + textureWidth / 2).toFloat(), (centerY + textureHeight / 2).toFloat(), 0.0f)
        val rotationQuaternion = Quaternionf().rotateZ(Math.toRadians(-rotationDegrees.toDouble()).toFloat())
        matrices.multiply(rotationQuaternion)
        matrices.translate(-(centerX + textureWidth / 2).toFloat(), -(centerY + textureHeight / 2).toFloat(), 0.0f)

        // Bind and draw the mask texture with applied rotation
        textureManager.bindTexture(maskTexture)
        GuiGraphics.drawTexture(maskTexture, centerX, centerY, centerX + textureWidth, centerY + textureHeight, textureWidth, textureHeight)

        // Pop matrix to revert rotation
        matrices.pop()
    }*/

    @Environment(EnvType.CLIENT)
    fun renderPhotodexOverlay(GuiGraphics: GuiGraphics, tickDelta: Float, scale: Float) {
        val client = Minecraft.getInstance()
        val matrices = GuiGraphics.pose()

        val screenWidth = client.window.width
        val screenHeight = client.window.height

        // Texture dimensions
        val textureWidth = 345
        val textureHeight = 207
        val scanScreen = cobblemonResource("textures/gui/pokedex/pokedex_screen_scan.png")
        val scanOverlayCorners = cobblemonResource("textures/gui/pokedex/scan/overlay_corners.png")
        val scanOverlayTop = cobblemonResource("textures/gui/pokedex/scan/overlay_border_top.png")
        val scanOverlayBottom = cobblemonResource("textures/gui/pokedex/scan/overlay_border_bottom.png")
        val scanOverlayLeft = cobblemonResource("textures/gui/pokedex/scan/overlay_border_left.png")
        val scanOverlayRight = cobblemonResource("textures/gui/pokedex/scan/overlay_border_right.png")
        val scanOverlayLines = cobblemonResource("textures/gui/pokedex/scan/overlay_scanlines.png")

        // Get player yaw and convert to degrees in a circle
        val yaw = client.player?.yRot ?: 0f
        val degrees = (yaw % 360 + 360) % 360  // Normalize the angle

        // Compass configuration
        val compassPoints = arrayOf("n", "i", "e", "i", "s", "i", "w", "i")
        val degreesPerSegment = 360 / compassPoints.size
        val centerIndex = Math.round(degrees / degreesPerSegment) % compassPoints.size
        val visibleSegments = arrayOfNulls<String>(5)  // Showing 5 segments at a time
        for (i in visibleSegments.indices) {
            val index = (centerIndex + i - 2 + compassPoints.size) % compassPoints.size
            visibleSegments[i] = compassPoints[index]
        }

        // Render Compass at the Top Center
        val compassSpacing = 20  // Width of each compass segment texture
        val compassStartX = (screenWidth - compassSpacing * visibleSegments.size) / 2
        val compassY = 10  // Top of the screen
        for (i in visibleSegments.indices) {
            val segmentTexture = getCompassTexture(visibleSegments[i] ?: "i")  // Assuming a method to get the right texture
            blitk(matrixStack = matrices, texture = segmentTexture, x = compassStartX + i * compassSpacing, y = compassY, width = 16, height = 16, alpha = 1.0F)
        }

        RenderSystem.enableBlend()
        // Pokédex zoom in/out animation
        val effectiveTicks = clamp(transitionTicks + (if (isScanning) 1 else -1) * tickDelta, 0F, 12F)
        if (effectiveTicks <= 12) {
            val scale = 1 + (if (effectiveTicks <= 2) 0F else ((effectiveTicks - 2) * 0.075F))

            // Calculate centered position
            val x = (screenWidth - (textureWidth * scale)) / 2
            val y = (screenHeight - (textureHeight * scale)) / 2

            val opacity = if (effectiveTicks <= 2) 1F else (10F - (effectiveTicks.toFloat() - 2F)) / 10F
            blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/pokedex_base_${type}.png"), x = x / scale, y = y / scale, width = textureWidth, height = textureHeight, scale = scale, alpha = opacity)
            blitk(matrixStack = matrices, texture = scanScreen, x = x / scale, y = y / scale, width = textureWidth, height = textureHeight, scale = scale, alpha = opacity)
        }

        // Scanning overlay
        val opacity = if (effectiveTicks >= 10) 1F else effectiveTicks/10F
        // Draw scan lines
        for (i in 0 until screenHeight) {
            if (i % 4 == 0) blitk(matrixStack = matrices, texture = scanOverlayLines, x = 0, y = i, width = screenWidth, height = 4, alpha = opacity)
        }

        // Draw border
        // Top left corner
        blitk(matrixStack = matrices, texture = scanOverlayCorners, x = 0, y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, alpha = opacity)
        // Top right corner
        blitk(matrixStack = matrices, texture = scanOverlayCorners, x = (screenWidth - 4), y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, uOffset = 4, alpha = opacity)
        // Bottom left corner
        blitk(matrixStack = matrices, texture = scanOverlayCorners, x = 0, y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, alpha = opacity)
        // Bottom right corner
        blitk(matrixStack = matrices, texture = scanOverlayCorners, x = (screenWidth - 4), y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, uOffset = 4, alpha = opacity)

        // Border sides
        val notchStartX = (screenWidth - 200) / 2
        blitk(matrixStack = matrices, texture = scanOverlayTop, x = 4, y = 0, width = notchStartX - 4, height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = scanOverlayTop, x = notchStartX + 200, y = 0, width = (screenWidth - (notchStartX + 200 + 4)), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = scanOverlayBottom, x = 4, y = (screenHeight - 3), width = (screenWidth - 8), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = scanOverlayLeft, x = 0, y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = scanOverlayRight, x = (screenWidth - 3), y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/overlay_notch.png"), x = notchStartX, y = 0, width = 200, height = 12, alpha = opacity)

        // Scan info frame
        if (focusTicks > 0) {
            blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_info_frame.png"),
                x = (screenWidth / 2) - 120,
                y = (screenHeight / 2) - 80,
                width = 92,
                height = 55,
                textureHeight = 550,
                textureWidth = 92,
                vOffset = focusTicks * 55,
                alpha = opacity
            )

            if (focusTicks == 9 && pokemonInFocus != null) {
                drawScaledText(
                    context = GuiGraphics,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = pokemonInFocus!!.pokemon.species.name.text().bold(),
                    x = (screenWidth / 2) - 74,
                    y = (screenHeight / 2) - 74,
                    shadow = true,
                    centered = true
                )
            }
        }

        val rotation = usageTicks % 360

        // Scan rings
        matrices.pushPose()
        matrices.translate((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), 0.0f)

        matrices.pushPose()
        matrices.mulPose(Quaternionf().rotateZ(Math.toRadians((-rotation) * 0.5).toFloat()))
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_outer.png"), x = -58, y = -58, width = 116, height = 116, alpha = opacity)
        matrices.popPose()

        for (i in 0 until 80) {
            val rotationQuaternion = Quaternionf().rotateZ(Math.toRadians((i * 4.5) + (rotation * 0.5)).toFloat())
            matrices.pushPose()
            matrices.mulPose(rotationQuaternion)
            blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_middle.png"), x = -50, y = -0.5F, width = 100, height = 1, alpha = opacity)
            matrices.popPose()
        }

        matrices.pushPose()
        matrices.mulPose(Quaternionf().rotateZ(Math.toRadians(-innerRingRotation.toDouble()).toFloat()))
        blitk(matrixStack = matrices, texture = cobblemonResource("textures/gui/pokedex/scan/scan_ring_inner.png"), x = -42, y = -42, width = 84, height = 84, alpha = opacity)
        matrices.popPose()

        matrices.popPose()

        RenderSystem.disableBlend()
    }

    fun getCompassTexture(direction: String): ResourceLocation {
        return cobblemonResource("textures/gui/pokedex/compass/$direction.png")
    }

    @Environment(EnvType.CLIENT)
    fun onRenderOverlay(GuiGraphics: GuiGraphics, tickCounter: DeltaTracker) {
        if (!dexActive) {
            val tickDelta = tickCounter.getGameTimeDeltaPartialTick(false)
            renderPhotodexOverlay(GuiGraphics, tickDelta, 1.0F)
        }
        //if (isScanning) {
            // render the scanner overlay
            //renderPhotodexOverlay(GuiGraphics, 1.0F)

            // render the scan progress
            //renderScanProgress(GuiGraphics, scanningProgress)
        //}
    }

    @Environment(EnvType.CLIENT)
    fun onMouseClick() {
        // Todo try to start a buffer to taking a pic to try to wait until overlay is down
        bufferImageSnap = true

        /*if (isScanning) {

            Minecraft.getInstance().player?.let {
                //println("You have taken a picture")
                playSound(CobblemonSounds.POKEDEX_SNAP_PICTURE)
                //detectPokemon(it.world, it, Hand.MAIN_HAND)

                // todo take picture
                snapPicture(it)
            }
        }*/
    }

    @Environment(EnvType.CLIENT)
    fun onMouseHeld() {
        if (isScanning) {
            Minecraft.getInstance().player?.let {
                //println("You are scanning")
                //playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)

                // if pokemonInFocus is not null start scanning it
                if (pokemonInFocus != null) {
                    // todo if the pokemonInFocus is equal to pokemonBeingScanned
                    if (pokemonInFocus == pokemonBeingScanned) {
                        scanPokemon(pokemonInFocus!!, pokedexUser!!)
                    } else {
                        // reset scanning progress
                        scanningProgress = 0
                        pokemonBeingScanned = pokemonInFocus


                    }
                } else {
                    pokemonBeingScanned = null
                }
                detectPokemon(it.level(), it, InteractionHand.MAIN_HAND)
            }
        }
    }

    fun scanPokemon(pokemonEntity: PokemonEntity, player: ServerPlayer) {
        // increment scan progress
        if (scanningProgress < 100)
            scanningProgress += 2

        if (scanningProgress % 2 == 0) { // 20 for 1 second

            // todo get a better (maybe shorter) looping sound so it ends nicer
            //playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)

            // play this temp sound for now
            playSound(CobblemonSounds.POKEDEX_SCAN_LOOP)
        }

        // if scan progress is 100 then send packet to Pokedex
        if (scanningProgress == 100) {
            val species = pokemonEntity.pokemon.species.resourceIdentifier
            val form = pokemonEntity.pokemon.form.formOnlyShowdownId()

            val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
            pokedexData.onPokemonSeen(species, form)
            // kill overlay before opening dex
            dexActive = true
            player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
            PokedexUIPacket(type, species).sendToPlayer(player)
            playSound(CobblemonSounds.POKEDEX_SCAN)

            scanningProgress = 0
        }
    }

    fun detectPokemonInView(player: Player, maxDistance: Double): List<Species> {
        val detectedSpecies = mutableListOf<Species>()
        val eyePos = player.getEyePosition(1.0F)
        val lookVec = player.getEyePosition(1.0F)
        val client = Minecraft.getInstance()
        val fov = client.options.fov().get()
        val screenWidth = client.window.guiScaledWidth
        val screenHeight = client.window.guiScaledHeight

        // Number of raycasts in each dimension
        val raycastResolution = 10
        val fovRadians = Math.toRadians(fov.toDouble())

        // Calculate the step size for each ray in terms of angle
        val angleStep = fovRadians / raycastResolution

        // Iterate over the grid within the frustum
        for (x in -raycastResolution..raycastResolution) {
            for (y in -raycastResolution..raycastResolution) {
                // Calculate the direction of the current ray
                val offsetX = x * angleStep
                val offsetY = y * angleStep

                val rayDirection = lookVec.rotatePitch(offsetY).rotateYaw(offsetX)
                val rayEnd = eyePos.add(rayDirection.scale(maxDistance * zoomLevel))  // Adjusted raycast distance based on zoomLevel

                val hitResult = player.level().clip(
                    ClipContext(
                        eyePos,
                        rayEnd,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        player
                    )
                )

                if (hitResult.type == HitResult.Type.ENTITY) {
                    val entity = (hitResult as EntityHitResult).entity
                    if (entity is PokemonEntity) {
                        detectedSpecies.add(entity.pokemon.species)
                    }
                }
            }
        }

        return detectedSpecies.distinct()
    }

    // Helper extension functions for rotating vectors
    fun Vec3.rotateYaw(angle: Double): Vec3 {
        val cos = Math.cos(angle)
        val sin = Math.sin(angle)
        return Vec3(this.x * cos - this.z * sin, this.y, this.x * sin + this.z * cos)
    }

    fun Vec3.rotatePitch(angle: Double): Vec3 {
        val cos = Math.cos(angle)
        val sin = Math.sin(angle)
        return Vec3(this.x, this.y * cos - this.z * sin, this.y * sin + this.z * cos)
    }

    private fun detectPokemon(world: Level, user: Player, hand: InteractionHand, currentTick: Int = -1) {
        if (isScanning) {
            val eyePos = user.getEyePosition(1.0F)
            val lookVec = user.getViewVector(1.0F)
            val maxDistance = 9.0 * zoomLevel  // Adjusted raycast distance based on zoomLevel
            val boundingBoxSize = 12.0 * zoomLevel
            var closestEntity: Entity? = null
            var closestDistance = maxDistance

            // Define a large bounding box around the player
            val boundingBox = AABB(
                user.x - boundingBoxSize, user.y - boundingBoxSize, user.z - boundingBoxSize,
                user.x + boundingBoxSize, user.y + boundingBoxSize, user.z + boundingBoxSize
            )

            // Get all entities within the boundingBox
            val entities = user.level().getEntitiesOfClass(Entity::class.java, boundingBox) { it != user }

            for (entity in entities) {
                val entityBox: AABB = entity.boundingBox

                // Calculate the size of the bounding box
                val boxWidth = entityBox.xsize
                val boxHeight = entityBox.ysize
                val boxDepth = entityBox.zsize

                val boxVolume = boxWidth * boxHeight * boxDepth

                val minSize = 0.2 // Smallest bounding box volume (joltik at .2)
                val maxSize = 3.0 // Largest bounding box volume (wailord at 21.5)
                val minSizeScale = 2.0 // Maximum inflation for getting closer to smallest hitbox
                val maxSizeScale = 1.0 // No inflation for getting closer to largest hitbox
                val steepCoefficient = 20.0

                // Normalize the volume within the defined range
                val normalizedSize = (boxVolume - minSize) / (maxSize - minSize).coerceAtLeast(0.01)

                // Calculate the scaling factor using very steep exponential decay to make smaller hitboxes bigger
                val inflationFactor = maxSizeScale + (minSizeScale - maxSizeScale) * Math.exp(-steepCoefficient * normalizedSize)

                // Inflate the base bounding box
                val inflatedBox = entityBox.inflate(
                    (inflationFactor - 1) * boxWidth / 2,
                    (inflationFactor - 1) * boxHeight / 2,
                    (inflationFactor - 1) * boxDepth / 2
                )

                val intersection = inflatedBox.clip(eyePos, eyePos.add(lookVec.scale(maxDistance)))

                if (intersection.isPresent) {
                    val distanceToEntity = eyePos.distanceTo(intersection.get())
                    if (distanceToEntity < closestDistance) {
                        closestEntity = entity
                        closestDistance = distanceToEntity
                    }
                }
            }

            if (closestEntity != null && closestEntity is PokemonEntity) {
                gracePeriod = 0
                pokemonInFocus = closestEntity
                if (focusTicks < 9) focusTicks++

                // If detected pokemon is not the same as the last detected pokemon
                if (pokemonInFocus != lastPokemonInFocus || currentTick == 1) {
                    // user.sendMessage(Text.of("${closestEntity.pokemon.species.name} is in focus!"))
                    // Play sound for showing details of the focused pokemon
                    playSound(CobblemonSounds.POKEDEX_SCAN_DETAIL)
                }

                lastPokemonInFocus = pokemonInFocus
            } else if (closestEntity == null && gracePeriod < 20) {
                gracePeriod++
            } else {
                pokemonInFocus = null
                lastPokemonInFocus = null
                if (focusTicks > 0) focusTicks--
                // Play POKEDEX_DETAIL_DISAPPEAR sound here (if necessary)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun snapPicture(player: LocalPlayer) {
        Minecraft.getInstance().execute {
            val client = Minecraft.getInstance()

            // Hide the HUD
            val originalHudHidden = client.options.hideGui
            client.options.hideGui = true

            // Take a screenshot
            val framebuffer: RenderTarget = client.mainRenderTarget
            val nativeImage: NativeImage = Screenshot.takeScreenshot(framebuffer)

            // Convert NativeImage to BufferedImage
            val imageBytes = nativeImage.asByteArray()
            val bufferedImage: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))

            // Crop the image to a square centered on the original image
            val croppedImage = cropToSquare(bufferedImage)

            // Save the captured image to file for debugging
            val capturedImageFile = File("captured_image.png")
            ImageIO.write(croppedImage, "png", capturedImageFile)
            println("Saved captured image to ${capturedImageFile.absolutePath}")

            // Prepare the image to be sent
            val baos = ByteArrayOutputStream()
            ImageIO.write(croppedImage, "png", baos)
            val imageBytesToSend = baos.toByteArray()

            // Send the packet to the server
            MapUpdatePacket(imageBytesToSend).sendToServer()

            // Restore the HUD
            client.options.hideGui = originalHudHidden
        }
    }

    fun cropToSquare(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val size = minOf(width, height)

        val x = (width - size) / 2
        val y = (height - size) / 2

        return image.getSubimage(x, y, size, size)
    }

    /*private fun updatePlayerMap(player: Player, image: BufferedImage) {
        val inventory = player.inventory
        for (i in 0 until inventory.size()) {
            val stack = inventory.getStack(i)
            if (stack.item == Items.MAP) {
                val mapStack = ItemStack(Items.FILLED_MAP)
                val world = player.world as ServerLevel
                val mapId = world.increaseAndGetMapId().id

                val nbt = NbtCompound()
                nbt.putString("dimension", world.registryKey.value.toString())
                nbt.putInt("xCenter", 0)
                nbt.putInt("zCenter", 0)
                nbt.putBoolean("locked", true)
                nbt.putBoolean("unlimitedTracking", false)
                nbt.putBoolean("trackingPosition", false)
                nbt.putByte("scale", 3.toByte())
                val mapState = MapState.fromNbt(nbt, world.registryManager)

                val resizedImage = convertToBufferedImage(image.getScaledInstance(128, 128, Image.SCALE_DEFAULT))
                val pixels = convertPixelArray(resizedImage)
                // todo Use AccessWidener to get access to it
                val mapColors = MapColor.COLORS.filterNotNull().toTypedArray()

                for (x in 0 until 128) {
                    for (y in 0 until 128) {
                        val color = Color(pixels[y][x], true)
                        mapState.colors[x + y * 128] = nearestColor(mapColors, color).toByte()
                    }
                }

                world.putMapState(MapIdComponent(mapId), mapState)
                val mapIdComponent = MapIdComponent(mapId)
                mapStack.set(DataComponentTypes.MAP_ID, mapIdComponent)

                inventory.setStack(i, mapStack)
                player.sendMessage(Text.literal("SnapPicture: Map updated with screenshot"), true)
                return
            }
        }
        player.sendMessage(Text.literal("No empty map found in inventory"), true)
    }


    private val shadeCoeffs = doubleArrayOf(180.0 / 255.0, 220.0 / 255.0, 255.0 / 255.0, 135.0 / 255.0)

    fun convertToBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }
        val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val bGr = bimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()
        return bimage
    }

    fun convertPixelArray(image: BufferedImage): Array<IntArray> {
        val width = image.width
        val height = image.height
        val pixels = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y][x] = image.getRGB(x, y)
            }
        }
        return pixels
    }

    fun nearestColor(mapColors: Array<MapColor>, color: Color): Int {
        var closestColorIndex = 0
        var minDistance = Double.MAX_VALUE

        for (i in mapColors.indices) {
            val mcColor = mapColorToRGBColor(mapColors[i])
            val distance = colorDistance(color.red, color.green, color.blue, mcColor[0], mcColor[1], mcColor[2])
            if (distance < minDistance) {
                minDistance = distance
                closestColorIndex = i
            }
        }

        return closestColorIndex
    }

    private fun mapColorToRGBColor(color: MapColor): IntArray {
        val mcColor = color.color
        val mcColorVec = intArrayOf(
            ColorHelper.Argb.getRed(mcColor),
            ColorHelper.Argb.getGreen(mcColor),
            ColorHelper.Argb.getBlue(mcColor)
        )

        val coeff = shadeCoeffs[color.id and 3]
        return intArrayOf(
            (mcColorVec[0] * coeff).toInt(),
            (mcColorVec[1] * coeff).toInt(),
            (mcColorVec[2] * coeff).toInt()
        )
    }

    private fun colorDistance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return Math.sqrt(dr * dr + dg * dg + db * db)
    }*/

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().execute {
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
        }
    }

    /*@Environment(EnvType.CLIENT)
    private fun registerInputHandlers() {
        val windowHandle = Minecraft.getInstance().window.handle

        if (!isScrollCallbackRegistered) {
            // Register scroll callback
            GLFW.glfwSetScrollCallback(windowHandle) { _, _, yOffset ->
                println("Scroll Callback Triggered: yOffset = $yOffset")

                if (yOffset != 0.0) {
                    zoomLevel += yOffset * 0.05 // Smaller increment
                    zoomLevel = zoomLevel.coerceIn(1.0, 4.0) // More controlled zoom range
                    changeFOV(70 / zoomLevel)
                }
            }
            isScrollCallbackRegistered = true
        }

        if (!isMouseButtonCallbackRegistered) {
            // Register mouse button callback
            GLFW.glfwSetMouseButtonCallback(windowHandle) { _, button, action, _ ->
                if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
                    println("Mouse Button 1 Left Pressed")
                    Minecraft.getInstance().player?.let {
                        if (it.world.isClient) {
                            detectPokemon(it.world, it, Hand.MAIN_HAND)
                        }
                    }
                } else if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
                    println("Mouse Button 1 Left Released")
                    // Implement your logic for release here
                }

                if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS) {
                    println("Mouse Button 2 Right Pressed")
                } else if (inUse && button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_RELEASE) {
                    println("Mouse Button 2 Right Released")
                    inUse = false
                    // Implement your logic for release here
                }
            }
            isMouseButtonCallbackRegistered = true
        }
    }

    private fun unregisterInputHandlers() {
        val windowHandle = Minecraft.getInstance().window.handle

        if (isScrollCallbackRegistered) {
            GLFW.glfwSetScrollCallback(windowHandle, null)?.free()
            isScrollCallbackRegistered = false
        }

        if (isMouseButtonCallbackRegistered) {
            GLFW.glfwSetMouseButtonCallback(windowHandle, null)?.free()
            isMouseButtonCallbackRegistered = false
        }
    }*/

    /*override fun use(world: Level, player: Player, usedHand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(usedHand)

        if (player !is ServerPlayer) return TypedActionResult.success(itemStack, world.isClient)

        // Check if the player is interacting with a Pokémon
        val entity = player.world
            .getOtherEntities(player, Box.of(player.pos, 16.0, 16.0, 16.0))
            .filter { player.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(player) } as? PokemonEntity?

        if (!player.isSneaking) {
            if (entity != null) {
                val species = entity.pokemon.species.resourceIdentifier
                val form = entity.pokemon.form.formOnlyShowdownId()

                val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
                pokedexData.onPokemonSeen(species, form)
                player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
                PokedexUIPacket(type, species).sendToPlayer(player)
                player.playSoundToPlayer(CobblemonSounds.POKEDEX_SCAN, SoundCategory.PLAYERS, 1F, 1F)
            } else {
                PokedexUIPacket(type).sendToPlayer(player)
            }
            player.playSoundToPlayer(CobblemonSounds.POKEDEX_SHOW, SoundCategory.PLAYERS, 1F, 1F)
        }

        return TypedActionResult.success(itemStack, world.isClient)
    }*/
}