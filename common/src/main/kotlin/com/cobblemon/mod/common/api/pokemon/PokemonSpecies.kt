/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.registry.CobblemonRegistry
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.SpeciesAdditions
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.adapters.*
import com.google.common.collect.HashBasedTable
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object PokemonSpecies : CobblemonRegistry<Species>() {

    val implemented = mutableListOf<Species>()
    private val speciesByDex = HashBasedTable.create<String, Int, Species>()

    init {
        SpeciesAdditions.observable.subscribe {
            this.forEach(Species::initialize)
            this.forEach(Species::resolveEvolutionMoves)
            this.cacheExtraLookups()
            Cobblemon.showdownThread.queue {
                it.registerSpecies()
                it.indicateSpeciesInitialized()
                // Reload this with the mod
                CobblemonHeldItemManager.load()
                Cobblemon.LOGGER.info("Loaded {} Pokémon species", this.count())
            }
        }
    }

    override fun registry(): Registry<Species> = CobblemonRegistries.SPECIES

    override fun registryKey(): ResourceKey<Registry<Species>> = CobblemonRegistries.SPECIES_KEY

    /**
     * Finds a [Species] by its national Pokédex entry number.
     *
     * @param ndex The [Species.nationalPokedexNumber].
     * @return The [Species] if existing.
     */
    fun getByPokedexNumber(ndex: Int, namespace: String = Cobblemon.MODID) = speciesByDex.get(namespace, ndex)

    /**
     * Picks a random [Species].
     *
     * @throws [NoSuchElementException] if there are no Pokémon species loaded.
     *
     * @return A randomly selected [Species].
     */
    fun random(): Species = implemented.random()

    private fun cacheExtraLookups() {
        this.implemented.clear()
        this.speciesByDex.clear()
        this.forEach { species ->
            if (species.implemented) {
                implemented += species
            }
            this.speciesByDex.put(species.resourceLocation().namespace, species.nationalPokedexNumber, species)
        }
    }

    /**
     * The representation of a [Species] and/or [FormData] in the context of showdown.
     * This is intended as a sort of DTO that can be easily converted between JSON and Java/JS objects.
     *
     * @param species The [Species] being converted or the base species if the form is not null.
     */
    @Suppress("unused")
    internal class ShowdownSpecies(species: Species) {
        init {
            TODO("Rework ShowdownSpecies")
        }
        /*
        val num = species.nationalPokedexNumber
        val name = createShowdownName(species)
        val baseSpecies = ???
        val forme = form?.name
        // ToDo baseForme
        val otherFormes = if (form == null && species.forms.isNotEmpty()) species.forms.map { "${this.name}-${it.name}" } else emptyList()
        val formeOrder = if (form == null && this.otherFormes.isNotEmpty()) arrayListOf(this.name, *this.otherFormes.toTypedArray()) else emptyList()
        val abilities: Map<String, String> = mapOf(
            "0" to "No Ability",
            "1" to "No Ability",
            "H" to "No Ability",
            "S" to "No Ability"
        )
        val types = (form?.types ?: species.types).map { it.showdownId() }
        val preevo: String? = (form?.preEvolution ?: species.preEvolution)?.let { if (it.form == it.species.standardForm) createShowdownName(it.species) else "${createShowdownName(it.species)}-${it.form.name}" }
        // For the context of battles the content here doesn't matter whatsoever and due to how PokemonProperties work we can't guarantee an actual specific species is defined.
        val evos = if ((form?.evolutions ?: species.evolutions).isEmpty()) emptyList() else arrayListOf("")
        val nfe = this.evos.isNotEmpty()
        val eggGroups = (form?.eggGroups ?: species.eggGroups).map { it.showdownID }
        val gender: String? = when (form?.maleRatio ?: species.maleRatio) {
            0F -> "F"
            1F -> "M"
            -1F, 1.125F -> "N"
            else -> null
        }
        val genderRatio = if (this.gender == null)
            mapOf(
                "maleRatio" to (form?.maleRatio ?: species.maleRatio),
                "femaleRation" to (1F - (form?.maleRatio ?: species.maleRatio))
            ) else null
        val baseStats = mapOf(
            "hp" to (form?.baseStats?.get(Stats.HP) ?: species.baseStats[Stats.HP] ?: 1),
            "atk" to (form?.baseStats?.get(Stats.ATTACK) ?: species.baseStats[Stats.ATTACK] ?: 1),
            "def" to (form?.baseStats?.get(Stats.DEFENCE) ?: species.baseStats[Stats.DEFENCE] ?: 1),
            "spa" to (form?.baseStats?.get(Stats.SPECIAL_ATTACK) ?: species.baseStats[Stats.SPECIAL_ATTACK] ?: 1),
            "spd" to (form?.baseStats?.get(Stats.SPECIAL_DEFENCE) ?: species.baseStats[Stats.SPECIAL_DEFENCE] ?: 1),
            "spe" to (form?.baseStats?.get(Stats.SPEED) ?: species.baseStats[Stats.SPEED] ?: 1)
        )
        val heightm = (form?.height ?: species.height) / 10
        val weightkg = (form?.weight ?: species.weight) / 10
        // This is ugly, but we already have it hardcoded in the mod anyway
        val maxHP = if (species.showdownId() == "shedinja") 1 else null
        val canGigantamax: String? = if (form?.gigantamaxMove != null) form.gigantamaxMove.showdownId() else null
        val cannotDynamax = form?.dynamaxBlocked ?: species.dynamaxBlocked
        // ToDo battleOnly
        // ToDo changesFrom
        val requiredMove = form?.requiredMove
        val requiredItem = form?.requiredItem
        val requiredItems = form?.requiredItems
         */
    }

}