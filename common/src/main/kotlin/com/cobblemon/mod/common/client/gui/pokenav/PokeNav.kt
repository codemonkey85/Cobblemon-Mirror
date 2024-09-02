/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokenav

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PokeNavigatorBinding
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class PokeNav : Screen(Component.translatable("cobblemon.ui.pokenav.title")) {

    companion object {
        // Limiting
        private const val MAX_BUTTONS_PER_ROW = 3
        private const val MAX_BUTTONS_PER_COLUMN = 2
        // Spacing between Button and Border / other Buttons
        private const val HORIZONTAL_SPACING = 8
        private const val VERTICAL_SPACING = 26
        // Size of Background
        private const val backgroundHeight = 125
        private const val backgroundWidth = 218
        // Size of Buttons
        private const val buttonHeight = 39
        private const val buttonWidth = 64
        // Textures
        private val background = cobblemonResource("textures/gui/pokenav/pokenav_base.png")
        private val exit = cobblemonResource("textures/gui/pokenav/pokenav_exit.png")
        private val pokemon = cobblemonResource("textures/gui/pokenav/pokenav_pokemon.png")
        private val select = cobblemonResource("textures/gui/pokenav/pokenav_select.png")
    }

    /**
     * Changed by Licious April 29th 2022
     * Seemed to make more sense for our use case.
     */
    private val buttons: Table<Int, Int, PokeNavImageButton> = HashBasedTable.create()
    private var currentSelectionPos = 0 to 0
    // ^ always start at the first entry
    private var aboutToClose = false
    // Start waiting for key movements until the mouse is moved for the first time
    private var focusWithKey = true

    override fun init() {
        this.buttons.clear()
        // Pokemon Button
        this.insertButton(pokemon, this::onPressPokemon, lang("ui.pokemon")) { CobblemonClient.storage.myParty.slots.filterNotNull().isNotEmpty() }

        // EXIT Button
        this.insertButton(exit, this::onPressExit, lang("ui.exit"))

        this.buttons.values().forEach { button ->
            addRenderableWidget(button)
            if (!button.canClick())
                addRenderableWidget(this.fillerButtonOf(button.posX, button.posY))
        }

        super.init()
    }

    /**
     * Changed by Licious April 29th 2022
     * Cleaned up the original code see [moveSelected] for how the selection moves around.
     */
    override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        val movement: Pair<Int, Int> = when (pKeyCode) {
            InputConstants.KEY_RIGHT, InputConstants.KEY_D -> 1 to 0
            InputConstants.KEY_LEFT, InputConstants.KEY_A -> -1 to 0
            InputConstants.KEY_UP, InputConstants.KEY_W -> 0 to -1
            InputConstants.KEY_DOWN, InputConstants.KEY_S -> 0 to 1
            InputConstants.KEY_SPACE -> {
                val button = this.buttons.get(currentSelectionPos.first, currentSelectionPos.second)
                button?.playDownSound(Minecraft.getInstance().soundManager)
                button?.onPress()
                0 to 0
            }
            PokeNavigatorBinding.boundKey().value, InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> {
                this.aboutToClose = true
                0 to 0
            }
            else -> 0 to 0
        }
        this.moveSelected(movement.first, movement.second)
        return super.keyPressed(pKeyCode, pScanCode, pModifiers)
    }

    override fun keyReleased(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        if ((pKeyCode == PokeNavigatorBinding.boundKey().value || pKeyCode == InputConstants.KEY_LSHIFT || pKeyCode == InputConstants.KEY_RSHIFT) && aboutToClose) {
            Minecraft.getInstance().setScreen(null) // So we only close if the Key was released
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers)
    }

    /**
     * What should happen on Button press - END
     */

    /**
     * Rendering the background texture
     */
    override fun render(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        renderBackground(context, pMouseX, pMouseY, pPartialTicks)

        // Rendering UI Background
        blitk(
            matrixStack = context.pose(),
            texture = background,
            x = (width - backgroundWidth) / 2, y = (height - backgroundHeight) / 2,
            width = backgroundWidth, height = backgroundHeight
        )

        super.render(context, pMouseX, pMouseY, pPartialTicks)

        /**
         * Rendering Selection
         *
         * Changed by Licious April 29th 2022
         * Get selected button and gray out the selection if the button can't be clicked
         */
        val selectedButton = this.buttons.get(currentSelectionPos.first, currentSelectionPos.second) ?: return
        if (!this.focusWithKey) {
            for (button in this.buttons.values()) {
                if (button.isHovered) {
                    this.currentSelectionPos = button.posX to button.posY
                    break
                }
            }
        }
        blitk(
            matrixStack = context.pose(),
            texture = select,
            x = getWidthForPos(currentSelectionPos.first) + 2.55, y = getHeightFor(currentSelectionPos.second) + 2.45,
            width = 59, height = 34.5,
            red = if (selectedButton.canClick()) 1 else PokeNavFillerButton.RED,
            green = if (selectedButton.canClick()) 1 else PokeNavFillerButton.GREEN,
            blue = if (selectedButton.canClick()) 1 else PokeNavFillerButton.BLUE,
            alpha = if (selectedButton.canClick()) 1 else PokeNavFillerButton.ALPHA,
        )
    }

    override fun afterMouseMove() {
        this.focusWithKey = false
        super.afterMouseMove()
    }

    override fun isPauseScreen() = true

    /**
     * Moves the selection with the given params.
     *
     * @param x How many slots to move in the X axis.
     * @param y How many slots to move in the Y axis.
     *
     * @author Licious
     * @since April 29th, 2022
     */
    private fun moveSelected(x: Int, y: Int) {
        // No op necessary
        if (x == 0 && y == 0) {
            return
        }
        this.focusWithKey = true
        val currentX = this.currentSelectionPos.first
        val currentY = this.currentSelectionPos.second
        val maxColumn = this.currentMaxColumn()
        val maxRow = this.currentMaxRow()
        var newX = currentX + x
        var newY = currentY + y
        // Handle special movement between rows and columns. Any movements that could result in being out bounds are ignored so no need to check those.
        when {
            // Moving downwards on last row, go to first row.
            newY > maxRow -> newY = 0
            // Moving upwards on first row, go to last row
            newY < 0 -> newY = maxRow
            // Final column but there's a next row, we move down 1 and start back at 0
            newX > maxColumn && newY < this.maxRowAt(0) && this.maxColumnAt(newY + 1) >= 0 -> {
                newX = 0
                newY++
            }
            // Final column but no next row, we move all the way back to the top
            newX > maxColumn -> {
                newX = 0
                newY = 0
            }
            // First column but there's a previous row, we move to the max possible column in the previous row.
            newX < 0 && newY > 0 -> newX = this.maxColumnAt(--newY)
        }
        // If there's no button at the new coordinates we don't move
        if (this.buttonExists(newX, newY)) {
            this.currentSelectionPos = newX to newY
        }
    }

    private fun buttonExists(x: Int, y: Int) = this.buttons.get(x, y) != null

    /**
     * Finds the highest column N possible on the current row.
     *
     * @return The highest column.
     */
    private fun currentMaxColumn(): Int {
        val y = this.currentSelectionPos.second
        return this.maxColumnAt(y)
    }

    /**
     * Finds the highest column N possible on the given row.
     *
     * @return The highest column.
     */
    private fun maxColumnAt(y: Int): Int {
        for (x in MAX_BUTTONS_PER_ROW downTo 0) {
            if (this.buttonExists(x, y)) {
                return x
            }
        }
        throw IllegalStateException("No buttons exist")
    }

    /**
     * Finds the highest row N possible on the current column.
     *
     * @return The highest row.
     */
    private fun currentMaxRow(): Int {
        val x = this.currentSelectionPos.first
        return this.maxRowAt(x)
    }

    /**
     * Finds the highest row N possible on the given column.
     *
     * @return The highest row.
     */
    private fun maxRowAt(x: Int): Int {
        for (y in MAX_BUTTONS_PER_COLUMN downTo 0) {
            if (this.buttonExists(x, y)) {
                return y
            }
        }
        throw IllegalStateException("No buttons exist")
    }

    /**
     * Method for calculating the width based on the background, spacing and button position
     */
    private fun getWidthForPos(posX: Int): Int {
        val baseX = (this.width - backgroundWidth) / 2
        return baseX + posX * buttonWidth + (posX + 1) * HORIZONTAL_SPACING - posX * 3
    }

    /**
     * Method for calculating the height based on the background, spacing and button position
     */
    private fun getHeightFor(posY: Int): Int {
        return (height - backgroundHeight) / 2 + posY * buttonHeight + posY * VERTICAL_SPACING + if (posY == 0) 8 else 0
    }

    /**
     * Creates and adds a [PokeNavFillerButton] at the first possible row and column.
     *
     * @throws [IllegalStateException] if the UI cannot fit more buttons.
     *
     * @param identifier The [ResourceLocation] of this button.
     * @param onPress The action ran when the button is clicked, will not execute if [canClick] is false.
     * @param text The display [Component] of the button.
     * @param canClick Used to check if the button can be clicked. Will affect asset rendering to visually symbolize if false.
     */
    private fun insertButton(
        identifier: ResourceLocation,
        onPress: Button.OnPress,
        text: MutableComponent,
        canClick: () -> Boolean = { true }
    ) {
        val insertion = this.findNextInsertion()
        val posX = insertion.first
        val posY = insertion.second
        this.buttons.put(posX, posY, PokeNavImageButton(
            posX, posY,
            getWidthForPos(posX), getHeightFor(posY),
            buttonWidth, buttonHeight,
            0, 0, 0,
            identifier, buttonWidth, buttonHeight,
            onPress, text, canClick
        ))
    }

    private fun fillerButtonOf(posX: Int, posY: Int) = PokeNavFillerButton(
        posX, posY,
        getWidthForPos(posX), getHeightFor(posY),
        buttonWidth, buttonHeight,
        0, 0, 0,
        buttonWidth, buttonHeight
    )

    private fun findNextInsertion(): Pair<Int, Int> {
        for (y in 0 until MAX_BUTTONS_PER_COLUMN) {
            for (x in 0 until MAX_BUTTONS_PER_ROW) {
                if (this.buttons.get(x, y) == null) {
                    return x to y
                }
            }
        }
        throw IllegalStateException("Cannot fit more buttons")
    }

    /**
     * What should happen on Button press - START
     */

    private fun onPressPokemon(button: Button) {
        try {
            Summary.open(CobblemonClient.storage.myParty.slots, true, CobblemonClient.storage.selectedSlot)
        } catch (e: Exception) {
            Minecraft.getInstance().setScreen(null)
            Cobblemon.LOGGER.debug("Failed to open the summary from the PokeNav screen", e)
        }
    }

    private fun onPressExit(button: Button) {
        Minecraft.getInstance().setScreen(null)
    }

}