/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

open class CountableContext(var times: Int)

open class CountableCriterion<T : CountableContext>(
    playerCtx: Optional<ContextAwarePredicate>,
    val count: Int
): SimpleCriterionCondition<T>(playerCtx) {

    companion object {
        val CODEC: Codec<CountableCriterion<CountableContext>> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CountableCriterion<CountableContext>::playerCtx),
            Codec.INT.optionalFieldOf("count", 0).forGetter(CountableCriterion<CountableContext>::count)
        ).apply(it, ::CountableCriterion) }
    }

    override fun matches(player: ServerPlayer, context: T) = context.times >= count

}

///**
// * A type of advancement criterion condition that requires some number of completions. This can be extended to add
// * more conditions than just the count.
// *
// * @author Hiroku
// * @since November 4th, 2022
// */
//abstract class CountableCriterionCondition<T : CountableContext>(id: Identifier, predicate: LootContextPredicate) : SimpleCriterionCondition<T>(id, predicate) {
//    var count = 0
//    override fun fromJson(json: JsonObject) {
//        count = json.get("count")?.asInt ?: 0
//    }
//
//    override fun toJson(json: JsonObject) {
//        json.addProperty("count", count)
//    }
//
//    override fun matches(player: ServerPlayer, context: T) = context.times >= count
//}
//
///**
// * A concrete subclass of [CountableCriterionCondition] so that you can have advancements that really just need the
// * count and nothing else.
// *
// * This is just a quirk of using generics like this, don't worry about it. The criterion conditions that get used must
// * not be generic typed.
// *
// * @author Hiroku
// * @since November 4th, 2022
// */
//class SimpleCountableCriterionCondition(id: Identifier, predicate: LootContextPredicate) : CountableCriterionCondition<CountableContext>(id, predicate)
//fun SimpleCriterionTrigger<CountableContext, SimpleCountableCriterionCondition>.trigger(player: ServerPlayer, times: Int) = trigger(player, CountableContext(times))
//
///**
// * Some type of context that has a count associated with it, representing how many times the trigger has occurred.
// *
// * @author Hiroku
// * @since November 4th, 2022
// */
//open class CountableContext(var times: Int)