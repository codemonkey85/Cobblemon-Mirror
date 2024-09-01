/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * Represents a stat of a Pokémon.
 * If you wish to implement custom stats be sure to implement your own [StatProvider].
 * Any custom implementation must be provided to both sides.
 *
 * @author Licious
 * @since November 6th, 2022
 */
interface Stat {

    /**
     * The [ResourceLocation] of this stat.
     */
    val identifier: ResourceLocation

    /**
     * The display name of this stat.
     * This should ideally provide the lang.
     */
    val displayName: Component

    /**
     * The type of this stat.
     */
    val type: Type

    /**
     * The name used by Showdown to store the stats (attack is 'atk', for example)
     */
    val showdownId: String

    /**
     * Represents the type of this stat.
     */
    enum class Type {

        /**
         * Represents stats that always exist.
         * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Stat#Permanent_stats) page.
         */
        PERMANENT,

        /**
         * Represents stats that only exist during a battle.
         * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Stat#In-battle_stats) page.
         */
        BATTLE_ONLY

    }

    companion object {

        /**
         * A [Codec] for [Stat] without filtering the [Stat.Type].
         */
        @JvmStatic
        val ALL_CODEC: Codec<Stat> = CodecUtils.createByIdentifierCodec(
            Cobblemon.statProvider::fromIdentifier,
            Stat::identifier
        ) { identifier -> "No Stat for ID $identifier" }

        /**
         * A [Codec] for [Stat] with the [Stat.Type.PERMANENT].
         */
        @JvmStatic
        val PERMANENT_ONLY_CODEC: Codec<Stat> = ALL_CODEC.comapFlatMap(
            { stat -> if (stat.type == Type.PERMANENT) DataResult.success(stat) else DataResult.error { "${stat.identifier} is not of type ${Type.PERMANENT}" } },
            { stat -> stat }
        )

        /**
         * A [Codec] for [Stat] with the [Stat.Type.BATTLE_ONLY].
         */
        @JvmStatic
        val BATTLE_ONLY_CODEC: Codec<Stat> = ALL_CODEC.comapFlatMap(
            { stat -> if (stat.type == Type.BATTLE_ONLY) DataResult.success(stat) else DataResult.error { "${stat.identifier} is not of type ${Type.BATTLE_ONLY}" } },
            { stat -> stat }
        )

    }

}