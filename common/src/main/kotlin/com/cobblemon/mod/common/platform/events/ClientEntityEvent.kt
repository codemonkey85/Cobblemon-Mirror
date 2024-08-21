/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform.events

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity

/**
 * Events fired for client side [Entity]s.
 *
 * @author Segfault Guy
 * @since August 18th, 2024
 */
interface ClientEntityEvent {

    /** The [Entity] triggering the event. */
    val entity: Entity

    /** The client's [ClientLevel]. */
    val level: ClientLevel

    /** Event when [entity] loads into the client's [level]. */
    data class Load(override val entity: Entity, override val level: ClientLevel) : ClientEntityEvent

    /** Event when [entity] unloads from the client's [level]. */
    data class Unload(override val entity: Entity, override val level: ClientLevel) : ClientEntityEvent
}