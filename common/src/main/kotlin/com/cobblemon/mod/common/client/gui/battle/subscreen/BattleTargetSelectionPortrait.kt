/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.battles.InBattleMove
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager

class BattleTargetSelectionPortrait(
        battleGUI: BattleGUI,
        request: SingleActionRequest,
        val move: InBattleMove
) : BattleActionSelection(
    battleGUI = battleGUI,
    request = request,
    x = 0,
    y = 0,
    width = 100,
    height = 100,
    battleLang("ui.select_move")
) {

    val targets = request.activePokemon.getAllActivePokemon()

    val backButton = BattleBackButton(x + 9F, Minecraft.getInstance().window.guiScaledHeight - 22F)
    val selectableTargetList = move.target.targetList(request.activePokemon)
    val multiTargetList = if(selectableTargetList == null) request.activePokemon.getMultiTargetList(move.target) else null
    val mc = Minecraft.getInstance()
    var hideBattleOverlay = true

    val baseTiles = targets.mapIndexed { index, target ->
        val isAlly = target.isAllied(request.activePokemon)
        val teamSize = request.activePokemon.getSidePokemon().count()
        val fieldPos = if(isAlly) index % teamSize else teamSize - 1 - (index % teamSize)
        var x = BattleOverlay.HORIZONTAL_INSET + (teamSize - fieldPos - 1) * BattleOverlay.HORIZONTAL_SPACING.toFloat()
        val y = BattleOverlay.VERTICAL_INSET + fieldPos * BattleOverlay.VERTICAL_SPACING.toFloat()
        if (!isAlly) {
            x = mc.window.guiScaledWidth - x - BattleOverlay.TILE_WIDTH
        }
        TargetTile(this, target, x, y, fieldPos)
    }
    var targetTiles = baseTiles

    open inner class TargetTile(
        val targetSelection: BattleTargetSelectionPortrait,
        val target: ActiveClientBattlePokemon,
        val x: Float,
        val y: Float,
        val index: Int,
    ) {
        private val responseTarget = selectableTargetList?.firstOrNull { it.getPNX() == target.getPNX() }?.getPNX()
        private val isMultiTarget = multiTargetList?.firstOrNull { it.getPNX() == target.getPNX() } != null
        open val response: MoveActionResponse get() = MoveActionResponse(targetSelection.move.id, responseTarget)

        open val selectable: Boolean get() = isMultiTarget || (responseTarget != null)
        val hue = target.getHue()
        val rgb = if ((target.battlePokemon?.hpValue ?: 0F) > 0)
            Triple(((hue shr 16) and 0b11111111) / 255F, ((hue shr 8) and 0b11111111) / 255F, (hue and 0b11111111) / 255F)
            else Triple(0.5f, 0.5f, 0.5f)

        fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
            val activeClientBattlePokemon = target
            val selectedPNX = request.activePokemon.getPNX()
            val isAlly = target.isAllied(request.activePokemon)

            CobblemonClient.battleOverlay.drawTile(
                context,
                delta,
                activeClientBattlePokemon,
                isAlly,
                index,
                PokedexEntryProgress.NONE,
                activeClientBattlePokemon.getPNX() == selectedPNX,
                selectable && isHovered(mouseX.toDouble(), mouseY.toDouble()),
                true
            )
        }

        fun isHovered(mouseX: Double, mouseY: Double): Boolean {
            if(isMultiTarget) {
                return targetTiles.firstOrNull { it.selectable && mouseX >= it.x && mouseX <= it.x + BattleOverlay.COMPACT_TILE_WIDTH && mouseY >= it.y && mouseY <= it.y + BattleOverlay.COMPACT_TILE_HEIGHT } != null
            }
            return mouseX >= x && mouseX <= x + BattleOverlay.COMPACT_TILE_WIDTH && mouseY >= y && mouseY <= y + BattleOverlay.COMPACT_TILE_HEIGHT
        }

        fun onClick() {
            if (!selectable) return
            targetSelection.playDownSound(Minecraft.getInstance().soundManager)
            targetSelection.battleGUI.selectAction(targetSelection.request, response)
        }
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if(!CobblemonClient.battleOverlay.hidePortraits != hideBattleOverlay) {
            CobblemonClient.battleOverlay.hidePortraits = hideBattleOverlay
        }
        targetTiles.forEach {
            it.render(context, mouseX, mouseY, delta)
        }

        backButton.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val target = targetTiles.find { it.isHovered(mouseX, mouseY) }
        if (target != null) {
            target.onClick()
            hideBattleOverlay = false
            CobblemonClient.battleOverlay.hidePortraits = hideBattleOverlay
            return true
        } else if (backButton.isHovered(mouseX, mouseY)) {
            playDownSound(Minecraft.getInstance().soundManager)
            battleGUI.changeActionSelection(null)
            hideBattleOverlay = false
            CobblemonClient.battleOverlay.hidePortraits = hideBattleOverlay
        }

        return false
    }


    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }
}