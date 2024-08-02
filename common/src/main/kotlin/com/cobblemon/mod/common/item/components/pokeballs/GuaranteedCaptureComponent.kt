/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components.pokeballs

import com.mojang.serialization.Codec

data class GuaranteedCaptureComponent(val guaranteed: Boolean) {
    companion object {
        val CODEC: Codec<GuaranteedCaptureComponent> = Codec.BOOL.xmap(
            { GuaranteedCaptureComponent(it) },
            { it.guaranteed }
        )
    }
}
