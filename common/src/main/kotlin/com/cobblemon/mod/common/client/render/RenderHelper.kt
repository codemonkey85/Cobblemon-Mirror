/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.api.gui.drawText
import com.cobblemon.mod.common.api.gui.drawTextJustifiedRight
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.CobblemonResources
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

fun renderScaledGuiItemIcon(itemStack: ItemStack, x: Double, y: Double, scale: Double = 1.0, zTranslation: Float = 100.0F, matrixStack: PoseStack? = null) {
    val itemRenderer = Minecraft.getInstance().itemRenderer
    val textureManager = Minecraft.getInstance().textureManager
    val model = itemRenderer.getModel(itemStack, null, null, 0)

    textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false)
    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS)
    RenderSystem.enableBlend()
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    //TODO Make sure this doesnt break anything
    val modelViewStack = matrixStack ?: PoseStack()
    modelViewStack.pushPose()
    modelViewStack.translate(x, y, (zTranslation + 0).toDouble())
    modelViewStack.translate(8.0 * scale, 8.0 * scale, 0.0)
    modelViewStack.scale(1.0F, -1.0F, 1.0F)
    modelViewStack.scale(16.0F * scale.toFloat(), 16.0F * scale.toFloat(), 16.0F * scale.toFloat())
    RenderSystem.applyModelViewMatrix()

    val stack = matrixStack ?: PoseStack()
    val immediate = Minecraft.getInstance().renderBuffers().bufferSource()
    val bl = !model.usesBlockLight()
    if (bl) Lighting.setupForFlatItems()

    itemRenderer.render(
        itemStack,
        ItemDisplayContext.GUI,
        false,
        stack,
        immediate,
        15728880,
        OverlayTexture.NO_OVERLAY,
        model
    )

    immediate.endBatch()
    RenderSystem.enableDepthTest()
    if (bl) Lighting.setupFor3DItems()

    modelViewStack.popPose()
    RenderSystem.applyModelViewMatrix()
}

fun getDepletableRedGreen(
    ratio: Float,
    yellowRatio: Float = 0.5F,
    redRatio: Float = 0.2F
): Pair<Float, Float> {
    val m = -2

    val r = if (ratio > redRatio) {
        m * ratio - m
    } else {
        1.0
    }

    val g = if (ratio > yellowRatio) {
        1.0
    } else if (ratio > redRatio) {
        ratio * 1 / yellowRatio
    } else {
        0.0
    }

    return r.toFloat() to g.toFloat()
}


fun drawScaledText(
    context: GuiGraphics,
    font: ResourceLocation? = null,
    text: MutableComponent,
    x: Number,
    y: Number,
    scale: Float = 1F,
    opacity: Number = 1F,
    maxCharacterWidth: Int = Int.MAX_VALUE,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    centered: Boolean = false,
    shadow: Boolean = false,
    pMouseX: Int? = null,
    pMouseY: Int? = null
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }

    val textWidth = Minecraft.getInstance().font.width(if (font != null) text.font(font) else text)
    val extraScale = if (textWidth < maxCharacterWidth) 1F else (maxCharacterWidth / textWidth.toFloat())
    val fontHeight = if (font == null) 5F else 6F
    val matrices = context.pose()
    matrices.pushPose()
    matrices.scale(scale * extraScale, scale * extraScale, 1F)
    val isHovered = drawText(
        context = context,
        font = font,
        text = text,
        x = x.toFloat() / (scale * extraScale),
        y = y.toFloat() / (scale * extraScale) + (1F - extraScale) * fontHeight * scale,
        centered = centered,
        colour = colour,
        shadow = shadow,
        pMouseX = pMouseX?.toFloat()?.div((scale * extraScale)),
        pMouseY = pMouseY?.toFloat()?.div(scale * extraScale)?.plus((1F - extraScale) * fontHeight * scale)
    )
    matrices.popPose()
    // Draw tooltip that was created with onHover and is attached to the MutableComponent
    if (isHovered) {
        context.renderComponentHoverEffect(Minecraft.getInstance().font, text.style, pMouseX!!, pMouseY!!)
    }
}

fun drawScaledText(
    context: GuiGraphics,
    text: FormattedCharSequence,
    x: Number,
    y: Number,
    scaleX: Float = 1F,
    scaleY: Float = 1F,
    opacity: Number = 1F,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    centered: Boolean = false,
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val matrixStack = context.pose()
    matrixStack.pushPose()
    matrixStack.scale(scaleX, scaleY, 1F)
    drawText(
        context = context,
        text = text,
        x = x.toFloat() / scaleX,
        y = y.toFloat() / scaleY,
        centered = centered,
        colour = colour,
        shadow = shadow
    )
    matrixStack.popPose()
}

fun drawScaledTextJustifiedRight(
    context: GuiGraphics,
    font: ResourceLocation? = null,
    text: MutableComponent,
    x: Number,
    y: Number,
    scale: Float = 1F,
    opacity: Number = 1F,
    maxCharacterWidth: Int = Int.MAX_VALUE,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val textWidth = Minecraft.getInstance().font.width(if (font != null) text.font(font) else text)
    val extraScale = if (textWidth < maxCharacterWidth) 1F else (maxCharacterWidth / textWidth.toFloat())
    val fontHeight = if (font == null) 5F else 6F
    val matrixStack = context.pose()
    matrixStack.pushPose()
    matrixStack.scale(scale * extraScale, scale * extraScale, 1F)
    drawTextJustifiedRight(
        context = context,
        font = font,
        text = text,
        x = x.toFloat() / (scale * extraScale),
        y = y.toFloat() / (scale * extraScale) + (1F - extraScale) * fontHeight * scale,
        colour = colour,
        shadow = shadow
    )
    matrixStack.popPose()
}

fun drawScaledTextJustifiedRight(
    context: GuiGraphics,
    text: MutableComponent,
    x: Number,
    y: Number,
    scaleX: Float = 1F,
    scaleY: Float = 1F,
    opacity: Number = 1F,
    colour: Int = 0x00FFFFFF + ((opacity.toFloat() * 255).toInt() shl 24),
    shadow: Boolean = false
) {
    if (opacity.toFloat() < 0.05F) {
        return
    }
    val matrixStack = context.pose()
    matrixStack.pushPose()
    matrixStack.scale(scaleX, scaleY, 1F)
    drawTextJustifiedRight(
        context = context,
        text = text,
        x = x.toFloat() / scaleX,
        y = y.toFloat() / scaleY,
        colour = colour,
        shadow = shadow
    )
    matrixStack.popPose()
}

fun renderBeaconBeam(
    matrixStack: PoseStack,
    buffer: MultiBufferSource,
    textureLocation: ResourceLocation = CobblemonResources.PHASE_BEAM,
    partialTicks: Float,
    totalLevelTime: Long,
    yOffset: Float = 0F,
    height: Float,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    beamRadius: Float,
    glowRadius: Float,
    glowAlpha: Float
) {
    val i = yOffset + height
    val beamRotation = Math.floorMod(totalLevelTime, 40).toFloat() + partialTicks
    matrixStack.pushPose()
    matrixStack.mulPose(Axis.YP.rotationDegrees(beamRotation * 2.25f - 45.0f))
    var f9 = -beamRadius
    val f12 = -beamRadius
    renderPart(
        matrixStack,
        buffer.getBuffer(RenderType.beaconBeam(textureLocation, false)),
        red,
        green,
        blue,
        alpha,
        yOffset,
        i,
        0.0f,
        beamRadius,
        beamRadius,
        0.0f,
        f9,
        0.0f,
        0.0f,
        f12
    )
    // Undo the rotation so that the glow is at a rotated offset
    matrixStack.popPose()
    val f6 = -glowRadius
    val f7 = -glowRadius
    val f8 = -glowRadius
    f9 = -glowRadius
    renderPart(
        matrixStack,
        buffer.getBuffer(RenderType.beaconBeam(textureLocation, true)),
        red,
        green,
        blue,
        glowAlpha,
        yOffset,
        i,
        f6,
        f7,
        glowRadius,
        f8,
        f9,
        glowRadius,
        glowRadius,
        glowRadius
    )
}

fun renderPart(
    matrixStack: PoseStack,
    vertexBuffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    yMin: Float,
    yMax: Float,
    p_112164_: Float,
    p_112165_: Float,
    p_112166_: Float,
    p_112167_: Float,
    p_112168_: Float,
    p_112169_: Float,
    p_112170_: Float,
    p_112171_: Float
) {
    val pose = matrixStack.last()
    val matrix4f = pose.pose()
    val matrix3f = pose.normal()
    renderQuad(
        pose,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112164_,
        p_112165_,
        p_112166_,
        p_112167_
    )
    renderQuad(
        pose,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112170_,
        p_112171_,
        p_112168_,
        p_112169_
    )
    renderQuad(
        pose,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112166_,
        p_112167_,
        p_112170_,
        p_112171_
    )
    renderQuad(
        pose,
        vertexBuffer,
        red,
        green,
        blue,
        alpha,
        yMin,
        yMax,
        p_112168_,
        p_112169_,
        p_112164_,
        p_112165_
    )
}

fun renderQuad(
    matrixEntry: PoseStack.Pose,
    buffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    yMin: Float,
    yMax: Float,
    x1: Float,
    z1: Float,
    x2: Float,
    z2: Float
) {
    addVertex(matrixEntry, buffer, red, green, blue, alpha, yMax, x1, z1, 1F, 0F)
    addVertex(matrixEntry, buffer, red, green, blue, alpha, yMin, x1, z1, 1F, 1F)
    addVertex(matrixEntry, buffer, red, green, blue, alpha, yMin, x2, z2, 0F, 1F)
    addVertex(matrixEntry, buffer, red, green, blue, alpha, yMax, x2, z2, 0F, 0F)
}

fun addVertex(
    matrixEntry: PoseStack.Pose,
    buffer: VertexConsumer,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    y: Float,
    x: Float,
    z: Float,
    texU: Float,
    texV: Float
) {
    buffer
        .addVertex(matrixEntry.pose(), x, y, z)
        .setColor(red, green, blue, alpha)
        .setUv(texU, texV)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(15728880)
        .setNormal(matrixEntry, 0.0f, 1.0f, 0.0f)
}