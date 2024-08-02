/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.client.render.models.blockbench.TexturedModel
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.reflect.TypeToken
import net.minecraft.server.packs.PackType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

/**
 *  A repo to load our ONE egg model lmao
 */
object EggModelRepo : JsonDataRegistry<TexturedModel> {
    override val gson = TexturedModel.GSON
    override val typeToken = TypeToken.get(TexturedModel::class.java)
    override val resourcePath = "bedrock/egg_models"
    override val id = cobblemonResource("egg_models")
    override val type = PackType.CLIENT_RESOURCES
    override val observable = SimpleObservable<EggModelRepo>()
    val eggModels = hashMapOf<ResourceLocation, TexturedModel>()

    override fun reload(data: Map<ResourceLocation, TexturedModel>) {
        eggModels.putAll(data)
    }

    override fun sync(player: ServerPlayer) {
    }

}