/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.keybind.keybinds.SummaryBinding
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft

/**
 * Util class for preventing clicks until the player has a party member, this will automatically handle starter selection
 */
abstract class CobblemonPartyLockedKeyBinding(
    name: String,
    type: InputConstants.Type,
    key: Int,
    category: String
) : CobblemonKeyBinding(name, type, key, category) {

    private var skippedStarterSelectionMessageShown = false

    override fun onTick() {
        if (this.consumeClick() && this.hasPartyMembers()) {
            this.onPress()
        }
    }

    /**
     *  Checks if the player has any party members, and
     *
     *  - Handles the starter selection if the player doesn't have any party members.
     *  - Shows a message if the player initially skipped the starter selection.
     */
    private fun hasPartyMembers(): Boolean {
        val havePokemon = CobblemonClient.storage.myParty.slots.any { it != null }
        val starterSelected = CobblemonClient.clientPlayerData.starterSelected
        val startersLocked = CobblemonClient.clientPlayerData.starterLocked
        if (!starterSelected && !havePokemon) {
            if (startersLocked) {
                Minecraft.getInstance().player?.displayClientMessage(lang("ui.starter.cannotchoose").red(), false)
            } else {
                RequestStarterScreenPacket().sendToServer()
            }
            return false
        } else if (!startersLocked && !starterSelected && havePokemon) {
            if (!skippedStarterSelectionMessageShown) {
                Minecraft.getInstance().player?.displayClientMessage(
                    lang(
                        "ui.starter.skippedchoosing",
                        SummaryBinding.boundKey().displayName
                    ).yellow(), false
                )
                /** Only show the info message about skipping the selection once per MC instance */
                skippedStarterSelectionMessageShown = true
            }
            return true
        }
        return true
    }

}