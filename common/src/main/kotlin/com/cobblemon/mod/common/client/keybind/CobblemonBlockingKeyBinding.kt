/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft

/**
 * An extension for the [CobblemonKeyBinding] to prevent the [onPress] from rapidly triggering when holding down the associated key
 *
 * @author Qu
 * @since 2022-02-23
 */
abstract class CobblemonBlockingKeyBinding(
    name: String,
    type: InputConstants.Type = InputConstants.Type.KEYSYM,
    key: Int,
    category: String
) : CobblemonKeyBinding(name, type, key, category) {
    var wasDown = false
    var timeDown = 0F

    open fun onRelease() {}

    override fun onTick() {
        if (isDown && !wasDown) {
            wasDown = true
            timeDown = 0F
            onPress()
        } else if (!isDown && wasDown) {
            onRelease()
            wasDown = false
        } else if (!isDown) {
            wasDown = false
        } else if (wasDown) {
            timeDown += Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(false)
        }
    }
}