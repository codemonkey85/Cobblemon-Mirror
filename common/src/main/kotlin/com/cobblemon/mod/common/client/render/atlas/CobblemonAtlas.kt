/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.atlas

import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.resources.TextureAtlasHolder
import net.minecraft.resources.ResourceLocation

class CobblemonAtlas(
    textureManager: TextureManager,
    atlasId: ResourceLocation,
    sourcePath: ResourceLocation
) : TextureAtlasHolder(
    textureManager,
    atlasId,
    sourcePath
)
