/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution.requirement

import com.cobblemon.mod.common.pokemon.evolution.requirements.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

fun interface EvolutionRequirementType<T : EvolutionRequirement> {

    fun codec(): MapCodec<T>

    companion object {

        internal val REGISTRY: Registry<EvolutionRequirementType<*>> = MappedRegistry(
            ResourceKey.createRegistryKey(cobblemonResource("evolution_requirement_type")),
            Lifecycle.stable()
        )

        // TODO: Fix me and add new types, see AllOfCondition, AnyOfCondition & InvertedCondition
        //@JvmStatic
        //val ANY = this.register(cobblemonResource("any"), AnyRequirement.CODEC)

        @JvmStatic
        val AREA = this.register(cobblemonResource("area"), AreaRequirement.CODEC)

        @JvmStatic
        val BATTLE_CRITICAL_HITS = this.register(cobblemonResource("battle_critical_hits"), BattleCriticalHitsRequirement.CODEC)

        @JvmStatic
        val BIOME = this.register(cobblemonResource("biome"), BiomeRequirement.CODEC)

        @JvmStatic
        val BLOCKS_TRAVELED = this.register(cobblemonResource("blocks_traveled"), BlocksTraveledRequirement.CODEC)

        @JvmStatic
        val DAMAGE_TAKEN = this.register(cobblemonResource("damage_taken"), DamageTakenRequirement.CODEC)

        @JvmStatic
        val DEFEAT = this.register(cobblemonResource("defeat"), DefeatRequirement.CODEC)

        @JvmStatic
        val FRIENDSHIP = this.register(cobblemonResource("friendship"), FriendshipRequirement.CODEC)

        @JvmStatic
        val HELD_ITEM = this.register(cobblemonResource("held_item"), HeldItemRequirement.CODEC)

        @JvmStatic
        val LEVEL = this.register(cobblemonResource("level"), LevelRequirement.CODEC)

        @JvmStatic
        val MOON_PHASE = this.register(cobblemonResource("moon_phase"), MoonPhaseRequirement.CODEC)

        @JvmStatic
        val HAS_MOVE = this.register(cobblemonResource("has_move"), MoveSetRequirement.CODEC)

        @JvmStatic
        val HAS_MOVE_TYPE = this.register(cobblemonResource("has_move_type"), MoveTypeRequirement.CODEC)

        @JvmStatic
        val PARTY_MEMBER = this.register(cobblemonResource("party_member"), PartyMemberRequirement.CODEC)

        @JvmStatic
        val ADVANCEMENT = this.register(cobblemonResource("advancement"), PlayerHasAdvancementRequirement.CODEC)

        @JvmStatic
        val PROPERTIES = this.register(cobblemonResource("properties"), PokemonPropertiesRequirement.CODEC)

        @JvmStatic
        val PROPERTY_RANGE = this.register(cobblemonResource("property_range"), PropertyRangeRequirement.CODEC)

        @JvmStatic
        val RECOIL = this.register(cobblemonResource("recoil"), RecoilRequirement.CODEC)

        @JvmStatic
        val STAT_RATIO = this.register(cobblemonResource("stat_ratio"), StatRatioRequirement.CODEC)

        @JvmStatic
        val STRUCTURE = this.register(cobblemonResource("structure"), StructureRequirement.CODEC)

        @JvmStatic
        val TIME_RANGE = this.register(cobblemonResource("time_range"), TimeRangeRequirement.CODEC)

        @JvmStatic
        val USE_MOVE = this.register(cobblemonResource("use_move"), UseMoveRequirement.CODEC)

        @JvmStatic
        val WEATHER = this.register(cobblemonResource("weather"), WeatherRequirement.CODEC)

        @JvmStatic
        val WORLD = this.register(cobblemonResource("world"), WorldRequirement.CODEC)

        @JvmStatic
        fun <T : EvolutionRequirement> register(id: ResourceLocation, codec: MapCodec<T>): EvolutionRequirementType<T> {
            return Registry.register(REGISTRY, id, EvolutionRequirementType { codec })
        }

    }

}