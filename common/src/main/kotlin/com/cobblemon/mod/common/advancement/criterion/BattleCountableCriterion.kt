/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

open class BattleCountableContext(var battle : PokemonBattle, times : Int) : CountableContext(times)

class BattleCountableCriterion(
    playerCtx: Optional<ContextAwarePredicate>,
    val battleTypes: List<String>,
    count: Int
): CountableCriterion<BattleCountableContext>(playerCtx, count) {

    companion object {
        val CODEC: Codec<BattleCountableCriterion> = RecordCodecBuilder.create { it.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(BattleCountableCriterion::playerCtx),
            Codec.STRING.listOf().optionalFieldOf("battle_types", listOf("any")).forGetter(BattleCountableCriterion::battleTypes),
            Codec.INT.optionalFieldOf( "count", 0).forGetter(BattleCountableCriterion::count)
        ).apply(it, ::BattleCountableCriterion) }
    }

    override fun matches(player: ServerPlayer, context: BattleCountableContext): Boolean {
        var typeCheck = false
        val advancementData = Cobblemon.playerDataManager.getGenericData(player).advancementData
        if (battleTypes.isEmpty() || battleTypes.contains("any")) {
            typeCheck = true
        }

        if (battleTypes.contains("pvp")) {
            typeCheck = context.battle.isPvP
            context.times = advancementData.totalPvPBattleVictoryCount
        }

        if (battleTypes.contains("pvw")) {
            typeCheck = context.battle.isPvW
            context.times = advancementData.totalPvWBattleVictoryCount
        }

        if (battleTypes.contains("pvn")) {
            typeCheck = context.battle.isPvN
            context.times = advancementData.totalPvWBattleVictoryCount
        }

        if (battleTypes.size > 1) {
            context.times = advancementData.totalBattleVictoryCount
        }

        return typeCheck && super.matches(player, context)
    }

}

//open class BattleCountableCriterionTrigger(identifier: Identifier, criterionClass: Class<BattleCountableCriterion>) : SimpleCriterionTrigger<BattleCountableContext, BattleCountableCriterion>(identifier, criterionClass) {
//
//}
//
//class BattleCountableCriterion(id: Identifier, predicate: LootContextPredicate) : CountableCriterion<BattleCountableContext>(id, predicate) {
//
//    private var battleTypes = mutableListOf("any")
//
//    override fun fromJson(json: JsonObject) {
//        super.fromJson(json)
//        if(!json.get("battle_types").isJsonNull) {
//            battleTypes.clear()
//            json.get("battle_types").asJsonArray.asList().forEach() {
//                battleTypes.add(it.asString)
//            }
//        }
//    }
//
//    override fun toJson(json: JsonObject) {
//        super.toJson(json)
//        json.add("battle_types", battleTypes.toJsonArray())
//    }
//
//    override fun matches(player: ServerPlayer, context: BattleCountableContext): Boolean {
//        var typeCheck = false
//        val advancementData = Cobblemon.playerData.get(player).advancementData
//        if (battleTypes.isEmpty() || battleTypes.contains("any")) {
//            typeCheck = true
//        }
//        if (battleTypes.contains("pvp")) {
//            typeCheck = context.battle.isPvP
//            context.times = advancementData.totalPvPBattleVictoryCount
//        }
//        if (battleTypes.contains("pvw")) {
//            typeCheck = context.battle.isPvW
//            context.times = advancementData.totalPvWBattleVictoryCount
//        }
//        if (battleTypes.contains("pvn")) {
//            typeCheck = context.battle.isPvN
//            context.times = advancementData.totalPvWBattleVictoryCount
//        }
//        if (battleTypes.size > 1) {
//            context.times = advancementData.totalBattleVictoryCount
//        }
//        return context.times >= count && typeCheck
//    }
//}