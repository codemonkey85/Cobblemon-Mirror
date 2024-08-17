/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

class LevelUpContext(val level: Int, val pokemon: Pokemon)

class LevelUpCriterion(
    playerCtx: Optional<ContextAwarePredicate>,
    val level: Int,
    val evolved: Boolean
): SimpleCriterionCondition<LevelUpContext>(playerCtx) {

    companion object {
        val CODEC: Codec<LevelUpCriterion> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LevelUpCriterion::playerCtx),
            Codec.INT.optionalFieldOf("level", 0).forGetter(LevelUpCriterion::level),
            Codec.BOOL.optionalFieldOf("evolved", true).forGetter(LevelUpCriterion::evolved)
        ).apply(it, ::LevelUpCriterion) }
    }

    override fun matches(player: ServerPlayer, context: LevelUpContext): Boolean {
        val preEvo = context.pokemon.preEvolution == null
        val hasEvolution = !context.pokemon.evolutions.none()
        var evolutionCheck = true
        if (preEvo || hasEvolution) {
            evolutionCheck = preEvo != hasEvolution
        }
        return level == context.level && evolutionCheck == evolved
    }

}

/*class LevelUpCriterionCondition(id: Identifier, entity: LootContextPredicate) : SimpleCriterionCondition<LevelUpContext>(id, entity) {
    var level = 0
    var evolved = true
    override fun toJson(json: JsonObject) {
        json.addProperty("level", level)
        json.addProperty("has_evolved", evolved)
    }

    override fun fromJson(json: JsonObject) {
        level = json.get("level")?.asInt ?: 0
        evolved = json.get("has_evolved")?.asBoolean ?: true
    }

    override fun matches(player: ServerPlayer, context: LevelUpContext): Boolean {
        val preEvo = context.pokemon.preEvolution == null
        val hasEvolution = !context.pokemon.evolutions.none()
        var evolutionCheck = true
        if (preEvo || hasEvolution) {
            evolutionCheck = !(preEvo == hasEvolution)
        }
        return level == context.level && evolutionCheck == evolved
    }
}*/