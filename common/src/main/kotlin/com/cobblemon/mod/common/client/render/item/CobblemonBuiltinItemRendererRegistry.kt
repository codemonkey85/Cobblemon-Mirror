/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import net.minecraft.world.item.Item

/**
 * A registry of [CobblemonBuiltinItemRenderer]s.
 *
 * @author Nick, Licious
 * @since December 28th, 2022
 */
object CobblemonBuiltinItemRendererRegistry {

    private val renderers = hashMapOf<Item, CobblemonBuiltinItemRenderer>()

    fun register(item: Item, renderer: CobblemonBuiltinItemRenderer) {
        this.renderers[item] = renderer
    }

    fun rendererOf(item: Item): CobblemonBuiltinItemRenderer? = this.renderers[item]

}