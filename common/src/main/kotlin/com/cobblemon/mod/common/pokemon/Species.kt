/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.registry.RegistryElement
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.entity.PoseType.Companion.FLYING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SWIMMING_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.ai.PokemonBehaviour
import com.cobblemon.mod.common.pokemon.lighthing.LightingData
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.codec.internal.species.ClientSpeciesP1
import com.cobblemon.mod.common.util.codec.internal.species.SpeciesP1
import com.cobblemon.mod.common.util.codec.internal.species.SpeciesP2
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import java.util.*
import kotlin.jvm.optionals.getOrNull

class Species(
    nationalPokedexNumber: Int,
    baseStats: Map<Stat, Int>,
    maleRatio: Float,
    catchRate: Int,
    baseScale: Float,
    baseExperienceYield: Int,
    baseFriendship: Int,
    evYield: Map<Stat, Int>,
    experienceGroup: ExperienceGroup,
    hitbox: EntityDimensions,
    primaryType: ElementalType,
    secondaryType: Optional<ElementalType>,
    abilityPool: AbilityPool,
    shoulderMountable: Boolean,
    shoulderEffects: Set<ShoulderEffect>,
    learnset: Learnset,
    standingEyeHeight: Optional<Float>,
    swimmingEyeHeight: Optional<Float>,
    flyingEyeHeight: Optional<Float>,
    behaviour: PokemonBehaviour,
    eggCycles: Int,
    eggGroups: Set<EggGroup>,
    dynamaxBlocked: Boolean,
    implemented: Boolean,
    height: Float,
    weight: Float,
    internal val preEvolutionKey: Optional<ResourceKey<Species>>,
    battleTheme: ResourceLocation,
    lightingData: Optional<LightingData>,
) : RegistryElement<Species>, ShowdownIdentifiable {

    var name: String = "Bulbasaur"
    val translatedName: MutableComponent
        get() = Component.translatable("${this.resourceIdentifier.namespace}.species.${this.resourceIdentifier.path.replace("/", ".")}.name")
    var nationalPokedexNumber: Int = nationalPokedexNumber
        private set

    var baseStats = baseStats.toMutableMap()
        private set
    /** The ratio of the species being male. If -1, the Pokémon is genderless. */
    var maleRatio = maleRatio
        private set
    var catchRate = catchRate
        private set
    // Only modifiable for debugging sizes
    var baseScale = baseScale
    var baseExperienceYield = baseExperienceYield
        private set
    var baseFriendship = baseFriendship
        private set
    var evYield = evYield
        private set
    var experienceGroup = experienceGroup
        private set
    var hitbox = hitbox
    var primaryType: ElementalType = primaryType
        internal set
    var secondaryType: ElementalType? = secondaryType.getOrNull()
        internal set
    var abilities = abilityPool
        private set
    var shoulderMountable = shoulderMountable
        private set
    var shoulderEffects = shoulderEffects
        private set
    var moves = learnset
        private set
    internal var standingEyeHeight: Float? = standingEyeHeight.getOrNull()
    internal var swimmingEyeHeight: Float? = swimmingEyeHeight.getOrNull()
    internal var flyingEyeHeight: Float? = flyingEyeHeight.getOrNull()
    var behaviour = behaviour
        private set
    // TODO: Confirm if it will be dropped
    var pokedex = mutableListOf<String>()
        private set
    // TODO: Migrate to loot tables.
    var drops = DropTable()
        private set
    var eggCycles = eggCycles
        private set
    var eggGroups = eggGroups
        private set
    var dynamaxBlocked = dynamaxBlocked
        private set
    var implemented = implemented
        private set

    /**
     * The height in decimeters
     */
    var height = height
        private set

    /**
     * The weight in hectograms
     */
    var weight = weight
        private set

    // TODO: Property to define base form aka base species
    //val standardForm by lazy { FormData(_evolutions = this.evolutions).initialize(this) }

    /**
     * Contains the evolutions of this species.
     * If you're trying to find out the possible evolutions of a Pokémon you should always work with their [FormData].
     * The base species is the [standardForm].
     * Do not access this property immediately after a species is loaded, it requires all species in the game to be loaded.
     * To be aware of this gamestage subscribe to [PokemonSpecies.observable].
     */
    var evolutions: MutableSet<Evolution> = hashSetOf()
        private set

    val preEvolution: Species? get() = this.preEvolutionKey.map { PokemonSpecies.get(it) }.orElse(null)

    @Transient
    lateinit var resourceIdentifier: ResourceLocation

    val types: Iterable<ElementalType>
        get() = secondaryType?.let { listOf(primaryType, it) } ?: listOf(primaryType)

    var battleTheme: ResourceLocation = battleTheme

    var lightingData: LightingData? = lightingData.getOrNull()
        private set

    fun initialize() {
        Cobblemon.statProvider.provide(this)
        this.lightingData?.let { this.lightingData = it.copy(lightLevel = it.lightLevel.coerceIn(0, 15)) }
        // These properties are lazy, these need all species to be reloaded but SpeciesAdditions is what will eventually trigger this after all species have been loaded
        this.evolutions.size
    }

    // Ran after initialize due to us creating a Pokémon here which requires all the properties in #initialize to be present for both this and the results, this is the easiest way to quickly resolve species
    internal fun resolveEvolutionMoves() {
        this.evolutions.forEach { evolution ->
            if (evolution.learnableMoves.isNotEmpty() && evolution.result.species != null) {
                val pokemon = evolution.result.create()
                pokemon.species.moves.evolutionMoves += evolution.learnableMoves
            }
        }
    }

    fun create(level: Int = 10) = PokemonProperties.parse("species=\"${this.name}\" level=${level}").create()

    fun eyeHeight(entity: PokemonEntity): Float {
        val multiplier = this.resolveEyeHeight(entity) ?: VANILLA_DEFAULT_EYE_HEIGHT
        return entity.bbHeight * multiplier
    }

    private fun resolveEyeHeight(entity: PokemonEntity): Float? = when {
        entity.getCurrentPoseType() in SWIMMING_POSES -> this.swimmingEyeHeight ?: standingEyeHeight
        entity.getCurrentPoseType() in FLYING_POSES -> this.flyingEyeHeight ?: standingEyeHeight
        else -> this.standingEyeHeight
    }

    fun canGmax() = this.resourceIdentifier.path.endsWith("gmax")

    override fun showdownId(): String {
        return ShowdownIdentifiable.EXCLUSIVE_REGEX.replace(this.resourceLocation().simplify(), "")
    }

    override fun registry(): Registry<Species> = CobblemonRegistries.SPECIES

    override fun resourceKey(): ResourceKey<Species> = this.registry().getResourceKey(this)
        .orElseThrow { IllegalStateException("Unregistered Species") }

    override fun isTaggedBy(tag: TagKey<Species>): Boolean = this.registry()
        .getHolder(this.resourceKey())
        .orElseThrow { IllegalStateException("Unregistered Species") }
        .`is`(tag)

    companion object {
        private const val VANILLA_DEFAULT_EYE_HEIGHT = .85F

        @JvmStatic
        val DIRECT_CODEC: Codec<Species> = RecordCodecBuilder.create { instance ->
            instance.group(
                SpeciesP1.CODEC.forGetter(SpeciesP1::from),
                SpeciesP2.CODEC.forGetter(SpeciesP2::from),
            ).apply(instance, Companion::fromPartials)
        }

        @JvmStatic
        val PACKET_CODEC: Codec<Species> = RecordCodecBuilder.create { instance ->
            instance.group(
                ClientSpeciesP1.CODEC.forGetter(ClientSpeciesP1::from),
            ).apply(instance, Companion::fromClientPartials)
        }

        @JvmStatic
        val CODEC: Codec<Holder<Species>> = RegistryFileCodec.create(CobblemonRegistries.SPECIES_KEY, DIRECT_CODEC)
        @JvmStatic
        val LIST_CODEC: Codec<HolderSet<Species>> = RegistryCodecs.homogeneousList(CobblemonRegistries.SPECIES_KEY, DIRECT_CODEC)

        private fun fromPartials(p1: SpeciesP1, p2: SpeciesP2): Species = Species(
            p1.nationalPokedexNumber,
            p1.baseStats,
            p1.maleRatio,
            p1.catchRate,
            p1.baseScale,
            p1.baseExperienceYield,
            p1.baseFriendship,
            p1.evYield,
            p1.experienceGroup,
            p1.hitbox,
            p1.primaryType.value(),
            p1.secondaryType.map { it.value() },
            p1.abilityPool,
            p1.shoulderMountable,
            p1.shoulderEffects,
            p1.learnset,
            p2.standingEyeHeight,
            p2.swimmingEyeHeight,
            p2.flyingEyeHeight,
            p2.behaviour,
            p2.eggCycles,
            p2.eggGroups,
            p2.dynamaxBlocked,
            p2.implemented,
            p2.height,
            p2.weight,
            p2.preEvolution,
            p2.battleTheme,
            p2.lightingData,
        )

        private fun fromClientPartials(p1: ClientSpeciesP1): Species = Species(
            p1.nationalPokedexNumber,
            p1.baseStats,
            0F,
            0,
            p1.baseScale,
            0,
            0,
            emptyMap(),
            p1.experienceGroup,
            p1.hitbox,
            p1.primaryType.value(),
            p1.secondaryType.map { it.value() },
            AbilityPool(),
            false,
            emptySet(),
            p1.learnset,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            PokemonBehaviour(),
            0,
            emptySet(),
            false,
            p1.implemented,
            p1.height,
            p1.weight,
            Optional.empty(),
            p1.battleTheme,
            p1.lightingData,
        )
    }
}