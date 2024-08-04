/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.conversions

import com.cobblemon.mod.common.api.storage.adapter.CobblemonAdapter
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.exists
import net.minecraft.nbt.CompoundTag

interface CobblemonConverter<S> : CobblemonAdapter<S> {

    fun root(): Path

    fun exists(target: Path): Boolean {
        return target.exists()
    }

    fun party(user: UUID, nbt: CompoundTag): PlayerPartyStore

    fun pc(user: UUID, nbt: CompoundTag): PCStore

    fun translate(nbt: CompoundTag): Pokemon

}