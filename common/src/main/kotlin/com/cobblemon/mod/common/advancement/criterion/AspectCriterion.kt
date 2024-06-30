/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import java.util.Optional

class AspectCriterion(
    playerCtx: Optional<LootContextPredicate>,
    val pokemon: Identifier,
    val aspects: List<String>
): SimpleCriterionCondition<MutableMap<Identifier, MutableSet<String>>>(playerCtx) {

    companion object {
        val CODEC: Codec<AspectCriterion> = RecordCodecBuilder.create { it.group(
            //All three of these codecs used to use Codecs.createStrictOptionalFieldCodec, that no longer exists
            LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(AspectCriterion::playerCtx),
            Identifier.CODEC.optionalFieldOf("pokemon", cobblemonResource("pikachu")).forGetter(AspectCriterion::pokemon),
            Codec.STRING.listOf().optionalFieldOf("aspects", listOf()).forGetter(AspectCriterion::aspects)
        ) .apply(it, ::AspectCriterion) }
    }

    override fun matches(player: ServerPlayerEntity, context: MutableMap<Identifier, MutableSet<String>>): Boolean {
        val caughtAspects = context.getOrDefault(pokemon, mutableSetOf())
        return this.aspects.all { it in caughtAspects }
    }
}

//open class AspectCriterionTrigger(identifier: Identifier, criterionClass: Class<AspectCriterionCondition>) : SimpleCriterionTrigger<MutableMap<Identifier, MutableSet<String>>, AspectCriterionCondition>(identifier, criterionClass) {
//
//}
//
//class AspectCriterionCondition(id: Identifier, predicate: LootContextPredicate) : SimpleCriterionCondition<MutableMap<Identifier, MutableSet<String>>>(id, predicate) {
//    var pokemon = Identifier.of("cobblemon:pikachu")
//    var aspects = mutableListOf<String>()
//    override fun toJson(json: JsonObject) {
//        json.add("aspects", JsonArray(aspects.size).also {
//            aspects.forEach { aspect -> it.add(aspect) }
//        })
//        json.addProperty("pokemon", pokemon.toString())
//    }
//
//    override fun fromJson(json: JsonObject) {
//        aspects.clear()
//        json.getAsJsonArray("aspects").forEach { element ->
//            aspects.add(element.asString)
//        }
//        pokemon = json.get("pokemon").asString.asIdentifierDefaultingNamespace()
//    }
//
//    override fun matches(player: ServerPlayerEntity, context: MutableMap<Identifier, MutableSet<String>>): Boolean {
//        val caughtAspects = context.getOrDefault(pokemon, mutableSetOf())
//        return this.aspects.all { it in caughtAspects }
//    }
//
//}