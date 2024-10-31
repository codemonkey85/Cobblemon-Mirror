/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.wheel

import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f

data class InteractWheelOption(
    val iconResource: ResourceLocation,
    val secondaryIconResource: ResourceLocation? = null,
    val tooltipText: String?,
    val colour: () -> Vector3f? = { null },
    val onPress: () -> Unit
)