/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.entity.EntitySideDelegate

interface PosableEntity {
    fun getCurrentPoseType(): PoseType
    val delegate: EntitySideDelegate<*>
    val struct: QueryStruct

    fun addPosableFunctions(struct: QueryStruct) {
        struct.addFunction("pose_type") { StringValue(getCurrentPoseType().name) }
        delegate.addToStruct(struct)
    }
}