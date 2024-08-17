/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.util

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation

/**
 * Checks if a resource exists at this location
 */
fun ResourceLocation.exists(): Boolean {
    return Minecraft.getInstance().resourceManager.getResource(this).isPresent
}

fun runOnRender(action: () -> Unit) {
    Minecraft.getInstance().execute(action)
}