/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.keybind.keybinds.*
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.client.KeyMapping

/**
 * Main registry for KeyBinds
 *
 * @author Qu
 * @since 2022-02-17
 */
object CobblemonKeyBinds {
    private val keyBinds = arrayListOf<CobblemonKeyBinding>()

    init {
        PlatformEvents.CLIENT_TICK_POST.subscribe { this.onTick() }
        if (Cobblemon.config.enableDebugKeys) {
            DebugKeybindings.keybindings.forEach {
                this.queue(it)
            }
        }
    }

    val HIDE_PARTY = this.queue(HidePartyBinding)
    // ToDo enable again down the line
    //val POKENAV = this.queue(PokeNavigatorBinding)
    val SUMMARY = this.queue(SummaryBinding)
    val PARTY_OVERLAY_DOWN = this.queue(DownShiftPartyBinding)
    val PARTY_OVERLAY_UP = this.queue(UpShiftPartyBinding)
    val SEND_OUT_POKEMON = this.queue(PartySendBinding)

    fun register(registrar: (KeyMapping) -> Unit) {
        this.keyBinds.forEach(registrar::invoke)
    }

    // Both Forge and Fabric recommend this as the method to detect clicks inside a game
    private fun onTick() {
        this.keyBinds.forEach(CobblemonKeyBinding::onTick)
    }

    private fun queue(keyBinding: CobblemonKeyBinding): KeyMapping {
        this.keyBinds.add(keyBinding)
        return keyBinding
    }

}