/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.mojang.blaze3d.vertex.VertexBuffer

class BerryBlockEntityRenderState : BerryBlockEntity.RenderState {
    val lastRenderFrame = -1
    override var needsRebuild = true
    val vbo: VertexBuffer = VertexBuffer(VertexBuffer.Usage.STATIC)
    var vboLightLevel: Int = 0
    var drawVbo: Boolean = false

    override fun close() {
        vbo.close()
    }
}