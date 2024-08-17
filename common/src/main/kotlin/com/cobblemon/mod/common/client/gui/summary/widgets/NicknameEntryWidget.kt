/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetNicknamePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component

class NicknameEntryWidget(
    var pokemon: Pokemon, x: Int, y: Int, width: Int, height: Int, val isParty: Boolean, text: Component
): EditBox(
    Minecraft.getInstance().font,
    x, y, width, height, text
) {
    companion object {
        private const val MAX_NAME_LENGTH = 12
    }

    var pokemonName = ""

    init {
        setMaxLength(MAX_NAME_LENGTH)
        setSelectedPokemon(pokemon)
    }

    fun setSelectedPokemon(pokemon: Pokemon) {
        if (isFocused) {
            isFocused = false
        }

        this.pokemon = pokemon
        this.pokemonName = I18n.get(pokemon.species.translatedName.string)

        this.setResponder {
            if (it.isNotBlank()) {
                this.updateNickname(it)
            } else {
                this.updateNickname(pokemonName)
            }
        }
        value = pokemon.getDisplayName().string
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)) {
            isFocused = true
            true
        } else {
            false
        }
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        value = value.trim().ifBlank { pokemonName }
        if (!focused) {
            this.updateNickname(value)
        }
    }

    private fun updateNickname(newNickname: String) {
        if (pokemon.nickname == null || pokemon.nickname?.string != newNickname) {
            val effectiveNickname = if (newNickname == pokemonName) null else newNickname
            CobblemonNetwork.sendToServer(
                SetNicknamePacket(
                    pokemonUUID = pokemon.uuid,
                    nickname = effectiveNickname,
                    isParty = isParty
                )
            )
        }
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (cursorPosition != value.length) moveCursorToEnd(Screen.hasShiftDown())

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.translatable(if (isFocused) "$value|" else value).bold(),
            x = x,
            y = y,
            shadow = true
        )
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.updateNickname(value.trim().ifBlank { this.pokemonName })
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}