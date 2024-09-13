/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.data.species

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.tags.CobblemonSpeciesTags
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.data.CobblemonRegistryTagsProvider
import com.cobblemon.mod.common.data.species.SpeciesProvider.Companion.SKIPPED
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.isNotEmpty
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

class SpeciesTagProvider(
    packOutput: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>
) : CobblemonRegistryTagsProvider<Species>(packOutput, CobblemonRegistries.SPECIES_KEY, lookupProvider) {

    private val showdownSpeciesTagRemaps = hashMapOf(
        "Mythical" to CobblemonSpeciesTags.MYTHICAL,
        "Restricted Legendary" to CobblemonSpeciesTags.LEGENDARY,
        "Sub-Legendary" to CobblemonSpeciesTags.LEGENDARY,
        "Paradox" to CobblemonSpeciesTags.PARADOX,
    )

    private val regionals = hashMapOf(
        "Alola" to CobblemonSpeciesTags.REGIONAL_OF_ALOLA,
        "Galar" to CobblemonSpeciesTags.REGIONAL_OF_GALAR,
        "Hisui" to CobblemonSpeciesTags.REGIONAL_OF_HISUI,
        "Paldea" to CobblemonSpeciesTags.REGIONAL_OF_PALDEA,
    )

    override fun addTags(provider: HolderLookup.Provider) {
        val showdownEntriesByName = ShowdownService.service.getBaseSpecies().associate {
            it.asJsonObject["name"].asString to it.asJsonObject
        }
        showdownEntriesByName.forEach { (_, jObject) ->
            val id = SpeciesProvider.idForSpecies(jObject)
            if (SKIPPED.contains(id.path) || jObject["num"].asInt <= 0) {
                return@forEach
            }
            this.resolveGenerationTag(jObject, id)
            this.checkShowdownTags(jObject, id)
            this.checkIfBaby(jObject, id, showdownEntriesByName)
            this.checkIfMega(id)
            this.checkIfPseudoLegendary(jObject, id, showdownEntriesByName)
            this.checkIfRegional(jObject, id)
        }
        this.resolveBiasFakemon()
    }

    private fun resolveGenerationTag(jObject: JsonObject, id: ResourceLocation) {
        val generationTag = when (val generation = jObject["gen"].asInt) {
            1 -> CobblemonSpeciesTags.GENERATION_1
            2 -> CobblemonSpeciesTags.GENERATION_2
            3 -> CobblemonSpeciesTags.GENERATION_3
            4 -> CobblemonSpeciesTags.GENERATION_4
            5 -> CobblemonSpeciesTags.GENERATION_5
            6 -> CobblemonSpeciesTags.GENERATION_6
            7 -> CobblemonSpeciesTags.GENERATION_7
            8 -> CobblemonSpeciesTags.GENERATION_8
            9 -> CobblemonSpeciesTags.GENERATION_9
            else -> throw IllegalArgumentException("Failure on $id; Missing tag for generation $generation")
        }
        this.tag(generationTag)
            .addOptional(id)
    }

    private fun checkShowdownTags(jObject: JsonObject, id: ResourceLocation) {
        val isUltraBeast = jObject.getAsJsonObject("abilities")["0"].asString == BEAST_BOOST
        val speciesTags = jObject.getAsJsonArray("tags").map(JsonElement::getAsString)
        // Showdown tags all ultra beasts as sub-legendary, they're not considered legends however by game & community standards
        if (isUltraBeast) {
            if (speciesTags.size > 1) {
                Cobblemon.LOGGER.warn(
                    "{} is flagged by us as ultra beast but has the showdown tags [{}]",
                    id.toString(),
                    speciesTags.joinToString()
                )
            }
            this.tag(CobblemonSpeciesTags.ULTRA_BEAST)
                .addOptional(id)
        } else {
            speciesTags.forEach { speciesTag ->
                val showdownTag = this.showdownSpeciesTagRemaps[speciesTag]
                    ?: throw IllegalArgumentException("Failure on $id; Missing tag remap for showdown tag $speciesTag")
                this.tag(showdownTag)
                    .addOptional(id)
            }
        }
    }

    private fun checkIfBaby(jObject: JsonObject, id: ResourceLocation, showdownEntriesByName: Map<String, JsonObject>) {
        val isInUndiscovered = jObject.getAsJsonArray("eggGroups").any { it.asString == EggGroup.UNDISCOVERED.showdownId() }
        val evolutions = jObject.getAsJsonArray("evos")
        if (isInUndiscovered && !evolutions.isEmpty) {
            val evolutionCanBreed = evolutions.any { name ->
                showdownEntriesByName[name.asString]?.getAsJsonArray("eggGroups")
                    ?.any { eggGroup -> eggGroup.asString != EggGroup.UNDISCOVERED.showdownId() }
                    ?: false
            }
            if (evolutionCanBreed) {
                this.tag(CobblemonSpeciesTags.BABY)
                    .addOptional(id)
            }
        }
    }

    private fun checkIfMega(id: ResourceLocation) {
        val tag = when {
            id.path.endsWith(MEGA) || id.path.endsWith(MEGA_X) || id.path.endsWith(MEGA_Y) -> CobblemonSpeciesTags.MEGA
            id.path.endsWith(PRIMAL) -> CobblemonSpeciesTags.PRIMAL
            id.path.endsWith(GMAX) -> CobblemonSpeciesTags.GMAX
            else -> return
        }
        this.tag(tag)
            .addOptional(id)
    }

    private fun checkIfPseudoLegendary(jObject: JsonObject, id: ResourceLocation, showdownEntriesByName: Map<String, JsonObject>) {
        // Megas should remap to their base form for this process
        val baseSpecies = jObject["baseSpecies"].asString
        val root = if (baseSpecies.isNotEmpty()) {
            showdownEntriesByName[baseSpecies] ?: throw IllegalArgumentException("Failure on $id; Cannot find base species $baseSpecies")
        } else {
            jObject
        }
        // All pseudos have 600 BST
        val hasPseudoBst = root.getAsJsonObject("baseStats")
            .entrySet()
            .sumOf { it.value.asInt } == PSEUDO_LEGENDARY_BST

        // All pseudos have 2 pre evolution stages
        val preEvo1 = showdownEntriesByName[root["prevo"].asString] ?: return
        val has2PreEvo = showdownEntriesByName[preEvo1["prevo"].asString]?.isNotEmpty() ?: return

        // There's a 3rd rule where they're all in the slow levelling group however we can't query showdown for this
        // It doesn't affect the outcome as there's no collision with this method as of gen 9
        // Ideally we figure out how to hook datagen stuff in prior stuff into the holder provider
        if (hasPseudoBst && has2PreEvo) {
            this.tag(CobblemonSpeciesTags.PSEUDO_LEGENDARY)
                .addOptional(id)
        }
    }

    private fun checkIfRegional(jObject: JsonObject, id: ResourceLocation) {
        val form = jObject["forme"].asString
        if (form.isNotEmpty()) {
            val tag = this.regionals[form] ?: return
            this.tag(tag)
                .addOptional(id)
            this.tag(CobblemonSpeciesTags.REGIONAL)
                .addOptional(id)
            return
        }
        if (!jObject.has("otherFormes")) {
            return
        }
        val hasRegional = jObject.getAsJsonArray("otherFormes")
            .any { withForm -> this.regionals.keys.any { key -> withForm.asString.endsWith(key) } }
        if (hasRegional) {
            val baseRegionTag = when (val generation = jObject["gen"].asInt) {
                1 -> CobblemonSpeciesTags.REGIONAL_OF_KANTO
                2 -> CobblemonSpeciesTags.REGIONAL_OF_JOHTO
                3 -> CobblemonSpeciesTags.REGIONAL_OF_HOENN
                4 -> CobblemonSpeciesTags.REGIONAL_OF_SINNOH
                5 -> CobblemonSpeciesTags.REGIONAL_OF_UNOVA
                6 -> CobblemonSpeciesTags.REGIONAL_OF_KALOS
                7 -> CobblemonSpeciesTags.REGIONAL_OF_ALOLA
                8 -> CobblemonSpeciesTags.REGIONAL_OF_GALAR
                9 -> CobblemonSpeciesTags.REGIONAL_OF_PALDEA
                else -> throw IllegalArgumentException("Failure on $id; Missing region source tag for generation $generation")
            }
            this.tag(baseRegionTag)
                .addOptional(id)
            this.tag(CobblemonSpeciesTags.REGIONAL)
                .addOptional(id)
        }
    }

    private fun resolveBiasFakemon() {
        this.tag(CobblemonSpeciesTags.REGIONAL_OF_ALOLA)
            .addOptional(SpeciesProvider.ALOLAN_PIKACHU)
            .addOptional(SpeciesProvider.ALOLAN_EXEGGCUTE)

        this.tag(CobblemonSpeciesTags.REGIONAL_OF_HISUI)
            .addOptional(SpeciesProvider.HISUIAN_CYNDAQUIL)
            .addOptional(SpeciesProvider.HISUIAN_QUILAVA)
            .addOptional(SpeciesProvider.HISUIAN_OSHAWOTT)
            .addOptional(SpeciesProvider.HISUIAN_DEWOTT)
            .addOptional(SpeciesProvider.HISUIAN_PETILIL)
            .addOptional(SpeciesProvider.HISUIAN_GOOMY)
            .addOptional(SpeciesProvider.HISUIAN_ROWLET)
            .addOptional(SpeciesProvider.HISUIAN_DARTRIX)

        this.tag(CobblemonSpeciesTags.ORIGINALS)
            .addOptional(SpeciesProvider.ALOLAN_PIKACHU)
            .addOptional(SpeciesProvider.ALOLAN_EXEGGCUTE)
            .addOptional(SpeciesProvider.HISUIAN_CYNDAQUIL)
            .addOptional(SpeciesProvider.HISUIAN_QUILAVA)
            .addOptional(SpeciesProvider.HISUIAN_OSHAWOTT)
            .addOptional(SpeciesProvider.HISUIAN_DEWOTT)
            .addOptional(SpeciesProvider.HISUIAN_PETILIL)
            .addOptional(SpeciesProvider.HISUIAN_GOOMY)
            .addOptional(SpeciesProvider.HISUIAN_ROWLET)
            .addOptional(SpeciesProvider.HISUIAN_DARTRIX)

        this.tag(CobblemonSpeciesTags.FAKEMON)
            .addOptionalTag(CobblemonSpeciesTags.ORIGINALS.location)
    }

    companion object {
        private const val BEAST_BOOST = "Beast Boost"
        private const val MEGA = "mega"
        private const val MEGA_X = "mega-x"
        private const val MEGA_Y = "mega-y"
        private const val PRIMAL = "primal"
        private const val GMAX = "gmax"
        private const val PSEUDO_LEGENDARY_BST = 600
    }
}