/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import java.util.Optional

class TradePokemonContext(val traded: Pokemon, val received: Pokemon)

class TradePokemonCriterion(
    playerCtx: Optional<ContextAwarePredicate>,
    val traded: String,
    val received: String,
    val tradedHeldItem: String,
    val receivedHeldItem: String
): SimpleCriterionCondition<TradePokemonContext>(playerCtx) {

    companion object {
        val CODEC: Codec<TradePokemonCriterion> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TradePokemonCriterion::playerCtx),
            Codec.STRING.optionalFieldOf("traded", "any").forGetter(TradePokemonCriterion::traded),
            Codec.STRING.optionalFieldOf("received", "any").forGetter(TradePokemonCriterion::received),
            Codec.STRING.optionalFieldOf("traded_held_item", "minecraft:air").forGetter(TradePokemonCriterion::tradedHeldItem),
            Codec.STRING.optionalFieldOf("received_held_item", "minecraft:air").forGetter(TradePokemonCriterion::receivedHeldItem)
        ).apply(it, ::TradePokemonCriterion) }
    }

    override fun matches(player: ServerPlayer, context: TradePokemonContext): Boolean {
        val heldItem1 = context.traded.heldItem().item.builtInRegistryHolder().key().location()
        val heldItem2 = context.received.heldItem().item.builtInRegistryHolder().key().location()

        if (traded != "any" && context.traded.species.resourceIdentifier != traded.asIdentifierDefaultingNamespace()) {
            return false
        }

        if (received != "any" && context.received.species.resourceIdentifier != received.asIdentifierDefaultingNamespace()) {
            return false
        }

        if (heldItem1 != tradedHeldItem.asIdentifierDefaultingNamespace() && heldItem1 != "minecraft:air".asIdentifierDefaultingNamespace()) {
            return false
        }

        if (heldItem2 != receivedHeldItem.asIdentifierDefaultingNamespace() && heldItem2 != "minecraft:air".asIdentifierDefaultingNamespace()) {
            return false
        }

        return true
    }
}

/*class TradePokemonCriterionCondition(id: Identifier, entity: LootContextPredicate) : SimpleCriterionCondition<TradePokemonContext>(id, entity) {
    var traded = "any"
    var received = "any"
    var tradedHeldItem = "any"
    var receivedHeldItem = "any"
    override fun toJson(json: JsonObject) {
        json.addProperty("traded", traded)
        json.addProperty("received", received)
        json.addProperty("traded_held_item", tradedHeldItem)
        json.addProperty("received_held_item", receivedHeldItem)
    }

    override fun fromJson(json: JsonObject) {
        traded = json.get("traded")?.asString ?: "any"
        received = json.get("received")?.asString ?: "any"
        tradedHeldItem = json.get("traded_held_item")?.asString ?: "minecraft:air"
        receivedHeldItem = json.get("received_held_item")?.asString ?: "minecraft:air"
    }

    override fun matches(player: ServerPlayer, context: TradePokemonContext): Boolean {
        val heldItem1 = context.traded.heldItem().item.registryEntry.ResourceKey().value
        val heldItem2 = context.received.heldItem().item.registryEntry.ResourceKey().value
        return (context.traded.species.resourceIdentifier == traded.asIdentifierDefaultingNamespace() || traded == "any") &&
                (context.received.species.resourceIdentifier == received.asIdentifierDefaultingNamespace() || received == "any") &&
                (heldItem1 == tradedHeldItem.asIdentifierDefaultingNamespace() || heldItem1 == "minecraft:air".asIdentifierDefaultingNamespace()) &&
                (heldItem2 == receivedHeldItem.asIdentifierDefaultingNamespace() || heldItem2 == "minecraft:air".asIdentifierDefaultingNamespace())
    }
}

open class TradePokemonContext(val traded: Pokemon, val received: Pokemon)*/