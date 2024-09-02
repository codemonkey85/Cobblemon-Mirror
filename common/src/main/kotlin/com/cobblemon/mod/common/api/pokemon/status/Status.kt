/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.status

import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import net.minecraft.resources.ResourceLocation
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec

/**
 * Represents the base of a status
 *
 * @author Deltric
 */
open class Status(
    val name: ResourceLocation,
    val showdownName: String = "",
    val applyMessage: String,
    val removeMessage: String
) {
    fun getActionEffect() = ActionEffects.actionEffects[name]

    companion object {
        /**
         * A [Codec] for [Status].
         */
        @JvmStatic
        val CODEC: Codec<Status> = CodecUtils.createByIdentifierCodec(
            Statuses::getStatus,
            Status::name
        ) { identifier -> "No Status for ID $identifier" }
    }
}