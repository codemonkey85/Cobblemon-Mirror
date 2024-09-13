/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB

/**
 * A [EntityQueryRequirement] for when a [Pokemon] is expected to be in a certain area.
 *
 * @property box The [AABB] expected to be in.
 * @author Licious
 * @since March 21st, 2022
 */
class AreaRequirement(val box: AABB) : EntityQueryRequirement {

    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity) = this.box.contains(queriedEntity.position())

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.AREA

    companion object {
        @JvmStatic
        val CODEC: MapCodec<AreaRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecUtils.BOX.fieldOf("box").forGetter(AreaRequirement::box)
            ).apply(instance, ::AreaRequirement)
        }
    }

}