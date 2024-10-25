/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

import com.cobblemon.mod.common.api.reactive.Observable.Companion.stopAfter
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import java.util.UUID
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

class BottomlessPosition(val currentIndex: Int) : StorePosition

/**
 * A [PokemonStore] that has no maximum capacity. It's used internally as an overflow store.
 *
 * @author Hiroku
 * @since May 2nd, 2022
 */
open class BottomlessStore(override val uuid: UUID) : PokemonStore<BottomlessPosition>() {
    val pokemon = mutableListOf<Pokemon>()
    val storeChangeObservable = SimpleObservable<Unit>()

    override fun iterator() = pokemon.iterator()

    override fun get(position: BottomlessPosition) = position.currentIndex
        .takeIf { it in pokemon.indices }
        ?.let { pokemon[it] }

    override fun getFirstAvailablePosition() = BottomlessPosition(pokemon.size)
    override fun isValidPosition(position: BottomlessPosition) = position.currentIndex >= 0
    operator fun get(index: Int) = index.takeIf { it in pokemon.indices }?.let { pokemon[it] }
    override fun getObservingPlayers() = emptySet<ServerPlayer>()
    override fun sendTo(player: ServerPlayer) {}

    override fun initialize() {
        pokemon.forEachIndexed { index, pokemon ->
            pokemon.storeCoordinates.set(StoreCoordinates(this, BottomlessPosition(index)))
            pokemon.getChangeObservable().pipe(
                stopAfter { it.storeCoordinates.get()?.store != this }
            ).subscribe { storeChangeObservable.emit(Unit) }
        }
    }

    override fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess): CompoundTag {
        pokemon.forEachIndexed { index, pokemon -> nbt.put(DataKeys.STORE_SLOT + index, pokemon.saveToNBT(registryAccess)) }
        return nbt
    }

    override fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess): BottomlessStore {
        var i = -1
        while (nbt.contains(DataKeys.STORE_SLOT + ++i)) {
            val pokemonNBT = nbt.getCompound(DataKeys.STORE_SLOT + i)
            try {
                pokemon.add(Pokemon.loadFromNBT(registryAccess, pokemonNBT))
            } catch(_: InvalidSpeciesException) {
                handleInvalidSpeciesNBT(pokemonNBT)
            }
        }
        return this
    }

    override fun saveToJSON(json: JsonObject, registryAccess: RegistryAccess): JsonObject {
        pokemon.forEachIndexed { index, pokemon -> json.add(DataKeys.STORE_SLOT + index, pokemon.saveToJSON(registryAccess)) }
        return json
    }

    override fun loadFromJSON(json: JsonObject, registryAccess: RegistryAccess): BottomlessStore {
        var i = -1
        while (json.has(DataKeys.STORE_SLOT + ++i)) {
            val pokemonJSON = json.getAsJsonObject(DataKeys.STORE_SLOT + i)
            try {
                pokemon.add(Pokemon.loadFromJSON(registryAccess, pokemonJSON))
            } catch (_: InvalidSpeciesException) {
                handleInvalidSpeciesJSON(pokemonJSON)
            }
        }
        return this
    }

    override fun loadPositionFromNBT(nbt: CompoundTag): StoreCoordinates<BottomlessPosition> {
        val slot = nbt.getByte(DataKeys.STORE_SLOT).toInt()
        return StoreCoordinates(this, BottomlessPosition(slot))
    }

    override fun savePositionToNBT(position: BottomlessPosition, nbt: CompoundTag) {
        nbt.putByte(DataKeys.STORE_SLOT, position.currentIndex.toByte())
    }

    override fun getAnyChangeObservable() = storeChangeObservable

    override fun setAtPosition(position: BottomlessPosition, pokemon: Pokemon?) {
        if (position.currentIndex == this.pokemon.size && pokemon != null) {
            this.pokemon.add(pokemon)
            storeChangeObservable.emit(Unit)
        } else if (position.currentIndex in 0 until this.pokemon.size) {
            var startIndex = position.currentIndex
            if (pokemon != null) {
                this.pokemon.add(position.currentIndex, pokemon)
                startIndex += 1
            } else {
                this.pokemon.removeAt(position.currentIndex)
            }
            for(i in startIndex until this.pokemon.size) {
                this.pokemon[i].storeCoordinates.set(StoreCoordinates(this, BottomlessPosition(i)))
            }
            storeChangeObservable.emit(Unit)
        }
    }
}