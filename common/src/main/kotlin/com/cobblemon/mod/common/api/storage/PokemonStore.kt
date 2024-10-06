/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.storage.factory.FileBackedPokemonStoreFactory
import com.cobblemon.mod.common.api.storage.factory.PokemonStoreFactory
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import java.util.UUID
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

/**
 * The base class for all stores of [Pokemon]. A store specifies the kind of coordinate it needs to be given
 * to use in the generic typing.
 *
 * Saving of a store is not done automatically, and in fact a store has no concept of persistence on its own.
 * You may find [PokemonStoreFactory] instructive, as the factory is responsible for handling a storage's persistence.
 *
 * <strong>Note:</strong> If you are implementing this and will rely on one of Cobblemon's save solutions
 * such as a [FileBackedPokemonStoreFactory], then you must include a constructor that accepts a single [UUID] parameter.
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
abstract class PokemonStore<T : StorePosition> : Iterable<Pokemon> {
    /** The UUID of the store. The exact uniqueness requirements depend on the method used for saving. */
    abstract val uuid: UUID
    /** Gets the [Pokemon] at the given position. */
    abstract operator fun get(position: T): Pokemon?
    /** Gets the first empty position that a [Pokemon] might be put. */
    abstract fun getFirstAvailablePosition(): T?
    /** Gets an iterable of all [ServerPlayer]s that should be notified of any changes to the Pokémon in this store. */
    abstract fun getObservingPlayers(): Iterable<ServerPlayer>
    /** Sends the contents of this store to a player as if they've never seen it before. This initializes the store then sends each contained Pokémon. */
    abstract fun sendTo(player: ServerPlayer)

    /**
     * Runs initialization logic for this store, knowing that it has just been constructed in a [PokemonStoreFactory].
     *
     * The minimum of what this function should do is iterate over all the Pokémon in this store and set their store
     * coordinates.
     *
     * If this does not get called, or it does not do its job properly, serious de-sync issues may follow.
     */
    abstract fun initialize()

    /**
     * Sets the given position with the given [Pokemon], which can be null. This is for internal use only because
     * other, more public methods will additionally send updates to the client, and for logical reasons this means
     * there must be an internal and external set method.
     */
    protected abstract fun setAtPosition(position: T, pokemon: Pokemon?)

    /** Returns true if the given position is pointing to a legitimate location in this store. */
    abstract fun isValidPosition(position: T): Boolean

    /** Sends the given packet to all observing players. */
    open fun sendPacketToObservers(packet: NetworkPacket<*>) = getObservingPlayers().forEach { it.sendPacket(packet) }

    /** Adds the given [Pokemon] to the first available space. Returns false if there is no space. */
    open fun add(pokemon: Pokemon): Boolean {
        remove(pokemon)
        val position = getFirstAvailablePosition() ?: return false // Couldn't fit, shrug emoji
        set(position, pokemon)
        return true
    }

    /**
     * Sets the specified position to the specified [Pokemon]. If there is already a Pokémon in that slot,
     * it will be removed from the store entirely.
     *
     * This method will also notify any observing players about the changes.
     */
    open operator fun set(position: T, pokemon: Pokemon) {
        val existing = get(position)
        if (existing == pokemon) {
            return
        }

        if (existing != pokemon && existing != null) {
            remove(existing)
        }

        setAtPosition(position, pokemon)
        pokemon.storeCoordinates.set(StoreCoordinates(this, position))
    }

    /** Swaps the Pokémon at the specified positions. If one of the spaces is empty, it will simply move the not-null one to that space. */
    open fun swap(position1: T, position2: T) {
        val pokemon1 = get(position1)
        val pokemon2 = get(position2)
        setAtPosition(position1, pokemon2)
        setAtPosition(position2, pokemon1)
        pokemon1?.storeCoordinates?.set(StoreCoordinates(this, position2))
        pokemon2?.storeCoordinates?.set(StoreCoordinates(this, position1))
    }

    /**
     * Moves the specified [Pokemon] to the specified space. This will do nothing if the Pokémon is not in this store.
     *
     * This is a shortcut to running [PokemonStore.swap]
     */
    fun move(pokemon: Pokemon, position: T) {
        val currentPosition = pokemon.storeCoordinates.get() ?: return
        if (currentPosition.store != this) {
            return
        }
        swap(currentPosition.position as T, position)
    }

    /** Removes any Pokémon that may be at the specified spot. Returns true if there was a Pokémon to remove. */
    open fun remove(position: T): Boolean {
        val pokemon = get(position)
        return if (pokemon == null) {
            false
        } else {
            return remove(pokemon)
        }
    }

    /** Removes the specified Pokémon from this store. Returns true if the Pokémon was in this store and was successfully removed. */
    open fun remove(pokemon: Pokemon): Boolean {
        val currentPosition = pokemon.storeCoordinates.get() ?: return false
        if (currentPosition.store != this) {
            return false
        }
        currentPosition as StoreCoordinates<T>
        if (get(currentPosition.position) != pokemon) {
            return false
        }
        pokemon.recall()
        pokemon.storeCoordinates.set(null)
        setAtPosition(currentPosition.position, null)
        return true
    }

    operator fun get(uuid: UUID) = find { it.uuid == uuid }

    open fun handleInvalidSpeciesNBT(nbt: CompoundTag) {}
    abstract fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess): CompoundTag
    abstract fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess): PokemonStore<T>
    open fun handleInvalidSpeciesJSON(json: JsonObject) {}
    abstract fun saveToJSON(json: JsonObject, registryAccess: RegistryAccess): JsonObject
    abstract fun loadFromJSON(json: JsonObject, registryAccess: RegistryAccess): PokemonStore<T>
    abstract fun savePositionToNBT(position: T, nbt: CompoundTag)
    abstract fun loadPositionFromNBT(nbt: CompoundTag): StoreCoordinates<T>

    /**
     * Returns an [Observable] that emits Unit whenever there is a change to this store. This includes any save-worthy
     * change to a [Pokemon] contained in the store. You can access an [Observable] in each [Pokemon] that emits Unit for
     * each change, accessed by [Pokemon.getChangeObservable].
     */
    abstract fun getAnyChangeObservable(): Observable<Unit>
}