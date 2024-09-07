/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.featurerenderers

import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.gui.GuiGraphics

/**
 * A renderer for some kind of [SynchronizedSpeciesFeature] so that it will display
 * in the summary screen of the Pokémon (under other stats).
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
interface SummarySpeciesFeatureRenderer<T : SynchronizedSpeciesFeature> {
    /** The name of the feature (so we know where to look to find it in the [Pokemon]) */
    val name: String
    /** Draws it at a particular position. */
    fun render(GuiGraphics: GuiGraphics, x: Float, y: Float, pokemon: Pokemon, feature: T)
    fun render(GuiGraphics: GuiGraphics, x: Float, y: Float, pokemon: Pokemon): Boolean {
        val feature = pokemon.getFeature<T>(name) ?: return false
        render(GuiGraphics, x, y, pokemon, feature)
        return true
    }
}