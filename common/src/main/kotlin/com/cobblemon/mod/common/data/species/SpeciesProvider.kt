package com.cobblemon.mod.common.data.species

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.CommonAbility
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.data.DataExport
import com.cobblemon.mod.common.data.OutputtingDataProvider
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.pokemon.ai.PokemonBehaviour
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityDimensions
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class SpeciesProvider(
    packOutput: PackOutput,
    lookupProvider: CompletableFuture<Provider>
) : OutputtingDataProvider<Species, SpeciesProvider.SpeciesDataExport>(packOutput, lookupProvider) {

    override fun getName(): String = "species"

    // TODO: Figure out how to provide the abilities, elemental types and moves
    override fun buildEntries(lookupProvider: Provider, consumer: Consumer<SpeciesDataExport>) {
        ShowdownService.service.getBaseSpecies().forEach { json ->
            val jObject = json.asJsonObject
            val id = this.idForSpecies(jObject["name"].asString)
            val nationalPokedexNumber = jObject.get("num").asInt
            // Cursed shit, plz skip
            if (nationalPokedexNumber <= 0) {
                return@forEach
            }
            val baseStats = this.extractBaseStats(jObject)
            val maleRatio = this.extractMaleRatio(jObject)
            val catchRate = 1 // TODO: Fetch me from PokeAPI
            val baseScale = 1F // TODO: Get me from somewhere hardcoded
            val baseExperienceYield = 1 // TODO: Fetch me from PokeAPI
            val baseFriendship = 1 // TODO: Fetch me from PokeAPI
            val evYield = hashMapOf<Stat, Int>() // TODO: Fetch me from PokeAPI
            val experienceGroup = ExperienceGroup.SLOW // TODO: Fetch me from PokeAPI
            val hitbox = EntityDimensions.fixed(1F, 1F)//TODO: Get me from somewhere hardcoded
            val typesArray = jObject.getAsJsonArray("types")
            val typeLookup = lookupProvider.lookupOrThrow(CobblemonRegistries.ELEMENTAL_TYPE_KEY)
            val primaryType: ElementalType = typeLookup.getOrThrow(
                ResourceKey.create(
                    CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                    typesArray.get(0).asString
                        .lowercase()
                        .asIdentifierDefaultingNamespace()
                )
            ).value()
            var secondaryType: ElementalType? = null
            if (typesArray.size() == 2) {
                secondaryType = typeLookup.getOrThrow(
                    ResourceKey.create(
                        CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                        typesArray.get(1).asString
                            .lowercase()
                            .asIdentifierDefaultingNamespace()
                    )
                ).value()
            }
            val abilityPool = this.extractAbilities(lookupProvider, jObject)
            val shoulderMountable = false // TODO Get me from somewhere hardcoded
            val shoulderEffects = emptySet<ShoulderEffect>() // TODO Get me from somewhere hardcoded
            val learnset = if (jObject.has("learnset")) {
                this.extractLearnset(lookupProvider, jObject.getAsJsonObject("learnset"))
            } else {
                Cobblemon.LOGGER.warn("$id has no learnset")
                Learnset()
            }
            val standingEyeHeight = Optional.empty<Float>() // TODO Get me from somewhere hardcoded
            val swimmingEyeHeight = Optional.empty<Float>() // TODO Get me from somewhere hardcoded
            val flyingEyeHeight = Optional.empty<Float>() // TODO Get me from somewhere hardcoded
            val behaviour = PokemonBehaviour() // TODO: Hardcode
            val eggCycles = 1 // TODO: Fetch me from PokeAPI
            val eggGroups = this.extractEggGroups(jObject)
            val dynamaxBlocked = jObject.has("cannotDynamax")
            val implemented = true // TODO: Find resource for species if present true
            val height = jObject.get("heightm").asFloat * 10F
            val weight = jObject.get("weightkg").asFloat * 10F
            val preEvolution = this.extractPreEvolution(jObject)
            val species = Species(
                nationalPokedexNumber,
                baseStats,
                maleRatio,
                catchRate,
                baseScale,
                baseExperienceYield,
                baseFriendship,
                evYield,
                experienceGroup,
                hitbox,
                primaryType,
                Optional.ofNullable(secondaryType),
                abilityPool,
                shoulderMountable,
                shoulderEffects,
                learnset,
                standingEyeHeight,
                swimmingEyeHeight,
                flyingEyeHeight,
                behaviour,
                eggCycles,
                eggGroups,
                dynamaxBlocked,
                implemented,
                height,
                weight,
                preEvolution,
                CobblemonSounds.PVW_BATTLE.location,
                Optional.empty()
            )
            consumer.accept(SpeciesDataExport(id, species))
        }
    }

    override fun pathProvider(): PackOutput.PathProvider = this.createPathForCobblemonRegistryData(CobblemonRegistries.SPECIES_KEY)

    private fun idForSpecies(name: String): ResourceLocation {
        val split = name.lowercase().split("-").toMutableList()
        split[0] = split[0].replace(ShowdownIdentifiable.EXCLUSIVE_REGEX, "")
        if (split.size > 1) {
            val result = split.subList(1, split.size)
                .joinToString("_") { it.replace(ShowdownIdentifiable.EXCLUSIVE_REGEX, "") }
            return cobblemonResource("${split[0]}/$result")
        }
        return cobblemonResource(split.joinToString())
    }

    private fun extractBaseStats(jObject: JsonObject): Map<Stat, Int> {
        val baseObject = jObject.getAsJsonObject("baseStats")
        val map = hashMapOf<Stat, Int>()
        Stats.PERMANENT.forEach { stat ->
            map[stat] = baseObject[stat.showdownId].asInt
        }
        return map
    }

    private fun extractMaleRatio(jObject: JsonObject): Float {
        if (jObject.has("gender")) {
            return when {
                jObject.has("F") -> 0F
                jObject.has("M") -> 1F
                else -> -1F
            }
        }
        return jObject.getAsJsonObject("genderRatio")["M"].asFloat
    }

    private fun extractAbilities(lookupProvider: Provider, jObject: JsonObject): AbilityPool {
        val abilityLookup = lookupProvider.lookupOrThrow(CobblemonRegistries.ABILITY_KEY)
        val pool = AbilityPool()
        var abilityIndex = 0
        val abilityJson = jObject.getAsJsonObject("abilities")
        while (abilityJson.has(abilityIndex.toString())) {
            val id = abilityJson[abilityIndex.toString()].asString
                .lowercase()
                .replace(ShowdownIdentifiable.EXCLUSIVE_REGEX, "")
                .asIdentifierDefaultingNamespace()
            abilityIndex++
            val template = abilityLookup.getOrThrow(ResourceKey.create(CobblemonRegistries.ABILITY_KEY, id))
            val commonAbility = CommonAbility(template)
            pool.add(commonAbility.priority, commonAbility)
        }
        if (abilityJson.has("H")) {
            val id = abilityJson["H"].asString
                .lowercase()
                .replace(ShowdownIdentifiable.EXCLUSIVE_REGEX, "")
                .asIdentifierDefaultingNamespace()
            val template = abilityLookup.getOrThrow(ResourceKey.create(CobblemonRegistries.ABILITY_KEY, id))
            val hiddenAbility = HiddenAbility(template)
            pool.add(hiddenAbility.priority, hiddenAbility)
        }
        return pool
    }

    private fun extractLearnset(lookupProvider: Provider, jObject: JsonObject): Learnset {
        val learnset = Learnset()
        // TODO: Update me to use holders.
        return learnset
        val characterRegex = Regex("[a-zA-Z]")
        val digitRegex = Regex("[0-9]+")
        val levelUpMoves = hashMapOf<MoveTemplate, Pair<Int, Int>>()
        val formChangeMoves = hashSetOf<MoveTemplate>()
        val tmMoves = hashSetOf<MoveTemplate>()
        val eggMoves = hashSetOf<MoveTemplate>()
        val tutorMoves = hashSetOf<MoveTemplate>()
        jObject.entrySet().forEach { (moveId, data) ->
            val sources = data.asJsonArray
            val move = lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY)
                .getOrThrow(ResourceKey.create(CobblemonRegistries.MOVE_KEY, moveId.asIdentifierDefaultingNamespace()))
                .value()
            sources.forEach {
                val source = it.asString
                val digitMatches = digitRegex.findAll(source).toList()
                val generation = digitMatches.first().value.toInt()
                val sourceType = characterRegex.find(source)?.value ?: throw IllegalStateException("Couldn't resolve source type for $source")
                when (sourceType) {
                    "L" -> {
                        val level = digitMatches.getOrNull(1)?.value?.toInt() ?: throw IllegalStateException("Couldn't get level for $source")
                        val existing = levelUpMoves[move]
                        if (existing == null || existing.second < generation) {
                            levelUpMoves[move] = level to generation
                        }
                    }
                    "R" -> formChangeMoves.add(move)
                    "M" -> tmMoves.add(move)
                    "E" -> eggMoves.add(move)
                    else -> tutorMoves.add(move)
                }
            }
        }
        levelUpMoves.forEach { (move, pair) ->
            learnset.levelUpMoves.getOrPut(pair.first) { arrayListOf() }.add(move)
        }
        learnset.formChangeMoves += formChangeMoves
        learnset.tmMoves += tmMoves
        learnset.eggMoves += eggMoves
        learnset.tutorMoves += tutorMoves
        return learnset
    }

    private fun extractEggGroups(jObject: JsonObject): Set<EggGroup> {
        val byId = EggGroup.entries.associateBy { it.showdownId() }
        return jObject.getAsJsonArray("eggGroups")
            .mapNotNull { byId[it.asString] }
            .toSet()
    }

    private fun extractPreEvolution(jObject: JsonObject): Optional<ResourceKey<Species>> {
        if (!jObject.has("prevo")) {
            return Optional.empty()
        }
        val preEvoShowdownId = jObject["prevo"].asString
        if (preEvoShowdownId.isEmpty()) {
            return Optional.empty()
        }
        val preEvolutionId = this.idForSpecies(preEvoShowdownId)
        return Optional.of(ResourceKey.create(CobblemonRegistries.SPECIES_KEY, preEvolutionId))
    }

    class SpeciesDataExport(
        private val id: ResourceLocation,
        private val value: Species
    ) : DataExport<Species> {
        override fun id(): ResourceLocation = this.id

        override fun codec(): Codec<Species> = Species.DIRECT_CODEC

        override fun value(): Species = this.value
    }

}