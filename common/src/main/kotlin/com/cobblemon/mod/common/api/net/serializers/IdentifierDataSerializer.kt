/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.resources.ResourceLocation

/**
 * Data serializer of [ResourceLocation] for DataTracker things.
 *
 * @author Hiroku
 * @since May 22nd, 2023
 */
object IdentifierDataSerializer : EntityDataSerializer<ResourceLocation> {
    val ID = cobblemonResource("identifier")
    override fun copy(value: ResourceLocation) = ResourceLocation.fromNamespaceAndPath(value.namespace, value.path)
    fun read(buf: RegistryFriendlyByteBuf) = ResourceLocation.fromNamespaceAndPath(buf.readString(), buf.readString())
    fun write(buf: RegistryFriendlyByteBuf, value: ResourceLocation) {
        buf.writeString(value.namespace)
        buf.writeString(value.path)
    }

    override fun codec() = StreamCodec.of(::write, ::read)
}