/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

/**
 * Class representing a type of a Pokemon or Move
 *
 * @param name: The English name used to load / find it (spaces -> _)
 * @param displayName: A Component used to display the name, normally a TranslatableText
 * @param textureXMultiplier: The multiplier by which the TypeWidget shall move the display
 * @param resourceLocation: The location of the resource used in the TypeWidget
 */
class ElementalType(
    val name: String,
    val displayName: MutableComponent,
    val hue: Int,
    val textureXMultiplier: Int,
    val resourceLocation: ResourceLocation = ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, "ui/types.png")
) {

    companion object {
        @JvmStatic
        val BY_STRING_CODEC: Codec<ElementalType> = CodecUtils.createByStringCodec(
            ElementalTypes::get,
            ElementalType::name
        ) { id -> "No ElementalType for ID $id" }
    }

}