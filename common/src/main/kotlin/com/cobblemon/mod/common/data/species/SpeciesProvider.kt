/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.data.species

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.CommonAbility
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PassiveEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.moves.entry.LearnsetEntry
import com.cobblemon.mod.common.api.pokemon.moves.entry.variant.*
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.api.tags.RegistryBasedCondition
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.data.DataExport
import com.cobblemon.mod.common.data.OutputtingDataProvider
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.pokemon.ai.PokemonBehaviour
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.cobblemon.mod.common.pokemon.evolution.requirements.*
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.simplify
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.net.URL
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

// IMPORTANT NOTE: Don't use PokemonProperties#parse here you'll need to manually allocate properties.
class SpeciesProvider(
    packOutput: PackOutput,
    lookupProvider: CompletableFuture<Provider>
) : OutputtingDataProvider<Species, SpeciesProvider.SpeciesDataExport>(packOutput, lookupProvider) {

    override fun getName(): String = "species"

    // If you want to make a species be able to go on the player shoulder place them here
    private val shoulderMountable = setOf(
        "caterpie",
        "eevee",
        "mew",
        "pidgey",
        "pikachu",
        "spearow",
        "squirtle",
        "weedle",
        "zubat",
        "bellossom",
        "cleffa",
        "igglybuff",
        "larvitar",
        "murkrow",
        "pichu",
        "smoochum",
        "totodile",
        "tyrogue",
        "wooper",
        "anorith",
        "beldum",
        "minum",
        "plusle",
        "ralts",
        "sableye",
        "tailow",
        "zigzagoon",
        "budew",
        "buneary",
        "chatot",
        "chingling",
        "combee",
        "kricketot",
        "pachirisu",
        "starly",
        "turtwig",
        "archen",
        "elgyem",
        "joltik",
        "litwick",
        "petilil",
        "petilil/hisui",
        "pidove",
        "roggenrola",
        "zorua",
        "zorua/hisui",
        "flabebe",
        "fletchling",
        "klefki",
        "scatterbug",
        "skrelp",
        "comfey",
        "cutiefly",
        "fomantis",
        "mimikyu",
        "morelull",
        "popplio",
        "alcremie",
        "dreepy",
        "milcery",
        "rookidee",
        "sizzlipede",
        "charcadet",
        "flittle",
        "gimmighoul",
        "glimmet",
        "shroodle",
        "squawkabilly",
        "tatsugiri",
    )
    // If you want to add shoulder effects to a species place them here
    private val shoulderEffects = mapOf<String, Set<ShoulderEffect>>()
    private val standingEyeHeights = mapOf<String, Float>()
    private val swimmingEyeHeights = mapOf<String, Float>()
    private val flyingEyeHeights = mapOf<String, Float>()

    private val pokeApiRemaps = mapOf(
        "mr_mime" to "mr-mime",
        "mr_mime/galar" to "mr-mime-galar",
        "tauros/paldea-combat" to "tauros-paldea-combat-breed",
        "tauros/paldea-blaze" to "tauros-paldea-blaze-breed",
        "tauros/paldea-aqua" to "tauros-paldea-aqua-breed",
        "deoxys" to "deoxys-normal",
        "wormadam" to "wormadam-plant",
        "giratina" to "giratina-altered",
        "shaymin" to "shaymin-land",
        "basculin" to "basculin-red-striped",
        "darmanitan" to "darmanitan-standard",
        "darmanitan/galar" to "darmanitan-galar-standard",
        "tornadus" to "tornadus-incarnate",
        "thundurus" to "thundurus-incarnate",
        "landorus" to "landorus-incarnate",
        "keldeo" to "keldeo-ordinary",
        "meloetta" to "meloetta-aria",
        "greninja/bond" to "greninja-battle-bond",
        "meowstic" to "meowstic-male",
        "meowstic/f" to "meowstic-female",
        "aegislash" to "aegislash-shield",
        "pumpkaboo" to "pumpkaboo-average",
        "gourgeist" to "gourgeist-average",
        "zygarde" to "zygarde-50",
        "zygarde/ten" to "zygarde-10",
        "oricorio" to "oricorio-baile",
        "lycanroc" to "lycanroc-midday",
        "wishiwashi" to "wishiwashi-solo",
        "minior" to "minior-red-meteor",
        "minior/meteor" to "minior-red",
        "mimikyu" to "mimikyu-disguised",
        "necrozma/dusk-mane" to "necrozma-dusk",
        "necrozma/dawn-wings" to "necrozma-dawn",
        "toxtricity" to "toxtricity-amped",
        "toxtricity/gmax" to "toxtricity-amped-gmax",
        "eiscue" to "eiscue-ice",
        "indeedee" to "indeedee-male",
        "indeedee/f" to "indeedee-female",
        "morpeko" to "morpeko-full-belly",
        "urshifu" to "urshifu-single-strike",
        "urshifu/gmax" to "urshifu-single-strike-gmax",
        "basculegion" to "basculegion-male",
        "basculegion/f" to "basculegion-female",
        "enamorus" to "enamorus-incarnate",
        "oinkologne/f" to "oinkologne-female", // for some reason pokeAPI does not have a 'oinkologne-male' just 'oinkologne',
        "maushold/four" to "maushold",
        "squawkabilly/blue" to "squawkabilly-blue-plumage",
        "squawkabilly/yellow" to "squawkabilly-yellow-plumage",
        "squawkabilly/white" to "squawkabilly-white-plumage",
        "ogerpon/wellspring" to "ogerpon-wellspring-mask",
        "ogerpon/hearthflame" to "ogerpon-hearthflame-mask",
        "ogerpon/cornerstone" to "ogerpon-cornerstone-mask",
        "ogerpon/teal-tera" to "ogerpon",
        "ogerpon/wellspring-tera" to "ogerpon-wellspring-mask",
        "ogerpon/hearthflame-tera" to "ogerpon-hearthflame-mask",
        "ogerpon/cornerstone-tera" to "ogerpon-cornerstone-mask",
    )

    private val baseExperienceFixes = mapOf(
        "growlithe/hisui" to 70,
        "arcanine/hisui" to 194,
        "voltorb/hisui" to 66,
        "electrode/hisui" to 172,
        "typhlosion/hisui" to 267,
        "wooper/paldea" to 42,
        "qwilfish/hisui" to 88,
        "sneasel/hisui" to 86,
        "dialga/origin" to 340,
        "palkia/origin" to 340,
        "samurott/hisui" to 264,
        "lilligant/hisui" to 168,
        "basculin/white-striped" to 161,
        "zorua/hisui" to 66,
        "zoroark/hisui" to 179,
        "braviary/hisui" to 179,
        "sliggoo/hisui" to 158,
        "goodra/hisui" to 300,
        "avalugg/hisui" to 180,
        "decidueye/hisui" to 265,
        "ursaluna/bloodmoon" to 275,
        "enamorus/therian" to 116,
        "palafin/hero" to 228,
        "dudunsparce/three-segment" to 182,
        "gimmighoul/roaming" to 60,
        "terapagos/terastal" to 90,
        "terapagos/stellar" to 90,
        "tauros/paldea-combat" to 172,
        "tauros/paldea-blaze" to 172,
        "tauros/paldea-aqua" to 172,
        "basculegion/f" to 265,
        "oinkologne/f" to 171,
        "squawkabilly/blue" to 146,
        "squawkabilly/yellow" to 146,
        "squawkabilly/white" to 146,
        "ogerpon/wellspring" to 275,
        "ogerpon/hearthflame" to 275,
        "ogerpon/cornerstone" to 275,
        "ogerpon/wellspring-tera" to 275,
        "ogerpon/hearthflame-tera" to 275,
        "ogerpon/cornerstone-tera" to 275,
    )

    private val baseFriendshipFixes = mapOf(
        "wyrdeer" to 50,
        "kleavor" to 50,
        "ursaluna" to 50,
        "ursaluna/bloodmoon" to 50,
        "basculegion" to 50,
        "basculegion/f" to 50,
        "sneasler" to 50,
        "overqwil" to 50,
        "enamorus" to 50,
        "enamorus/therian" to 50,
    )

    private val eggCyclesFixes = mapOf(
        "wyrdeer" to 20,
        "kleavor" to 20,
        "ursaluna" to 20,
        "ursaluna/bloodmoon" to 20,
        "basculegion" to 20,
        "basculegion/f" to 20,
        "sneasler" to 20,
        "overqwil" to 20,
        "enamorus" to 20,
        "enamorus/therian" to 20,
    )

    private val evolutionAppendages = mapOf<String, (MutableSet<Evolution>, Provider) -> Unit>(
        "grotle" to this::appendSaplingTorterras,
    )

    private lateinit var showdownEvolutionRequirements: Map<String, Set<EvolutionRequirement>>

    private val usedIds = hashSetOf<String>()

    // TODO: Evolutions, Cobblemon original forms
    override fun buildEntries(lookupProvider: Provider, consumer: Consumer<SpeciesDataExport>) {
        val baseSpecies = ShowdownService.service.getBaseSpecies()
            .associate { json ->
                val jObject = json.asJsonObject
                jObject["name"].asString to jObject
            }
        val generated = hashMapOf<ResourceLocation, Species>()
        this.showdownEvolutionRequirements = this.generateShowdownEvoRequirements(lookupProvider)
        baseSpecies.forEach { (_, jObject) ->
            val id = idForSpecies(jObject)
            if (SKIPPED.contains(id.path)) {
                return@forEach
            }
            try {
                val nationalPokedexNumber = jObject["num"].asInt
                // Cursed shit, plz skip
                if (nationalPokedexNumber <= 0) {
                    return@forEach
                }
                val pokeApiSpeciesUrl = URL("https://pokeapi.co/api/v2/pokemon-species/${nationalPokedexNumber}/")
                val pokeApiSpeciesJson = JsonParser.parseString(pokeApiSpeciesUrl.readText()).asJsonObject
                val pokeApiPokemonUrl = this.resolvePokeApiPokemonUrl(id, pokeApiSpeciesJson)
                val pokeApiPokemonJson = JsonParser.parseString(pokeApiPokemonUrl.readText()).asJsonObject
                this.fixPokeApiData(id, pokeApiPokemonJson, pokeApiSpeciesJson)
                val baseStats = this.extractBaseStats(jObject)
                val maleRatio = this.extractMaleRatio(jObject)
                val catchRate = pokeApiSpeciesJson["capture_rate"].asInt
                val baseScale = 1F // TODO: Get me from somewhere hardcoded
                val baseExperienceYield = pokeApiPokemonJson["base_experience"].asInt
                val baseFriendship = pokeApiSpeciesJson["base_happiness"].asInt
                val evYield = this.extractEvs(pokeApiPokemonJson)
                val experienceGroup = this.extractExperienceGroup(pokeApiSpeciesJson)
                val hitbox = EntityDimensions.fixed(1F, 1F) //TODO: Get me from somewhere hardcoded
                val typesArray = jObject.getAsJsonArray("types")
                val typeLookup = lookupProvider.lookupOrThrow(CobblemonRegistries.ELEMENTAL_TYPE_KEY)
                val primaryType: Holder<ElementalType> = typeLookup.getOrThrow(
                    ResourceKey.create(
                        CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                        typesArray.get(0).asString
                            .lowercase()
                            .asIdentifierDefaultingNamespace()
                    )
                )
                var secondaryType: Holder<ElementalType>? = null
                if (typesArray.size() == 2) {
                    secondaryType = typeLookup.getOrThrow(
                        ResourceKey.create(
                            CobblemonRegistries.ELEMENTAL_TYPE_KEY,
                            typesArray.get(1).asString
                                .lowercase()
                                .asIdentifierDefaultingNamespace()
                        )
                    )
                }
                val abilityPool = this.extractAbilities(lookupProvider, jObject)
                val shoulderMountable = this.shoulderMountable.contains(id.path)
                val shoulderEffects = this.shoulderEffects[id.path] ?: emptySet()
                val learnset = if (jObject.has("learnset")) {
                    this.extractLearnset(lookupProvider, jObject.getAsJsonObject("learnset"))
                } else {
                    // Showdown doesn't' define learnsets that are "useless" as the base species has all the same moves, we need to define them
                    val base = jObject["baseSpecies"].asString
                    val baseEntry = baseSpecies[base]!!
                    this.extractLearnset(lookupProvider, baseEntry.getAsJsonObject("learnset"))
                }
                val standingEyeHeight = Optional.ofNullable(this.standingEyeHeights[id.path])
                val swimmingEyeHeight = Optional.ofNullable(this.swimmingEyeHeights[id.path])
                val flyingEyeHeight = Optional.ofNullable(this.flyingEyeHeights[id.path])
                val behaviour = PokemonBehaviour() // TODO: Hardcode
                val eggCycles = pokeApiSpeciesJson["hatch_counter"].asInt
                val eggGroups = this.extractEggGroups(jObject)
                val dynamaxBlocked = jObject.get("cannotDynamax").asBoolean
                val implemented = true // TODO: Find resource for species if present true
                val height = jObject.get("heightm").asFloat
                val weight = jObject.get("weightkg").asFloat
                val preEvolution = this.extractPreEvolution(jObject, baseSpecies)
                val evolutions = this.extractEvolutions(id, jObject, baseSpecies, lookupProvider)
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
                    Optional.empty(),
                    evolutions
                )
                val runtimeShowdownId = ShowdownIdentifiable.EXCLUSIVE_REGEX.replace(id.simplify(), "")
                check(usedIds.add(runtimeShowdownId)) {
                    "Duplicate showdown ID generated for $id » $runtimeShowdownId"
                }
                generated[id] = species
                consumer.accept(SpeciesDataExport(id, species))
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Caught exception for species $id:")
                e.printStackTrace()
            }
        }
        //this.createBiasForms(generated, lookupProvider, consumer)
    }

    private fun generateShowdownEvoRequirements(lookupProvider: Provider): Map<String, Set<EvolutionRequirement>> = mapOf(
        "at night" to setOf(TimeRangeRequirement(TimeRange.timeRanges["night"]!!)),
        "during the day" to setOf(TimeRangeRequirement(TimeRange.timeRanges["day"]!!)),
        "with an atk stat > its def stat" to setOf(StatRatioRequirement(Stats.ATTACK, Stats.DEFENCE, StatRatioRequirement.Comparison.HIGHER)),
        "with an atk stat < its def stat" to setOf(StatRatioRequirement(Stats.ATTACK, Stats.DEFENCE, StatRatioRequirement.Comparison.LOWER)),
        "with an atk stat equal to its def stat" to setOf(StatRatioRequirement(Stats.ATTACK, Stats.DEFENCE, StatRatioRequirement.Comparison.EQUAL)),
        "with a dark-type in the party" to setOf(PartyMemberRequirement(PokemonProperties().apply { type = ElementalTypes.DARK.location().simplify() }, true)),
        "with the console turned upside-down" to setOf(PartyMemberRequirement(PokemonProperties().apply { nickname = Component.literal("jeb") }, true)),
        "from a special rockruff" to setOf(TimeRangeRequirement(TimeRange.timeRanges["dusk"]!!)),
        "with a remoraid in party" to setOf(PartyMemberRequirement(PokemonProperties().apply { species = "remoraid" }, true)),
        "with a fairy-type move and two levels of affection" to setOf(FriendshipRequirement(160), MoveTypeRequirement(lookupProvider.lookupOrThrow(CobblemonRegistries.ELEMENTAL_TYPE_KEY).getOrThrow(ElementalTypes.FAIRY))),
        "with a karrablast" to emptySet(), // Handled in impl
        "with a shelmet" to emptySet(), // Handled in impl
        "land 3 critical hits in 1 battle" to setOf(BattleCriticalHitsRequirement(3)),
        "have 49+ hp lost and walk under stone sculpture in dusty bowl" to setOf(DamageTakenRequirement(49)),
        "spin while holding a sweet" to emptySet(), // This has not been designed yet
        "use agile style psyshield bash 20 times" to setOf(UseMoveRequirement(lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY).getOrThrow(Moves.PSYSHIELD_BASH), 20)),
        "receive 294+ recoil without fainting" to setOf(RecoilRequirement(294)),
        "use strong style barb barrage 20 times" to setOf(UseMoveRequirement(lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY).getOrThrow(Moves.BARB_BARRAGE), 20)),
        "walk 1000 steps in let's go" to setOf(BlocksTraveledRequirement(1000)),
        "use rage fist 20 times and level-up" to setOf(UseMoveRequirement(lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY).getOrThrow(Moves.RAGE_FIST), 20)),
        "defeat 3 bisharp leading pawniard and level-up" to setOf(DefeatRequirement(PokemonProperties().apply { species = "bisharp" }, 3)), // TODO: Add held item req for cobblemon:leaders_crest when implemented
        "level up with 999 coins in the bag" to setOf(PokemonPropertiesRequirement(
            PokemonProperties().apply {
                species = "gimmighoul"
                customProperties += IntSpeciesFeature("gimmighoul_coins", 999)
            }
        )),
    )

    override fun pathProvider(): PackOutput.PathProvider = this.createPathForCobblemonRegistryData(CobblemonRegistries.SPECIES_KEY)

    private fun resolvePokeApiPokemonUrl(id: ResourceLocation, pokeApiJson: JsonObject): URL {
        val jsonArray = pokeApiJson.getAsJsonArray("varieties")
        if (jsonArray.size() == 1) {
            return URL(jsonArray.first().asJsonObject["pokemon"].asJsonObject["url"].asString)
        }
        val name = this.pokeApiRemaps[id.path] ?: id.path.replace("/", "-")
        val result = jsonArray.firstOrNull { element ->
            element.asJsonObject["pokemon"].asJsonObject["name"].asString == name
        } ?: throw IllegalStateException("Can't resolve a PokeAPI variety")
        return URL(result.asJsonObject["pokemon"].asJsonObject["url"].asString)
    }

    private fun fixPokeApiData(speciesId: ResourceLocation, pokemonData: JsonObject, speciesData: JsonObject) {
        baseFriendshipFixes[speciesId.path]?.let { friendship ->
            if (speciesData["base_happiness"].isJsonNull) {
                speciesData.addProperty("base_happiness", friendship)
            } else {
                Cobblemon.LOGGER.warn("Base friendship of $speciesId is present in PokeAPI, fix can be removed")
            }
        }
        baseExperienceFixes[speciesId.path]?.let { experience ->
            if (pokemonData["base_experience"].isJsonNull) {
                pokemonData.addProperty("base_experience", experience)
            } else {
                Cobblemon.LOGGER.warn("Base experience of $speciesId is present in PokeAPI, fix can be removed")
            }
        }
        eggCyclesFixes[speciesId.path]?.let { eggCycles ->
            if (speciesData["hatch_counter"].isJsonNull) {
                speciesData.addProperty("hatch_counter", eggCycles)
            } else {
                Cobblemon.LOGGER.warn("Egg cycles of $speciesId is present in PokeAPI, fix can be removed")
            }
        }
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
        val gender = jObject["gender"].asString
        if (gender.isNotEmpty()) {
            return when(gender) {
                "F" -> 0F
                "M" -> 1F
                else -> -1F
            }
        }
        return jObject.getAsJsonObject("genderRatio")["M"].asFloat
    }

    private fun extractEvs(pokeApiJson: JsonObject): Map<Stat, Int> {
        val ev = hashMapOf<Stat, Int>()
        val stats = pokeApiJson["stats"].asJsonArray
        stats.forEachIndexed { index, entry ->
            val effort = entry.asJsonObject["effort"].asInt
            if (effort > 0) {
                val stat = when (index) {
                    0 -> Stats.HP
                    1 -> Stats.ATTACK
                    2 -> Stats.DEFENCE
                    3 -> Stats.SPECIAL_ATTACK
                    4 -> Stats.SPECIAL_DEFENCE
                    else -> Stats.SPEED
                }
                ev[stat] = effort
            }
        }
        return ev
    }

    private fun extractExperienceGroup(pokeApiJson: JsonObject): ExperienceGroup {
        return when(pokeApiJson["growth_rate"].asJsonObject["name"].asString) {
            "slow" -> ExperienceGroup.SLOW
            "fast" -> ExperienceGroup.FAST
            "medium-slow" -> ExperienceGroup.MEDIUM_SLOW
            "medium" -> ExperienceGroup.MEDIUM_FAST
            "slow-then-very-fast" -> ExperienceGroup.ERRATIC
            else -> ExperienceGroup.FLUCTUATING
        }
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
        val characterRegex = Regex("[a-zA-Z]")
        val digitRegex = Regex("[0-9]+")
        val levelUpMoves = hashMapOf<Holder<MoveTemplate>, Pair<Int, Int>>()
        val entries = hashSetOf<LearnsetEntry>()
        jObject.entrySet().forEach { (moveId, data) ->
            val sources = data.asJsonArray
            val move = lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY)
                .getOrThrow(ResourceKey.create(CobblemonRegistries.MOVE_KEY, moveId.asIdentifierDefaultingNamespace()))
            sources.forEach {
                val source = it.asString
                val digitMatches = digitRegex.findAll(source).toList()
                val generation = digitMatches.first().value.toInt()
                val sourceType = characterRegex.find(source)?.value ?: throw IllegalStateException("Couldn't resolve source type for $source")
                when (sourceType) {
                    "L" -> {
                        val level = digitMatches.getOrNull(1)?.value?.toInt() ?: throw IllegalStateException("Couldn't get level for $source")
                        // On evolution move handled in the dedicated method
                        if (level != 0) {
                            val existing = levelUpMoves[move]
                            if (existing == null || existing.second < generation) {
                                levelUpMoves[move] = level to generation
                            }
                        }
                    }
                    "R" -> entries.add(FormChangeLearnsetEntry(move))
                    "M" -> entries.add(TmLearnsetEntry(move))
                    "E" -> entries.add(EggLearnsetEntry(move))
                    else -> entries.add(TutorLearnsetEntry(move))
                }
            }
        }
        return Learnset(levelUpMoves.entries.map { LevelUpLearnsetEntry(it.key, it.value.first) }.sortedBy { it.level }.toSet() + entries)
    }

    private fun extractOnEvolutionMoves(lookupProvider: Provider, jObject: JsonObject): HolderSet<MoveTemplate> {
        val characterRegex = Regex("[a-zA-Z]")
        val digitRegex = Regex("[0-9]+")
        val entries = hashSetOf<Holder<MoveTemplate>>()
        jObject.getAsJsonObject("learnset").entrySet().forEach { (moveId, data) ->
            val sources = data.asJsonArray
            val move = lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY)
                .getOrThrow(ResourceKey.create(CobblemonRegistries.MOVE_KEY, moveId.asIdentifierDefaultingNamespace()))
            for (entry in sources) {
                val source = entry.asString
                val digitMatches = digitRegex.findAll(source).toList()
                val sourceType = characterRegex.find(source)?.value ?: throw IllegalStateException("Couldn't resolve source type for $source")
                if (sourceType != "L") {
                    continue
                }
                val level = digitMatches.getOrNull(1)?.value?.toInt() ?: throw IllegalStateException("Couldn't get level for $source")
                if (level != 0) {
                    continue
                }
                entries += move
            }
        }
        return HolderSet.direct(*entries.toTypedArray())
    }

    private fun extractEggGroups(jObject: JsonObject): Set<EggGroup> {
        val byId = EggGroup.entries.associateBy { it.showdownId() }
        return jObject.getAsJsonArray("eggGroups")
            .mapNotNull { byId[it.asString] }
            .toSet()
    }

    private fun extractPreEvolution(jObject: JsonObject, baseSpecies: Map<String, JsonObject>): Optional<ResourceKey<Species>> {
        val preEvoName = jObject["prevo"].asString
        if (preEvoName.isEmpty()) {
            return Optional.empty()
        }
        val preEvolutionJObject = baseSpecies[preEvoName] ?: throw IllegalArgumentException("Cannot resolve species for name $preEvoName")
        return Optional.of(ResourceKey.create(CobblemonRegistries.SPECIES_KEY, idForSpecies(preEvolutionJObject)))
    }

    private fun extractEvolutions(baseId: ResourceLocation, jObject: JsonObject, baseSpecies: Map<String, JsonObject>, lookupProvider: Provider): Set<Evolution> {
        val evolutions = linkedSetOf<Evolution>()
        jObject.getAsJsonArray("evos").map { it.asString }.forEach { key ->
            val evolution = baseSpecies[key]?.asJsonObject ?: return@forEach
            val resultId = idForSpecies(evolution)
            val evoId = "${baseId.simplify()}_${resultId.simplify()}"
            val result = PokemonProperties().apply { species = resultId.simplify() }
            if (evolution.has("evoType")) {
                evolutions += when (val evoType = evolution["evoType"].asString) {
                    "trade" -> this.resolveTradeEvolution(evolution, evoId, result, baseSpecies, lookupProvider)
                    "useItem" -> this.resolveItemEvolution(evolution, evoId, result, lookupProvider)
                    "levelMove" -> this.resolveLevelUpWithMoveEvolution(evolution, evoId, result, lookupProvider)
                    "levelExtra" -> this.resolveLevelUpWithExtraEvolution(evolution, evoId, result, lookupProvider)
                    "levelFriendship" -> this.resolveFriendshipEvolution(evolution, evoId, result, lookupProvider)
                    "levelHold" -> this.resolveHoldingItemEvolution(evolution, evoId, result, lookupProvider)
                    "other" -> this.resolveOtherEvolution(evolution, evoId, result, lookupProvider)
                    else -> throw IllegalArgumentException("Cannot resolve evo type $evoType")
                }
            } else {
                evolutions +=  this.resolveLevelUpEvolution(evolution, evoId, result, lookupProvider)
            }
        }
        this.evolutionAppendages[baseId.path]?.let { it(evolutions, lookupProvider) }
        return evolutions
    }

    private fun resolveExtraEvolutionRequirements(jObject: JsonObject): Set<EvolutionRequirement> {
        if (!jObject.has("evoCondition")) {
            return emptySet()
        }
        val requirement = jObject["evoCondition"].asString.lowercase()
        return this.showdownEvolutionRequirements[requirement] ?: throw IllegalArgumentException("There's no equivalent for showdown requirement \"$requirement\"")
    }

    private fun resolveLevelUpEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): LevelUpEvolution {
        val level = jObject["evoLevel"].asInt
        val evolution = LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = false,
            requirements = setOf(LevelRequirement(level, level)) + this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
        return evolution
    }

    private fun resolveTradeEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, baseSpecies: Map<String, JsonObject>, lookupProvider: Provider): TradeEvolution {
        val properties = PokemonProperties()
        var consumeHeldItem = false
        val requirements = hashSetOf<EvolutionRequirement>()
        if (jObject.has("evoCondition")) {
            val tradePartnerShowdownId = jObject["evoCondition"].asString.replace("with a ", "")
            val tradePartnerId = idForSpecies(baseSpecies[tradePartnerShowdownId] ?: throw IllegalArgumentException(""))
            properties.species = tradePartnerId.simplify()
        }
        if (jObject.has("evoItem")) {
            val item = this.itemFromShowdown(jObject["evoItem"].asString, lookupProvider)
            requirements.add(HeldItemRequirement(NbtItemPredicate(RegistryBasedCondition.ResourceLocationCondition(item.unwrapKey().get().location()), Optional.empty())))
            consumeHeldItem = true
        }
        val evolution = TradeEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            requiredContext = properties,
            consumeHeldItem = consumeHeldItem,
            requirements = requirements + this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
        )
        return evolution
    }

    private fun resolveItemEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): ItemInteractionEvolution {
        val item = this.itemFromShowdown(jObject["evoItem"].asString, lookupProvider)
        val evolution = ItemInteractionEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            requiredContext = NbtItemPredicate(RegistryBasedCondition.ResourceLocationCondition(item.unwrapKey().get().location()), Optional.empty()),
            consumeHeldItem = false,
            requirements = this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject)
        )
        return evolution
    }

    private fun itemFromShowdown(showdownId: String, lookupProvider: Provider): Holder<Item> {
        val id = if (showdownId == "Up-Grade") {
            cobblemonResource("upgrade")
        } else {
            cobblemonResource(
                showdownId.lowercase().replace("-", "_")
                    .replace("'", "")
                    .replace(Regex(" "), "_")
            )
        }
        val itemRegistry = lookupProvider.lookupOrThrow(Registries.ITEM)
        return itemRegistry.getOrThrow(ResourceKey.create(Registries.ITEM, id))
    }

    private fun resolveLevelUpWithMoveEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): LevelUpEvolution {
        val moveId = jObject["evoMove"].asString.lowercase()
        val move = lookupProvider.lookupOrThrow(CobblemonRegistries.MOVE_KEY)
            .getOrThrow(ResourceKey.create(CobblemonRegistries.MOVE_KEY, cobblemonResource(moveId.replace(ShowdownIdentifiable.EXCLUSIVE_REGEX, ""))))
        val evolution = LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = false,
            requirements = setOf(MoveSetRequirement(move)) + this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
        return evolution
    }

    private fun resolveLevelUpWithExtraEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): Evolution {
        if (jObject.has("evoCondition") && jObject["evoCondition"].asString == "near a special magnetic field") {
            val itemRegistry = lookupProvider.lookupOrThrow(Registries.ITEM)
            val item = itemRegistry.getOrThrow(ResourceKey.create(Registries.ITEM, cobblemonResource("thunder_stone")))
            return ItemInteractionEvolution(
                id = evoId,
                result = result,
                shedder = null,
                optional = true,
                requiredContext = NbtItemPredicate(RegistryBasedCondition.ResourceLocationCondition(item.unwrapKey().get().location()), Optional.empty()),
                consumeHeldItem = false,
                requirements = this.resolveExtraEvolutionRequirements(jObject),
                learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject)
            )
        }
        return LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = false,
            requirements = this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
    }

    private fun resolveFriendshipEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): Evolution {
        val evolution = LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = false,
            requirements = setOf(FriendshipRequirement(160)) + this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
        return evolution
    }

    private fun resolveHoldingItemEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): LevelUpEvolution {
        val item = this.itemFromShowdown(jObject["evoItem"].asString, lookupProvider)
        val evolution = LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = true,
            requirements = setOf(HeldItemRequirement(NbtItemPredicate(RegistryBasedCondition.ResourceLocationCondition(item.unwrapKey().get().location()), Optional.empty()))) + this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
        return evolution
    }

    private fun resolveOtherEvolution(jObject: JsonObject, evoId: String, result: PokemonProperties, lookupProvider: Provider): LevelUpEvolution {
        val evolution = LevelUpEvolution(
            id = evoId,
            result = result,
            shedder = null,
            optional = true,
            consumeHeldItem = true,
            requirements = this.resolveExtraEvolutionRequirements(jObject),
            learnableMoves = this.extractOnEvolutionMoves(lookupProvider, jObject),
            permanent = true,
        )
        return evolution
    }

    /**
     * Check the [wiki](https://wiki.cobblemon.com/index.php/Pok%C3%A9mon/Unique_Forms#Minecraft_Forms) for details.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun appendSaplingTorterras(evolutions: MutableSet<Evolution>, lookupProvider: Provider) {
        val baseId = "grotle_torterra"
        val base = evolutions.first { it.id == baseId }
        val saplings = mapOf(
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.OAK_SAPLING) to "oak",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.BIRCH_SAPLING) to "birch",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.DARK_OAK_SAPLING) to "dark_oak",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.ACACIA_SAPLING) to "acacia",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.JUNGLE_SAPLING) to "jungle",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.SPRUCE_SAPLING) to "spruce",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.MANGROVE_PROPAGULE) to "mangrove",
            RegistryBasedCondition.ResourceLocationCondition.fromItem(Items.CHERRY_SAPLING) to "cherry",
            RegistryBasedCondition.TagCondition(CobblemonItemTags.AZALEA_TREE) to "azalea",
        )
        saplings.forEach { (condition, aspect) ->
            evolutions += LevelUpEvolution(
                id = "${base.id}_$aspect",
                result = PokemonProperties().apply {
                    species = "torterra"
                    customProperties += StringSpeciesFeature("tree", aspect)
                },
                shedder = base.shedder,
                optional = base.optional,
                consumeHeldItem = true,
                requirements = base.requirements + setOf(HeldItemRequirement(NbtItemPredicate(condition, Optional.empty()))),
                learnableMoves = base.learnableMoves,
                permanent = (base as? PassiveEvolution)?.permanent ?: true,
            )
        }
    }

    private fun createBiasForms(generated: Map<ResourceLocation, Species>, lookupProvider: Provider, consumer: Consumer<SpeciesDataExport>) {
        this.createAlolanBiasForms(generated, lookupProvider, consumer)
        this.createHisuianBiasForms(generated, lookupProvider, consumer)
    }

    private fun createAlolanBiasForms(generated: Map<ResourceLocation, Species>, lookupProvider: Provider, consumer: Consumer<SpeciesDataExport>) {
        val pikachu = generated[cobblemonResource("pikachu")] ?: throw IllegalArgumentException("Missing base Pikachu")
        this.createAlolanBiasPikachu(pikachu, consumer)
        val exeggcute = generated[cobblemonResource("exeggcute")] ?: throw IllegalArgumentException("Missing base Exeggcute")
    }

    private fun createHisuianBiasForms(generated: Map<ResourceLocation, Species>, lookupProvider: Provider, consumer: Consumer<SpeciesDataExport>) {
    }

    private fun createAlolanBiasPikachu(base: Species, consumer: Consumer<SpeciesDataExport>) {
        // TODO: handle evolution
        consumer.accept(SpeciesDataExport(ALOLAN_PIKACHU, base))
    }

    class SpeciesDataExport(
        private val id: ResourceLocation,
        private val value: Species
    ) : DataExport<Species> {
        override fun id(): ResourceLocation = this.id

        override fun codec(): Codec<Species> = Species.DIRECT_CODEC

        override fun value(): Species = this.value
    }

    companion object {

        val SKIPPED = hashSetOf(
            "pikachu/original",
            "pikachu/hoenn",
            "pikachu/sinnoh",
            "pikachu/unova",
            "pikachu/kalos",
            "pikachu/alola",
            "pikachu/partner",
            "pikachu/world",
            "eevee/partner",
            "gumshoos/totem",
            "raticate/alola-totem",
            "wishiwashi/totem",
            "araquanid/totem",
            "salazzle/totem",
            "marowak/alola-totem",
            "lurantis/totem",
            "vikavolt/totem",
            "mimikyu/totem",
            "mimikyu/busted-totem",
            "kommo-o/totem",
            "ribombee/totem",
        )

        val ALOLAN_PIKACHU = cobblemonResource("pikachu/alola-bias")
        val ALOLAN_EXEGGCUTE = cobblemonResource("exeggcute/alola-bias")
        val HISUIAN_CYNDAQUIL = cobblemonResource("cyndaquil/hisui-bias")
        val HISUIAN_QUILAVA = cobblemonResource("quilava/hisui-bias")
        val HISUIAN_OSHAWOTT = cobblemonResource("oshawott/hisui-bias")
        val HISUIAN_DEWOTT = cobblemonResource("dewott/hisui-bias")
        val HISUIAN_PETILIL = cobblemonResource("petilil/hisui-bias")
        val HISUIAN_GOOMY = cobblemonResource("goomy/hisui-bias")
        val HISUIAN_ROWLET = cobblemonResource("rowlet/hisui-bias")
        val HISUIAN_DARTRIX = cobblemonResource("dartrix/hisui-bias")

        fun idForSpecies(jObject: JsonObject): ResourceLocation {
            val baseSpecies = jObject["baseSpecies"].asString.lowercase()
                .replace("\u2019", "")
                .replace("\u0301", "")
                .replace(". ", "_")
                .replace(".", "")
                .replace(" ", "_")
                .replace(":", "")
            val form = jObject["forme"].asString
            if (form.isNotEmpty()) {
                return cobblemonResource(
                    baseSpecies
                            + "/" +
                            form.lowercase()
                                .replace(" ", "_")
                                .replace("10%", "ten")
                                .replace("'", "")
                )
            }
            return cobblemonResource(baseSpecies)
        }

    }

}