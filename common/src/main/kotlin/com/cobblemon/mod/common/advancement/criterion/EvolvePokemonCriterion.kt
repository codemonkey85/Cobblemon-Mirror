/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import java.util.Optional

class EvolvePokemonContext(val species : ResourceLocation, val evolution : ResourceLocation, times: Int) : CountableContext(times)

class EvolvePokemonCriterion(
    playerCtx: Optional<ContextAwarePredicate>,
    val species: String,
    val evolution: String,
    count: Int
): CountableCriterion<EvolvePokemonContext>(playerCtx, count) {

    companion object {
        val CODEC: Codec<EvolvePokemonCriterion> = RecordCodecBuilder.create { it.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(EvolvePokemonCriterion::playerCtx),
            Codec.STRING.optionalFieldOf("species", "any").forGetter(EvolvePokemonCriterion::species),
            Codec.STRING.optionalFieldOf("evolution", "any").forGetter(EvolvePokemonCriterion::evolution),
            Codec.INT.optionalFieldOf("count", 0).forGetter(EvolvePokemonCriterion::count)
        ).apply(it, ::EvolvePokemonCriterion) }
    }

    override fun matches(player: ServerPlayer, context: EvolvePokemonContext): Boolean {
        return context.times >= count && (context.species == species.asIdentifierDefaultingNamespace() || species == "any") &&
                (context.evolution == evolution.asIdentifierDefaultingNamespace() || evolution == "any")
    }
}

//class EvolvePokemonCriterion(id: Identifier, entity: LootContextPredicate) : CountableCriterion<EvolvePokemonContext>(id, entity) {
//    var species = "any"
//    var evolution = "any"
//    override fun toJson(json: JsonObject) {
//        super.toJson(json)
//        json.addProperty("species", species)
//        json.addProperty("evolution", evolution)
//    }
//
//    override fun fromJson(json: JsonObject) {
//        super.fromJson(json)
//        species = json.get("species")?.asString ?: "any"
//        evolution = json.get("evolution")?.asString ?: "any"
//    }
//
//    override fun matches(player: ServerPlayer, context: EvolvePokemonContext): Boolean {
//        return context.times >= count && (context.species == species.asIdentifierDefaultingNamespace() || species == "any") &&
//                (context.evolution == evolution.asIdentifierDefaultingNamespace() || evolution == "any")
//    }
//}
//
//open class EvolvePokemonContext(val species : Identifier, val evolution : Identifier, times: Int) : CountableContext(times)