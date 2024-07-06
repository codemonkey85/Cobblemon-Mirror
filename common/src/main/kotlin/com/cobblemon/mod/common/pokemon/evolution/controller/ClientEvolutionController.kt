/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.controller

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import com.mojang.serialization.Codec

class ClientEvolutionController : EvolutionController<EvolutionDisplay> {

    private val evolutions = hashSetOf<EvolutionDisplay>()

    private lateinit var pokemon: Pokemon

    override val size: Int
        get() = this.evolutions.size

    override fun pokemon(): Pokemon = this.pokemon

    override fun attachPokemon(pokemon: Pokemon) {
        this.pokemon = pokemon
    }

    override fun start(evolution: EvolutionDisplay) {
        CobblemonNetwork.sendToServer(AcceptEvolutionPacket(this.pokemon, evolution))
    }

    override fun progress(): Collection<EvolutionProgress<*>> {
        // Nothing is done on the client
        return emptyList()
    }

    override fun <P : EvolutionProgress<*>> trackProgress(progress: P): P {
        // Nothing is done on the client
        return progress
    }

    override fun <P : EvolutionProgress<*>> progressFirstOrCreate(predicate: (progress: EvolutionProgress<*>) -> Boolean, progressFactory: () -> P): P {
        // Nothing is done on the client
        return progressFactory()
    }

    override fun saveToNBT(): Tag {
        return CompoundTag()
    }

    override fun loadFromNBT(nbt: Tag) {
        // Nothing is done on the client
    }

    override fun saveToJson(): JsonElement {
        return JsonArray()
    }

    override fun loadFromJson(json: JsonElement) {
        // Nothing is done on the client
    }

    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf, toClient: Boolean) {
        // Nothing is done on the client
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.readList { AddEvolutionPacket.decodeDisplay(it as RegistryFriendlyByteBuf) }.forEach { this.add(it) }
    }

    override fun add(element: EvolutionDisplay) = this.evolutions.add(element)

    override fun addAll(elements: Collection<EvolutionDisplay>) = this.evolutions.addAll(elements)

    override fun clear() {
        this.evolutions.clear()
    }

    override fun iterator() = this.evolutions.iterator()

    override fun remove(element: EvolutionDisplay) = this.evolutions.remove(element)

    override fun removeAll(elements: Collection<EvolutionDisplay>) = this.evolutions.removeAll(elements.toSet())

    override fun retainAll(elements: Collection<EvolutionDisplay>) = this.evolutions.retainAll(elements.toSet())

    override fun contains(element: EvolutionDisplay) = this.evolutions.contains(element)

    override fun containsAll(elements: Collection<EvolutionDisplay>) = this.evolutions.containsAll(elements)

    override fun isEmpty() = this.evolutions.isEmpty()

    companion object {

        @JvmStatic
        val CODEC: Codec<ClientEvolutionController> = EvolutionDisplay.CODEC.listOf()
            .xmap(
                { displays -> ClientEvolutionController().apply { addAll(displays) } },
                { controller -> controller.evolutions.toMutableList() }
            )

    }

}