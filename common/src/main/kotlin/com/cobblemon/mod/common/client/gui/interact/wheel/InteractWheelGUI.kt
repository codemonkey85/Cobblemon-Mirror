/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.wheel

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.startselection.widgets.preview.ArrowButton
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.Multimap
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.max

class InteractWheelGUI(private val options: Multimap<Orientation, InteractWheelOption>, title: Component) : Screen(title) {
    companion object {
        const val SIZE = 138
        const val OPTION_SIZE = 69
        private val backgroundResource = cobblemonResource("textures/gui/interact/interact_base.png")
        private val buttonResources = mutableMapOf(
            Orientation.TOP_LEFT to cobblemonResource("textures/gui/interact/button_left_top.png"),
            Orientation.TOP_RIGHT to cobblemonResource("textures/gui/interact/button_right_top.png"),
            Orientation.BOTTOM_LEFT to cobblemonResource("textures/gui/interact/button_left_bottom.png"),
            Orientation.BOTTOM_RIGHT to cobblemonResource("textures/gui/interact/button_right_bottom.png"),
        )
    }

    private val buttons = mutableListOf<InteractWheelButton>()
    private var maxPage = 1
    private var currentPage = 0

    override fun init() {
        calculateMaxPage()
        addButton(Orientation.TOP_LEFT, options[Orientation.TOP_LEFT].toList().getOrNull(0))
        addButton(Orientation.TOP_RIGHT, options[Orientation.TOP_RIGHT].toList().getOrNull(0))
        addButton(Orientation.BOTTOM_LEFT, options[Orientation.BOTTOM_LEFT].toList().getOrNull(0))
        addButton(Orientation.BOTTOM_RIGHT, options[Orientation.BOTTOM_RIGHT].toList().getOrNull(0))
        if (maxPage > 1) {
            addRenderableWidget(ArrowButton(
                // x = left 3rd, y = center
                pX = (width / 3) - 12,
                pY = (height / 2) - 7,
                pWidth = 9,
                pHeight = 14,
                right = false,
                onPress = {
                    // loop to last page if on page 0, otherwise go to previous page
                    setPage(if (currentPage == 0) maxPage - 1 else currentPage - 1)
                }
            ))
            addRenderableWidget(ArrowButton(
                // x = right 3rd, y = center
                pX = (width / 3) * 2,
                pY = (height / 2) - 7,
                pWidth = 9,
                pHeight = 14,
                right = true,
                onPress = { setPage((currentPage + 1) % max(1, maxPage)) }
            ))
        }
    }

    private fun calculateMaxPage() {
        maxPage = max(
            max(
                options[Orientation.TOP_LEFT].size,
                options[Orientation.TOP_RIGHT].size
            ),
            max(
                options[Orientation.BOTTOM_LEFT].size,
                options[Orientation.BOTTOM_RIGHT].size
            )
        )
    }

    private fun setPage(page: Int) {
        currentPage = page
        buttons.forEach { removeWidget(it) }
        buttons.clear()
        val orientations = Orientation.values()
        orientations.forEach { orientation ->
            val option = options[orientation].toList().getOrNull(page)
            addButton(orientation, option)
        }
    }

    private fun addButton(orientation: Orientation, option: InteractWheelOption?) {
        val (x, y) = getButtonPosition(orientation)
        addRenderableWidget(InteractWheelButton(
            iconResource = option?.iconResource,
            buttonResource = buttonResources[orientation]!!,
            tooltipText = option?.tooltipText,
            x = x,
            y = y,
            isEnabled = option != null,
            colour = option?.colour ?: { null },
            onPress = { option?.onPress?.invoke() }
        ))
    }

    override fun <T> addRenderableWidget(drawableElement: T): T where T : GuiEventListener?, T : Renderable?, T : NarratableEntry? {
        if (drawableElement is InteractWheelButton) {
            buttons.add(drawableElement)
        }
        return super.addRenderableWidget(drawableElement)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val (x, y) = getDimensions()
        blitk(
            matrixStack = context.pose(),
            texture = backgroundResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE
        )
        super.render(context, mouseX, mouseY, delta)
    }

    private fun getDimensions(): Pair<Int, Int> {
        return Pair(
            (width - SIZE) / 2,
            (height - SIZE) / 2
        )
    }

    private fun getButtonPosition(orientation: Orientation): Pair<Int, Int> {
        val (x, y) = getDimensions()
        return when (orientation) {
            Orientation.TOP_LEFT -> Pair(x, y)
            Orientation.TOP_RIGHT -> Pair(x + OPTION_SIZE, y)
            Orientation.BOTTOM_LEFT -> Pair(x, y + OPTION_SIZE)
            Orientation.BOTTOM_RIGHT -> Pair(x + OPTION_SIZE, y + OPTION_SIZE)
        }
    }

    override fun isPauseScreen() = false

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseInCenter(mouseX, mouseY)) return false
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun isMouseInCenter(mouseX: Double, mouseY: Double): Boolean {
        val x = (((width - SIZE) / 2) + 44).toFloat()
        val xMax = x + 50
        val y = (((height - SIZE) / 2) + 44).toFloat()
        val yMax = y + 50
        return mouseX.toFloat() in x..xMax && mouseY.toFloat() in y..yMax
    }

}