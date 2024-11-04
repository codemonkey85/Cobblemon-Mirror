/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawPosablePortrait
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.battle.ClientBallDisplay
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleMessagePane
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokeBallModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import java.lang.Double.max
import java.lang.Double.min
import java.util.UUID
import kotlin.math.floor
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.Mth.ceil
import org.joml.Vector3f
import kotlin.math.floor

class BattleOverlay : Gui(Minecraft.getInstance()), Schedulable {
    companion object {
        const val MAX_OPACITY = 1.0
        const val MIN_OPACITY = 0.5
        const val OPACITY_CHANGE_PER_SECOND = 0.1
        const val HORIZONTAL_INSET = 12
        const val VERTICAL_INSET = 10
        const val HORIZONTAL_SPACING = 4
        const val VERTICAL_SPACING = 40
        const val COMPACT_VERTICAL_SPACING = 30
        const val INFO_OFFSET_X = 7
        const val COMPACT_INFO_OFFSET_X = 6

        const val TILE_WIDTH = 140
        const val COMPACT_TILE_WIDTH = 128

        const val TILE_HEIGHT = 40
        const val COMPACT_TILE_HEIGHT = 28
        const val COMPACT_TILE_TEXTURE_HEIGHT = 56

        const val PORTRAIT_DIAMETER = 28
        const val COMPACT_PORTRAIT_DIAMETER = 19
        const val PORTRAIT_OFFSET_X = 5
        const val COMPACT_PORTRAIT_OFFSET_X = 4

        const val PORTRAIT_OFFSET_Y = 8
        const val COMPACT_PORTRAIT_OFFSET_Y = 7

        const val ROLE_CYCLE_SECONDS = 2.5

        const val SCALE = 0.5F

        private val PROMPT_TEXT_OPACITY_CURVE = sineFunction(period = 4F, verticalShift = 0.5F, amplitude = 0.5F)

        val battleInfoBase = cobblemonResource("textures/gui/battle/battle_info_base.png")
        val battleInfoBaseFlipped = cobblemonResource("textures/gui/battle/battle_info_base_flipped.png")
        val battleInfoBaseCompact = cobblemonResource("textures/gui/battle/battle_info_base_condensed.png")
        val battleInfoBaseFlippedCompact = cobblemonResource("textures/gui/battle/battle_info_base_flipped_condensed.png")
        val battleInfoRole = cobblemonResource("textures/gui/battle/battle_info_role.png")
        val battleInfoRoleFlipped = cobblemonResource("textures/gui/battle/battle_info_role_flipped.png")
        val battleInfoUnderlay = cobblemonResource("textures/gui/battle/battle_info_underlay.png")
        val caughtIndicator = cobblemonResource("textures/gui/battle/battle_owned_indicator.png")

    }

    var opacity = MIN_OPACITY
    val opacityRatio: Double
        get() = (opacity - MIN_OPACITY) / (MAX_OPACITY - MIN_OPACITY)
    var passedSeconds = 0F

    var lastKnownBattle: UUID? = null
    lateinit var messagePane: BattleMessagePane
    var hidePortraits = false
    override val schedulingTracker = SchedulingTracker()

    override fun render(context: GuiGraphics, tickCounter: DeltaTracker) {
        val tickDelta = tickCounter.realtimeDeltaTicks.takeIf { !Minecraft.getInstance()!!.isPaused } ?: 0F
        schedulingTracker.update(tickDelta / 20F)
        passedSeconds += tickDelta / 20
        if (passedSeconds > 100) {
            passedSeconds -= 100
        }
        val battle = CobblemonClient.battle ?: return
        opacity = if (battle.minimised) {
            max(opacity - tickDelta * OPACITY_CHANGE_PER_SECOND, MIN_OPACITY)
        } else {
            min(opacity + tickDelta * OPACITY_CHANGE_PER_SECOND, MAX_OPACITY)
        }
        val currentScreen = Minecraft.getInstance().screen
        if (!hidePortraits) {
            val playerUUID = Minecraft.getInstance().player?.uuid ?: return
            val side1 = if (battle.side1.actors.any { it.uuid == playerUUID }) battle.side1 else battle.side2
            val side2 = if (side1 == battle.side1) battle.side2 else battle.side1

            // Command highlight for Double and Triple Battles
            val isBattleGUIActive = currentScreen is BattleGUI && currentScreen.getCurrentActionSelection() != null
            val selectedPNX = if((battle.battleFormat.battleType.slotsPerActor > 1 || battle.battleFormat.battleType.actorsPerSide > 1) && isBattleGUIActive) battle.getFirstUnansweredRequest()?.activePokemon?.getPNX() else null

            side1.activeClientBattlePokemon.forEachIndexed { index, activeClientBattlePokemon -> drawTile(context, tickDelta, activeClientBattlePokemon, true, index, PokedexEntryProgress.NONE, activeClientBattlePokemon.getPNX() == selectedPNX, false, battle.battleFormat.battleType.pokemonPerSide > 1) }
            side2.activeClientBattlePokemon.forEachIndexed { index, activeClientBattlePokemon -> drawTile(context, tickDelta, activeClientBattlePokemon, false, side2.activeClientBattlePokemon.count() - index - 1, battle.knowledge, false, false, battle.battleFormat.battleType.pokemonPerSide > 1) }
        }

        if (Minecraft.getInstance().screen !is BattleGUI && battle.mustChoose) {
            val textOpacity = PROMPT_TEXT_OPACITY_CURVE(passedSeconds)
            drawScaledText(
                context = context,
                text = battleLang("ui.actions_label", PartySendBinding.boundKey().displayName),
                x = Minecraft.getInstance().window.guiScaledWidth / 2,
                y = Minecraft.getInstance().window.guiScaledHeight / 5,
                opacity = textOpacity,
                centered = true
            )
        }

        if (currentScreen == null || currentScreen is ChatScreen) {
            if (lastKnownBattle != battle.battleId) {
                lastKnownBattle = battle.battleId
                messagePane = BattleMessagePane(CobblemonClient.battle!!.messages)
            }
            messagePane.opacity = 0.3F
            messagePane.render(context, 0, 0, 0F)
        }
    }

    fun drawTile(context: GuiGraphics, tickDelta: Float, activeBattlePokemon: ActiveClientBattlePokemon, left: Boolean, rank: Int, dexState: PokedexEntryProgress, hasCommand: Boolean = false, isHovered: Boolean = false, isCompact: Boolean = false) {
        val mc = Minecraft.getInstance()

        val battlePokemon = activeBattlePokemon.battlePokemon ?: return
        val battle = CobblemonClient.battle ?: return
        val slotCount = battle.battleFormat.battleType.slotsPerActor
        val playerNumberOffset = (activeBattlePokemon.getActorShowdownId()[1].digitToInt() - 1) / 2 * 10

        var x = HORIZONTAL_INSET + (slotCount - rank - 1) * HORIZONTAL_SPACING.toFloat()
        val y = VERTICAL_INSET + rank * (if (isCompact) COMPACT_VERTICAL_SPACING else VERTICAL_SPACING) + (if (left) playerNumberOffset else (battle.battleFormat.battleType.actorsPerSide - 1) * 10 - playerNumberOffset)
        if (!left) {
            x = mc.window.guiScaledWidth - x - if(isCompact) COMPACT_TILE_WIDTH else TILE_WIDTH
        }
        val invisibleX = if (left) {
            -(if(isCompact) COMPACT_TILE_WIDTH else TILE_WIDTH) - 1F
        } else {
            mc.window.guiScaledWidth.toFloat()
        }
        activeBattlePokemon.invisibleX = invisibleX
        activeBattlePokemon.xDisplacement = x
        activeBattlePokemon.animate(tickDelta)
        x = activeBattlePokemon.xDisplacement


        val hue = activeBattlePokemon.getHue()
        val r = ((hue shr 16) and 0b11111111) / 255F
        val g = ((hue shr 8) and 0b11111111) / 255F
        val b = (hue and 0b11111111) / 255F

        val truePokemon = activeBattlePokemon.actor.pokemon.find { it.uuid == activeBattlePokemon.battlePokemon?.uuid }
        drawBattleTile(
            context = context,
            x = x,
            y = y.toFloat(),
            partialTicks = tickDelta,
            reversed = !left,
            species = battlePokemon.species,
            level = battlePokemon.level,
            displayName = battlePokemon.displayName,
            gender = battlePokemon.gender,
            status = battlePokemon.status,
            state = battlePokemon.state,
            colour = Triple(r, g, b),
            opacity = opacity.toFloat(),
            ballState = activeBattlePokemon.ballCapturing,
            maxHealth = battlePokemon.maxHp.toInt(),
            health = battlePokemon.hpValue,
            isFlatHealth = battlePokemon.isHpFlat,
            isSelected = hasCommand,
            isHovered = isHovered,
            isCompact = isCompact,
            actorDisplayName = if (!battle.isPvW &&
                    ((left && activeBattlePokemon.actor.activePokemon.firstOrNull { (it.battlePokemon?.hpValue ?: 0F) > 0F } == activeBattlePokemon)
                    || (!left && activeBattlePokemon.actor.activePokemon.lastOrNull { (it.battlePokemon?.hpValue ?: 0F) > 0F } == activeBattlePokemon))) activeBattlePokemon.actor.displayName
                    else null,
            dexState = dexState
        )
    }

    fun drawBattleTile(
        context: GuiGraphics,
        x: Float,
        y: Float,
        partialTicks: Float,
        reversed: Boolean,
        species: Species,
        level: Int,
        displayName: MutableComponent,
        gender: Gender,
        status: PersistentStatus?,
        state: PosableState,
        colour: Triple<Float, Float, Float>?,
        opacity: Float,
        ballState: ClientBallDisplay? = null,
        maxHealth: Int,
        health: Float,
        isSelected: Boolean = false,
        isHovered: Boolean = false,
        isCompact: Boolean = false,
        actorDisplayName: MutableComponent? = null,
        isFlatHealth: Boolean,
        dexState: PokedexEntryProgress
    ) {
        val tileWidth = if (isCompact) COMPACT_TILE_WIDTH else TILE_WIDTH
        val portraitOffsetX = if (isCompact) COMPACT_PORTRAIT_OFFSET_X else PORTRAIT_OFFSET_X
        val portraitOffsetY = if (isCompact) COMPACT_PORTRAIT_OFFSET_Y else PORTRAIT_OFFSET_Y
        val portraitDiameter = if (isCompact) COMPACT_PORTRAIT_DIAMETER else PORTRAIT_DIAMETER
        val infoOffsetX = if (isCompact) COMPACT_INFO_OFFSET_X else INFO_OFFSET_X
        val portraitStartX = x + if (!reversed) portraitOffsetX else { tileWidth - portraitDiameter - portraitOffsetX }
        val matrixStack = context.pose()
        blitk(
            matrixStack = matrixStack,
            texture = battleInfoUnderlay,
            y = y + portraitOffsetY,
            x = portraitStartX,
            height = portraitDiameter,
            width = portraitDiameter,
            alpha = opacity
        )

        if (status != null) {
            val statusWidth = if (isCompact) 40 else 37
            blitk(
                matrixStack = matrixStack,
                texture = cobblemonResource("textures/gui/battle/battle_status_" + status.showdownName + ".png"),
                x = x + if (reversed) 65 else (if (isCompact) 23 else 38),
                y = y + if (isCompact) 22 else 28,
                height = if (isCompact) 6 else 7,
                width = statusWidth,
                uOffset = if (reversed) 0 else (74 - statusWidth),
                vOffset = if (isCompact) 1 else 0,
                textureHeight = 7,
                textureWidth = 74,
                alpha = opacity
            )

            drawScaledText(
                context = context,
                font = if (isCompact) null else CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.status." + status.showdownName).bold(),
                x = x + if (isCompact) (if (reversed) 87 else 30) else (if (reversed) 86 else 41),
                y = y + if (isCompact) 23 else 27,
                scale = if (isCompact) SCALE else 1F,
                opacity = opacity
            )
        }

        // Second render the Pokémon through the scissors
        context.enableScissor(
            portraitStartX.toInt(),
            (y + portraitOffsetY).toInt(),
            (portraitStartX + portraitDiameter).toInt(),
            (y + portraitDiameter + portraitOffsetY).toInt(),
        )
        matrixStack.pushPose()
        matrixStack.translate(
            portraitStartX + portraitDiameter / 2.0,
            y.toDouble() + portraitOffsetY - if (isCompact) 15.0 else 5.0,
            0.0
        )

        if (ballState != null && ballState.currentPose != "shut")  {
            ballState.currentPose = "shut"
        }

        if (ballState != null && ballState.stateEmitter.get() == EmptyPokeBallEntity.CaptureState.SHAKE) {
            drawPokeBall(
                state = ballState,
                matrixStack = matrixStack,
                reversed = reversed,
                partialTicks = partialTicks
            )
        } else {
            drawPosablePortrait(
                identifier = species.resourceIdentifier,
                matrixStack = matrixStack,
                scale = 18F * (ballState?.scale ?: 1F) * if (isCompact) 0.65F else 1.0f,
                contextScale = species.getForm(state.currentAspects).baseScale,
                repository = PokemonModelRepository,
                reversed = reversed,
                state = state,
                partialTicks = partialTicks
            )
        }
        matrixStack.popPose()
        context.disableScissor()

        // Third render the tile
        blitk(
            matrixStack = matrixStack,
            texture = if (isCompact) (if (reversed) battleInfoBaseFlippedCompact else battleInfoBaseCompact) else (if (reversed) battleInfoBaseFlipped else battleInfoBase),
            x = x,
            y = y,
            height = if (isCompact) COMPACT_TILE_HEIGHT else TILE_HEIGHT,
            width = tileWidth,
            textureHeight = if (isCompact) COMPACT_TILE_TEXTURE_HEIGHT else TILE_HEIGHT,
            vOffset = if (isHovered && isCompact) COMPACT_TILE_HEIGHT else 0,
            alpha = opacity,
        )

        val stage = floor((passedSeconds / ROLE_CYCLE_SECONDS % 1) * 5)
        if (colour != null && (!isSelected || stage != 4.0)) {
            val (r, g, b) = colour
            blitk(
                matrixStack = matrixStack,
                texture = if (reversed) battleInfoRoleFlipped else battleInfoRole,
                x = x + if (isCompact) (if (reversed) 93 else 8) else (if (reversed) 102 else 11),
                y = y + 1,
                height = 3,
                textureHeight = 12,
                width = 27,
                alpha = opacity,
                vOffset = if (isSelected) stage * 3 else 9,
                red = r,
                green = g,
                blue = b
            )

            if (dexState == PokedexEntryProgress.CAUGHT) {
                blitk(
                    matrixStack = matrixStack,
                    texture = caughtIndicator,
                    x = (x + 7) / SCALE,
                    y = (y + 9) / SCALE,
                    height = 10,
                    width = 10,
                    scale = SCALE,
                    alpha = opacity
                )
            }
        }

        // Draw labels
        val infoBoxX = x + if (!reversed) (portraitDiameter + portraitOffsetX + infoOffsetX) else infoOffsetX
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = displayName.bold(),
            x = infoBoxX + (if (dexState == PokedexEntryProgress.CAUGHT) 7 else 0),
            y = y + if (isCompact) 5 else 7,
            opacity = opacity,
            shadow = true
        )

        if (gender != Gender.GENDERLESS) {
            val isMale = gender == Gender.MALE
            val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = textSymbol,
                x = infoBoxX + 63,
                y = y + if (isCompact) 5 else 7,
                colour = if (isMale) 0x32CBFF else 0xFC5454,
                opacity = opacity,
                shadow = true
            )
        }

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.lv").bold(),
            x = infoBoxX + 69,
            y = y + if (isCompact) 5 else 7,
            opacity = opacity,
            shadow = true
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = level.toString().text().bold(),
            x = infoBoxX + 82,
            y = y + if (isCompact) 5 else 7,
            opacity = opacity,
            shadow = true
        )
        val hpRatio = if (isFlatHealth) health / maxHealth else health
        val (healthRed, healthGreen) = getDepletableRedGreen(hpRatio)
        val fullWidth = 97
        val barWidth = hpRatio * fullWidth
        val barOffsetX = if (isCompact) (if (reversed) 1 else 3) else 2
        val barX = if (!reversed) infoBoxX - barOffsetX else infoBoxX - barOffsetX + (fullWidth - barWidth)
        blitk(
            matrixStack = matrixStack,
            texture = CobblemonResources.WHITE,
            x = barX,
            y = y + if (isCompact) 16 else 22,
            height = 4,
            width = barWidth,
            red = healthRed * 0.8F,
            green = healthGreen * 0.8F,
            blue = 0.27F
        )

        val text = if (isFlatHealth) {
            "${health.toInt()}/$maxHealth"
        } else {
            "${ceil(health * 100)}%"
        }.text()

        drawScaledText(
            context = context,
            text = text,
            x = infoBoxX + (if (!reversed) 39.5 else 44.5),
            y = y + if(isCompact) 16 else 22,
            scale = 0.5F,
            opacity = opacity,
            centered = true,
            shadow = true
        )

        // Actor Display Name
        if(actorDisplayName != null) {
            if (!reversed) {
                drawScaledText(
                    context = context,
                    text = actorDisplayName,
                    x = x + 9,
                    y = y - 5,
                    scale = SCALE,
                    shadow = true,
                    opacity = opacity,
                )
            } else {
                drawScaledTextJustifiedRight(
                    context = context,
                    text = actorDisplayName,
                    x = x + tileWidth - 9,
                    y = y - 5,
                    scale = SCALE,
                    shadow = true,
                    opacity = opacity
                )
            }
        }
    }

    private fun drawPokeBall(
        state: ClientBallDisplay,
        matrixStack: PoseStack,
        scale: Float = 5F,
        partialTicks: Float,
        reversed: Boolean = false
    ) {
        val context = RenderContext()
        val model = PokeBallModelRepository.getPoser(state.pokeBall.name, state)
        val texture = PokeBallModelRepository.getTexture(state.pokeBall.name, state)
        val renderType = RenderType.entityCutout(texture)//model.getLayer(texture)

        RenderSystem.applyModelViewMatrix()
        val quaternion1 = Axis.YP.rotationDegrees(-32F * if (reversed) -1F else 1F)
        val quaternion2 = Axis.XP.rotationDegrees(5F)

        state.currentModel = model
        state.setPoseToFirstSuitable(PoseType.PORTRAIT)
        state.updatePartialTicks(partialTicks)
        model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)

        matrixStack.scale(scale, scale, -scale)
        matrixStack.translate(0.0, 5.5, -4.0)
        matrixStack.pushPose()

        matrixStack.scale(scale * state.scale, scale * state.scale, 0.1F)

        matrixStack.mulPose(quaternion1)
        matrixStack.mulPose(quaternion2)

        val light1 = Vector3f(2.2F, 4.0F, -4.0F)
        val light2 = Vector3f(1.1F, -4.0F, 7.0F)
        RenderSystem.setShaderLights(light1, light2)
        quaternion1.conjugate()

        val immediate = Minecraft.getInstance().renderBuffers().bufferSource()
        val buffer = immediate.getBuffer(renderType)
        val packedLight = LightTexture.pack(11, 7)
        model.render(context, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)

        immediate.endBatch()

        matrixStack.popPose()

        Lighting.setupFor3DItems()
    }

    fun onLogout() {
        this.opacity = MIN_OPACITY
        this.passedSeconds = 0F
        this.lastKnownBattle = null
    }

}