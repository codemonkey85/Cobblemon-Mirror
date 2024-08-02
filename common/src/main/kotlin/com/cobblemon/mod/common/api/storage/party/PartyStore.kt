/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.party

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.Observable.Companion.stopAfter
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.storage.InvalidSpeciesException
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StoreCoordinates
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.server
import com.google.gson.JsonObject
import java.util.UUID
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

/**
 * A [PokemonStore] for a party of Pokémon. This is a simple structure that by default will hold 6 nullable slots of Pokémon.
 *
 * Please note that a party has no notion of a player, as this type of store could be used for trainers. For a party store
 * that knows about the player it is attached to, see [PlayerPartyStore].
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
open class PartyStore(override val uuid: UUID) : PokemonStore<PartyPosition>() {
    protected val slots = MutableList<Pokemon?>(6) { null }
    protected val anyChangeObservable = SimpleObservable<Unit>()

    /** A list of player UUIDs representing players that are observing this store. This is NOT serialized/deserialized. */
    var observerUUIDs = mutableListOf<UUID>()

    override fun iterator() = slots.filterNotNull().iterator()
    /** Gets the Pokémon at the specified slot. It will return null if the slot is empty or the given slot is out of bounds. */
    fun get(slot: Int) = slot.takeIf { it < slots.size && it >= 0 }?.let { slots[it] }
    override operator fun get(position: PartyPosition) = get(position.slot)

    /** Sets the Pokémon at the specified slot. */
    fun set(slot: Int, pokemon: Pokemon) = set(PartyPosition(slot), pokemon)
    override fun setAtPosition(position: PartyPosition, pokemon: Pokemon?) {
        if (position.slot >= slots.size) {
            throw IllegalArgumentException("Slot position is out of bounds")
        } else {
            slots[position.slot] = pokemon
            if (pokemon != null) {
                if (pokemon.storeCoordinates.get()?.store != this) {
                    // It's new to this store. Attach the listener
                    trackPokemon(pokemon)
                }
            }
            anyChangeObservable.emit(Unit)
        }
    }

    fun trackPokemon(pokemon: Pokemon) {
        pokemon.getChangeObservable()
            .pipe(stopAfter { pokemon.storeCoordinates.get()?.store != this })
            .subscribe { anyChangeObservable.emit(Unit) }
    }

    override fun getFirstAvailablePosition(): PartyPosition? {
        for (i in slots.indices) {
            if (slots[i] == null) {
                return PartyPosition(i)
            }
        }

        return null
    }

    override fun isValidPosition(position: PartyPosition): Boolean {
        return position.slot in (0 until slots.size)
    }

    override fun getObservingPlayers() = server()?.playerList?.players?.filter { it.uuid in observerUUIDs } ?: emptyList()

    /** The total amount of slots in the party. */
    fun size() = slots.size

    /** The amount of party slots that are occupied by a [Pokemon]. */
    fun occupied() = slots.filterNotNull().count()

    override fun sendTo(player: ServerPlayer) {
        player.sendPacket(InitializePartyPacket(false, uuid, slots.size))
        slots.forEachIndexed { index, pokemon ->
            if (pokemon != null) {
                player.sendPacket(SetPartyPokemonPacket(uuid, PartyPosition(index), pokemon))
            }
        }
    }

    override operator fun set(position: PartyPosition, pokemon: Pokemon) {
        super.set(position, pokemon)
        sendPacketToObservers(SetPartyPokemonPacket(uuid, position, pokemon))
    }

    override fun remove(pokemon: Pokemon): Boolean {
        return if (super.remove(pokemon)) {
            sendPacketToObservers(RemoveClientPokemonPacket(this, pokemon.uuid))
            true
        } else {
            false
        }
    }

    /** Swaps the contents of the two given slots. */
    fun swap(slot1: Int, slot2: Int) {
        if (slot1 !in slots.indices || slot2 !in slots.indices) {
            return
        }
        swap(PartyPosition(slot1), PartyPosition(slot2))
    }

    override fun swap(position1: PartyPosition, position2: PartyPosition) {
        val pokemon1 = get(position1)
        val pokemon2 = get(position2)
        super.swap(position1, position2)
        if (pokemon1 != null && pokemon2 != null) {
            sendPacketToObservers(SwapClientPokemonPacket(this, pokemon1.uuid, pokemon2.uuid))
        } else if (pokemon1 != null || pokemon2 != null) {
            val newPosition = if (pokemon1 == null) position1 else position2
            val pokemon = pokemon1 ?: pokemon2!!
            sendPacketToObservers(MoveClientPartyPokemonPacket(uuid, pokemon.uuid, newPosition))
        }
    }

    override fun initialize() {
        for (slot in slots.indices) {
            val pokemon = get(slot) ?: continue
            pokemon.storeCoordinates.set(StoreCoordinates(this, PartyPosition(slot)))
            trackPokemon(pokemon)
        }
    }

    fun toGappyList() = slots.toList()

    /** Maps the slots of the party using the giving mapper function, but preserving the nulls in the party at the right spots. */
    fun <T : Any> mapNullPreserving(mapper: (Pokemon) -> T): List<T?> = toGappyList().map { it?.let(mapper) }

    override fun saveToNBT(nbt: CompoundTag): CompoundTag {
        nbt.putInt(DataKeys.STORE_SLOT_COUNT, slots.size)
        for (slot in slots.indices) {
            val pokemon = get(slot)
            if (pokemon != null) {
                nbt.put(DataKeys.STORE_SLOT + slot, pokemon.saveToNBT())
            }
        }
        return nbt
    }

    override fun loadFromNBT(nbt: CompoundTag): PartyStore {
        val slotCount = nbt.getInt(DataKeys.STORE_SLOT_COUNT)
        while (slotCount > slots.size) { slots.removeLast() }
        while (slotCount < slots.size) { slots.add(null) }
        for (slot in slots.indices) {
            val pokemonNBT = nbt.getCompound(DataKeys.STORE_SLOT + slot)
            try {
                if (!pokemonNBT.isEmpty) {
                    slots[slot] = Pokemon.loadFromNBT(pokemonNBT)
                }
            } catch (_: InvalidSpeciesException) {
                handleInvalidSpeciesNBT(pokemonNBT)
            }
        }

        removeDuplicates()

        return this
    }

    override fun saveToJSON(json: JsonObject): JsonObject {
        json.addProperty(DataKeys.STORE_SLOT_COUNT, slots.size)
        for (slot in slots.indices) {
            val pokemon = get(slot)
            if (pokemon != null) {
                json.add(DataKeys.STORE_SLOT + slot, pokemon.saveToJSON())
            }
        }
        return json
    }

    override fun loadFromJSON(json: JsonObject): PartyStore {
        val slotCount = json.get(DataKeys.STORE_SLOT_COUNT).asInt
        while (slotCount > slots.size) { slots.removeLast() }
        while (slotCount < slots.size) { slots.add(null) }
        for (slot in slots.indices) {
            val key = DataKeys.STORE_SLOT + slot
            if (json.has(key)) {
                val pokemonJSON = json.get(key).asJsonObject
                try {
                    slots[slot] = Pokemon.loadFromJSON(pokemonJSON)
                } catch (_: InvalidSpeciesException) {
                    handleInvalidSpeciesJSON(pokemonJSON)
                }
            }
        }

        removeDuplicates()

        return this
    }

    fun removeDuplicates() {
        val knownUUIDs = mutableListOf<UUID>()
        for (slot in 0 until this.slots.size) {
            val pokemon = get(slot) ?: continue
            if (pokemon.uuid !in knownUUIDs) {
                knownUUIDs.add(pokemon.uuid)
            } else {
                slots[slot] = null
                anyChangeObservable.emit(Unit)
            }
        }
    }

    override fun loadPositionFromNBT(nbt: CompoundTag): StoreCoordinates<PartyPosition> {
        val slot = nbt.getByte(DataKeys.STORE_SLOT).toInt()
        return StoreCoordinates(this, PartyPosition(slot))
    }

    override fun savePositionToNBT(position: PartyPosition, nbt: CompoundTag) {
        nbt.putByte(DataKeys.STORE_SLOT, position.slot.toByte())
    }

    override fun getAnyChangeObservable(): Observable<Unit> = anyChangeObservable

    fun heal() {
        forEach { it.heal() }
    }

    fun didSleep() {
        forEach { it.didSleep() }
    }

    fun getHealingRemainderPercent(): Float {
        var totalPercent = 0.0f
        for (pokemon in this) {
            totalPercent += (1.0f - (pokemon.currentHealth.toFloat() / pokemon.hp))
        }
        return totalPercent
    }

    fun toBattleTeam(clone: Boolean = false, checkHealth: Boolean = true, leadingPokemon: UUID? = null) = mapNotNull {
        // TODO Other 'able to battle' checks
        return@mapNotNull if (clone) {
            BattlePokemon.safeCopyOf(it)
        } else {
            BattlePokemon.playerOwned(it)
        }
    }.sortedBy { if (it.uuid == leadingPokemon) 0 else (indexOf(it.originalPokemon) + 1) }

    fun clearParty() {
        forEach {
            it.tryRecallWithAnimation()
            remove(it)
        }
    }
}

