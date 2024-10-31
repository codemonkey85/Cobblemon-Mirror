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
import com.cobblemon.mod.common.util.writeSizedInt
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import com.mojang.serialization.Codec
import kotlin.math.min

class MoveSet : Iterable<Move> {
    val observable = SimpleObservable<MoveSet>()
    private var emit = true

    private val moves = arrayOfNulls<Move>(MOVE_COUNT)


    override fun iterator() = moves.filterNotNull().iterator()

    operator fun get(index: Int) = index.takeIf { it in 0 until MOVE_COUNT }?.let { moves[it] }

    /**
     * Gets all Moves from the Pokémon but skips null Moves
     */
    fun getMoves() = moves.filterNotNull()
    fun getMovesWithNulls() = moves.toList()
    fun hasSpace() = moves.any { it == null }

    /**
     * Sets the given Move to given position
     */
    fun setMove(pos: Int, move: Move?) {
        if (pos !in 0 until MOVE_COUNT) {
            return
        }
        moves[pos] = move
        move?.observable?.subscribe { this.update() }
        update()
    }

    fun copyFrom(other: MoveSet) {
        doWithoutEmitting {
            clear()
            other.getMoves().forEach { add(it.copy()) }
        }
        update()
    }

    fun heal() {
        getMoves().forEach { it.currentPp = it.maxPp }
        update()
    }

    fun partialHeal() {
        getMoves().forEach { it.currentPp = min((it.currentPp + (it.maxPp / 2)), it.maxPp)}
        update()
    }

    fun clear() {
        doWithoutEmitting {
            for (i in 0 until MOVE_COUNT){
                setMove(i, null)
            }
        }
        update()
    }

    /**
     * Swaps the position of the two given Moves indices
     */
    fun swapMove(pos1: Int, pos2: Int) {
        // The fact that this works should be a fuckin crime wth
        moves[pos1] = moves[pos2].also {
            moves[pos2] = moves[pos1]
        }
        update()
    }

    /**
     * Returns a NbtList containing all the Moves
     */
    fun getNBT(): ListTag {
        val listTag = ListTag()
        listTag.addAll(getMoves().map { it.saveToNBT(CompoundTag()) })
        return listTag
    }

    /**
     * Writes the MoveSet to Buffer
     */
    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, getMoves().size)
        getMoves().forEach {
            it.saveToBuffer(buffer)
        }
    }

    fun saveToJSON(json: JsonObject): JsonObject {
        for ((i, move) in moves.filterNotNull().withIndex()) {
            val moveJSON = move.saveToJSON(JsonObject())
            json.add(DataKeys.POKEMON_MOVESET + i, moveJSON)
        }
        return json
    }

    fun add(move: Move) {
        if (any { it.template == move.template }) {
            return
        }
        for (i in 0 until MOVE_COUNT) {
            if (moves[i] == null) {
                moves[i] = move
                move.observable.subscribe { this.update() }
                update()
                return
            }
        }
    }

    fun update() {
        if (emit) {
            observable.emit(this)
        }
    }

    fun doWithoutEmitting(action: () -> Unit) {
        val previousEmit = emit
        emit = false
        action()
        emit = previousEmit
    }


    /**
     * Returns a MoveSet built from given NBT
     */
    fun loadFromNBT(nbt: CompoundTag): MoveSet {
        doWithoutEmitting {
            clear()
            nbt.getList(DataKeys.POKEMON_MOVESET, Tag.TAG_COMPOUND.toInt()).forEachIndexed { index, tag ->
                setMove(index, Move.loadFromNBT(tag as CompoundTag))
            }
        }
        update()
        return this
    }

    /**
     * Returns a MoveSet build from given Buffer
     */
    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): MoveSet {
        doWithoutEmitting {
            clear()
            val amountMoves = buffer.readSizedInt(IntSize.U_BYTE)
            for (i in 0 until amountMoves) {
                setMove(i, Move.loadFromBuffer(buffer))
            }
        }
        update()
        return this
    }

    fun loadFromJSON(json: JsonObject): MoveSet {
        doWithoutEmitting {
            clear()
            for (i in 0 until MOVE_COUNT) {
                val moveJSON = json.get(DataKeys.POKEMON_MOVESET + i) ?: continue
                val move = Move.loadFromJSON(moveJSON.asJsonObject)
                add(move)
            }
        }
        update()
        return this
    }

    companion object {
        const val MOVE_COUNT = 4
        @JvmStatic
        val CODEC: Codec<MoveSet> = Codec.list(Move.CODEC, 0, MOVE_COUNT)
            .xmap(
                { moveList ->
                    val moveSet = MoveSet()
                    moveList.forEach(moveSet::add)
                    return@xmap moveSet
                },
                { moveSet -> moveSet.toList() }
            )
    }
}