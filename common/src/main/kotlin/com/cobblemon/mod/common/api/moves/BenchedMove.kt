/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves

import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.RegistryFriendlyByteBuf

class BenchedMoves : Iterable<BenchedMove> {
    val observable = SimpleObservable<BenchedMoves>()
    private var emit = true
    private val benchedMoves = mutableListOf<BenchedMove>()

    fun doWithoutEmitting(action: () -> Unit) {
        val previousEmit = emit
        emit = false
        action()
        emit = previousEmit
    }

    fun copyFrom(other: BenchedMoves) {
        doWithoutEmitting {
            clear()
            other.forEach { add(it) }
        }
        update()
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

    fun add(benchedMove: BenchedMove) = doThenEmit { benchedMoves.add(benchedMove) }
    fun addAll(benchedMoves: Iterable<BenchedMove>) = doThenEmit { this.benchedMoves.addAll(benchedMoves) }
    fun clear() = doThenEmit { benchedMoves.clear() }
    fun remove(benchedMove: BenchedMove) = doThenEmit { benchedMoves.remove(benchedMove) }
    fun remove(moveTemplate: MoveTemplate) = doThenEmit { benchedMoves.removeIf { it.moveTemplate == moveTemplate } }
    override fun iterator() = benchedMoves.iterator()

    fun saveToNBT(nbt: ListTag): ListTag {
        nbt.addAll(benchedMoves.map { it.saveToNBT(CompoundTag()) })
        return nbt
    }

    fun saveToJSON(json: JsonArray): JsonArray {
        val jsons = benchedMoves.map { it.saveToJSON(JsonObject()) }
        jsons.forEach { json.add(it) }
        return json
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeShort(benchedMoves.size)
        benchedMoves.forEach { it.saveToBuffer(buffer) }
    }

    fun loadFromNBT(nbt: ListTag): BenchedMoves {
        doThenEmit {
            clear()
            nbt.forEach { benchedMoves.add(BenchedMove.loadFromNBT(it as CompoundTag)) }
        }

        return this
    }

    fun loadFromJSON(json: JsonArray): BenchedMoves {
        doThenEmit {
            clear()
            json.forEach { benchedMoves.add(BenchedMove.loadFromJSON(it.asJsonObject)) }
        }
        return this
    }

    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): BenchedMoves {
        doThenEmit {
            clear()
            repeat(times = buffer.readShort().toInt()) {
                benchedMoves.add(BenchedMove.loadFromBuffer(buffer))
            }
        }
        return this
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<BenchedMoves> = Codec.list(BenchedMove.CODEC)
            .xmap(
                { moveList ->
                    val benchedMoves = BenchedMoves()
                    benchedMoves.addAll(moveList)
                    return@xmap benchedMoves
                },
                BenchedMoves::toList
            )
    }
}

data class BenchedMove(val moveTemplate: MoveTemplate, val ppRaisedStages: Int) {
    fun saveToNBT(nbt: CompoundTag): CompoundTag {
        nbt.putString(DataKeys.POKEMON_MOVESET_MOVENAME, moveTemplate.name)
        nbt.putByte(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES, ppRaisedStages.toByte())
        return nbt
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.POKEMON_MOVESET_MOVENAME, moveTemplate.name)
        json.addProperty(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES, ppRaisedStages)
        return json
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(moveTemplate.name)
        buffer.writeSizedInt(IntSize.U_BYTE, ppRaisedStages)
    }

    companion object {
        fun loadFromNBT(nbt: CompoundTag): BenchedMove {
            val name = nbt.getString(DataKeys.POKEMON_MOVESET_MOVENAME)
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                nbt.getByte(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES).toInt()
            )
        }

        fun loadFromJSON(json: JsonObject): BenchedMove {
            val name = json.get(DataKeys.POKEMON_MOVESET_MOVENAME).asString
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                json.get(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES).asInt
            )
        }

        fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): BenchedMove {
            val name = buffer.readString()
            return BenchedMove(
                Moves.getByName(name) ?: MoveTemplate.dummy(name),
                buffer.readSizedInt(IntSize.U_BYTE)
            )
        }

        @JvmStatic
        val CODEC: Codec<BenchedMove> = RecordCodecBuilder.create { it.group(
            MoveTemplate.BY_STRING_CODEC.fieldOf(DataKeys.POKEMON_MOVESET_MOVENAME).forGetter(BenchedMove::moveTemplate),
            Codec.intRange(0, 3).fieldOf(DataKeys.POKEMON_MOVESET_RAISED_PP_STAGES).forGetter(BenchedMove::ppRaisedStages)
        ).apply(it, ::BenchedMove) }
    }
}