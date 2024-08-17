/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
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

class PokedexItem(val type: PokedexTypes) : CobblemonItem(Item.Properties()) {

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000

    override fun use(
        world: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        if (player is LocalPlayer) {
            CobblemonClient.pokedexUsageContext.type = type
        }
        if (player !is ServerPlayer) return InteractionResultHolder.success(itemStack)
        //Disables breaking blocks and damaging entities
        player.startUsingItem(usedHand)
        return InteractionResultHolder.fail(itemStack)
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
    @Environment(EnvType.CLIENT)
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