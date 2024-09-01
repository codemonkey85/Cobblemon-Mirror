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

class BerriesMechanic {
    val portionHealRatio: ExpressionLike = "0.33".asExpressionLike()
    val sitrusHealAmount: ExpressionLike = "v.pokemon.max_hp * 0.33".asExpressionLike()
    val friendshipRaiseAmount: ExpressionLike = "v.pokemon.friendship < 100 ? 10 : (v.pokemon.friendship < 200 ? 5 : 1)".asExpressionLike()
    val evLowerAmount: ExpressionLike = "10".asExpressionLike()
    val ppRestoreAmount: ExpressionLike = "10".asExpressionLike()
    val oranRestoreAmount: ExpressionLike = "10".asExpressionLike()
}