/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.battles.ShowdownPokemon
import com.cobblemon.mod.common.battles.SwitchActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import org.joml.Quaternionf
import org.joml.Vector3f

class BattleSwitchPokemonSelection(
    battleGUI: BattleGUI,
    request: SingleActionRequest
) : BattleActionSelection(
    battleGUI,
    request,
    x = 0,
    y = if (Minecraft.getInstance().window.guiScaledHeight > 630) Minecraft.getInstance().window.guiScaledHeight / 2 - 148 / 2
    else Minecraft.getInstance().window.guiScaledHeight - 226,
    width = Minecraft.getInstance().window.guiScaledWidth,
    height = Minecraft.getInstance().window.guiScaledHeight,
    battleLang("switch_pokemon")
) {
    companion object {
        const val SLOT_HORIZONTAL_SPACING = 4F
        const val SLOT_VERTICAL_SPACING = 2F

        const val BACKGROUND_HEIGHT = 148
        val underlayTexture = cobblemonResource("textures/gui/battle/selection_underlay.png")
    }

    val tiles = mutableListOf<SwitchTile>()
    val backButton = BattleBackButton(x + 9F, Minecraft.getInstance().window.guiScaledHeight - 22F )

    var isReviving = false

    init {
        val pendingActionRequests = CobblemonClient.battle!!.pendingActionRequests
        val switchingInPokemon = pendingActionRequests.mapNotNull { it.response }.filterIsInstance<SwitchActionResponse>().map { it.newPokemonId }
        val showdownPokemonToPokemon = request.side!!.pokemon
            .mapNotNull { showdownPokemon ->
                battleGUI.actor!!.pokemon
                    .find { it.uuid == showdownPokemon.uuid }
                    ?.let { showdownPokemon to it }
            }.filter { it.second.uuid !in switchingInPokemon }

        isReviving = request.side.pokemon.any { it.reviving && it.uuid == request.activePokemon.battlePokemon?.uuid }
        if (request.forceSwitch && !isReviving && showdownPokemonToPokemon.all {
            (it.second.uuid in battleGUI.actor!!.activePokemon.map { it.battlePokemon?.uuid } || ("fnt" in it.first.condition))}) { // on field or fainted
            // Occurs after a multi-knock out and the player doesn't have enough pokemon to fill every vacant slot
            battleGUI.selectAction(request, PassActionResponse)
        }

        showdownPokemonToPokemon.forEachIndexed { index, (showdownPokemon, pokemon) ->
            val (slotX, slotY) = getSlotPosition(index)
            val isFainted = "fnt" in showdownPokemon.condition
            val isCurrentlyInBattle = pokemon.uuid in battleGUI.actor!!.activePokemon.map { it.battlePokemon?.uuid }

            tiles.add(SwitchTile(this, slotX, slotY, pokemon, showdownPokemon, isFainted, isCurrentlyInBattle))
        }
    }

    fun getSlotPosition(index: Int): Pair<Float, Float> {
        val startX = ((width / 2) - SwitchTile.SELECT_WIDTH - 1)
        val startY = y + 34
        val row = index / 2
        val column = index % 2
        val slotX = startX.toFloat() + column * (SLOT_HORIZONTAL_SPACING + SwitchTile.SELECT_WIDTH)
        val slotY = startY.toFloat() + row * (SLOT_VERTICAL_SPACING + SwitchTile.SELECT_HEIGHT)
        return Pair(slotX, slotY)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (opacity <= 0.05F) return

        val matrixStack = context.pose()
        blitk(
            matrixStack = matrixStack,
            texture = underlayTexture,
            x = x,
            y = y,
            width = width,
            height = BACKGROUND_HEIGHT
        )

        // Draw Title Text
        val text = lang("ui.party")
        val textWidth = Minecraft.getInstance().font.width(text)
        drawScaledText(
            context = context,
            text = text,
            x = (width - textWidth) / 2,
            y = y + 17,
            shadow = true
        )

        for (index in 0 until 6) {
            val (slotX, slotY) = getSlotPosition(index)
            blitk(
                matrixStack = matrixStack,
                texture = SwitchTile.partySelectDisabledResourse,
                x = slotX,
                y = slotY,
                width = SwitchTile.SELECT_WIDTH,
                height = SwitchTile.SELECT_HEIGHT - 7,
                vOffset = SwitchTile.SELECT_HEIGHT,
                textureHeight = SwitchTile.SELECT_HEIGHT * 2,
            )
        }

        tiles.forEach { it.render(context, mouseX.toDouble(), mouseY.toDouble(), delta) }
        if(!request.forceSwitch) {
            backButton.render(context, mouseX, mouseY, delta)
        }
    }

    override fun mousePrimaryClicked(mouseX: Double, mouseY: Double): Boolean {
        if (backButton.isHovered(mouseX, mouseY)) {
            battleGUI.changeActionSelection(null)
            playDownSound(Minecraft.getInstance().soundManager)
            return true
        }
        val clicked = tiles.find { it.isHovered(mouseX, mouseY) && if (isReviving) it.isFainted else (!it.isFainted && !it.isCurrentlyInBattle) } ?: return false
        val pokemon = clicked.pokemon
        playDownSound(Minecraft.getInstance().soundManager)
        battleGUI.selectAction(request, SwitchActionResponse(pokemon.uuid))

        return true
    }

    override fun defaultButtonNarrationText(builder: NarrationElementOutput) {
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun narrationPriority() = NarratableEntry.NarrationPriority.HOVERED

    class SwitchTile(
        val selection: BattleSwitchPokemonSelection,
        val x: Float,
        val y: Float,
        val pokemon: Pokemon,
        val showdownPokemon: ShowdownPokemon,
        val isFainted: Boolean,
        val isCurrentlyInBattle: Boolean
    ) {
        companion object {
            const val SELECT_WIDTH = 94
            const val SELECT_HEIGHT = 29
            const val SCALE = 0.5F
            val partySelectResource = cobblemonResource("textures/gui/battle/party_select.png")
            val partySelectDisabledResourse = cobblemonResource("textures/gui/battle/party_select_disabled.png")
        }

        val state = FloatingState()

        fun isHovered(mouseX: Double, mouseY: Double) = mouseX in x..(x + SELECT_WIDTH) && mouseY in (y..(y + SELECT_HEIGHT))

        fun render(context: GuiGraphics, mouseX: Double, mouseY: Double, deltaTicks: Float) {
            state.currentAspects = pokemon.aspects
            val matrixStack = context.pose()
            val healthRatioSplits = showdownPokemon.condition.split(" ")[0].split("/")
            try {
                val (hp, maxHp) = if (healthRatioSplits.size == 1) 0 to 0
                    else healthRatioSplits[0].toInt() to pokemon.maxHealth

                val hpRatio = hp / maxHp.toFloat()
                val status = pokemon.status?.status?.showdownName
                if (hpRatio > 0F && status != null) {
                    blitk(
                        matrixStack = matrixStack,
                        texture = cobblemonResource("textures/gui/interact/party_select_status_$status.png"),
                        x = x + 27,
                        y = y + 24,
                        height = 5,
                        width = 37
                    )

                    drawScaledText(
                        context = context,
                        text = lang("ui.status.$status").bold(),
                        x = x + 32.5,
                        y = y + 24.5,
                        shadow = true,
                        scale = SCALE
                    )
                }

                blitk(
                    matrixStack = matrixStack,
                    texture = if (!isFainted && !isCurrentlyInBattle) partySelectResource else partySelectDisabledResourse,
                    x = x,
                    y = y,
                    width = SELECT_WIDTH,
                    height = SELECT_HEIGHT,
                    vOffset = if (!isFainted && (isHovered(mouseX, mouseY) || isCurrentlyInBattle)) SELECT_HEIGHT else 0,
                    textureHeight = SELECT_HEIGHT * 2,
                )

                val ballIcon = cobblemonResource("textures/gui/ball/" + pokemon.caughtBall.name.path + ".png")
                val ballHeight = 22
                blitk(
                    matrixStack = matrixStack,
                    texture = ballIcon,
                    x = (x + 85) / SCALE,
                    y = (y - 3) / SCALE,
                    height = ballHeight,
                    width = 18,
                    vOffset = if (isCurrentlyInBattle) ballHeight else 0,
                    textureHeight = ballHeight * 2,
                    scale = SCALE
                )

                // Render Pokémon
                matrixStack.pushPose()
                matrixStack.translate(x + SELECT_WIDTH - (25 / 2.0) - 4, y - 1.0, 0.0)
                matrixStack.scale(2.5F, 2.5F, 1F)
                drawProfilePokemon(
                    species = pokemon.species.resourceIdentifier,
                    matrixStack = matrixStack,
                    rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
                    state = state,
                    scale = 4.5F,
                    partialTicks = deltaTicks
                )
                matrixStack.popPose()

                // Ensure elements are not hidden behind Pokémon render
                matrixStack.pushPose()
                matrixStack.translate(0.0, 0.0, 100.0)
                // Held Item
                val heldItem = pokemon.heldItemNoCopy()
                if (!heldItem.isEmpty) {
                    renderScaledGuiItemIcon(
                        matrixStack = matrixStack,
                        itemStack = heldItem,
                        x = x + 81.0,
                        y = y + 11.0,
                        scale = 0.5
                    )
                }

                val textOpacity = if (isFainted || isCurrentlyInBattle) 0.7F else 1F

                // Target Level
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = lang("ui.lv").bold(),
                    x = x + 5,
                    y = y + 4,
                    opacity = textOpacity,
                    shadow = true
                )
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = pokemon.level.toString().text().bold(),
                    x = x + 5 + 13,
                    y = y + 4,
                    opacity = textOpacity,
                    shadow = true
                )

                val displayText = pokemon.getDisplayName().bold()
                // Pokémon Display Name
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = displayText,
                    x = x + 5,
                    y = y + 11,
                    opacity = textOpacity,
                    shadow = true
                )

                // Gender
                val gender = pokemon.gender
                val pokemonDisplayNameWidth = Minecraft.getInstance().font.width(displayText.font(CobblemonResources.DEFAULT_LARGE))
                if (gender != Gender.GENDERLESS) {
                    val isMale = gender == Gender.MALE
                    val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
                    drawScaledText(
                        context = context,
                        font = CobblemonResources.DEFAULT_LARGE,
                        text = textSymbol,
                        x = x + 6 + pokemonDisplayNameWidth,
                        y = y + 11,
                        colour = if (isMale) 0x32CBFF else 0xFC5454,
                        opacity = textOpacity,
                        shadow = true
                    )
                }

                // HP
                val barWidthMax = 90
                val barWidth = hpRatio * barWidthMax
                val (red, green) = getDepletableRedGreen(hpRatio)

                blitk(
                    matrixStack = matrixStack,
                    texture = CobblemonResources.WHITE,
                    x = x + 1,
                    y = y + 22,
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
                    text = "$hp/$maxHp".text(),
                    x = x + 14,
                    y = y + 24.5,
                    scale = SCALE,
                    centered = true
                )
                matrixStack.popPose()
            } catch (exception: Exception) {
                throw exception
            }
        }
    }
}