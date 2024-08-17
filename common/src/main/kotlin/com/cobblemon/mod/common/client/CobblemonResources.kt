/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

object CobblemonResources {
    /**
     * Textures
     */
    val RED = cobblemonResource("textures/red.png")
    val WHITE = cobblemonResource("textures/white.png")
    val PHASE_BEAM = cobblemonResource("textures/phase_beam.png")

    /**
     * Fonts
     */
    val DEFAULT_LARGE = ResourceLocation.parse("uniform")
}