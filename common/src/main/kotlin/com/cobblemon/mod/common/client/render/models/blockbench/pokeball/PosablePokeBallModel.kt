/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokeball

import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import net.minecraft.entity.Entity

class PosablePokeBallModel : PosableEntityModel<EmptyPokeBallEntity>() {
    override fun setupEntityTypeContext(entity: Entity?) {
        super.setupEntityTypeContext(entity)
        if (entity is EmptyPokeBallEntity) {
            context.put(RenderContext.ASPECTS, entity.aspects)
        }
    }
}