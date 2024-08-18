/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves.entry

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.mojang.serialization.Codec
import net.minecraft.core.Holder

interface LearnsetEntry {

    val move: Holder<MoveTemplate>

    val syncToClient: Boolean

    val type: LearnsetEntryType<*>

    companion object {

        @JvmStatic
        val CODEC: Codec<LearnsetEntry> = LearnsetEntryType.REGISTRY
            .byNameCodec()
            .dispatch(LearnsetEntry::type) { it.codec() }

    }

}