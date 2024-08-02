/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.breeding

import net.minecraft.resources.ResourceLocation

/**
 * Represents a pattern on an egg
 *
 * @author Apion
 * @since January 2nd, 2024
 */
data class EggPattern (
    val model: ResourceLocation,
    val baseTexturePath: ResourceLocation,
    val overlayTexturePath: ResourceLocation?,
    val baseInvSpritePath: ResourceLocation,
    val overlayInvSpritePath: ResourceLocation?,
)