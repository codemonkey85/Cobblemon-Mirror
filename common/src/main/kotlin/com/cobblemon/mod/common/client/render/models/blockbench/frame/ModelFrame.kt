/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.frame

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone

/**
 * A simple interface to allow coded models to specifically state what their root bone is. This is the top of a hierarchy
 * that barely ever gets used nowadays.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
interface ModelFrame {
    val rootPart: Bone
}