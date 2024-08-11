/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.*
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

fun interface PotentialAbilityType<T : PotentialAbility> {
    fun codec(): MapCodec<T>

    companion object {

        internal val REGISTRY: Registry<PotentialAbilityType<*>> = MappedRegistry(
            ResourceKey.createRegistryKey(cobblemonResource("potential_ability")),
            Lifecycle.stable()
        )

        @JvmStatic
        val COMMON = this.register(cobblemonResource("common"), CommonAbility.CODEC)

        @JvmStatic
        val HIDDEN = this.register(cobblemonResource("hidden"), HiddenAbility.CODEC)

        @JvmStatic
        fun <T : PotentialAbility> register(id: ResourceLocation, codec: MapCodec<T>): PotentialAbilityType<T> {
            return Registry.register(REGISTRY, id, PotentialAbilityType { codec })
        }

    }

}

/**
 * An ability on a species that may or may not be available for a specific instance of the species.
 *
 * Controls whether a Pokémon can learn an ability.
 *
 * @author Hiroku
 * @since July 27th, 2022
 */
interface PotentialAbility {
    val template: AbilityTemplate
    val priority: Priority
    val type: PotentialAbilityType<*>
    fun isSatisfiedBy(aspects: Set<String>): Boolean
    companion object {
        @JvmStatic
        val CODEC: Codec<PotentialAbility> = PotentialAbilityType.REGISTRY
            .byNameCodec()
            .dispatch(PotentialAbility::type) { it.codec() }
    }
}

