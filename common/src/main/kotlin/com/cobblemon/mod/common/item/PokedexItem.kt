/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class PokedexItem(val type: String) : CobblemonItem(Item.Properties()) {

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000

    override fun use(
        world: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        if (player !is ServerPlayer) return InteractionResultHolder.success(itemStack)
        //Disables breaking blocks and damaging entities
        player.startUsingItem(usedHand)
        return InteractionResultHolder.fail(itemStack)
    }

    override fun inventoryTick(
        stack: ItemStack,
        world: Level,
        entity: Entity,
        slot: Int,
        selected: Boolean
    ) {
        if (world.isClientSide) {
            val scanContext = CobblemonClient.pokedexUsageContext
            //if (focusTicks > 0) focusTicks--
        }
    }

    override fun onUseTick(
        world: Level,
        user: LivingEntity,
        stack: ItemStack,
        remainingUseTicks: Int
    ) {
        if (user is LocalPlayer) {
            val scanContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            scanContext.tick(user, ticksInUse, true)
        }
        super.onUseTick(world, user, stack, remainingUseTicks)
    }

    override fun releaseUsing(
        stack: ItemStack,
        world: Level,
        user: LivingEntity,
        remainingUseTicks: Int
    ) {
        if (user is LocalPlayer) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            usageContext.stopUsing(user, ticksInUse)
        }

        super.releaseUsing(stack, world, user, remainingUseTicks)
    }
}

    /*

    fun getCompassTexture(direction: String): ResourceLocation {
        return cobblemonResource("textures/gui/pokedex/compass/$direction.png")
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
    }
}
     */