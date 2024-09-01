/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike

class WalkBehaviour {
    val canWalk = true
    val avoidsLand = false
    var walkSpeed: ExpressionLike = "0.35".asExpressionLike()
}