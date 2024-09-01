/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.rules.SpawnRule
import com.cobblemon.mod.common.api.spawning.rules.component.FilterRuleComponent
import com.cobblemon.mod.common.api.spawning.rules.component.LocationRuleCalculator
import com.cobblemon.mod.common.api.spawning.rules.component.SpawnRuleComponent
import com.cobblemon.mod.common.api.spawning.rules.component.WeightTweakRuleComponent
import com.cobblemon.mod.common.api.spawning.rules.selector.*
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

/**
 * All of the [SpawnRule]s that will be applied as global [SpawningInfluence]s.
 *
 * @author Hiroku
 * @since September 30th, 2023
 */
object CobblemonSpawnRules : JsonDataRegistry<SpawnRule> {
    override val gson = GsonBuilder()
        .registerTypeAdapter(SpawnRuleComponent::class.java, SpawnRuleComponentAdapter)
        .registerTypeAdapter(SpawnDetailSelector::class.java, SpawnDetailSelectorAdapter)
        .registerTypeAdapter(SpawningContextSelector::class.java, SpawningContextSelectorAdapter)
        .registerTypeAdapter(SpawningCondition::class.java, SpawningConditionAdapter)
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(Component::class.java, TextAdapter)
        .create()

    override val typeToken = TypeToken.get(SpawnRule::class.java)
    override val resourcePath = "spawn_rules"

    init {
        SpawnRuleComponent.register<WeightTweakRuleComponent>("weight")
        SpawnRuleComponent.register<FilterRuleComponent>("filter")
        SpawnRuleComponent.register<LocationRuleCalculator>("location")

        SpawnDetailSelector.register<ExpressionSpawnDetailSelector>("expression")

        SpawningContextSelector.register<ExpressionSpawningContextSelector>("expression")
        SpawningContextSelector.register<ConditionalSpawningContextSelector>("conditional")
    }

    val rules = mutableMapOf<ResourceLocation, SpawnRule>()

    override fun reload(data: Map<ResourceLocation, SpawnRule>) {
        rules.clear()
        rules.putAll(data)
        data.forEach { (id, value) -> value.id = id }
        observable.emit(this)
    }

    override val id: ResourceLocation = cobblemonResource("spawn_rules")
    override val type: PackType = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonSpawnRules>()

    override fun sync(player: ServerPlayer) {}
}