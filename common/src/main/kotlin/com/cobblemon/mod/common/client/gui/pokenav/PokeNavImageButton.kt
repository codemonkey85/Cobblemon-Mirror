/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokenav

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.client.gui.GuiGraphics
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

open class PokeNavImageButton(
    val posX: Int, val posY: Int,
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    pXTexStart: Int, pYTexStart: Int, pYDiffText: Int,
    private val resourceLocation: ResourceLocation, pTextureWidth: Int, pTextureHeight: Int,
    onPress: OnPress,
    private val text: MutableComponent,
    private val canClick: () -> Boolean = { true }
    // TODO: Make lang key per button
): Button(pX, pY, pWidth, pHeight, "cobblemon.ui.pokenav.narrator.backbutton".asTranslated(), onPress, DEFAULT_NARRATION) {

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        // Render Button Image
        this.applyBlitk(context.pose(), pMouseX, pMouseY, pPartialTicks)
        // Draw Text
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = text.bold(),
            x = x + width / 2, y = y + height + 3,
            colour = ColourLibrary.WHITE, shadow = false,
            centered = true
        )
    }

    fun canClick() = this.canClick.invoke()

    override fun onPress() {
        if (this.canClick()) {
            super.onPress()
        }
    }

    override fun playDownSound(soundManager: SoundManager) {
        // Be silent if the button isn't clickable
        if (this.canClick()) {
            super.playDownSound(soundManager)
        }
    }

    protected open fun applyBlitk(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        blitk(
            matrixStack = pPoseStack,
            texture = resourceLocation,
            x = x, y = y + 0.25,
            width = width, height = height
        )
    }

}