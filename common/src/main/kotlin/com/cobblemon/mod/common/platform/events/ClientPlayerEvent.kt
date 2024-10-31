/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform.events

import net.minecraft.client.player.LocalPlayer

/**
 * Events related to a [ClientPlayer].
 * As implied by the name these are fired on the client side.
 * Keep in mind the concept of a login/out is still present in an integrated instance aka a singleplayer world.
 *
 * @author Licious
 * @since February 15th, 2023
 */
interface ClientPlayerEvent {

    /**
     * The [ClientPlayer] triggering the events.
     */
    val player: LocalPlayer

    /**
     * Fired when the [player] logs in.
     */
    data class Login(override val player: LocalPlayer) : ClientPlayerEvent

    /**
     * Fired when the [player] logs out.
     */
    data class Logout(override val player: LocalPlayer) : ClientPlayerEvent

}