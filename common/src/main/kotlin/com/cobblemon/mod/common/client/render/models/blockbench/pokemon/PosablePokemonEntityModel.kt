/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon

import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.Entity

class PosablePokemonEntityModel : PosableEntityModel<PokemonEntity>() {
    override fun setupEntityTypeContext(entity: Entity?) {
        super.setupEntityTypeContext(entity)
        (entity as? PokemonEntity)?.let {
            context.put(RenderContext.SCALE, it.pokemon.species.baseScale)
            context.put(RenderContext.SPECIES, it.pokemon.species.resourceIdentifier)
            context.put(RenderContext.ASPECTS, it.pokemon.aspects)
            PokemonModelRepository.getTexture(it.pokemon.species.resourceIdentifier, it.pokemon.aspects, 0f).let { texture -> context.put(
                RenderContext.TEXTURE, texture) }
        }
    }
}