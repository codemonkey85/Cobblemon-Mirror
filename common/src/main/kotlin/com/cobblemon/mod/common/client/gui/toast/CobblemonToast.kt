/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.toast

import com.cobblemon.mod.common.net.messages.client.toast.ToastPacket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import java.util.*
import kotlin.math.min

@Environment(EnvType.CLIENT)
class CobblemonToast(
    val id: UUID,
    var icon: ItemStack,
    var title: Component,
    var description: Component,
    var frameTexture: ResourceLocation,
    var progress: Float,
    var progressColor: Int
) : Toast {

    constructor(packet: ToastPacket) : this(packet.uuid, packet.icon, packet.title, packet.description, packet.frameTexture, packet.progress, packet.progressColor)

    private var lastProgress = 0F
    private var lastTime = 0L
    internal var nextVisibility: Toast.Visibility = Toast.Visibility.SHOW

    override fun render(context: GuiGraphics, manager: ToastComponent, startTime: Long): Toast.Visibility {
        context.blitSprite(this.frameTexture, 0, 0, this.width(), this.height())
        val textRenderer = manager.minecraft.font
        context.drawString(textRenderer, this.title, 30, 7, this.title.style.color?.value ?: -1, false)
        context.drawString(textRenderer, this.description, 30, 18, this.description.style.color?.value ?: -1, false)
        context.renderFakeItem(this.icon, 8, 8)
        if (this.hasProgressBar()) {
            context.fill(3, 28, 157, 29, -1)
            val f = Mth.clampedLerp(this.lastProgress, this.progress, (startTime - this.lastTime).toFloat() / 100F)
            context.fill(3, 28, (3F + 154F * f).toInt(), 29, this.progressColor)
            this.lastProgress = f
        }
        this.lastTime = startTime
        return this.nextVisibility
    }

    internal fun updateFrom(packet: ToastPacket) {
        this.icon = packet.icon
        this.title = packet.title
        this.description = packet.description
        this.frameTexture = packet.frameTexture
        this.progress = packet.progress
        this.progressColor = packet.progressColor
        this.lastProgress = min(this.progress, this.lastProgress)
        this.nextVisibility = when (packet.behaviour) {
            ToastPacket.Behaviour.SHOW_OR_UPDATE -> Toast.Visibility.SHOW
            ToastPacket.Behaviour.HIDE -> Toast.Visibility.HIDE
        }
    }

    private fun hasProgressBar(): Boolean = this.progress in 0F..1F

}