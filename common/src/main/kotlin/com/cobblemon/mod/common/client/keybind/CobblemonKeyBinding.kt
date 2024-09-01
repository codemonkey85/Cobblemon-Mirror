/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

/**
 * An extensions for Minecraft's [KeyBinding]
 * When creating a new [CobblemonKeyBinding] [onPress] will be called when the key is pressed.
 *
 * @author Qu
 * @since 2022-02-17
 */
abstract class CobblemonKeyBinding(
    name: String,
    type: InputConstants.Type = InputConstants.Type.KEYSYM,
    key: Int,
    category: String
): KeyMapping(name, type, key, category) {

    abstract fun onPress()

    open fun onTick() {
        if (this.consumeClick()) {
            onPress()
        }
    }
}