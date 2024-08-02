/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.Mth.ceil
import net.minecraft.util.Mth.floor
import net.minecraft.world.phys.Vec2
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class StatWidget(
    pX: Int, pY: Int,
    val pokemon: Pokemon,
    val tabIndex: Int = STATS
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Component.literal("StatWidget")) {

    companion object {
        // Stat Index
        private const val STATS = 0
        private const val IV = 1
        private const val EV = 2
        private const val BASE = 3
        private const val OTHER = 4

        private const val WIDTH = 134
        private const val HEIGHT = 148
        const val SCALE = 0.5F

        private const val WHITE = 0x00FFFFFF
        private const val GREY = 0x00AAAAAA
        private const val BLUE = 0x00548BFB
        private const val RED = 0x00FB5454

        private val statsBaseResource = cobblemonResource("textures/gui/summary/summary_stats_chart_base.png")
        private val statsChartResource = cobblemonResource("textures/gui/summary/summary_stats_chart.png")
        private val statsOtherBaseResource = cobblemonResource("textures/gui/summary/summary_stats_other_base.png")
        private val statsOtherBarTemplate = cobblemonResource("textures/gui/summary/summary_stats_other_bar.png")
        private val friendshipOverlayResource = cobblemonResource("textures/gui/summary/summary_stats_friendship_overlay.png")
        private val tabMarkerResource = cobblemonResource("textures/gui/summary/summary_stats_tab_marker.png")
        private val statIncreaseResource = cobblemonResource("textures/gui/summary/summary_stats_icon_increase.png")
        private val statDecreaseResource = cobblemonResource("textures/gui/summary/summary_stats_icon_decrease.png")

        private val statsLabel = lang("ui.stats")
        private val baseLabel = lang("ui.stats.base")
        private val ivLabel = lang("ui.stats.ivs")
        private val evLabel = lang("ui.stats.evs")
        private val otherLabel = lang("ui.stats.other")

        private val hpLabel = lang("ui.stats.hp")
        private val spAtkLabel = lang("ui.stats.sp_atk")
        private val atkLabel = lang("ui.stats.atk")
        private val spDefLabel = lang("ui.stats.sp_def")
        private val defLabel = lang("ui.stats.def")
        private val speedLabel = lang("ui.stats.speed")
    }

    var statTabIndex = tabIndex
    val renderableFeatures = SpeciesFeatures
        .getFeaturesFor(pokemon.species)
        .filterIsInstance<SynchronizedSpeciesFeatureProvider<*>>()
        .mapNotNull { it.getRenderer(pokemon) }

    private fun drawTriangle(
        colour: Vector3f,
        v1: Vec2,
        v2: Vec2,
        v3: Vec2
    ) {
        CobblemonResources.WHITE.let { RenderSystem.setShaderTexture(0, it) }
        RenderSystem.setShaderColor(colour.x, colour.y, colour.z, 0.6F)
        val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION)
        bufferBuilder.addVertex(v1.x, v1.y, 10F)
        bufferBuilder.addVertex(v2.x, v2.y, 10F)
        bufferBuilder.addVertex(v3.x, v3.y, 10F)
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow())
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
    }

    private fun drawStatHexagon(stats: Map<Stat, Int>, colour: Vector3f, maximum: Int) {
        val hexLeftX = x + 25.5
        val hexTopY = y + 22
        val hexAttackY = hexTopY + 24.5
        val hexDefenceY = hexAttackY + 47.0
        val hexBottomY = hexDefenceY + 24.5
        val hexRightX = x + 108.5
        val hexCenterX = (hexLeftX + hexRightX) / 2
        val hexCenterY = (hexTopY + hexBottomY) / 2
        val minTriangleSize = 8F
        val minXTriangleLen = sin(Math.toRadians(61.0)).toFloat() * minTriangleSize * 0.95F
        val minYTriangleLen = cos(Math.toRadians(60.0)).toFloat() * minTriangleSize

        val triangleLongEdge = (hexCenterY - hexTopY - minTriangleSize).toFloat()
        val triangleMediumEdge = (triangleLongEdge * sin(Math.toRadians(61.0))).toFloat()
        val triangleShortEdge = (triangleLongEdge * cos(Math.toRadians(61.0))).toFloat()

        val hpRatio = (stats.getOrDefault(Stats.HP, 0).toFloat() / maximum).coerceIn(0F, 1F)
        val atkRatio = (stats.getOrDefault(Stats.ATTACK, 0).toFloat() / maximum).coerceIn(0F, 1F)
        val defRatio = (stats.getOrDefault(Stats.DEFENCE, 0).toFloat() / maximum).coerceIn(0F, 1F)
        val spAtkRatio = (stats.getOrDefault(Stats.SPECIAL_ATTACK, 0).toFloat() / maximum).coerceIn(0F, 1F)
        val spDefRatio = (stats.getOrDefault(Stats.SPECIAL_DEFENCE, 0).toFloat() / maximum).coerceIn(0F, 1F)
        val spdRatio = (stats.getOrDefault(Stats.SPEED, 0).toFloat() / maximum).coerceIn(0F, 1F)

        val hpPoint = Vec2(
            hexCenterX.toFloat(),
            hexCenterY.toFloat() - minTriangleSize - hpRatio * triangleLongEdge
        )

        val attackPoint = Vec2(
            hexCenterX.toFloat() + minXTriangleLen + atkRatio * triangleMediumEdge,
            hexCenterY.toFloat() - minYTriangleLen - atkRatio * triangleShortEdge
        )

        val defencePoint = Vec2(
            hexCenterX.toFloat() + minXTriangleLen + defRatio * triangleMediumEdge,
            hexCenterY.toFloat() + minYTriangleLen + defRatio * triangleShortEdge
        )

        val specialAttackPoint = Vec2(
            hexCenterX.toFloat() - minXTriangleLen - spAtkRatio * triangleMediumEdge,
            hexCenterY.toFloat() - minYTriangleLen - spAtkRatio * triangleShortEdge
        )

        val specialDefencePoint = Vec2(
            hexCenterX.toFloat() - minXTriangleLen - spDefRatio * triangleMediumEdge,
            hexCenterY.toFloat() + minYTriangleLen + spDefRatio * triangleShortEdge
        )

        val speedPoint = Vec2(
            hexCenterX.toFloat(),
            hexCenterY.toFloat() + minTriangleSize + spdRatio * triangleLongEdge
        )

        val centerPoint = Vec2(
            hexCenterX.toFloat(),
            hexCenterY.toFloat()
        )

        // 1-o'clock
        drawTriangle(colour, hpPoint, centerPoint, attackPoint)
        // 3-o'clock
        drawTriangle(colour, attackPoint, centerPoint, defencePoint)
        // 5-o'clock
        drawTriangle(colour, defencePoint, centerPoint, speedPoint)
        // 7-o'clock
        drawTriangle(colour, speedPoint, centerPoint, specialDefencePoint)
        // 9-o'clock
        drawTriangle(colour, specialDefencePoint, centerPoint, specialAttackPoint)
        // 11-o'clock
        drawTriangle(colour, specialAttackPoint, centerPoint, hpPoint)


//        drawTriangle(colour, specialAttackPoint, centerPoint, hpPoint)
    }

    private fun drawFriendship(moduleX: Int, moduleY: Int, matrices: PoseStack, context: GuiGraphics, friendship: Int) {
        val barRatio = friendship / 255F
        val barWidth = ceil(barRatio * 108)

        blitk(
            matrixStack = matrices,
            texture = statsOtherBarTemplate,
            x = moduleX,
            y = moduleY,
            height = 28,
            width = 124
        )

        val red = 1
        val green: Number = if (pokemon.friendship >= 160) 0.28 else 0.56
        val blue: Number = if (pokemon.friendship >= 160) 0.4 else 0.64

        blitk(
            matrixStack = matrices,
            texture = CobblemonResources.WHITE,
            x = moduleX + 8,
            y = moduleY + 18,
            height = 8,
            width = barWidth,
            red = red,
            green = green,
            blue = blue
        )

        blitk(
            matrixStack = matrices,
            texture = friendshipOverlayResource,
            x = moduleX / SCALE,
            y = (moduleY + 16) / SCALE,
            height = 20,
            width = 248,
            scale = SCALE
        )

        // Label
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.stats.friendship").bold(),
            x = moduleX + 62,
            y = moduleY + 2.5,
            centered = true,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = friendship.toString().text(),
            x = moduleX + 11,
            y = moduleY + 6,
            scale = SCALE,
            centered = true
        )

        drawScaledText(
            context = context,
            text = "${floor(barRatio * 100)}%".text(),
            x = moduleX + 113,
            y = moduleY + 6,
            scale = SCALE,
            centered = true
        )
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val renderChart = statTabIndex != OTHER
        val matrices = context.pose()

        // Background
        blitk(
            matrixStack = matrices,
            texture = if (renderChart) statsBaseResource else statsOtherBaseResource,
            x= x,
            y = y,
            width = width,
            height = height
        )

        // Chart
        if (renderChart) {
            blitk(
                matrixStack = matrices,
                texture = statsChartResource,
                x= (x + 25.5) / SCALE,
                y = (y + 22) / SCALE,
                width = 166,
                height = 192,
                scale = SCALE
            )
        }

        when (statTabIndex) {
            STATS -> drawStatHexagon(
                mapOf(
                    Stats.HP to pokemon.hp,
                    Stats.ATTACK to pokemon.attack,
                    Stats.DEFENCE to pokemon.defence,
                    Stats.SPECIAL_ATTACK to pokemon.specialAttack,
                    Stats.SPECIAL_DEFENCE to pokemon.specialDefence,
                    Stats.SPEED to pokemon.speed
                ),
                colour = Vector3f(50F/255, 215F/255F, 1F),
                maximum = 400
            )
            BASE -> drawStatHexagon(
                pokemon.form.baseStats,
                colour = Vector3f(1F, 107F/255, 50F/255),
                maximum = 200
            )
            IV -> drawStatHexagon(
                pokemon.ivs.associate { it.key to it.value },
                colour = Vector3f(216F/255, 100F/255, 1F),
                maximum = 31
            )
            EV -> drawStatHexagon(
                pokemon.evs.associate { it.key to it.value },
                colour = Vector3f(1F, 1F, 100F/255),
                maximum = 252
            )
        }

        drawScaledText(
            context = context,
            text = statsLabel.bold(),
            x = x + 29,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == STATS) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            context = context,
            text = ivLabel.bold(),
            x = x + 48,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == IV) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            context = context,
            text = evLabel.bold(),
            x = x + 67,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == EV) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            context = context,
            text = baseLabel.bold(),
            x = x + 86,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == BASE) WHITE else GREY,
            centered = true
        )

        drawScaledText(
            context = context,
            text = otherLabel.bold(),
            x = x + 105,
            y = y + 143,
            scale = SCALE,
            colour = if (statTabIndex == OTHER) WHITE else GREY,
            centered = true
        )

        blitk(
            matrixStack = context.pose(),
            texture = tabMarkerResource,
            x= (x + 27 + (statTabIndex * 19)) / SCALE,
            y = (y + 140) / SCALE,
            width = 8,
            height = 4,
            scale = SCALE,
        )

        if (renderChart) {
            // Stat Labels
            renderTextAtVertices(
                context = context,
                hp = hpLabel.bold(),
                spAtk = spAtkLabel.bold(),
                atk = atkLabel.bold(),
                spDef = spDefLabel.bold(),
                def = defLabel.bold(),
                speed = speedLabel.bold()
            )

            // Stat Values
            renderTextAtVertices(
                context = context,
                offsetY = 5.5,
                enableColour = false,
                hp = getStatValueAsText(Stats.HP),
                spAtk = getStatValueAsText(Stats.SPECIAL_ATTACK),
                atk = getStatValueAsText(Stats.ATTACK),
                spDef = getStatValueAsText(Stats.SPECIAL_DEFENCE),
                def = getStatValueAsText(Stats.DEFENCE),
                speed = getStatValueAsText(Stats.SPEED)
            )

            // Nature-modified Stat Icons
            if (statTabIndex == STATS) {
                val nature = pokemon.effectiveNature
                renderModifiedStatIcon(matrices, nature.increasedStat, true)
                renderModifiedStatIcon(matrices, nature.decreasedStat, false)
            }
        } else {
            var drawY = y + 11

            drawFriendship(x + 5, drawY, matrices, context, pokemon.friendship)
            drawY += 30

            for (renderableFeature in renderableFeatures) {
                val rendered = renderableFeature.render(
                    drawContext = context,
                    x = x + 5F,
                    y = drawY.toFloat(),
                    pokemon = pokemon
                )

                if (rendered) {
                    drawY += 30
                }
            }

//            for (value in summaries) {
//                drawBarModule(x + 5, drawY, matrices, context, value)
//            }

        }
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        val index = getTabIndexFromPos(pMouseX, pMouseY)
        // Only play sound here as the rest of the widget is meant to be silent
        if (index in 0..4) {
            statTabIndex = index
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    private fun getStatValueAsText(stat: Stat): MutableComponent {
        val value = when(statTabIndex) {
            STATS -> if (stat == Stats.HP) "${pokemon.currentHealth} / ${pokemon.hp}" else pokemon.getStat(stat).toString()
            BASE -> pokemon.form.baseStats[stat].toString()
            IV -> pokemon.ivs.getOrDefault(stat).toString()
            EV -> pokemon.evs.getOrDefault(stat).toString()
            else -> "0"
        }
        return value.text()
    }

    private fun renderModifiedStatIcon(pPoseStack: PoseStack, stat: Stat?, increasedStat: Boolean) {
        if (stat != null) {
            var posX = x.toDouble()
            var posY = y.toDouble()

            when(stat) {
                Stats.HP -> { posX += 65; posY += 6 }
                Stats.SPECIAL_ATTACK -> { posX += 10; posY += 38 }
                Stats.ATTACK -> { posX += 120; posY += 38 }
                Stats.SPECIAL_DEFENCE -> { posX += 10; posY += 89 }
                Stats.DEFENCE -> { posX += 120; posY += 89 }
                Stats.SPEED -> { posX += 65; posY += 120 }
            }

            blitk(
                matrixStack = pPoseStack,
                texture = if (increasedStat) statIncreaseResource else statDecreaseResource,
                x= posX / SCALE,
                y = posY / SCALE,
                width = 8,
                height = 6,
                scale = SCALE,
            )
        }
    }

    private fun getModifiedStatColour(stat: Stat, enableColour: Boolean): Int {
        if (statTabIndex == STATS && enableColour) {
            val nature = pokemon.effectiveNature

            if (nature.increasedStat == stat) return RED
            if (nature.decreasedStat == stat) return BLUE
        }
        return WHITE
    }

    private fun renderTextAtVertices(
        context: GuiGraphics,
        offsetY: Double = 0.0,
        enableColour: Boolean = true,
        hp: MutableComponent,
        spAtk: MutableComponent,
        atk: MutableComponent,
        spDef: MutableComponent,
        def: MutableComponent,
        speed: MutableComponent
    ) {
        drawScaledText(
            context = context,
            text = hp,
            x = x + 67,
            y = y + 10.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.HP, enableColour),
            centered = true
        )

        drawScaledText(
            context = context,
            text = spAtk,
            x = x + 12,
            y = y + 42.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPECIAL_ATTACK, enableColour),
            centered = true
        )

        drawScaledText(
            context = context,
            text = atk,
            x = x + 122,
            y = y + 42.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.ATTACK,enableColour),
            centered = true
        )

        drawScaledText(
            context = context,
            text = spDef,
            x = x + 12,
            y = y + 93.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPECIAL_DEFENCE, enableColour),
            centered = true
        )

        drawScaledText(
            context = context,
            text = def,
            x = x + 122,
            y = y + 93.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.DEFENCE, enableColour),
            centered = true
        )

        drawScaledText(
            context = context,
            text = speed,
            x = x + 67,
            y = y + 124.5 + offsetY,
            scale = SCALE,
            colour = getModifiedStatColour(Stats.SPEED, enableColour),
            centered = true
        )
    }

    private fun getTabIndexFromPos(mouseX: Double, mouseY: Double): Int {
        val left = x + 19.5
        val top = y + 140.0
        if (mouseX in left..(left + 95.0) && mouseY in top..(top + 9.0)) {
            var startX = left
            var endX = left + 19
            for (index in 0..4) {
                if (mouseX in startX..endX) return index
                startX += 19
                endX += 19
            }
        }
        return -1
    }
}