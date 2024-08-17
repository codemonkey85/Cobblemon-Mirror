/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.predicate

import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.EntitySubPredicate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FishingBobberPredicate(val inOpenWater: Boolean) : EntitySubPredicate {
    override fun codec(): MapCodec<FishingBobberPredicate> = CODEC
    override fun matches(entity: Entity, serverLevel: ServerLevel, vec3: Vec3?): Boolean {
        return (entity as? PokeRodFishingBobberEntity)?.inOpenWater == this.inOpenWater
    }
    companion object {
        @JvmStatic
        val CODEC: MapCodec<FishingBobberPredicate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("in_open_water").forGetter(FishingBobberPredicate::inOpenWater)
            ).apply(instance, ::FishingBobberPredicate)
        }
    }
}