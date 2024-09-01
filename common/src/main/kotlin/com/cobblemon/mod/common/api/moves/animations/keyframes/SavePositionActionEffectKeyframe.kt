/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import java.util.concurrent.CompletableFuture

class SavePositionActionEffectKeyframe: ConditionalActionEffectKeyframe() {
    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val user = context.findOneProvider<UsersProvider>()?.entities?.firstOrNull() ?: return skip()
        context.runtime.environment.setSimpleVariable("${user.stringUUID}-pos", ObjectValue(user.position()))
        return skip()
    }
}