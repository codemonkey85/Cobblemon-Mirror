/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.trackeddata

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class CountTypeCaughtGlobalTrackedData(
    val type: String,
    var numOfType:Int = 0
) : GlobalTrackedData() {
    @Transient
    override val triggerEvents = setOf(EventTriggerType.CAUGHT, EventTriggerType.EVOLVE, EventTriggerType.TRADE)


    override fun onCatch(event: PokemonCapturedEvent): Boolean {
        if (event.pokemon.species.types.any { it.name == type }) {
            numOfType++
            return true
        }
        return false
    }

    override fun clone(): GlobalTrackedData {
        val result = CountTypeCaughtGlobalTrackedData(type)
        result.numOfType = numOfType
        return result
    }

    override fun internalEncode(buf: RegistryFriendlyByteBuf) {
        buf.writeString(type)
        buf.writeInt(numOfType)
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CountTypeCaughtGlobalTrackedData) return false
        return other == type
    }

    override fun getVariant(): ResourceLocation {
        return ID
    }

    companion object {
        val ID = cobblemonResource("count_type")
        val CODEC = RecordCodecBuilder.mapCodec<CountTypeCaughtGlobalTrackedData> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type },
                PrimitiveCodec.INT.fieldOf("numOfType").forGetter { it.numOfType }
            ).apply(instance, ::CountTypeCaughtGlobalTrackedData)
        }
        fun decode(buf: RegistryFriendlyByteBuf): CountTypeCaughtGlobalTrackedData {
            val type = buf.readString()
            val numOfType = buf.readInt()
            val result = CountTypeCaughtGlobalTrackedData(type)
            result.numOfType = numOfType
            return result
        }
    }
}