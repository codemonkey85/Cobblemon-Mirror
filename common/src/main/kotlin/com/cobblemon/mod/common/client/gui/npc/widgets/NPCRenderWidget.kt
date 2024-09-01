/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.npc.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawProfile
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.NPCModelRepository
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.resources.ResourceLocation

class NPCRenderWidget(
    val x: Int,
    val y: Int,
    var identifier: ResourceLocation,
    val aspects: MutableSet<String>
) : Renderable, GuiEventListener {
    val state = FloatingState()
    companion object {
        val profileBackgroundResource = cobblemonResource("textures/gui/npc/profile_background.png")
        const val WIDTH = 66
        const val HEIGHT = 66
    }

    override fun isFocused() = false
    override fun setFocused(focused: Boolean) {}
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            texture = profileBackgroundResource,
            x = x,
            y = y,
            width = WIDTH,
            height = HEIGHT
        )

        context.pose().pushPose()
        context.pose().translate(x + WIDTH / 2F, y + HEIGHT / 2F, 0F)

        drawProfile(
            repository = NPCModelRepository,
            resourceIdentifier = identifier,
            aspects = aspects,
            matrixStack = context.pose(),
            partialTicks = delta,
            scale = 30F,
            state = state
        )

        context.pose().popPose()
    }
}