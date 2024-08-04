/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.api.gui.ParentWidget
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

abstract class SoundlessWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    component: Component
): ParentWidget(pX, pY, pWidth, pHeight, component) {
    /**
     * Do not play sounds when clicking, because clicking a Widget anywhere produces a sound... :(
     */
    override fun playDownSound(pHandler: SoundManager) {
    }

}