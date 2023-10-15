/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.species.internal

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.species.Species
import com.cobblemon.mod.common.util.codec.setCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.EnumSet

/**
 * Intermediate for [Species] data.
 */
internal data class EggData(
    var eggCycles: Int,
    var eggGroups: EnumSet<EggGroup>,
) {

    companion object {

        val MAP_CODEC: MapCodec<EggData> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Codec.INT.fieldOf("eggCycles").forGetter(EggData::eggCycles),
                setCodec(EggGroup.CODEC).fieldOf("eggGroups").xmap({ set -> EnumSet.copyOf(set) }, { enumSet -> enumSet }).forGetter { eggData -> eggData.eggGroups }
            ).apply(builder, ::EggData)
        }

        val CODEC: Codec<EggData> = MAP_CODEC.codec()

    }

}