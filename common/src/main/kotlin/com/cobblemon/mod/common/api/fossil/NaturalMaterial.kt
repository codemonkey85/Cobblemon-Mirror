/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fossil

import com.cobblemon.mod.common.registry.ItemTagCondition
import net.minecraft.resources.ResourceLocation

data class NaturalMaterial(
    val content: Int = 0,
    val item: ResourceLocation?,
    val tag: ItemTagCondition? = null,
    val returnItem: ResourceLocation? = null
)
