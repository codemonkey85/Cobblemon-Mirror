/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.api.tags.RegistryBasedCondition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.Optional

/**
 * A [EntityQueryRequirement] for when a [Pokemon] is expected to be in a certain structure.
 *
 * @property structureCondition An optional [RegistryBasedCondition] if present must match.
 * @property structureAnticondition An optional [RegistryBasedCondition] if present must not match.
 * @author whatsy
 * @since December 5th, 2023
 */
class StructureRequirement(
    val structureCondition: Optional<RegistryBasedCondition<Structure>>,
    val structureAnticondition: Optional<RegistryBasedCondition<Structure>>,
) : EntityQueryRequirement {

    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean {
        val structures = queriedEntity.level().getChunk(queriedEntity.blockPosition()).allReferences
        val registry = queriedEntity.level().registryAccess().registryOrThrow(Registries.STRUCTURE)
        return structureCondition.map { condition -> structures.any { condition.fits(it.key, registry) }  }.orElse(true)
                && structureAnticondition.map { condition -> structures.none { condition.fits(it.key, registry) }  }.orElse(true)
    }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.STRUCTURE

    companion object {

        @JvmStatic
        val CODEC: MapCodec<StructureRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                RegistryBasedCondition.codec(Registries.STRUCTURE).optionalFieldOf("structureCondition")
                    .forGetter(StructureRequirement::structureCondition),
                RegistryBasedCondition.codec(Registries.STRUCTURE).optionalFieldOf("structureAnticondition")
                    .forGetter(StructureRequirement::structureAnticondition),
            ).apply(instance, ::StructureRequirement)
        }

    }

}