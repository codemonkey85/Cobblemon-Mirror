/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.api.ai.SleepDepth
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.biome.Biome

/**
 * Behavioural properties relating to a Pokémon sleeping. This can be wild sleeping or sleeping on the player or both.
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
class RestBehaviour {
    val canSleep = false
    val times = TimeRange.timeRanges["night"]!!
    val sleepChance = 1 / 600F
    val blocks = mutableListOf<RegistryLikeCondition<Block>>()
    val biomes = mutableListOf<RegistryLikeCondition<Biome>>()
    val light = IntRange(0, 15)
    val depth = SleepDepth.normal
    val willSleepOnBed = false
    companion object {
        @JvmStatic
        val CODEC: Codec<RestBehaviour> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("canSleep", false).forGetter(RestBehaviour::canSleep),
                TimeRange.CODEC.optionalFieldOf("times", TimeRange.timeRanges["night"]!!).forGetter(RestBehaviour::times),
                Codec.FLOAT.optionalFieldOf("sleepChance", 1 / 600F).forGetter(RestBehaviour::sleepChance),
                
                CodecUtils.intRange(Codec.intRange(0, 15)).optionalFieldOf("light", IntRange(0, 15)).forGetter(RestBehaviour::light),
                SleepDepth.CODEC.optionalFieldOf("depth", SleepDepth.normal).forGetter(RestBehaviour::depth),
                Codec.BOOL.optionalFieldOf("willSleepOnBed", false).forGetter(RestBehaviour::willSleepOnBed),
            ).apply(instance, ::RestBehaviour)
        }
    }
}