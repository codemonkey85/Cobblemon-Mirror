/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startmenu

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.gui.drawCenteredText
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec2
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2

class OctagonGUI(private val options: List<OctagonGUIOption>, title: Component) : Screen(title) {
    companion object {
        const val CENTER_RADIUS = 30F
        const val OFFSET = CENTER_RADIUS + 50F
        const val ICON_SIZE = 30

        fun wrapDegrees(a: Double) : Double {
            return (a + 360) % 360
        }

        // TODO: Probably move this to a separate object
        fun createStartMenu() : OctagonGUI {
            val options: MutableList<OctagonGUIOption> = mutableListOf()

            for (i in 0..8) {
                options.add(
                    OctagonGUIOption(
                        "Button $i",
                        cobblemonResource("textures/item/held_items/light_ball.png")
                    ) {
                        Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Button $i clicked?"))
                    }
                )
            }

            return OctagonGUI(options, Component.literal("AAAAAA"))
        }
    }

    private var maxPage = 0
    private var currentPage = 0

    private var currentButton = 0

    override fun init() {
        // Experiments with adding a more button instead of arrows
//        val mutable = options.toMutableList()
//        var i = 8
//        while(i < options.size) {
//            mutable.add(i - 1, navigateOption)
//            i += 8
//            ++maxPage
//        }
//        options = mutable.toList()
        maxPage = (options.size - 1) / 8
    }

    override fun renderBlurredBackground(delta: Float) {}
    override fun renderMenuBackground(context: GuiGraphics) {}
    override fun isPauseScreen() = false

    override fun render(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
        val pose = guiGraphics.pose()
        val centerX = width / 2
        val centerY = height / 2
        val mouseLoc = getHovered(i.toFloat(), j.toFloat())
        if (mouseLoc == -1) currentButton = -1


        // Background
        // Using as a center radius reference for now
        blitk(
            pose,
            cobblemonResource("textures/gui/startmenu/test_octagon.png"),
            centerX - CENTER_RADIUS,
            centerY - CENTER_RADIUS,
            CENTER_RADIUS * 2,
            CENTER_RADIUS * 2
        )

        if(maxPage > 0) renderArrows(pose, i, centerX, centerY)

        val textWidthOffset = Minecraft.getInstance().font.lineHeight / 2
        var index = currentPage * 8
        run buttons@ {
            OctagonOrientation.entries.forEach {
                if(index >= options.size) return@buttons

                // Gets the normalized vector from enum and scale it
                val vec2 = it.vec2.scale(OFFSET)

                var colour = 0xFFFFFF
                if (mouseLoc == it.angle) {
                    // Handle hovered stuff here
                    colour = 0xFF0000
                    currentButton = index
                }
                options[index].let {
                    // Render button
                    blitk(
                        pose,
                        it.iconResource,
                        centerX + (vec2.x) - ICON_SIZE / 2,
                        centerY + (vec2.y) - ICON_SIZE / 2,
                        ICON_SIZE,
                        ICON_SIZE
                    )
                    // Text?
                    drawCenteredText(
                        context = guiGraphics,
                        text = Component.literal(it.name),
                        x = centerX + vec2.x,
                        y = centerY + vec2.y - textWidthOffset,
                        colour = colour,
                        shadow = true
                    )
                }
                index++
            }
        }
        super.render(guiGraphics, i, j, f)
    }

    private fun renderArrows(pose: PoseStack, mouseX: Int, centerX : Int, centerY: Int) {
        var leftHovered = false
        var rightHovered = false

        // TODO: Leaving a gap, see handleArrowsClicked
        if(mouseX.absoluteValue > 2 && mouseX.absoluteValue < CENTER_RADIUS - 2) {
            if(mouseX > 0) rightHovered = true
            else leftHovered = true
        }
        // TODO: Render hover effects

        val leftCanClick = currentPage > 0
        val rightCanClick = currentPage < maxPage

        // TODO: Gray out (or hide?) arrows

        // Left
        blitk(
            pose,
            cobblemonResource("textures/gui/starterselection/starterselection_arrow_left.png"),
            centerX - 15 - 9 / 2,
            centerY - 14 / 2,
            14,
            9
        )

        // Right
        blitk(
            pose,
            cobblemonResource("textures/gui/starterselection/starterselection_arrow_right.png"),
            centerX + 15 - 9 / 2,
            centerY - 14 / 2,
            14,
            9
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(currentButton == -1) {
            if (maxPage > 0) handleArrowsClicked(mouseX)
        } else if(currentButton < options.size) {
            options[currentButton].onPress()
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    /**
     * Get the region on the screen that the client's mouse is currently at.
     *
     * @return -1 if the mouse is within the [CENTER_RADIUS], else 0 to 7 representing
     * each octagon region, incrementing clockwise with 0 at the middle left.
     */
    private fun getHovered(mouseX: Float, mouseY: Float) : Int {
        val x = (mouseX - (width / 2))
        val y = (mouseY - (height / 2))
        var vector = Vec2(x, y)

        if(vector.length() < CENTER_RADIUS) return -1

        vector = vector.normalized()
        var angle = (atan2(vector.y, vector.x) / PI + 1) * 180 + 22.5
        angle = wrapDegrees(angle)
        angle /= 360 / 8
        return angle.toInt()
    }

    private fun handleArrowsClicked(mouseX: Double) {
        val centerX = width / 2
        val x = mouseX - centerX

        // TODO: Leaving a gap between the arrow buttons as well as the outer buttons
        if(x.absoluteValue > 2 && x.absoluteValue < CENTER_RADIUS - 2) {
            if(x > 0 && currentPage < maxPage) ++currentPage
            else if (x < 0 && currentPage > 0) --currentPage
        }
    }

    override fun keyPressed(i: Int, j: Int, k: Int): Boolean {
        if (super.keyPressed(i, j, k)) return true
        if (CobblemonKeyBinds.START_MENU.matches(i, j)) {
            this.onClose()
        }
        return true
    }

}