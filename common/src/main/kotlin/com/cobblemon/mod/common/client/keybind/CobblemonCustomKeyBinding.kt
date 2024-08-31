/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind

import com.mojang.blaze3d.platform.InputConstants


/**
 * [CobblemonKeyBinding] extension if the action on press shall be something that is not
 * platform-agnostic
 *
 * @author Qu
 * @since 2022-02-17
 */
abstract class CobblemonCustomKeyBinding(
    name: String,
    type: InputConstants.Type = InputConstants.Type.KEYSYM,
    key: Int,
    category: String
) : CobblemonKeyBinding(name, type, key, category) {

    var run: Runnable? = null

    override fun onPress() {
        run?.run()
    }
}