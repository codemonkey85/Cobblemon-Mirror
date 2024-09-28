/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_DESCRIPTION_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.POKEMON_PORTRAIT_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import org.joml.Quaternionf
import org.joml.Vector3f

class SizeWidget(val pX: Int, val pY: Int) : SoundlessWidget(
    pX,
    pY,
    HALF_OVERLAY_WIDTH,
    POKEMON_DESCRIPTION_HEIGHT,
    lang("ui.pokedex.pokemon_info")
) {
    companion object {
        private val scrollBorder = cobblemonResource("textures/gui/pokedex/info_scroll_border.png")
        private val heightGrid = cobblemonResource("textures/gui/pokedex/height_grid.png")
        private val gridPlayer = cobblemonResource("textures/gui/pokedex/height_grid_player.png")
    }

    var renderablePokemon : RenderablePokemon? = null
    var baseScale: Float = 1F
    var pokemonHeight: Float = 0F
    var weight: Float = 0F

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()

//        drawScaledTextJustifiedRight(
//            context = context,
//            font = CobblemonResources.DEFAULT_LARGE,
//            text = Component.translatable("cobblemon.ui.pokedex.height", (pokemonHeight / 10).toString()).bold(),
//            x = pX + HALF_OVERLAY_WIDTH - 9,
//            y = pY - 10,
//            shadow = true
//        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = Component.translatable("cobblemon.ui.pokedex.weight", (weight / 10).toString()).bold(),
            x = pX + 9,
            y = pY - 10,
            shadow = true
        )

        context.enableScissor(
            pX + 1,
            pY,
            pX + HALF_OVERLAY_WIDTH - 1,
            pY + POKEMON_DESCRIPTION_HEIGHT
        )

        val modelScale = 7.5F

        matrices.pushPose()
        matrices.translate(
            pX.toDouble() + 50,
            pY.toDouble() + (POKEMON_DESCRIPTION_HEIGHT / 2) + 18.5,
            0.0
        )
        drawProfilePokemon(
            renderablePokemon = renderablePokemon!!,
            matrixStack =  matrices,
            partialTicks = delta,
            rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(0F, 0F, 0F)),
            state = FloatingState(),
            scale = modelScale,
            applyProfileTransform = false,
            applyBaseScale = true,
            r = 0F,
            g = 0F,
            b = 0F
        )
        matrices.popPose()
        context.disableScissor()

        // Ensure elements are not hidden behind Pokémon render
        matrices.pushPose()
        matrices.translate(0.0, 0.0, 100.0)

        blitk(
            matrixStack = context.pose(),
            texture = gridPlayer,
            x = pX + 85,
            y = pY + 23.5,
            width = 8,
            height = 16
        )

        blitk(
            matrixStack = matrices,
            texture = heightGrid,
            x = pX + 1,
            y = pY,
            width = POKEMON_PORTRAIT_WIDTH,
            height = POKEMON_DESCRIPTION_HEIGHT
        )

        blitk(
            matrixStack = context.pose(),
            texture = scrollBorder,
            x = (pX + 1) / SCALE,
            y = pY / SCALE,
            width = 274,
            height = 4,
            textureHeight = 8,
            vOffset = 0,
            scale = SCALE
        )

        blitk(
            matrixStack = context.pose(),
            texture = scrollBorder,
            x = (pX + 1) / SCALE,
            y = (pY + 40) / SCALE,
            width = 274,
            height = 4,
            textureHeight = 8,
            vOffset = 4,
            scale = SCALE
        )

        matrices.popPose()
    }
}