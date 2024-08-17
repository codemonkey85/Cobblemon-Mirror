/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pasture

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import java.util.*

class PastureLink(val linkId: UUID, val pcId: UUID, val dimension: ResourceLocation, val pos: BlockPos, val permissions: PasturePermissions) {
    fun getPC() = Cobblemon.storage.getPC(pcId)
}