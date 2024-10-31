/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.serialization

import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A Netty serializer.
 *
 * @author Licious
 * @since June 27th, 2022
 */
interface BufferSerializer {

    /**
     * Saves this object into a [RegistryFriendlyByteBuf].
     *
     * @param buffer The [RegistryFriendlyByteBuf] the data will be written to.
     * @param toClient If the resulting packet will be client-bound.
     */
    fun saveToBuffer(buffer: RegistryFriendlyByteBuf, toClient: Boolean)

    /**
     * Loads the data from the given [RegistryFriendlyByteBuf].
     *
     * @param buffer The [RegistryFriendlyByteBuf] with the received data.
     */
    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf)

}