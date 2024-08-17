/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.type

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
class SingleTypeWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    private val type: ElementalType,
    private val renderText: Boolean = true
) : TypeWidget(pX, pY, pWidth, pHeight, Component.literal("SingleTypeWidget - ${type.name}")) {

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()
        matrices.pushPose()
        matrices.translate(0.35, 0.0, 0.0)
        renderType(type, matrices)
        matrices.popPose()
        // Render Type Name
        if (this.renderText) {
            matrices.pushPose()
            drawScaledText(
                context = context,
                text = type.displayName,
                x = x + 35.5F, y = y + 3F,
                colour = ColourLibrary.WHITE, shadow = false,
                centered = true,
                maxCharacterWidth = 40,
                scale = 0.6F
            )
        }
    }
}