/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

class ParticleCollision(
    var enabled: Expression = NumberExpression(0.0),
    var radius: Expression = NumberExpression(0.1),
    var friction: Expression = NumberExpression(10.0),
    var bounciness: Expression = NumberExpression(0.0),
    var expiresOnContact: Boolean = false
) {
    companion object {
        val CODEC: Codec<ParticleCollision> = RecordCodecBuilder.create { instance ->
            instance.group(
                EXPRESSION_CODEC.fieldOf("enabled").forGetter { it.enabled },
                EXPRESSION_CODEC.fieldOf("radius").forGetter { it.radius },
                EXPRESSION_CODEC.fieldOf("friction").forGetter { it.friction },
                EXPRESSION_CODEC.fieldOf("bounciness").forGetter { it.bounciness },
                PrimitiveCodec.BOOL.fieldOf("expiresOnContact").forGetter { it.expiresOnContact }
            ).apply(instance, ::ParticleCollision)
        }
    }

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(enabled.getString())
        buffer.writeString(radius.getString())
        buffer.writeString(friction.getString())
        buffer.writeString(bounciness.getString())
        buffer.writeBoolean(expiresOnContact)
    }

    fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        enabled = MoLang.createParser(buffer.readString()).parseExpression()
        radius = MoLang.createParser(buffer.readString()).parseExpression()
        friction = MoLang.createParser(buffer.readString()).parseExpression()
        bounciness = MoLang.createParser(buffer.readString()).parseExpression()
        expiresOnContact = buffer.readBoolean()
    }
}