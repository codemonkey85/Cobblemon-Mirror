/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.util.adapters.riding.RideControllerAdapter
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class RidingProperties(
    val seats: List<Seat> = listOf(),
    val conditions: List<Expression> = listOf(),
    val controllers: List<RideController> = listOf()
) {
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): RidingProperties {
            val seats: List<Seat> = buffer.readList { _ -> Seat.decode(buffer) }
            val conditions = buffer.readList { buffer.readString().asExpression() }
            val controllers: List<RideController> = buffer.readList { _ ->
                val key = buffer.readIdentifier()
                val controller = RideControllerAdapter.types[key]?.getConstructor()?.newInstance() ?: error("Unknown controller key: $key")
                controller.decode(buffer)
                return@readList controller
            }

            return RidingProperties(seats = seats, conditions = conditions, controllers = controllers)
        }
    }

    val canRide: Boolean
        get() = seats.isNotEmpty() && controllers.isNotEmpty()

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(seats) { _, seat -> seat.encode(buffer) }
        buffer.writeCollection(conditions) { _, condition -> buffer.writeString(condition.getString()) }
        buffer.writeCollection(controllers) { _, controller -> controller.encode(buffer) }
    }
}
