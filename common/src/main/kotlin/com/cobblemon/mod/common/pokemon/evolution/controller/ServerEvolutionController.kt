/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.controller

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionAcceptedEvent
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgressFactory
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket.Companion.convertToDisplay
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket.Companion.encode
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundCategory

class ServerEvolutionController(override val pokemon: Pokemon) : EvolutionController<Evolution> {

    private val evolutions = hashSetOf<Evolution>()
    private val progress = arrayListOf<EvolutionProgress<*>>()

    override val size: Int
        get() = this.evolutions.size

    override fun start(evolution: Evolution) {
        CobblemonEvents.EVOLUTION_ACCEPTED.postThen(
            event = EvolutionAcceptedEvent(this.pokemon, evolution),
            ifSucceeded = {
                // Evolution will clear the pending stuff after if successful
                evolution.forceEvolve(this.pokemon)
            }
        )
    }

    override fun progress(): Collection<EvolutionProgress<*>> = this.progress.toList()

    override fun <P : EvolutionProgress<*>> trackProgress(progress: P): P {
        this.progress.add(progress)
        return progress
    }

    override fun <P : EvolutionProgress<*>> progressFirstOrCreate(predicate: (progress: EvolutionProgress<*>) -> Boolean, progressFactory: () -> P): P {
        val existing = this.progress.firstOrNull(predicate)
        if (existing == null) {
            val created = progressFactory()
            this.progress.add(created)
            return created
        }
        return existing as P
    }

    override fun saveToNBT(): NbtElement {
        val nbt = NbtCompound()
        val pendingList = NbtList()
        this.evolutions.forEach { evolution ->
            pendingList += NbtString.of(evolution.id)
        }
        nbt.put(PENDING, pendingList)
        val progressList = NbtList()
        this.progress.forEach { progress ->
            progressList += progress.saveToNBT().apply { putString(ID, progress.id().toString()) }
        }
        nbt.put(PROGRESS, progressList)
        return nbt
    }

    override fun loadFromNBT(nbt: NbtElement) {
        this.clear()
        val pendingList: NbtList
        val progressList: NbtList
        if (nbt is NbtCompound) {
            pendingList = nbt.getList(PENDING, NbtElement.STRING_TYPE.toInt())
            progressList = nbt.getList(PROGRESS, NbtElement.COMPOUND_TYPE.toInt())
        }
        else {
            pendingList = nbt as? NbtList ?: return
            progressList = NbtList()
        }
        for (tag in pendingList.filterIsInstance<NbtString>()) {
            val id = tag.asString()
            val evolution = this.findEvolutionFromId(id) ?: continue
            this.add(evolution)
        }
        for (tag in progressList.filterIsInstance<NbtCompound>()) {
            EvolutionProgressFactory.create(tag.getString(ID))?.let { progress ->
                progress.loadFromNBT(tag)
                if (progress.shouldKeep(this.pokemon)) {
                    this.progress.add(progress)
                }
            }
        }
    }

    override fun saveToJson(): JsonElement {
        val json = JsonObject()
        val pendingArray = this.evolutions
            .map { evolution -> evolution.id }
            .toJsonArray()
        json.add(PENDING, pendingArray)
        val progressArray = this.progress
            .map { progress -> progress.saveToJson().apply { addProperty(ID, progress.id().toString()) } }
            .toJsonArray()
        json.add(PROGRESS, progressArray)
        return json
    }

    override fun loadFromJson(json: JsonElement) {
        this.clear()
        val pendingArray: JsonArray
        val progressArray: JsonArray
        if (json is JsonObject) {
            pendingArray = json.getAsJsonArray(PENDING)
            progressArray = json.getAsJsonArray(PROGRESS)
        }
        else {
            pendingArray = json as? JsonArray ?: return
            progressArray = JsonArray()
        }
        for (element in pendingArray) {
            val id = (element as? JsonPrimitive)?.asString ?: continue
            val evolution = this.findEvolutionFromId(id) ?: continue
            this.add(evolution)
        }
        for (element in progressArray) {
            val jObject = element as? JsonObject ?: continue
            EvolutionProgressFactory.create(jObject.get(ID).asString)?.let { progress ->
                progress.loadFromJson(jObject)
                if (progress.shouldKeep(this.pokemon)) {
                    this.progress.add(progress)
                }
            }
        }
    }

    override fun saveToBuffer(buffer: PacketByteBuf, toClient: Boolean) {
        if (!toClient) {
            return
        }
        buffer.writeCollection(this.evolutions) { pb, value -> value.convertToDisplay(this.pokemon).encode(pb) }
    }

    override fun loadFromBuffer(buffer: PacketByteBuf) {
        // Nothing is done on the server
    }

    override fun add(element: Evolution): Boolean {
        if (this.evolutions.add(element)) {
            this.pokemon.getOwnerPlayer()?.sendMessage("cobblemon.ui.evolve.hint".asTranslated(pokemon.getDisplayName()).green())
            this.pokemon.notify(AddEvolutionPacket(this.pokemon, element))
            this.pokemon.getOwnerPlayer()?.playSound(CobblemonSounds.EVOLUTION_NOTIFICATION, SoundCategory.NEUTRAL, 1F, 1F)
            return true
        }
        return false
    }

    override fun addAll(elements: Collection<Evolution>): Boolean {
        var result = false
        elements.forEach { element ->
            if (this.add(element)) {
                result = true
            }
        }
        return result
    }

    override fun clear() {
        // We don't want to send unnecessary packets
        val pokemon = this.pokemon
        if (this.evolutions.isNotEmpty()) {
            this.evolutions.clear()
            this.pokemon.notify(ClearEvolutionsPacket { pokemon })
        }
        this.progress.clear()
    }

    override fun contains(element: Evolution) = this.evolutions.contains(element)

    override fun containsAll(elements: Collection<Evolution>) = this.evolutions.containsAll(elements)

    override fun isEmpty() = this.evolutions.isEmpty()

    override fun iterator() = this.evolutions.iterator()

    override fun remove(element: Evolution): Boolean {
        if (this.evolutions.remove(element)) {
            this.pokemon.notify(RemoveEvolutionPacket(this.pokemon, element))
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<Evolution>): Boolean {
        var result = false
        elements.forEach { element ->
            if (this.remove(element)) {
                result = true
            }
        }
        return result
    }

    override fun retainAll(elements: Collection<Evolution>): Boolean  {
        var result = false
        val comparedSet = elements.toSet()
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (!comparedSet.contains(element)) {
                iterator.remove()
                result = true
            }
        }
        return result
    }

    private fun findEvolutionFromId(id: String) = this.pokemon.evolutions.firstOrNull { evolution -> evolution.id.equals(id, true) }

    companion object {
        private const val PENDING = "pending"
        private const val PROGRESS = "progress"
        private const val ID = "id"
    }
}