/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.battles.ForfeitActionResponse
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

class ForfeitConfirmationSelection(
    battleGUI: BattleGUI,
    request: SingleActionRequest
) : BattleActionSelection(
    battleGUI,
    request,
    x = 12,
    y = 12,
    width = 250,
    height = 100,
    battleLang("ui.forfeit_confirmation")
) {

    val forfeitButton: BattleOptionTile
    val backButton = BattleBackButton(x - 3F, Minecraft.getInstance().window.guiScaledHeight - 22F )

    init {
        val x = (Minecraft.getInstance().window.guiScaledWidth / 2) - (BattleOptionTile.OPTION_WIDTH / 2)
        val y = (Minecraft.getInstance().window.guiScaledHeight / 2) - (BattleOptionTile.OPTION_HEIGHT / 2)

        forfeitButton = BattleOptionTile(battleGUI, x, y, BattleGUI.runResource, battleLang("ui.forfeit")) {
            battleGUI.selectAction(request, ForfeitActionResponse())

            // Need to fill out any other pending requests
            var pendingRequest = CobblemonClient.battle?.getFirstUnansweredRequest()
            while (pendingRequest != null) {
                battleGUI.selectAction(pendingRequest, PassActionResponse)
                pendingRequest = CobblemonClient.battle?.getFirstUnansweredRequest()
            }
            playDownSound(Minecraft.getInstance().soundManager)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (backButton.isHovered(mouseX, mouseY)) {
            battleGUI.changeActionSelection(null)
            playDownSound(Minecraft.getInstance().soundManager)
            return true
        }

        return forfeitButton.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (opacity <= 0.05F) {
            return
        }
        forfeitButton.render(context, mouseX, mouseY, delta)
        backButton.render(context, mouseX, mouseY, delta)
    }

}