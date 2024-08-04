/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.trackeddata

import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.util.writeIdentifier
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A type of tracked data specific to a species
 *
 * @author Apion
 * @since February 24, 2024
 */
abstract class SpeciesTrackedData(val speciesFilter: String) {
    abstract val triggerEvents: Set<EventTriggerType>
    @Transient
    val syncToClient = false

    open fun onCatch(event: PokemonCapturedEvent): Boolean {
        return false
    }

    open fun onEvolve(event: EvolutionCompleteEvent): Boolean {
        return false
    }

    open fun onTrade(event: TradeCompletedEvent): Boolean {
        return false
    }

    open fun onBattleStart(event: BattleStartedPostEvent): Boolean {
        return false
    }

    fun encode(buf: RegistryFriendlyByteBuf) {
        val variant = buf.writeIdentifier(TrackedDataTypes.classToVariant[this::class]!!)
        internalEncode(buf)
    }

    //This is only used to check for duplicate entries, so it doesn't have to check every field
    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean

    //The type of GlobalTrackedData, used for serializing/deserializing
    abstract fun getVariant(): ResourceLocation

    abstract fun clone(): GlobalTrackedData

    abstract fun internalEncode(buf: RegistryFriendlyByteBuf)

    companion object {
        val CODEC: Codec<SpeciesTrackedData> = ResourceLocation.CODEC.dispatch("variant", {
            TrackedDataTypes.classToVariant[it::class]
        }) {
            TrackedDataTypes.variantToCodec[it] as MapCodec<out SpeciesTrackedData>
        }
    }
}