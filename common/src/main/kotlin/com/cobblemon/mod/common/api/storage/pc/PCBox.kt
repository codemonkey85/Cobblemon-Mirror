/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc

import com.cobblemon.mod.common.api.reactive.Observable.Companion.stopAfter
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.storage.InvalidSpeciesException
import com.cobblemon.mod.common.api.storage.StoreCoordinates
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.google.gson.JsonObject
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

/**
 * A single box of a PC. The list of Pokémon is strictly sized at [POKEMON_PER_BOX] - 30.
 * Any change to any contained Pokémon is emitted through the [boxChangeEmitter].
 *
 * @author Hiroku
 * @since April 26th, 2022
 */
open class PCBox(val pc: PCStore) : Iterable<Pokemon> {
    override fun iterator() = pokemon.filterNotNull().iterator()

    val boxChangeEmitter = SimpleObservable<Unit>()

    protected var emit = true

    protected val pokemon = Array<Pokemon?>(POKEMON_PER_BOX) { null }

    open operator fun get(index: Int): Pokemon? {
        return if (index in 0 until POKEMON_PER_BOX) {
            pokemon[index]
        } else {
            null
        }
    }

    open operator fun set(index: Int, pokemon: Pokemon?) {
        if (index in 0 until POKEMON_PER_BOX) {
            this.pokemon[index] = pokemon
            if (pokemon != null) {
                val previousCoordinates = pokemon.storeCoordinates.get()
                val position = previousCoordinates?.position
                pokemon.storeCoordinates.set(StoreCoordinates(pc, PCPosition(boxNumber, index)))
                if (previousCoordinates?.store !is PCStore || previousCoordinates.store.uuid != pc.uuid || (position as PCPosition).box != boxNumber) {
                    trackPokemon(pokemon)
                }
            }
            if (emit) {
                boxChangeEmitter.emit(Unit)
            }
        }
    }

    val boxNumber: Int
        get() = this.pc.boxes.indexOf(this)

    val unoccupiedSlots: Int
        get() = POKEMON_PER_BOX - this.pokemon.filterNotNull().count()

    fun getFirstAvailablePosition(): PCPosition? {
        for (index in 0 until POKEMON_PER_BOX) {
            if (this.pokemon[index] == null) {
                return PCPosition(boxNumber, index)
            }
        }
        return null
    }

    open fun initialize() {
        val box = boxNumber
        pokemon.forEachIndexed { slot, pokemon ->
            if (pokemon != null) {
                val position = PCPosition(box, slot)
                pokemon.storeCoordinates.set(StoreCoordinates(pc, position))
                trackPokemon(pokemon)
            }
        }
        boxChangeEmitter.subscribe { pc.pcChangeObservable.emit(Unit) }
    }

    fun trackPokemon(pokemon: Pokemon) {
        pokemon.getChangeObservable()
            .pipe(
                stopAfter {
                    val coordinates = it.storeCoordinates.get() ?: return@stopAfter true
                    return@stopAfter coordinates.store !is PCStore || coordinates.store.uuid != pc.uuid || (coordinates.position as PCPosition).box != boxNumber
                }
            )
            .subscribe { boxChangeEmitter.emit(Unit) }
    }

    fun sendTo(player: ServerPlayer) {
        SetPCBoxPokemonPacket(this).sendToPlayer(player)
    }

    open fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess): CompoundTag {
        for (slot in 0 until POKEMON_PER_BOX) {
            val pokemon = pokemon[slot] ?: continue
            nbt.put(DataKeys.STORE_SLOT + slot, pokemon.saveToNBT(registryAccess))
        }
        return nbt
    }

    open fun saveToJSON(json: JsonObject, registryAccess: RegistryAccess): JsonObject {
        for (slot in 0 until POKEMON_PER_BOX) {
            val pokemon = pokemon[slot] ?: continue
            json.add(DataKeys.STORE_SLOT + slot, pokemon.saveToJSON(registryAccess))
        }
        return json
    }

    open fun loadFromJSON(json: JsonObject, registryAccess: RegistryAccess): PCBox {
        for (slot in 0 until POKEMON_PER_BOX) {
            if (json.has(DataKeys.STORE_SLOT + slot)) {
                val pokemonJson = json.getAsJsonObject(DataKeys.STORE_SLOT + slot)
                try {
                    pokemon[slot] = Pokemon.loadFromJSON(registryAccess, pokemonJson)
                } catch (_: InvalidSpeciesException) {
                    pc.handleInvalidSpeciesJSON(pokemonJson)
                }
            }
        }
        return this
    }

    open fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess): PCBox {
        for (slot in 0 until POKEMON_PER_BOX) {
            if (nbt.contains(DataKeys.STORE_SLOT + slot)) {
                val pokemonNBT = nbt.getCompound(DataKeys.STORE_SLOT + slot)
                try {
                    pokemon[slot] = Pokemon.loadFromNBT(registryAccess, pokemonNBT)
                } catch (_: InvalidSpeciesException) {
                    pc.handleInvalidSpeciesNBT(pokemonNBT)
                }
            }
        }
        return this
    }

    open fun getNonEmptySlots() = (0 until POKEMON_PER_BOX).filter { get(it) != null }.associateWith { get(it)!! }
    open fun getNonEmptySlotsForPackets() = getNonEmptySlots().mapValues { (_, pokemon) -> { _: RegistryAccess -> pokemon } }
}