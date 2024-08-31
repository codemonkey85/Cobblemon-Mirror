/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.resources.ResourceLocation

fun ResourceLocation.endsWith(suffix: String): Boolean {
    return this.toString().endsWith(suffix)
}

/**
 * Attempts to simplify an identifier as a string for user-friendly results.
 *
 * If the [Identifier.namespace] is [namespace] the result is [Identifier.path] else [Identifier.toString].
 *
 * @param namespace The namespace that if matched with [Identifier.namespace] allows the shortening.
 * @return The resulting string.
 */
fun ResourceLocation.simplify(namespace: String = Cobblemon.MODID): String = if (this.namespace == namespace) this.path else this.toString()