/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets.preview

import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * The current/next/previous pokemon display thingy
 *
 * Very good name
 *
 * @author Qu
 * @since 2022-07-30
 */
class StarterRoundabout(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    var pokemon: RenderablePokemon,
    private val clickAction: (mouseX: Double, mouseY: Double) -> Unit = { _, _ -> },
    private val rotationVector: Vector3f
): SoundlessWidget(pX, pY, pWidth, pHeight, Component.literal("StarterRoundabout")) {
    val state = FloatingState()
    companion object {
        const val MODEL_WIDTH = 30
        const val MODEL_HEIGHT = 30
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!this.visible) {
            return
        }
        val matrices = context.pose()
        this.isHovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= (this.y - MODEL_HEIGHT) && mouseY < this.y
        matrices.pushPose()
        /*
         * This correction term is due to where scaling comes from in a render. We are giving the drawProfilePokemon
         * a different scale to usual, which means our position offsets that were used in the summary GUI (which is
         * what was used to calibrate those offsets) are slightly off. In this specific case, -3 on the Y axis is
         * enough to correct this deviation.
         *
         * If you want to up the scale, then you'll also need to change the correction term (trial and error it)
         * - Hiro
         */
        val correctionTerm = -3.0
        matrices.translate(x.toDouble() + MODEL_WIDTH / 2.0, y.toDouble() - MODEL_HEIGHT.toDouble() + correctionTerm, 0.0)

        // This uses more weird x and y because the component is in an abnormal position, could fix it but also who cares at this point
        context.enableScissor(
            x,
            y - MODEL_HEIGHT,
            x + MODEL_WIDTH,
            y
        )
        drawProfilePokemon(
            renderablePokemon = pokemon,
            matrixStack = matrices,
            rotation = Quaternionf().fromEulerXYZDegrees(rotationVector),
            state = state,
            scale = 18F,
            partialTicks = delta
        )

        context.disableScissor()

        matrices.popPose()
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (this.clicked(pMouseX, pMouseY) && this.isValidClickButton(pButton)) {
            this.onClick(pMouseX, pMouseY)
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun clicked(mouseX: Double, mouseY: Double): Boolean {
        return this.active && this.visible && this.isHovered()
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        this.clickAction(mouseX, mouseY)
    }
}