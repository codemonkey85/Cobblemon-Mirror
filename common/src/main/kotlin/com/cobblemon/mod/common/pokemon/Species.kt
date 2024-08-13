/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
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
import com.cobblemon.mod.common.util.codec.internal.species.SpeciesP1
import com.cobblemon.mod.common.util.codec.internal.species.SpeciesP2
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
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
    preEvolution: Optional<Holder<Species>>,
    battleTheme: ResourceLocation,
    lightingData: Optional<LightingData>,
) : RegistryElement<Species>, ShowdownIdentifiable {

    var name: String = "Bulbasaur"
    val translatedName: MutableComponent
        get() = Component.translatable("${this.resourceIdentifier.namespace}.species.${this.unformattedShowdownId()}.name")
    var nationalPokedexNumber: Int = nationalPokedexNumber
        private set

    var baseStats = baseStats
        private set
    /** The ratio of the species being male. If -1, the Pokémon is genderless. */
    var maleRatio = maleRatio
        private set
    var catchRate = catchRate
        private set
    // Only modifiable for debugging sizes
    var baseScale = baseScale
        private set
    var baseExperienceYield = baseExperienceYield
        private set
    var baseFriendship = baseFriendship
        private set
    var evYield = evYield
        private set
    var experienceGroup = experienceGroup
        private set
    var hitbox = hitbox
        private set
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
    // TODO: Hiro said we could drop this but validate opinions of public first
    var features = mutableSetOf<String>()
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

    // TODO: Move to tags
    var labels = hashSetOf<String>()
        private set

    /**
     * Contains the evolutions of this species.
     * If you're trying to find out the possible evolutions of a Pokémon you should always work with their [FormData].
     * The base species is the [standardForm].
     * Do not access this property immediately after a species is loaded, it requires all species in the game to be loaded.
     * To be aware of this gamestage subscribe to [PokemonSpecies.observable].
     */
    var evolutions: MutableSet<Evolution> = hashSetOf()
        private set

    var preEvolution: Holder<Species>? = preEvolution.getOrNull()
        private set

    @Transient
    lateinit var resourceIdentifier: ResourceLocation

    val types: Iterable<ElementalType>
        get() = secondaryType?.let { listOf(primaryType, it) } ?: listOf(primaryType)

    var battleTheme: ResourceLocation = CobblemonSounds.PVW_BATTLE.location

    var lightingData: LightingData? = null
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

    fun getForm(aspects: Set<String>) = forms.lastOrNull { it.aspects.all { it in aspects } } ?: standardForm

    fun eyeHeight(entity: PokemonEntity): Float {
        val multiplier = this.resolveEyeHeight(entity) ?: VANILLA_DEFAULT_EYE_HEIGHT
        return entity.bbHeight * multiplier
    }

    private fun resolveEyeHeight(entity: PokemonEntity): Float? = when {
        entity.getCurrentPoseType() in SWIMMING_POSES -> this.swimmingEyeHeight ?: standingEyeHeight
        entity.getCurrentPoseType() in FLYING_POSES -> this.flyingEyeHeight ?: standingEyeHeight
        else -> this.standingEyeHeight
    }

    // TODO: Redo me
    //fun canGmax() = this.forms.find { it.formOnlyShowdownId() == "gmax" } != null

    // TODO: Use me as reference for packet codec
    /*override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(this.implemented)
        buffer.writeString(this.name)
        buffer.writeInt(this.nationalPokedexNumber)
        buffer.writeMap(this.baseStats,
            { _, stat -> Cobblemon.statProvider.encode(buffer, stat)},
            { _, value -> buffer.writeSizedInt(IntSize.U_SHORT, value) }
        )
        buffer.writeResourceKey(this.primaryType.resourceKey())
        buffer.writeNullable(this.secondaryType) { pb, type -> pb.writeResourceKey(type.resourceKey()) }
        buffer.writeString(this.experienceGroup.name)
        buffer.writeFloat(this.height)
        buffer.writeFloat(this.weight)
        buffer.writeFloat(this.baseScale)
        // Hitbox start
        buffer.writeFloat(this.hitbox.width)
        buffer.writeFloat(this.hitbox.height)
        buffer.writeBoolean(this.hitbox.fixed)
        // Hitbox end
        this.moves.encode(buffer)
        buffer.writeCollection(this.pokedex) { pb, line -> pb.writeString(line) }
        buffer.writeCollection(this.forms) { _, form -> form.encode(buffer) }
        buffer.writeIdentifier(this.battleTheme)
        buffer.writeCollection(this.features) { pb, feature -> pb.writeString(feature) }
        buffer.writeNullable(this.lightingData) { pb, data ->
            pb.writeInt(data.lightLevel)
            pb.writeEnumConstant(data.liquidGlowMode)
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        this.implemented = buffer.readBoolean()
        this.name = buffer.readString()
        this.nationalPokedexNumber = buffer.readInt()
        this.baseStats.putAll(buffer.readMap(
            { _ -> Cobblemon.statProvider.decode(buffer) },
            { _ -> buffer.readSizedInt(IntSize.U_SHORT) })
        )
        val typeRegistry = buffer.registryAccess().registryOrThrow(CobblemonRegistries.ELEMENTAL_TYPE_KEY)
        this.primaryType = typeRegistry.getOrThrow(buffer.readResourceKey(CobblemonRegistries.ELEMENTAL_TYPE_KEY))
        this.secondaryType = buffer.readNullable { pb -> typeRegistry.getOrThrow(pb.readResourceKey(CobblemonRegistries.ELEMENTAL_TYPE_KEY)) }
        this.experienceGroup = ExperienceGroups.findByName(buffer.readString())!!
        this.height = buffer.readFloat()
        this.weight = buffer.readFloat()
        this.baseScale = buffer.readFloat()
        this.hitbox = buffer.readEntityDimensions()
        this.moves.decode(buffer)
        this.pokedex.clear()
        this.pokedex += buffer.readList { pb -> pb.readString() }
        this.forms.clear()
        this.forms += buffer.readList{ FormData().apply { decode(buffer) } }.filterNotNull()
        this.battleTheme = buffer.readIdentifier()
        this.features.clear()
        this.features += buffer.readList { pb -> pb.readString() }
        this.lightingData = buffer.readNullable { pb -> LightingData(pb.readInt(), pb.readEnumConstant(LightingData.LiquidGlowMode::class.java)) }
        this.initialize()
    }

    override fun shouldSynchronize(other: Species): Boolean {
        if (other.resourceIdentifier.toString() != other.resourceIdentifier.toString())
            return false
        return other.showdownId() != this.showdownId()
                || other.nationalPokedexNumber != this.nationalPokedexNumber
                || other.baseStats != this.baseStats
                || other.hitbox != this.hitbox
                || other.primaryType != this.primaryType
                || other.secondaryType != this.secondaryType
                || other.standingEyeHeight != this.standingEyeHeight
                || other.swimmingEyeHeight != this.swimmingEyeHeight
                || other.flyingEyeHeight != this.flyingEyeHeight
                || other.dynamaxBlocked != this.dynamaxBlocked
                || other.pokedex != this.pokedex
                || other.forms != this.forms
                // We only sync level up moves atm
                || this.moves.shouldSynchronize(other.moves)
                || other.battleTheme != this.battleTheme
                || other.features != this.features
    }*/

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
        val CODEC: Codec<Species> = RecordCodecBuilder.create { instance ->
            instance.group(
                SpeciesP1.CODEC.forGetter(SpeciesP1::from),
                SpeciesP2.CODEC.forGetter(SpeciesP2::from),
            ).apply(instance, Companion::fromPartials)
        }

        @JvmStatic
        val PACKET_CODEC: Codec<Species> = Codec.unit(Species())

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
            p1.primaryType,
            p1.secondaryType,
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
    }
}