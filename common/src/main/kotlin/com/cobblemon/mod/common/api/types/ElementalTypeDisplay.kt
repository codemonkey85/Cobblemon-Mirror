/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ColorRGBA

data class ElementalTypeDisplay(
    val tint: ColorRGBA
) {

    companion object {
        @JvmStatic
        val CODEC: Codec<ElementalTypeDisplay> = RecordCodecBuilder.create { instance ->
            instance.group(
                ColorRGBA.CODEC.fieldOf("tint").forGetter(ElementalTypeDisplay::tint)
            ).apply(instance, ::ElementalTypeDisplay)
        }
    }

}