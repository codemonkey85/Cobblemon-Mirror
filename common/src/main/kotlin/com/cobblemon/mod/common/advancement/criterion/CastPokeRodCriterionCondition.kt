/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer

class CastPokeRodCriterionCondition(
    playerCtx: Optional<ContextAwarePredicate>,
    val hasBait: Boolean
): SimpleCriterionCondition<Boolean>(playerCtx) {
    companion object {
        val CODEC: Codec<CastPokeRodCriterionCondition> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CastPokeRodCriterionCondition::playerCtx),
            PrimitiveCodec.BOOL.fieldOf("hasBait").forGetter(CastPokeRodCriterionCondition::hasBait)
        ).apply(it, ::CastPokeRodCriterionCondition) }
    }

    override fun matches(player: ServerPlayer, context: Boolean): Boolean {
        return hasBait == context
    }
}