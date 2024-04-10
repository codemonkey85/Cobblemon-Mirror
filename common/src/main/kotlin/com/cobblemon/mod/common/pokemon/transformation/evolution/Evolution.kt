/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.transformation.evolution

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionTestedEvent
import com.cobblemon.mod.common.api.moves.BenchedMove
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.transformation.Transformation
import com.cobblemon.mod.common.api.pokemon.transformation.evolution.EvolutionDisplay
import com.cobblemon.mod.common.api.pokemon.transformation.evolution.EvolutionLike
import com.cobblemon.mod.common.api.pokemon.transformation.requirement.TransformationRequirement
import com.cobblemon.mod.common.api.pokemon.transformation.trigger.PassiveTrigger
import com.cobblemon.mod.common.api.pokemon.transformation.trigger.TransformationTrigger
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.transformation.triggers.ItemInteractionTrigger
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.sound.SoundCategory

/**
 * Represents an evolution of a [Pokemon], this is the server side counterpart of [EvolutionDisplay].
 *
 * @author Licious
 * @since March 19th, 2022
 */
class Evolution(
    override val id: String = "",
    override val requirements: Set<TransformationRequirement> = setOf(),
    override val trigger: TransformationTrigger = ItemInteractionTrigger(),
    /** The result of this evolution. */
    val result: PokemonProperties = PokemonProperties(),
    /** If this evolution allows the user to choose when to start it or not. */
    var optional: Boolean = true,
    /** The [MoveTemplate]s that will be offered to be learnt upon evolving. */
    val learnableMoves: Set<MoveTemplate> = setOf()
) : EvolutionLike, Transformation {

    override fun test(pokemon: Pokemon): Boolean {
        val result = super.test(pokemon)
        val event = EvolutionTestedEvent(pokemon, this, result, result)
        CobblemonEvents.EVOLUTION_TESTED.post(event)
        return super.test(pokemon)
    }

    /**
     * Starts this evolution or queues it if [optional] is true.
     *
     * @param pokemon The [Pokemon] being evolved.
     */
    override fun start(pokemon: Pokemon): Boolean {
        if (!this.test(pokemon)) return false;
        return if (this.optional) {
            pokemon.evolutionProxy.server().add(this)
        }
        else {
            this.forceStart(pokemon)
            true
        }
    }

    /**
     * Starts this evolution as soon as possible.
     * This will not present a choice to the client regardless of [optional].
     *
     * @param pokemon The [Pokemon] being evolved.
     */
    override fun forceStart(pokemon: Pokemon) {
        // This is a switch to enable/disable the evolution effect while we get particles improved
        val useEvolutionEffect = false

        if (pokemon.state is ShoulderedState) {
            pokemon.tryRecallWithAnimation()
        }

        val pokemonEntity = pokemon.entity
        if (pokemonEntity == null || !useEvolutionEffect) {
            evolutionMethod(pokemon)
            super.forceStart(pokemon)
        } else {
            pokemonEntity.evolutionEntity = pokemon.getOwnerPlayer()?.let { GenericBedrockEntity(world = it.world) }
            val evolutionEntity = pokemon.entity!!.evolutionEntity
            evolutionEntity?.apply {
                category = cobblemonResource("evolution")
                colliderHeight = pokemonEntity.height
                colliderWidth = pokemonEntity.width
                scale = pokemonEntity.scaleFactor
                syncAge = true // Otherwise particle animation will be starting from zero even if you come along partway through
                setPosition(pokemonEntity.x, pokemonEntity.y, pokemonEntity.z)
            }
            pokemon.getOwnerPlayer()?.world?.spawnEntity(evolutionEntity)
            afterOnServer(seconds = 9F) {
                if (!pokemonEntity.isRemoved) {
                    evolutionMethod(pokemon)
                    super.forceStart(pokemon)
                    afterOnServer(seconds = 1.5F) { pokemonEntity.cry() }
                    afterOnServer(seconds = 3F) {
                        if (evolutionEntity != null) {
                            evolutionEntity.kill()
                            if (!pokemonEntity.isRemoved) {
                                pokemonEntity.evolutionEntity = null
                            }
                        }
                    }
                }
            }
        }
    }

    fun evolutionMethod(pokemon: Pokemon) {
        this.result.apply(pokemon)
        this.learnableMoves.forEach { move ->
            if (pokemon.moveSet.hasSpace()) {
                pokemon.moveSet.add(move.create())
            } else {
                pokemon.benchedMoves.add(BenchedMove(move, 0))
            }
            pokemon.getOwnerPlayer()?.sendMessage(lang("experience.learned_move", pokemon.getDisplayName(), move.displayName))
        }
        // we want to instantly tick for example you might only evolve your Bulbasaur at level 34 so Venusaur should be immediately available
        pokemon.transformationTriggers<PassiveTrigger>().forEach { (_, transformation) -> transformation.start(pokemon) }
        pokemon.getOwnerPlayer()?.playSound(CobblemonSounds.EVOLVING, SoundCategory.NEUTRAL, 1F, 1F)
        CobblemonEvents.EVOLUTION_COMPLETE.post(EvolutionCompleteEvent(pokemon, this))
    }

}