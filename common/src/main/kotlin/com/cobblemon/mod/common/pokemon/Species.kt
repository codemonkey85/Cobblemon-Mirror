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
import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroups
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.PoseType.Companion.FLYING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.SWIMMING_POSES
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.ai.PokemonBehaviour
import com.cobblemon.mod.common.pokemon.lighthing.LightingData
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.cobblemon.mod.common.util.*
import com.mojang.serialization.Codec
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class Species : ClientDataSynchronizer<Species>, ShowdownIdentifiable {
    var name: String = "Bulbasaur"
    val translatedName: MutableComponent
        get() = Component.translatable("${this.resourceIdentifier.namespace}.species.${this.unformattedShowdownId()}.name")
    var nationalPokedexNumber = 1

    var baseStats = hashMapOf<Stat, Int>()
        private set
    /** The ratio of the species being male. If -1, the Pokémon is genderless. */
    var maleRatio: Float = 0.5F
        private set
    var catchRate = 45
        private set
    // Only modifiable for debugging sizes
    var baseScale = 1F
    var baseExperienceYield = 10
    var baseFriendship = 0
    var evYield = hashMapOf<Stat, Int>()
        private set
    var experienceGroup = ExperienceGroups.first()
    var hitbox = EntityDimensions.fixed(1F, 1F)
    var primaryType = ElementalTypes.GRASS
        internal set
    var secondaryType: ElementalType? = null
        internal set
    var abilities = AbilityPool()
        private set
    var shoulderMountable: Boolean = false
        private set
    var shoulderEffects = mutableListOf<ShoulderEffect>()
        private set
    var moves = Learnset()
        private set
    var features = mutableSetOf<String>()
        private set
    private var standingEyeHeight: Float? = null
    private var swimmingEyeHeight: Float? = null
    private var flyingEyeHeight: Float? = null
    var behaviour = PokemonBehaviour()
        private set
    var pokedex = mutableListOf<String>()
        private set
    var drops = DropTable()
        private set
    var eggCycles = 120
        private set
    var eggGroups = hashSetOf<EggGroup>()
        private set
    var dynamaxBlocked = false
    var implemented = false

    /**
     * The height in decimeters
     */
    var height = 1F
        private set

    /**
     * The weight in hectograms
     */
    var weight = 1F
        private set

    var forms = mutableListOf<FormData>()
        private set

    val standardForm by lazy { FormData(_evolutions = this.evolutions).initialize(this) }

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

    var preEvolution: PreEvolution? = null
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
        this.forms.forEach { it.initialize(this) }
        if (this.forms.isNotEmpty() && this.forms.none { it == this.standardForm }) {
            this.forms.add(0, this.standardForm)
        }
        this.lightingData?.let { this.lightingData = it.copy(lightLevel = it.lightLevel.coerceIn(0, 15)) }
        // These properties are lazy, these need all species to be reloaded but SpeciesAdditions is what will eventually trigger this after all species have been loaded
        this.preEvolution?.species
        this.preEvolution?.form
        this.evolutions.size
    }

    // Ran after initialize due to us creating a Pokémon here which requires all the properties in #initialize to be present for both this and the results, this is the easiest way to quickly resolve species + form
    internal fun resolveEvolutionMoves() {
        this.evolutions.forEach { evolution ->
            if (evolution.learnableMoves.isNotEmpty() && evolution.result.species != null) {
                val pokemon = evolution.result.create()
                pokemon.form.moves.evolutionMoves += evolution.learnableMoves
            }
        }
        this.forms.forEach(FormData::resolveEvolutionMoves)
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

    fun canGmax() = this.forms.find { it.formOnlyShowdownId() == "gmax" } != null

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(this.implemented)
        buffer.writeString(this.name)
        buffer.writeInt(this.nationalPokedexNumber)
        buffer.writeMap(this.baseStats,
            { _, stat -> Cobblemon.statProvider.encode(buffer, stat)},
            { _, value -> buffer.writeSizedInt(IntSize.U_SHORT, value) }
        )
        // ToDo remake once we have custom typing support
        buffer.writeString(this.primaryType.name)
        buffer.writeNullable(this.secondaryType) { pb, type -> pb.writeString(type.name) }
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
        this.primaryType = ElementalTypes.getOrException(buffer.readString())
        this.secondaryType = buffer.readNullable { pb -> ElementalTypes.getOrException(pb.readString()) }
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
    }

    /**
     * The literal Showdown ID of this species.
     * This will be a lowercase version of the [name] with all the non-alphanumeric characters removed. For example, "Mr. Mime" becomes "mrmime".
     * If a Species is not a part of the Cobblemon mon the [resourceIdentifier] will have the namespace appended at the start of the ID resulting in something such as 'somemodaspecies'
     *
     * @return The literal Showdown ID of this species.
     */
    override fun showdownId(): String {
        val id = this.unformattedShowdownId()
        if (this.resourceIdentifier.namespace == Cobblemon.MODID) {
            return id
        }
        return this.resourceIdentifier.namespace + id
    }

    /**
     * The unformatted literal Showdown ID of this species.
     * This will be a lowercase version of the [name] with all the non-alphanumeric characters removed. For example, "Mr. Mime" becomes "mrmime".
     * Unlike [showdownId] the [resourceIdentifier] namespace will not be appended at the start regardless if the species is from Cobblemon or a 3rd party addon.
     * The primary purpose of this is for lang keys.
     *
     * @return The unformatted literal Showdown ID of this species.
     */
    private fun unformattedShowdownId(): String = ShowdownIdentifiable.REGEX.replace(this.name.lowercase(), "")

    override fun toString() = this.showdownId()

    companion object {
        private const val VANILLA_DEFAULT_EYE_HEIGHT = .85F

        // TODO: Registries have dedicated Codecs, migrate to that once this is a proper registry impl
        /**
         * A [Codec] that maps to/from an [Identifier] associated as [Species.resourceIdentifier].
         * Uses [PokemonSpecies.getByIdentifier] to query.
         */
        @JvmStatic
        val BY_IDENTIFIER_CODEC: Codec<Species> = CodecUtils.createByIdentifierCodec(
            PokemonSpecies::getByIdentifier,
            Species::resourceIdentifier
        ) { identifier -> "No species for ID $identifier" }
    }
}