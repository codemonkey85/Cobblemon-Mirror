/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.pokedex

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokedex.PokedexLearnedInformation
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.SCALE
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.caughtIndicator
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.BASE_WIDTH
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth.clamp
import org.joml.Quaternionf
import kotlin.math.*

/**
 * Renders the UI for scanning a Pokémon
 *
 * @author Village
 */
class PokedexScannerRenderer {
    companion object {
        val SCAN_OVERLAY_NOTCH_WIDTH = 200
        val CENTER_INFO_FRAME_WIDTH = 128
        val CENTER_INFO_FRAME_HEIGHT = 16
        val OUTER_INFO_FRAME_WIDTH = 92
        val OUTER_INFO_FRAME_HEIGHT = 55
        val INNER_INFO_FRAME_WIDTH = 120
        val INNER_INFO_FRAME_HEIGHT = 20
        val INNER_INFO_FRAME_STEM_WIDTH = 28
        val UNKNOWN_MARK_WIDTH = 34
        val UNKNOWN_MARK_HEIGHT = 46
        val POINTER_WIDTH = 6
        val POINTER_HEIGHT = 10
        val POINTER_OFFSET = 30

        val SCAN_RING_MIDDLE_WIDTH = 100
        val SCAN_RING_MIDDLE_HEIGHT = 1

        val SCAN_RING_OUTER_DIAMETER = 116
        val SCAN_RING_INNER_DIAMETER = 84

        val SCAN_SCREEN = cobblemonResource("textures/gui/pokedex/pokedex_screen_scan.png")
        val SCAN_OVERLAY_CORNERS = cobblemonResource("textures/gui/pokedex/scan/overlay_corners.png")
        val SCAN_OVERLAY_TOP = cobblemonResource("textures/gui/pokedex/scan/overlay_border_top.png")
        val SCAN_OVERLAY_BOTTOM = cobblemonResource("textures/gui/pokedex/scan/overlay_border_bottom.png")
        val SCAN_OVERLAY_LEFT = cobblemonResource("textures/gui/pokedex/scan/overlay_border_left.png")
        val SCAN_OVERLAY_RIGHT = cobblemonResource("textures/gui/pokedex/scan/overlay_border_right.png")
        val SCAN_OVERLAY_LINES = cobblemonResource("textures/gui/pokedex/scan/overlay_scanlines.png")
        val SCAN_OVERLAY_NOTCH = cobblemonResource("textures/gui/pokedex/scan/overlay_notch.png")

        val SCAN_RING_OUTER = cobblemonResource("textures/gui/pokedex/scan/scan_ring_outer.png")
        val SCAN_RING_MIDDLE = cobblemonResource("textures/gui/pokedex/scan/scan_ring_middle.png")
        val SCAN_RING_INNER = cobblemonResource("textures/gui/pokedex/scan/scan_ring_inner.png")

        val CENTER_INFO_FRAME = cobblemonResource("textures/gui/pokedex/scan/scan_info_frame.png")
        val UNKNOWN_MARK = cobblemonResource("textures/gui/pokedex/scan/scan_unknown.png")
        val POINTER = cobblemonResource("textures/gui/pokedex/scan/pointer.png")

        fun infoFrameResource(isLeft: Boolean, tier: Int): ResourceLocation = cobblemonResource("textures/gui/pokedex/scan/scan_info_frame_${if (isLeft) "left" else "right"}_$tier.png")
    }

    fun renderInfoFrames(graphics: GuiGraphics, poseStack: PoseStack, usageContext: PokedexUsageContext, centerX: Int, centerY: Int, opacity: Float) {
        if (usageContext.focusIntervals > 0) {
            var infoDisplayedCounter = 0
            usageContext.availableInfoFrames.forEachIndexed {index, isLeftSide ->
                if (isLeftSide !== null) {
                    if (infoDisplayedCounter > 1 && !usageContext.isPokemonInFocusOwned) return@forEachIndexed
                    infoDisplayedCounter++
                    // Frames
                    val isInnerFrame = index == 1 || index == 2
                    val frameHeight = if (isInnerFrame) INNER_INFO_FRAME_HEIGHT else OUTER_INFO_FRAME_HEIGHT
                    val xOffset = (if (isInnerFrame) (-177) else (-120)) + (if (isLeftSide) 0 else (if (isInnerFrame) 234 else 148))
                    val yOffset = when(index) { 0 -> -80; 1 -> -26; 2 -> 6; 3 -> 25; else -> 0 }
                    blitk(
                        matrixStack = poseStack,
                        texture = infoFrameResource(isLeftSide, index),
                        x = centerX + xOffset,
                        y = centerY + yOffset,
                        width = if (!isInnerFrame) OUTER_INFO_FRAME_WIDTH else INNER_INFO_FRAME_WIDTH,
                        height = frameHeight,
                        textureHeight = frameHeight * 10,
                        vOffset = ceil(usageContext.focusIntervals) * frameHeight,
                        alpha = opacity
                    )

                    val xOffsetText = if (isInnerFrame) (((INNER_INFO_FRAME_WIDTH - INNER_INFO_FRAME_STEM_WIDTH) / 2) + if (isLeftSide) 0 else INNER_INFO_FRAME_STEM_WIDTH) else (OUTER_INFO_FRAME_WIDTH / 2)
                    val yOffsetText = when(index) { 0 -> 5; 1 -> 4; 2 -> 8; 3 -> 42; else -> 0 }

                    // Text
                    if (usageContext.focusIntervals == PokedexUsageContext.FOCUS_INTERVALS && usageContext.scannableEntityInFocus != null) {
                        val pokedexEntityData = usageContext.scannableEntityInFocus?.resolvePokemonScan() ?: return

                        if (infoDisplayedCounter == 1) {
                            drawScaledText(
                                context = graphics,
                                font = CobblemonResources.DEFAULT_LARGE,
                                text = lang("ui.lv.number", pokedexEntityData.level).bold(),
                                x = centerX + xOffset + xOffsetText,
                                y = centerY + yOffset + yOffsetText,
                                shadow = true,
                                centered = true
                            )
                        }

                        if (infoDisplayedCounter == 2) {
                            val hasTrainer = (usageContext.scannableEntityInFocus?.resolveEntityScan() as? PokemonEntity)?.ownerUUID !== null
                            val speciesName = pokedexEntityData.species.name.text().bold()
                            var yOffsetName = if (hasTrainer) 2 else 0
                            if (hasTrainer) {
                                drawScaledText(
                                    context = graphics,
                                    text = lang("ui.generic.trainer"),
                                    x = centerX + xOffset + xOffsetText,
                                    y = centerY + yOffset + yOffsetText - yOffsetName,
                                    shadow = true,
                                    centered = true,
                                    scale = 0.5F
                                )
                            }
                            drawScaledText(
                                context = graphics,
                                font = CobblemonResources.DEFAULT_LARGE,
                                text = speciesName,
                                x = centerX + xOffset + xOffsetText,
                                y = centerY + yOffset + yOffsetText + yOffsetName,
                                shadow = true,
                                centered = true
                            )

                            val gender = pokedexEntityData.gender
                            val speciesNameWidth = Minecraft.getInstance().font.width(speciesName.font(CobblemonResources.DEFAULT_LARGE))
                            if (gender != Gender.GENDERLESS) {
                                val isMale = gender == Gender.MALE
                                val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
                                drawScaledText(
                                    context = graphics,
                                    font = CobblemonResources.DEFAULT_LARGE,
                                    text = textSymbol,
                                    x = centerX + xOffset + xOffsetText + 2 + (speciesNameWidth / 2),
                                    y = centerY + yOffset + yOffsetText + yOffsetName,
                                    colour = if (isMale) 0x32CBFF else 0xFC5454,
                                    shadow = true
                                )
                            }

                            if (usageContext.isPokemonInFocusOwned) {
                                blitk(
                                    matrixStack = poseStack,
                                    texture = caughtIndicator,
                                    x = (centerX + xOffset + xOffsetText - 7 - (speciesNameWidth / 2)) / SCALE,
                                    y = (centerY + yOffset + yOffsetText + yOffsetName + 2) / SCALE,
                                    height = 10,
                                    width = 10,
                                    scale = SCALE
                                )
                            }
                        }

                        if (infoDisplayedCounter == 3) {
                            val typeText = lang("type.suffix", pokedexEntityData.form.types.map { it.displayName.copy() }.reduce { acc, next -> acc.plus("/").plus(next) })
                            val typeWidth = Minecraft.getInstance().font.width(typeText.font(CobblemonResources.DEFAULT_LARGE))

                            // Split into 2 lines if text width is too long
                            if (typeWidth > (OUTER_INFO_FRAME_WIDTH - 8) && pokedexEntityData.form.secondaryType !== null) {
                                drawScaledText(
                                    context = graphics,
                                    font = CobblemonResources.DEFAULT_LARGE,
                                    text = lang("type.suffix", pokedexEntityData.form.primaryType.displayName).bold(),
                                    x = centerX + xOffset + xOffsetText - 3,
                                    y = centerY + yOffset + yOffsetText,
                                    shadow = true,
                                    centered = true
                                )

                                drawScaledText(
                                    context = graphics,
                                    font = CobblemonResources.DEFAULT_LARGE,
                                    text = lang("type.suffix", pokedexEntityData.form.secondaryType!!.displayName).bold(),
                                    x = centerX + xOffset + xOffsetText + 3,
                                    y = centerY + yOffset + yOffsetText,
                                    shadow = true,
                                    centered = true
                                )
                            } else {
                                drawScaledText(
                                    context = graphics,
                                    font = CobblemonResources.DEFAULT_LARGE,
                                    text = typeText.bold(),
                                    x = centerX + xOffset + xOffsetText,
                                    y = centerY + yOffset + yOffsetText,
                                    shadow = true,
                                    centered = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun renderScanRings(poseStack: PoseStack, usageContext: PokedexUsageContext, centerX: Int, centerY: Int, opacity: Float) {
        val rotation = usageContext.usageIntervals % 360

        poseStack.pushPose()
        poseStack.translate(centerX.toFloat(), centerY.toFloat(), 0.0f)

        poseStack.pushPose()
        poseStack.mulPose(Quaternionf().rotateZ(Math.toRadians((-rotation) * 0.5).toFloat()))
        blitk(matrixStack = poseStack, texture = SCAN_RING_OUTER, x = -(SCAN_RING_OUTER_DIAMETER / 2), y = -(SCAN_RING_OUTER_DIAMETER / 2), width = SCAN_RING_OUTER_DIAMETER, height = SCAN_RING_OUTER_DIAMETER, alpha = opacity)
        poseStack.popPose()

        var progressOpacity = opacity
        var segments = 40
        if (usageContext.scanningProgress > 0) {
            if (usageContext.scanningProgress < 20) {
                progressOpacity -= usageContext.scanningProgress * 0.05F
            } else {
                if (progressOpacity != opacity) progressOpacity = opacity
                segments = floor((usageContext.scanningProgress - 20.0) / 2.0).toInt()
            }
        }

        for (i in 0 until segments) {
            val rotationQuaternion = Quaternionf().rotateZ(Math.toRadians((i * 4.5) + (rotation * 0.5)).toFloat())
            poseStack.pushPose()
            poseStack.mulPose(rotationQuaternion)
            blitk(matrixStack = poseStack, texture = SCAN_RING_MIDDLE, x = -(SCAN_RING_MIDDLE_WIDTH / 2), y = -(SCAN_RING_MIDDLE_HEIGHT.toFloat() / 2F), width = SCAN_RING_MIDDLE_WIDTH, height = SCAN_RING_MIDDLE_HEIGHT, alpha = progressOpacity)
            poseStack.popPose()
        }

        poseStack.pushPose()
        poseStack.mulPose(Quaternionf().rotateZ(Math.toRadians(-usageContext.innerRingRotation.toDouble()).toFloat()))
        blitk(matrixStack = poseStack, texture = SCAN_RING_INNER, x = -(SCAN_RING_INNER_DIAMETER / 2), y = -(SCAN_RING_INNER_DIAMETER / 2), width = SCAN_RING_INNER_DIAMETER, height = SCAN_RING_INNER_DIAMETER, alpha = opacity)
        poseStack.popPose()

        poseStack.popPose()
    }

    fun getRegisterText(info: PokedexLearnedInformation): MutableComponent {
        val type = when (info) {
            PokedexLearnedInformation.FORM -> lang("ui.pokedex.info.form")
            PokedexLearnedInformation.VARIATION -> lang("ui.pokedex.info.variation")
            else -> lang("ui.pokemon")
        }
        return lang("ui.pokedex.registered_suffix", type).bold()
    }

    fun renderScanOverlay(graphics: GuiGraphics, tickDelta: Float) {
        val client = Minecraft.getInstance()
        val matrices = graphics.pose()
        val usageContext = CobblemonClient.pokedexUsageContext

        val screenWidth = client.window.guiScaledWidth
        val screenHeight = client.window.guiScaledHeight

        RenderSystem.enableBlend()

        // Pokédex transition in/out animation
        val effectiveIntervals = clamp(usageContext.transitionIntervals + (if (usageContext.scanningGuiOpen) 1 else -1) * tickDelta, 0F, 12F)

        if (effectiveIntervals <= PokedexUsageContext.TRANSITION_INTERVALS) {
            val scale = 1 + (if (effectiveIntervals <= 2) 0F else ((effectiveIntervals - 2) * 0.075F))

            val centerX = (screenWidth - (BASE_WIDTH * scale)) / 2
            val centerY = (screenHeight - (BASE_HEIGHT * scale)) / 2

            val opacity = if (effectiveIntervals <= 2) 1F else (10F - (effectiveIntervals - 2F)) / 10F
            blitk(matrixStack = matrices, texture = CobblemonClient.pokedexUsageContext.type.getTexturePath(), x = centerX / scale, y = centerY / scale, width = BASE_WIDTH, height = BASE_HEIGHT, scale = scale, alpha = opacity)
            blitk(matrixStack = matrices, texture = SCAN_SCREEN, x = centerX / scale, y = centerY / scale, width = BASE_WIDTH, height = BASE_HEIGHT, scale = scale, alpha = opacity)
        }

        // Scanning overlay
        val opacity = if (effectiveIntervals >= 10) 1F else effectiveIntervals/10F
        // Draw scan lines
        val interlacePos = ceil((usageContext.usageIntervals % 14) * 0.5) * 0.5
        for (i in 0 until screenHeight) {
            if (i % 4 == 0) blitk(matrixStack = matrices, texture = SCAN_OVERLAY_LINES, x = 0, y = i - interlacePos, width = screenWidth, height = 4, alpha = opacity)
        }

        // Draw borders
        // Top left corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = 0, y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, alpha = opacity)
        // Top right corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = (screenWidth - 4), y = 0, width = 4, height = 4, textureWidth = 8, textureHeight = 8, uOffset = 4, alpha = opacity)
        // Bottom left corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = 0, y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, alpha = opacity)
        // Bottom right corner
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_CORNERS, x = (screenWidth - 4), y = (screenHeight - 4), width = 4, height = 4, textureWidth = 8, textureHeight = 8, vOffset = 4, uOffset = 4, alpha = opacity)

        // Border sides
        val notchStartX = (screenWidth - SCAN_OVERLAY_NOTCH_WIDTH) / 2
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_TOP, x = 4, y = 0, width = notchStartX - 4, height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_TOP, x = notchStartX + SCAN_OVERLAY_NOTCH_WIDTH, y = 0, width = (screenWidth - (notchStartX + SCAN_OVERLAY_NOTCH_WIDTH + 4)), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_BOTTOM, x = 4, y = (screenHeight - 3), width = (screenWidth - 8), height = 3, alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_LEFT, x = 0, y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_RIGHT, x = (screenWidth - 3), y = 4, width = 3, height = (screenHeight - 8), alpha = opacity)
        blitk(matrixStack = matrices, texture = SCAN_OVERLAY_NOTCH, x = notchStartX, y = 0, width = SCAN_OVERLAY_NOTCH_WIDTH, height = 12, alpha = opacity)

        val centerX = screenWidth / 2
        val centerY = screenHeight / 2

        // Scan info frame
        renderInfoFrames(graphics, matrices, usageContext, centerX, centerY, opacity)

        // Scan rings
        renderScanRings(matrices, usageContext, centerX, centerY, opacity)

        if (usageContext.displayRegisterInfoIntervals > 0) {
            blitk(
                matrixStack = matrices,
                texture = CENTER_INFO_FRAME,
                x = centerX - (CENTER_INFO_FRAME_WIDTH / 2),
                y = centerY - (CENTER_INFO_FRAME_HEIGHT / 2),
                width = CENTER_INFO_FRAME_WIDTH,
                height = CENTER_INFO_FRAME_HEIGHT,
                textureHeight = CENTER_INFO_FRAME_HEIGHT * 6,
                vOffset = min(ceil(usageContext.displayRegisterInfoIntervals), PokedexUsageContext.CENTER_INFO_DISPLAY_INTERVALS) * CENTER_INFO_FRAME_HEIGHT,
                alpha = opacity
            )

            if (usageContext.displayRegisterInfoIntervals >= PokedexUsageContext.CENTER_INFO_DISPLAY_INTERVALS) {
                drawScaledText(
                    context = graphics,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = getRegisterText(usageContext.newPokemonInfo),
                    x = centerX,
                    y = centerY - (CENTER_INFO_FRAME_HEIGHT / 2) + 4,
                    shadow = true,
                    centered = true
                )
            }
        } else {
            if (usageContext.scanningProgress > 0) {
                // If scan progress reaches max ticks - 10,decrement opacity using last 10 ticks
                // else increment opacity using scan progress
                var centerOpacity = (
                        if (usageContext.scanningProgress > (PokedexUsageContext.MAX_SCAN_PROGRESS - 10))
                            (PokedexUsageContext.MAX_SCAN_PROGRESS - usageContext.scanningProgress)
                        else usageContext.scanningProgress
                        ) * 0.1F

                blitk(
                    matrixStack = matrices,
                    texture = UNKNOWN_MARK,
                    x = centerX - (UNKNOWN_MARK_WIDTH / 2),
                    y = centerY - (UNKNOWN_MARK_HEIGHT / 2) + 2,
                    width = UNKNOWN_MARK_WIDTH,
                    height = UNKNOWN_MARK_HEIGHT,
                    alpha = max(0F, min(opacity, centerOpacity))
                )

                matrices.pushPose()
                matrices.translate(centerX.toFloat(), centerY.toFloat(), 0.0f)

                matrices.pushPose()
                matrices.mulPose(Quaternionf().rotateZ(Math.toRadians(usageContext.innerRingRotation * 0.5).toFloat()))
                blitk(
                    matrixStack = matrices,
                    texture = POINTER,
                    x = -POINTER_WIDTH - POINTER_OFFSET,
                    y = -(POINTER_HEIGHT / 2),
                    width = POINTER_WIDTH,
                    height = POINTER_HEIGHT,
                    textureWidth = POINTER_WIDTH * 2,
                    alpha = max(0F, min(opacity, centerOpacity))
                )

                blitk(
                    matrixStack = matrices,
                    texture = POINTER,
                    x = POINTER_OFFSET,
                    y = -(POINTER_HEIGHT / 2),
                    width = POINTER_WIDTH,
                    height = POINTER_HEIGHT,
                    textureWidth = POINTER_WIDTH * 2,
                    uOffset = POINTER_WIDTH,
                    alpha = max(0F, min(opacity, centerOpacity))
                )
                matrices.popPose()
                matrices.popPose()
            } else if (usageContext.viewInfoTicks > 0) {
                val pointerOpacity = usageContext.viewInfoTicks * 0.1F

                blitk(
                    matrixStack = matrices,
                    texture = POINTER,
                    x = centerX - POINTER_WIDTH - POINTER_OFFSET + usageContext.viewInfoTicks,
                    y = centerY - (POINTER_HEIGHT / 2),
                    width = POINTER_WIDTH,
                    height = POINTER_HEIGHT,
                    textureWidth = POINTER_WIDTH * 2,
                    alpha = max(0F, min(opacity, pointerOpacity))
                )

                blitk(
                    matrixStack = matrices,
                    texture = POINTER,
                    x = centerX + POINTER_OFFSET - usageContext.viewInfoTicks,
                    y = centerY - (POINTER_HEIGHT / 2),
                    width = POINTER_WIDTH,
                    height = POINTER_HEIGHT,
                    textureWidth = POINTER_WIDTH * 2,
                    uOffset = POINTER_WIDTH,
                    alpha = max(0F, min(opacity, pointerOpacity))
                )
            }
        }

        RenderSystem.disableBlend()
    }

    fun onRenderOverlay(graphics: GuiGraphics, tickCounter: DeltaTracker) {
        if ((CobblemonClient.pokedexUsageContext.scanningGuiOpen || CobblemonClient.pokedexUsageContext.transitionIntervals > 0) && Minecraft.getInstance().options.cameraType.isFirstPerson) {
            val tickDelta = tickCounter.realtimeDeltaTicks
            renderScanOverlay(graphics, tickDelta)
        }
    }
}