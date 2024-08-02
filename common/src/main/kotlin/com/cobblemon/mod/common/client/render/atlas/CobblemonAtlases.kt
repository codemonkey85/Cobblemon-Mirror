/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.atlas

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.TextureAtlasHolder

object CobblemonAtlases {
    val atlases = mutableSetOf<TextureAtlasHolder>()

    val BERRY_SPRITE_ATLAS = register("textures/atlas/berries.png", "berries")
    val EGG_PATTERN_ATLAS = register("textures/atlas/egg_patterns.png", "egg_patterns")
    val EGG_PATTERN_SPRITE_ATLAS = register("textures/atlas/egg_pattern_sprites.png", "egg_pattern_sprites")

    fun register(atlasId: String, sourcePath: String): TextureAtlasHolder {
        val atlas = CobblemonAtlas(
            Minecraft.getInstance().textureManager,
            cobblemonResource(atlasId),
            cobblemonResource(sourcePath)
        )
        atlases.add(atlas)
        return atlas
    }

}