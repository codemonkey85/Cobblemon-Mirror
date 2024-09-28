/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.blockentity

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.google.gson.annotations.SerializedName
import net.minecraft.client.model.geom.ModelPart

class BlockEntityModel(root: Bone) : PosableModel(root) {
    @Transient
    @SerializedName("Don't bloody deserialize this, Gson! I mean it!")
    override val rootPart = (root as ModelPart).children.entries.first().let { root.registerChildWithAllChildren(it.key) }
    var maxScale = 1F
    var yTranslation = 0F
}