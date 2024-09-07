/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.codecs.UnboundedMapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * This is an interpretation of the Bedrock Edition particle system. The behaviour of the effects are
 * intended to be the same, but the grouping of elements and the transfer JSON format is custom so that
 * it actually makes some damn sense.
 *
 * @author Hiroku
 * @since January 2nd, 2023
 */
class BedrockParticleOptions(
    var id: ResourceLocation = ResourceLocation.parse("effect"),
    var emitter: BedrockParticleEmitter = BedrockParticleEmitter(),
    var particle: BedrockParticle = BedrockParticle(),
    var curves: MutableList<MoLangCurve> = mutableListOf(),
    var space: ParticleSpace = ParticleSpace(),
    var events: MutableMap<String, ParticleEvent> = mutableMapOf()
) {
    companion object {
        val CODEC: Codec<BedrockParticleOptions> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter { it.id },
                BedrockParticleEmitter.CODEC.fieldOf("emitter").forGetter { it.emitter },
                BedrockParticle.CODEC.fieldOf("particle").forGetter { it.particle },
                MoLangCurve.codec.listOf().fieldOf("curves").forGetter { it.curves },
                ParticleSpace.CODEC.fieldOf("space").forGetter { it.space },
                UnboundedMapCodec(PrimitiveCodec.STRING, ParticleEvent.CODEC).fieldOf("events").forGetter { it.events }
            ).apply(instance) { id, emitter, particle, curves, space, events ->
                BedrockParticleOptions(
                    id = id,
                    emitter = emitter,
                    particle = particle,
                    curves = curves.toMutableList(),
                    space = space,
                    events = events
                )
            }
        }
    }

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(id)
        emitter.writeToBuffer(buffer)
        particle.writeToBuffer(buffer)
        buffer.writeCollection(curves) { pb, curve -> MoLangCurve.writeToBuffer(buffer, curve) }
        space.writeToBuffer(buffer)
        buffer.writeMap(events, { _, v -> buffer.writeString(v) }) { _, event -> event.encode(buffer) }
    }

    fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        id = buffer.readIdentifier()
        emitter.readFromBuffer(buffer)
        particle.readFromBuffer(buffer)
        curves = buffer.readList { MoLangCurve.readFromBuffer(buffer) }
        space.readFromBuffer(buffer)
        events = buffer.readMap({ buffer.readString() }) { ParticleEvent().also { it.decode(buffer) } }
    }
}