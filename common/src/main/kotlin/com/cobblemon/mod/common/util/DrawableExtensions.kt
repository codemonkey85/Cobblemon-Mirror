/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Renderable

fun Renderable.scaleIt(value: Number) = (Minecraft.getInstance().window.guiScale * value.toFloat()).toInt()