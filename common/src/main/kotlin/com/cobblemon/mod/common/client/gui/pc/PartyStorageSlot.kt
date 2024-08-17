/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.client.storage.ClientParty
import com.cobblemon.mod.common.pokemon.Pokemon

class PartyStorageSlot(
    x: Int, y: Int,
    private val parent: StorageWidget,
    private val party: ClientParty,
    val position: PartyPosition,
    onPress: OnPress
) : StorageSlot(x, y, parent, onPress) {

    override fun getPokemon(): Pokemon? {
        return party.get(position)
    }

    override fun shouldRender(): Boolean {
        val grabbedSlot = parent.grabbedSlot
        return if (grabbedSlot == null) {
            true
        } else {
            grabbedSlot.getPokemon() != getPokemon()
        }
    }
}