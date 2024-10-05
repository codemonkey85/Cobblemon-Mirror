/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.gui

import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_DIAMETER
import com.cobblemon.mod.common.client.render.SpriteType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.toHex
import com.mojang.authlib.minecraft.client.MinecraftClient
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix4f
import org.joml.Vector3f

@JvmOverloads
fun blitk(
    matrixStack: PoseStack,
    texture: ResourceLocation? = null,
    x: Number,
    y: Number,
    height: Number = 0,
    width: Number = 0,
    uOffset: Number = 0,
    vOffset: Number = 0,
    textureWidth: Number = width,
    textureHeight: Number = height,
    blitOffset: Number = 0,
    red: Number = 1,
    green: Number = 1,
    blue: Number = 1,
    alpha: Number = 1F,
    blend: Boolean = true,
    scale: Float = 1F
) {
    RenderSystem.setShader { GameRenderer.getPositionTexShader() }
    texture?.run { RenderSystem.setShaderTexture(0, this) }
    if (blend) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
    }
    RenderSystem.setShaderColor(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat())
    matrixStack.pushPose()
    matrixStack.scale(scale, scale, 1F)
    drawRectangle(
        matrixStack.last().pose(),
        x.toFloat(), y.toFloat(), x.toFloat() + width.toFloat(), y.toFloat() + height.toFloat(),
        blitOffset.toFloat(),
        uOffset.toFloat() / textureWidth.toFloat(), (uOffset.toFloat() + width.toFloat()) / textureWidth.toFloat(),
        vOffset.toFloat() / textureHeight.toFloat(), (vOffset.toFloat() + height.toFloat()) / textureHeight.toFloat()
    )
    matrixStack.popPose()
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
}

fun drawRectangle(
    matrix: Matrix4f,
    x: Float,
    y: Float,
    endX: Float,
    endY: Float,
    blitOffset: Float,
    minU: Float,
    maxU: Float,
    minV: Float,
    maxV: Float
) {
    val bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    bufferbuilder.addVertex(matrix, x, endY, blitOffset).setUv(minU, maxV)
    bufferbuilder.addVertex(matrix, endX, endY, blitOffset).setUv(maxU, maxV)
    bufferbuilder.addVertex(matrix, endX, y, blitOffset).setUv(maxU, minV)
    bufferbuilder.addVertex(matrix, x, y, blitOffset).setUv(minU, minV)
    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow())
}

@JvmOverloads
fun drawCenteredText(
    context: GuiGraphics,
    font: ResourceLocation? = null,
    text: Component,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true
) {
    val comp = (text as MutableComponent).let { if (font != null) it.font(font) else it }
    val textRenderer = Minecraft.getInstance().font
    context.drawString(textRenderer, comp, x.toInt() - textRenderer.width(comp) / 2, y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawText(
    context: GuiGraphics,
    font: ResourceLocation? = null,
    text: MutableComponent,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true,
    pMouseX: Number? = null,
    pMouseY: Number? = null
): Boolean {
    val comp = if (font == null) text else text.setStyle(text.style.withFont(font))
    val textRenderer = Minecraft.getInstance().font
    var x = x
    val width = textRenderer.width(comp)
    if (centered) {
        x = x.toDouble() - width / 2
    }
    context.drawString(textRenderer, comp, x.toInt(), y.toInt(), colour, shadow)
    var isHovered = false
    if (pMouseY != null && pMouseX != null) {
        if (pMouseX.toInt() >= x.toInt() && pMouseX.toInt() <= x.toInt() + width &&
            pMouseY.toInt() >= y.toInt() && pMouseY.toInt() <= y.toInt() + textRenderer.lineHeight
        ) {
            isHovered = true
        }
    }
    return isHovered
}

@JvmOverloads
fun drawTextJustifiedRight(
    context: GuiGraphics,
    font: ResourceLocation? = null,
    text: MutableComponent,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true
) {
    val comp = text.let { if (font != null) it.font(font) else it }
    val font = Minecraft.getInstance().font
    context.drawString(font, comp, x.toInt() - font.width(comp), y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawText(
    context: GuiGraphics,
    text: FormattedCharSequence,
    x: Number,
    y: Number,
    centered: Boolean = false,
    colour: Int,
    shadow: Boolean = true
) {
    val textRenderer = Minecraft.getInstance().font
    var tweakedX = x
    if (centered) {
        val width = textRenderer.width(text)
        tweakedX = tweakedX.toDouble() - width / 2
    }
    context.drawString(textRenderer, text, tweakedX.toInt(), y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawString(
    context: GuiGraphics,
    text: String,
    x: Number,
    y: Number,
    colour: Int,
    shadow: Boolean = true,
    font: ResourceLocation? = null
) {
    val comp = Component.literal(text).also {
        font?.run {
            it.toFlatList(it.style.withFont(this))
        }
    }
    val textRenderer = Minecraft.getInstance().font
    context.drawString(textRenderer, comp, x.toInt(), y.toInt(), colour, shadow)
}

@JvmOverloads
fun drawPosablePortrait(
    identifier: ResourceLocation,
    matrixStack: PoseStack,
    scale: Float = 13F,
    contextScale: Float = 1F,
    reversed: Boolean = false,
    state: PosableState,
    repository: VaryingModelRepository<*>,
    partialTicks: Float,
    limbSwing: Float = 0F,
    limbSwingAmount: Float = 0F,
    ageInTicks: Float = 0F,
    headYaw: Float = 0F,
    headPitch: Float = 0F,
    r: Float = 1F,
    g: Float = 1F,
    b: Float = 1F,
    a: Float = 1F
) {
    RenderSystem.applyModelViewMatrix()
    matrixStack.pushPose()
    matrixStack.translate(0.0, PORTRAIT_DIAMETER.toDouble() + 2.0, 0.0)
    matrixStack.scale(scale, scale, -scale)
    matrixStack.translate(0.0, -PORTRAIT_DIAMETER / 18.0, 0.0)

    val sprite = repository.getSprite(identifier, state, SpriteType.PORTRAIT);

    if (sprite == null) {
        val model = repository.getPoser(identifier, state)
        state.currentModel = model
        val texture = repository.getTexture(identifier, state)

        val context = RenderContext()
        model.context = context
        repository.getTextureNoSubstitute(identifier, state).let { context.put(RenderContext.TEXTURE, it) }
        context.put(RenderContext.SCALE, contextScale)
        context.put(RenderContext.SPECIES, identifier)
        context.put(RenderContext.ASPECTS, state.currentAspects)
        context.put(RenderContext.POSABLE_STATE, state)

        val renderType = RenderType.entityCutout(texture)

        val quaternion1 = Axis.YP.rotationDegrees(-32F * if (reversed) -1F else 1F)
        val quaternion2 = Axis.XP.rotationDegrees(5F)

        val originalPose = state.currentPose
        state.setPoseToFirstSuitable(PoseType.PORTRAIT)
        state.updatePartialTicks(partialTicks)
        model.applyAnimations(null, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch)
        originalPose?.let { state.setPose(it) }

        matrixStack.translate(
            model.portraitTranslation.x * if (reversed) -1F else 1F,
            model.portraitTranslation.y,
            model.portraitTranslation.z - 4
        )
        matrixStack.scale(model.portraitScale, model.portraitScale, 1 / model.portraitScale)
        matrixStack.mulPose(quaternion1)
        matrixStack.mulPose(quaternion2)

        val light1 = Vector3f(0.2F, 1.0F, -1.0F)
        val light2 = Vector3f(0.1F, 0.0F, 8.0F)
        RenderSystem.setShaderLights(light1, light2)
        quaternion1.conjugate()

        val immediate = Minecraft.getInstance().renderBuffers().bufferSource()
        val buffer = immediate.getBuffer(renderType)
        val packedLight = LightTexture.pack(11, 7)

        val colour = toHex(r, g, b, a)
        model.withLayerContext(immediate, state, repository.getLayers(identifier, state)) {
            model.render(context, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, colour)
            immediate.endBatch()
        }

        model.setDefault()

        Lighting.setupFor3DItems()
    } else {
        renderSprite(matrixStack, sprite)
    }

    matrixStack.popPose()
}

fun drawProfile(
    repository: VaryingModelRepository<*>,
    resourceIdentifier: ResourceLocation,
    matrixStack: PoseStack,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F
) {
    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    val sprite = repository.getSprite(resourceIdentifier, state, SpriteType.PROFILE)

    if (sprite == null) {

        val model = repository.getPoser(resourceIdentifier, state)
        val texture = repository.getTexture(resourceIdentifier, state)

        val context = RenderContext()
        model.context = context
        repository.getTextureNoSubstitute(resourceIdentifier, state).let { context.put(RenderContext.TEXTURE, it) }
        context.put(RenderContext.SCALE, 1F)
        context.put(RenderContext.SPECIES, resourceIdentifier)
        context.put(RenderContext.ASPECTS, state.currentAspects)
        context.put(RenderContext.POSABLE_STATE, state)
        state.currentModel = model

        val renderType = RenderType.entityCutout(texture)//model.getLayer(texture)

        state.setPoseToFirstSuitable(PoseType.PORTRAIT)
        state.updatePartialTicks(partialTicks)
        model.applyAnimations(null, state, 0F, 0F, 0F, 0F, 0F)
        matrixStack.translate(model.profileTranslation.x, model.profileTranslation.y, model.profileTranslation.z - 4.0)
        matrixStack.scale(model.profileScale, model.profileScale, 1 / model.profileScale)
//    matrixStack.multiply(rotation)
        val quaternion1 = Axis.YP.rotationDegrees(-32F * if (false) -1F else 1F)
        val quaternion2 = Axis.XP.rotationDegrees(5F)
        matrixStack.mulPose(quaternion1)
        matrixStack.mulPose(quaternion2)
        Lighting.setupForEntityInInventory()
        val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        entityRenderDispatcher.setRenderShadow(true)

        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        val buffer = bufferSource.getBuffer(renderType)
        val light1 = Vector3f(-1F, 1F, 1.0F)
        val light2 = Vector3f(1.3F, -1F, 1.0F)
        RenderSystem.setShaderLights(light1, light2)
        val packedLight = LightTexture.pack(11, 7)

        model.withLayerContext(bufferSource, state, repository.getLayers(resourceIdentifier, state)) {
            model.render(context, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)
            bufferSource.endBatch()
        }
        model.setDefault()
        entityRenderDispatcher.setRenderShadow(true)
        Lighting.setupFor3DItems()
    } else {
        renderSprite(matrixStack, sprite)
    }
}

fun renderSprite(matrixStack: PoseStack, sprite: ResourceLocation) {
    val matrix: PoseStack.Pose = matrixStack.last()
    matrix.pose().translate(-1f, 0f, 0f)

    RenderSystem.setShaderTexture(0, sprite);
    RenderSystem.setShader(GameRenderer::getPositionTexShader)

    var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)

    buffer.addVertex(matrix, 2f, 0f, 0.0f).setUv(1f, 0f)
    buffer.addVertex(matrix, 0f, 0f, 0.0f).setUv(0f, 0f)
    buffer.addVertex(matrix, 0f, 2f, 0.0f).setUv(0f, 1f)
    buffer.addVertex(matrix, 2f, 2f, 0.0f).setUv(1f, 1f)

    BufferUploader.drawWithShader(buffer.buildOrThrow())
}