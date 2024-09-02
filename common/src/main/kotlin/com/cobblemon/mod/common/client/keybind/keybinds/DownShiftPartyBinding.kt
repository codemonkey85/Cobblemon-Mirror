/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.keybind.keybinds

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinding
import com.cobblemon.mod.common.client.keybind.KeybindCategories
import com.mojang.blaze3d.platform.InputConstants

object DownShiftPartyBinding : CobblemonKeyBinding(
    "key.cobblemon.downshiftparty",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_DOWN,
    KeybindCategories.COBBLEMON_CATEGORY
) {
    override fun onPress() {
        CobblemonClient.storage.shiftSelected(true)
    }
}