/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.npc

import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.world.entity.Entity

class PosableNPCModel : PosableEntityModel<NPCEntity>() {
    override fun setupEntityTypeContext(entity: Entity?) {
        super.setupEntityTypeContext(entity)
        if (entity is NPCEntity) {
            context.put(RenderContext.ASPECTS, entity.aspects)
        }
    }
}