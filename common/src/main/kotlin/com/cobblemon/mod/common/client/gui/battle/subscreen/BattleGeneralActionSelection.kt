/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class BattleGeneralActionSelection(
    battleGUI: BattleGUI,
    request: SingleActionRequest
) : BattleActionSelection(
    battleGUI,
    request,
    BattleGUI.OPTION_ROOT_X,
    Minecraft.getInstance().window.guiScaledHeight - BattleGUI.OPTION_VERTICAL_OFFSET,
    (BattleOptionTile.OPTION_WIDTH + 3 * BattleGUI.OPTION_HORIZONTAL_SPACING).toInt(),
    (BattleOptionTile.OPTION_HEIGHT + 3 * BattleGUI.OPTION_VERTICAL_SPACING).toInt(),
    battleLang("choose_action")
) {
    val backButton = BattleBackButton(BattleGUI.OPTION_ROOT_X - 3F, Minecraft.getInstance().window.guiScaledHeight - 22F)
    val lastAnwseredRequest = CobblemonClient.battle?.getLastAnsweredRequest()

    val tiles = mutableListOf<BattleOptionTile>()
    init {
        var rank = 0

        addOption(rank++, battleLang("ui.fight"), BattleGUI.fightResource) {
            playDownSound(Minecraft.getInstance().soundManager)
            battleGUI.changeActionSelection(BattleMoveSelection(battleGUI, request))
        }

        if (request.moveSet?.trapped != true) {
            addOption(rank++, battleLang("ui.switch"), BattleGUI.switchResource) {
                battleGUI.changeActionSelection(BattleSwitchPokemonSelection(battleGUI, request))
                playDownSound(Minecraft.getInstance().soundManager)
            }
        }

        CobblemonClient.battle?.let { battle ->
            if (battle.battleFormat.battleType.pokemonPerSide == 1 && battle.side2.actors.first().type == ActorType.WILD) {
                addOption(rank++, battleLang("ui.capture"), BattleGUI.bagResource) {
                    CobblemonClient.battle?.minimised = true
                    Minecraft.getInstance().player?.displayClientMessage(battleLang("throw_pokeball_prompt"), false)
                    playDownSound(Minecraft.getInstance().soundManager)
                }

                addOption(rank++, battleLang("ui.run"), BattleGUI.runResource) {
                    CobblemonClient.battle?.minimised = true
                    Minecraft.getInstance().player?.displayClientMessage(battleLang("run_prompt"), false)
                    playDownSound(Minecraft.getInstance().soundManager)
                }
            } else {
                addOption(rank++, battleLang("ui.forfeit"), BattleGUI.forfeitResource) {
                    battleGUI.changeActionSelection(ForfeitConfirmationSelection(battleGUI, request))
                    playDownSound(Minecraft.getInstance().soundManager)
                }
            }
        }
    }

    private fun addOption(rank: Int, text: MutableComponent, texture: ResourceLocation, onClick: () -> Unit) {
        val startY = Minecraft.getInstance().window.guiScaledHeight - BattleGUI.OPTION_VERTICAL_OFFSET
        val x = if (rank % 2 == 0) BattleGUI.OPTION_ROOT_X else BattleGUI.OPTION_ROOT_X + BattleGUI.OPTION_HORIZONTAL_SPACING + BattleOptionTile.OPTION_WIDTH
        val y = if (rank > 1) startY + BattleOptionTile.OPTION_HEIGHT + BattleGUI.OPTION_HORIZONTAL_SPACING else startY
        tiles.add(
            BattleOptionTile(
                battleGUI = battleGUI,
                x = x,
                y = y,
                resource = texture,
                text = text,
                onClick = onClick
            )
        )
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if(lastAnwseredRequest != null) {
            backButton.render(context, mouseX, mouseY, delta)
        }
        for (tile in tiles) {
            tile.render(context, mouseX, mouseY, delta)
        }
    }

    override fun mousePrimaryClicked(mouseX: Double, mouseY: Double): Boolean {
        if (lastAnwseredRequest != null && backButton.isHovered(mouseX, mouseY)) {
            playDownSound(Minecraft.getInstance().soundManager)
            CobblemonClient.battle?.cancelLastAnsweredRequest()
            battleGUI.selectAction(request, null)
            battleGUI.changeActionSelection(null)
        }
        return tiles.any { it.mousePrimaryClicked(mouseX, mouseY) }
    }

    override fun defaultButtonNarrationText(builder: NarrationElementOutput) {
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun narrationPriority() = NarratableEntry.NarrationPriority.NONE
}