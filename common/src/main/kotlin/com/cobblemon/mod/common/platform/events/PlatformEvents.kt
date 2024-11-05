/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform.events

import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable

/**
 * A class for our converted events from platform specific to a trigger on common.
 * If you're using this class as API it is recommended that you use the platform specific events instead.
 *
 * @author Licious
 * @since February 13th, 2023
 */
object PlatformEvents {
    @JvmField
    val SERVER_STARTING = EventObservable<ServerEvent.Starting>()
    @JvmField
    val SERVER_STARTED = EventObservable<ServerEvent.Started>()
    @JvmField
    val SERVER_STOPPING = EventObservable<ServerEvent.Stopping>()
    @JvmField
    val SERVER_STOPPED = EventObservable<ServerEvent.Stopped>()

    @JvmField
    val SERVER_TICK_PRE = EventObservable<ServerTickEvent.Pre>()
    @JvmField
    val SERVER_TICK_POST = EventObservable<ServerTickEvent.Post>()

    @JvmField
    val CLIENT_TICK_PRE = EventObservable<ClientTickEvent.Pre>()
    @JvmField
    val CLIENT_TICK_POST = EventObservable<ClientTickEvent.Post>()

    @JvmField
    val SERVER_PLAYER_LOGIN = EventObservable<ServerPlayerEvent.Login>()
    @JvmField
    val SERVER_PLAYER_LOGOUT = EventObservable<ServerPlayerEvent.Logout>()
    @JvmField
    val CLIENT_PLAYER_LOGIN = EventObservable<ClientPlayerEvent.Login>()
    @JvmField
    val CLIENT_PLAYER_LOGOUT = EventObservable<ClientPlayerEvent.Logout>()
    @JvmField
    val PLAYER_DEATH = CancelableObservable<ServerPlayerEvent.Death>()
    @JvmField
    val RIGHT_CLICK_BLOCK = CancelableObservable<ServerPlayerEvent.RightClickBlock>()
    @JvmField
    val RIGHT_CLICK_ENTITY = CancelableObservable<ServerPlayerEvent.RightClickEntity>()

    @JvmField
    val CHANGE_DIMENSION = EventObservable<ChangeDimensionEvent>()

    @JvmField
    val CLIENT_ITEM_TOOLTIP = EventObservable<ItemTooltipEvent>()

    @JvmField
    val CLIENT_ENTITY_LOAD = EventObservable<ClientEntityEvent.Load>()
    @JvmField
    val CLIENT_ENTITY_UNLOAD = EventObservable<ClientEntityEvent.Unload>()

    @JvmField
    val RENDER = EventObservable<RenderEvent>()
}
