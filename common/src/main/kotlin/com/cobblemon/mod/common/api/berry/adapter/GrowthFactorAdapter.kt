/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry.adapter

import com.cobblemon.mod.common.api.berry.GrowthFactor
import com.cobblemon.mod.common.util.adapters.CobblemonGrowthFactorAdapter
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

/**
 * A type adapter for [GrowthFactor]s.
 * For the default implementation see [CobblemonGrowthFactorAdapter].
 *
 * @author Licious
 * @since December 2nd, 2022
 */
interface GrowthFactorAdapter : JsonDeserializer<GrowthFactor>, JsonSerializer<GrowthFactor> {

    /**
     * Register a [GrowthFactor] to be used by this adapter.
     *
     * @param type The [KClass] of the [GrowthFactor].
     * @param identifier The expected [ResourceLocation] in the parsed JSON.
     */
    fun register(type: KClass<out GrowthFactor>, identifier: ResourceLocation)

}
