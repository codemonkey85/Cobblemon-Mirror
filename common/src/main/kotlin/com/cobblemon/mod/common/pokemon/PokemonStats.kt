/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.writeSizedInt
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Holds a mapping from a Stat to value that should be reducible to a short for NBT and net.
 */
abstract class PokemonStats : Iterable<Map.Entry<Stat, Int>> {
    abstract val acceptableRange: IntRange
    abstract val defaultValue: Int
    override fun iterator() = stats.entries.iterator()

    /** Emits any stat change. */
    val observable = SimpleObservable<PokemonStats>()

    private val stats = mutableMapOf<Stat, Int>()
    private var emit = true

    fun doWithoutEmitting(action: () -> Unit) {
        emit = false
        action()
        emit = true
    }

    fun doThenEmit(action: () -> Unit) {
        doWithoutEmitting(action)
        update()
    }

    fun update() {
        if (emit) {
            observable.emit(this)
        }
    }

    operator fun get(key: Stat) = stats[key]
    open operator fun set(key: Stat, value: Int) {
        if (this.canSet(key, value)) {
            stats[key] = value
            update()
        }
    }

    protected open fun canSet(stat: Stat, value: Int) = value in acceptableRange

    fun saveToNBT(nbt: CompoundTag): CompoundTag {
        this.stats.forEach { (stat, value) ->
            // don't waste space if default
            if (value != this.defaultValue) {
                nbt.putShort(this.cleanStatIdentifier(stat.identifier), value.toShort())
            }
        }
        return nbt
    }

    fun loadFromNBT(nbt: CompoundTag): PokemonStats {
        stats.clear()
        Stats.PERMANENT.forEach { stat ->
            val identifier = this.cleanStatIdentifier(stat.identifier)
            this[stat] = nbt.getShort(identifier).toInt().coerceIn(this.acceptableRange)
        }
        return this
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        this.stats.forEach { (stat, value) ->
            // don't waste space if default
            if (value != this.defaultValue) {
                json.addProperty(this.cleanStatIdentifier(stat.identifier), value)
            }
        }
        return json
    }

    fun loadFromJSON(json: JsonObject): PokemonStats {
        stats.clear()

        Stats.PERMANENT.forEach { stat ->
            val identifier = this.cleanStatIdentifier(stat.identifier)
            this[stat] = json.get(identifier)?.asInt?.coerceIn(this.acceptableRange)
                ?: this.defaultValue
        }
        return this
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, stats.size)
        for ((stat, value) in stats) {
            Cobblemon.statProvider.encode(buffer, stat)
            buffer.writeSizedInt(IntSize.U_SHORT, value)
        }
    }

    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf) {
        stats.clear()
        repeat(times = buffer.readUnsignedByte().toInt()) {
            val stat = Cobblemon.statProvider.decode(buffer)
            val value = buffer.readUnsignedShort()
            stats[stat] = value
        }
    }

    fun getOrDefault(stat: Stat) = this[stat] ?: this.defaultValue

    // util to prevent unnecessary long identifiers, usually vanilla defaults to Minecraft but in our context defaulting to cobblemon makes more sense
    private fun cleanStatIdentifier(identifier: ResourceLocation): String = identifier.toString().substringAfter("${Cobblemon.MODID}:")

}