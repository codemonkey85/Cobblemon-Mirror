/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.biome.Biome

/**
 * A [EntityQueryRequirement] for when a [Pokemon] is expected to be in a certain [Biome].
 *
 * @property biomeCondition The [ResourceLocation] of the [Biome] the queried entity is expected to be in.
 * @author Licious
 * @since March 21st, 2022
 */
class BiomeRequirement : EntityQueryRequirement {
    val biomeCondition: RegistryLikeCondition<Biome>? = null
    val biomeAnticondition: RegistryLikeCondition<Biome>? = null
    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean {
        val biome = queriedEntity.level().getBiome(queriedEntity.blockPosition()).value()
        val registry = queriedEntity.level().registryAccess().registryOrThrow(Registries.BIOME)
        return (biomeCondition == null || biomeCondition.fits(biome, registry)) && (biomeAnticondition == null || !biomeAnticondition.fits(biome, registry))
    }

    companion object {
        const val ADAPTER_VARIANT = "biome"
    }
}