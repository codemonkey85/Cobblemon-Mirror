/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.controller

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionController
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.evolution.PreProcessor
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asTranslated
import com.mojang.serialization.Codec
import net.minecraft.client.Minecraft

class ClientEvolutionController(
    private val pokemon: Pokemon,
    evolutions: Set<EvolutionDisplay>,
) : EvolutionController<EvolutionDisplay, ClientEvolutionController.Intermediate> {

    private val evolutions = evolutions.toMutableSet()

    override val size: Int
        get() = this.evolutions.size

    override fun pokemon(): Pokemon = this.pokemon

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

    override fun add(element: EvolutionDisplay): Boolean {
        var result = this.evolutions.add(element)
        if(result) {
            Minecraft.getInstance().player?.sendSystemMessage("cobblemon.ui.evolve.hint".asTranslated(pokemon.getDisplayName()).green())
            Minecraft.getInstance().player?.playSound(CobblemonSounds.EVOLUTION_NOTIFICATION, 1F, 1F)
        }
        return result
    }

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

    override fun asIntermediate(): Intermediate = Intermediate(this.evolutions)

    data class Intermediate(val evolutions: Set<EvolutionDisplay>): PreProcessor {
        override fun create(pokemon: Pokemon): ClientEvolutionController = ClientEvolutionController(pokemon, this.evolutions)
    }

    companion object {

        @JvmStatic
        val CODEC: Codec<Intermediate> = EvolutionDisplay.CODEC.listOf()
            .xmap(
                { displays -> Intermediate(displays.toSet()) },
                { controller -> controller.evolutions.toMutableList() }
            )

    }

}