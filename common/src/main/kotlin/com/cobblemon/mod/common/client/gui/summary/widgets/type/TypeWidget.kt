/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.type

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component

abstract class TypeWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    pMessage: Component
): SoundlessWidget(pX, pY, pWidth, pHeight, pMessage) {

    companion object {
        val typeResource = cobblemonResource("textures/gui/types.png")
        private const val OFFSET = 0.5
    }

    fun renderType(type: ElementalType, pPoseStack: PoseStack, pX: Int = x, pY: Int = y) {
        blitk(
            matrixStack = pPoseStack,
            texture = typeResource,
            x = pX + OFFSET, y = pY,
            width = width, height = height,
            uOffset = width * type.textureXMultiplier.toFloat() + 0.1,
            textureWidth = width * 18
        )
    }

    fun renderType(mainType: ElementalType, secondaryType: ElementalType, pPoseStack: PoseStack) {
        renderType(secondaryType, pPoseStack, x + 16)
        renderType(mainType, pPoseStack)
    }
}