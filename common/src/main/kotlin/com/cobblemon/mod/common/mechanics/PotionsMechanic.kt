/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mechanics

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike

class PotionsMechanic {
    val potionRestoreAmount: ExpressionLike = "60".asExpressionLike()
    val superPotionRestoreAmount: ExpressionLike = "100".asExpressionLike()
    val hyperPotionRestoreAmount: ExpressionLike = "150".asExpressionLike()
}