/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters.riding

import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.pokemon.riding.controllers.BirdAirController
import com.cobblemon.mod.common.pokemon.riding.controllers.GenericLandController
import com.cobblemon.mod.common.pokemon.riding.controllers.GenericLiquidController
import com.cobblemon.mod.common.pokemon.riding.controllers.RunUpToFlightCompositeController
import com.cobblemon.mod.common.pokemon.riding.controllers.SwimDashController
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.resources.ResourceLocation

/**
 * Adapter for deserializing [RideController] types.
 *
 * @author Hiroku
 * @since April 30th, 2024
 */
object RideControllerAdapter : JsonDeserializer<RideController> {
    val types: MutableMap<ResourceLocation, Class<out RideController>> = mutableMapOf(
        GenericLandController.KEY to GenericLandController::class.java,
        GenericLiquidController.KEY to GenericLiquidController::class.java,
        SwimDashController.KEY to SwimDashController::class.java,
        RunUpToFlightCompositeController.KEY to RunUpToFlightCompositeController::class.java,
        BirdAirController.KEY to BirdAirController::class.java
    )

    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): RideController {
        val root = element.asJsonObject
        val key = root.get("key").asString
        val controllerType = types[key.asIdentifierDefaultingNamespace()] ?: throw IllegalArgumentException("Unknown controller: $key")
        return context.deserialize(element, controllerType)
    }
}