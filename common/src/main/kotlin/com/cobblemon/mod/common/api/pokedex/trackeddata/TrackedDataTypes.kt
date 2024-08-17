/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.trackeddata

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

//When serializing and deserializing we need to know what string goes to what codec and
//What class goes to what string
object TrackedDataTypes {
    val classToVariant = mutableMapOf<KClass<*>, ResourceLocation>()
    val variantToCodec = mutableMapOf<ResourceLocation, MapCodec<*>>()
    val variantToDecoder = mutableMapOf<ResourceLocation, (RegistryFriendlyByteBuf) -> GlobalTrackedData>()
    init {
        register(CountTypeCaughtGlobalTrackedData.ID, CountTypeCaughtGlobalTrackedData::class, CountTypeCaughtGlobalTrackedData.CODEC, CountTypeCaughtGlobalTrackedData::decode)
    }

    fun register(variant: ResourceLocation, classObj: KClass<*>, codec: MapCodec<*>, decoder: (RegistryFriendlyByteBuf) -> GlobalTrackedData) {
        classToVariant[classObj] = variant
        variantToCodec[variant] = codec
        variantToDecoder[variant] = decoder
    }
}