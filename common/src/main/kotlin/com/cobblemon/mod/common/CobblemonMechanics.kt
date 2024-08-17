/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.mechanics.BerriesMechanic
import com.cobblemon.mod.common.mechanics.PotionsMechanic
import com.cobblemon.mod.common.mechanics.RemediesMechanic
import com.cobblemon.mod.common.util.adapters.ExpressionAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager

object CobblemonMechanics : DataRegistry {
    override val id: ResourceLocation = cobblemonResource("mechanics")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonMechanics>()
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .create()

    var remedies = RemediesMechanic()
    var berries = BerriesMechanic()
    var potions = PotionsMechanic()

    override fun sync(player: ServerPlayer) {}
    override fun reload(manager: ResourceManager) {
        remedies = loadMechanic(manager, "remedies", RemediesMechanic::class.java)
        berries = loadMechanic(manager, "berries", BerriesMechanic::class.java)
        potions = loadMechanic(manager, "potions", PotionsMechanic::class.java)
    }

    private fun <T> loadMechanic(manager: ResourceManager, name: String, clazz: Class<T>): T {
        manager.getResourceOrThrow(cobblemonResource("mechanics/$name.json")).open().use {
            return gson.fromJson(it.reader(), clazz)
        }
    }
}