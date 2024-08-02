/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.adapters

import com.cobblemon.mod.common.api.pokemon.evolution.adapters.RequirementAdapter
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.evolution.requirements.*
import com.google.common.collect.HashBiMap
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * The default implementation of [RequirementAdapter].
 *
 * @author Licious
 * @since March 21st, 2022
 */
object CobblemonRequirementAdapter : RequirementAdapter {

    private const val VARIANT = "variant"

    private val types = HashBiMap.create<String, KClass<out EvolutionRequirement>>()

    init {
        this.registerType(AreaRequirement.ADAPTER_VARIANT, AreaRequirement::class)
        this.registerType(BiomeRequirement.ADAPTER_VARIANT, BiomeRequirement::class)
        this.registerType(FriendshipRequirement.ADAPTER_VARIANT, FriendshipRequirement::class)
        this.registerType(HeldItemRequirement.ADAPTER_VARIANT, HeldItemRequirement::class)
        this.registerType(WorldRequirement.ADAPTER_VARIANT, WorldRequirement::class)
        this.registerType(MoveSetRequirement.ADAPTER_VARIANT, MoveSetRequirement::class)
        this.registerType(MoveTypeRequirement.ADAPTER_VARIANT, MoveTypeRequirement::class)
        this.registerType(PartyMemberRequirement.ADAPTER_VARIANT, PartyMemberRequirement::class)
        this.registerType(PokemonPropertiesRequirement.ADAPTER_VARIANT, PokemonPropertiesRequirement::class)
        this.registerType(TimeRangeRequirement.ADAPTER_VARIANT, TimeRangeRequirement::class)
        this.registerType(LevelRequirement.ADAPTER_VARIANT, LevelRequirement::class)
        this.registerType(WeatherRequirement.ADAPTER_VARIANT, WeatherRequirement::class)
        this.registerType(StatCompareRequirement.ADAPTER_VARIANT, StatCompareRequirement::class)
        this.registerType(StatEqualRequirement.ADAPTER_VARIANT, StatEqualRequirement::class)
        this.registerType(AttackDefenceRatioRequirement.ADAPTER_VARIANT, AttackDefenceRatioRequirement::class)
        this.registerType(BattleCriticalHitsRequirement.ADAPTER_VARIANT, BattleCriticalHitsRequirement::class)
        this.registerType(DamageTakenRequirement.ADAPTER_VARIANT, DamageTakenRequirement::class)
        this.registerType(UseMoveRequirement.ADAPTER_VARIANT, UseMoveRequirement::class)
        this.registerType(MoonPhaseRequirement.ADAPTER_VARIANT, MoonPhaseRequirement::class)
        this.registerType(RecoilRequirement.ADAPTER_VARIANT, RecoilRequirement::class)
        this.registerType(DefeatRequirement.ADAPTER_VARIANT, DefeatRequirement::class)
        this.registerType(BlocksTraveledRequirement.ADAPTER_VARIANT, BlocksTraveledRequirement::class)
        this.registerType(StructureRequirement.ADAPTER_VARIANT, StructureRequirement::class)
        this.registerType(AnyRequirement.ADAPTER_VARIANT, AnyRequirement::class)
        this.registerType(PropertyRangeRequirement.ADAPTER_VARIANT, PropertyRangeRequirement::class)
        this.registerType(PlayerHasAdvancementRequirement.ADAPTER_VARIANT, PlayerHasAdvancementRequirement::class)
    }

    override fun <T : EvolutionRequirement> registerType(id: String, type: KClass<T>) {
        this.types[id.lowercase()] = type
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EvolutionRequirement {
        val variant = json.asJsonObject.get(VARIANT).asString.lowercase()
        val type = this.types[variant] ?: throw IllegalArgumentException("Cannot resolve evolution requirement type for variant $variant")
        return context.deserialize(json, type.java)
    }

    override fun serialize(src: EvolutionRequirement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val json = context.serialize(src, src::class.java).asJsonObject
        val variant = this.types.inverse()[src::class] ?: throw IllegalArgumentException("Cannot resolve evolution requirement for type ${src::class.qualifiedName}")
        json.addProperty(VARIANT, variant)
        return json
    }

}