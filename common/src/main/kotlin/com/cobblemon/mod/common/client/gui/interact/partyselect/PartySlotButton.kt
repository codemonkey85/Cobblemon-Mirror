/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.partyselect

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.summary.widgets.PartySlotWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Button.CreateNarration
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

class PartySlotButton(
    x: Int, y: Int,
    val pokemon: PokemonProperties,
    val aspects: Set<String>,
    val currentHealth: Int,
    val maxHealth: Int,
    val heldItem: ItemStack,
    val enabled: Boolean = true,
    val parent: PartySelectGUI,
    onPress: OnPress
) : Button(x, y, WIDTH, HEIGHT, Component.literal("Pokemon"), onPress, CreateNarration { "".text() }) {

    companion object {
        private val slotResource = cobblemonResource("textures/gui/interact/party_select_slot.png")
        private val slotFaintedResource = cobblemonResource("textures/gui/interact/party_select_slot_fainted.png")

        const val WIDTH = 69
        const val HEIGHT = 27
        const val SCALE = 0.5F
    }

    val state = FloatingState()

    private val renderablePokemon = pokemon.asRenderablePokemon().also { it.aspects = aspects }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height && enabled
        val alpha = if (enabled) 1.0 else 0.5
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = if (currentHealth <= 0F) slotFaintedResource else slotResource,
            x = x,
            y = y,
            width = width,
            height = height,
            vOffset = if (isHovered) height else 0,
            textureHeight = height * 2,
            alpha = alpha
        )

        context.pose().pushPose()
        context.pose().translate(x.toDouble() + 13, y.toDouble() - 2, 0.0)

//        if (!hovered) {
//            state.reset()
//        }

        drawProfilePokemon(
            renderablePokemon = renderablePokemon,
            matrixStack = context.pose(),
            rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
            state = state,
            scale = 10F,
            partialTicks = if (!isHovered) 0F else pPartialTicks
        )
        context.pose().popPose()

        val ballIcon = cobblemonResource("textures/gui/ball/" + pokemon.pokeball!!.asIdentifierDefaultingNamespace().path + ".png")
        val ballHeight = 22
        blitk(
            matrixStack = matrices,
            texture = ballIcon,
            x = (x - 2) / SCALE,
            y = (y - 3) / SCALE,
            height = ballHeight,
            width = 18,
            textureHeight = ballHeight * 2,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.lv.number", pokemon.level!!),
            x = x + 24,
            y = y + 6.5,
            shadow = true,
            scale = SCALE
        )

        // Pokémon Name
        val displayName = pokemon.nickname ?: renderablePokemon.species.translatedName
        drawScaledText(
            context = context,
            text = displayName.copy(),
            x = x + 24,
            y = y + 12.5,
            scale = SCALE
        )

        if ("male" in pokemon.aspects || "female" in pokemon.aspects) {
            blitk(
                matrixStack = matrices,
                texture = if ("male" in pokemon.aspects) PartySlotWidget.genderIconMale else PartySlotWidget.genderIconFemale,
                x = (x + 60.5) / SCALE,
                y = (y + 12.5) / SCALE,
                height = 7,
                width = 5,
                scale = SCALE
            )
        }

        // HP
        val hpRatio = currentHealth / maxHealth.toFloat()
        val barWidthMax = 65
        val barWidth = hpRatio * barWidthMax
        val (red, green) = getDepletableRedGreen(hpRatio)

        blitk(
            matrixStack = matrices,
            texture = CobblemonResources.WHITE,
            x = x + 1,
            y = y + 20,
            width = barWidth,
            height = 1,
            textureWidth = barWidth / hpRatio,
            uOffset = barWidthMax - barWidth,
            red = red * 0.8F,
            green = green * 0.8F,
            blue = 0.27F
        )

        drawScaledText(
            context = context,
            text = "$currentHealth/$maxHealth".text(),
            x = x + 14,
            y = y + 22.5,
            scale = SCALE,
            centered = true
        )

        val status = pokemon.status
        if (hpRatio > 0F && status != null) {
            blitk(
                matrixStack = matrices,
                texture = cobblemonResource("textures/gui/interact/party_select_status_$status.png"),
                x = x + 27,
                y = y + 22,
                height = 5,
                width = 37
            )

            drawScaledText(
                context = context,
                text = lang("ui.status.$status").bold(),
                x = x + 32.5,
                y = y + 22.5,
                shadow = true,
                scale = SCALE
            )
        }

        // Held Item
        if (!heldItem.isEmpty) {
            renderScaledGuiItemIcon(
                itemStack = heldItem,
                x = x + 14.0,
                y = y + 9.5,
                scale = 0.5,
                matrixStack = matrices
            )
        }
    }

    override fun playDownSound(soundManager: SoundManager) {}
}