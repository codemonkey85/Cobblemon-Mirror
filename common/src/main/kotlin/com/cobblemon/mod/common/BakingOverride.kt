/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation

/**
 * Contains information for forcing a model to be baked
 *
 * @param modelLocation The location of the model
 * @param modelIdentifier The identifier that the BakedModel will be registered to
 */
data class BakingOverride(
    val modelLocation: ResourceLocation,
    val modelIdentifier: ModelResourceLocation
) {
    fun getModel(): BakedModel {
        return Minecraft.getInstance().modelManager.getModel(modelIdentifier)
    }
}
