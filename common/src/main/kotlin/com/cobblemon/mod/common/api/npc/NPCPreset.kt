/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import net.minecraft.resources.ResourceLocation

class NPCPreset {
    lateinit var id: ResourceLocation
    var aspects: Set<String>? = null
    var party: NPCPartyProvider? = null
//    var mergeMode = MergeMode.KEEP
}