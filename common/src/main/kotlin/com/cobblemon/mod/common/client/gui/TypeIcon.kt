/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics

class TypeIcon(
    val x: Number,
    val y: Number,
    val type: ElementalType,
    val secondaryType: ElementalType? = null,
    val centeredX: Boolean = false,
    val small: Boolean = false,
    val secondaryOffset: Float = 15F,
    val doubleCenteredOffset: Float = 7.5F,
    val opacity: Float = 1F
) {
    companion object {
        private const val TYPE_ICON_DIAMETER = 36
        private const val SCALE = 0.5F

        private val typesResource = cobblemonResource("textures/gui/types.png")
        private val smallTypesResource = cobblemonResource("textures/gui/types_small.png")
    }

    fun render(context: GuiGraphics) {
        val diameter = if (small) (TYPE_ICON_DIAMETER / 2) else TYPE_ICON_DIAMETER
        val offsetX = if (centeredX) (((diameter / 2) * SCALE) + (if (secondaryType != null) (doubleCenteredOffset) else 0F)) else 0F;

        if (secondaryType != null) {
            blitk(
                matrixStack = context.pose(),
                texture = if (small) smallTypesResource else typesResource,
                x = (x.toFloat() + secondaryOffset - offsetX) / SCALE,
                y = y.toFloat() / SCALE,
                height = diameter,
                width = diameter,
                uOffset = diameter * secondaryType!!.textureXMultiplier.toFloat() + 0.1,
                textureWidth = diameter * 18,
                alpha = opacity,
                scale = SCALE
            )
        }

        blitk(
            matrixStack = context.pose(),
            texture = if (small) smallTypesResource else typesResource,
            x = (x.toFloat() - offsetX) / SCALE,
            y = y.toFloat() / SCALE,
            height = diameter,
            width = diameter,
            uOffset = diameter * type.textureXMultiplier.toFloat() + 0.1,
            textureWidth = diameter * 18,
            alpha = opacity,
            scale = SCALE
        )
    }
}