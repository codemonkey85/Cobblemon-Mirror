/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.codecs.UnboundedMapCodec
import net.minecraft.network.RegistryFriendlyByteBuf

/*
 * The different references to events that can be triggered by an effect.
 */

/**
 * It really is simple.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class SimpleEventTrigger(var event: String): Encodable, Decodable {
    companion object {
        val CODEC = RecordCodecBuilder.create<SimpleEventTrigger> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("event").forGetter { it.event }
            ).apply(instance, ::SimpleEventTrigger)
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(event)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        event = buffer.readString()
    }

    fun trigger(storm: ParticleStorm, particle: SnowstormParticle?) {
        val event = storm.effect.events[event] ?: return
        event.run(storm, particle)
    }
}

/**
 * An event trigger that outlines specific times at which point groups of effects will play. This can be used
 * on the basis of time but is also used in the case of travel distance.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class EventTriggerTimeline(var map: MutableMap<Double, MutableList<String>>): Encodable, Decodable {
    companion object {
        val CODEC = RecordCodecBuilder.create<EventTriggerTimeline> { instance ->
            instance.group(
                UnboundedMapCodec(PrimitiveCodec.DOUBLE, PrimitiveCodec.STRING.listOf()).fieldOf("map").forGetter { it.map }
            ).apply(instance, ::EventTriggerTimeline)
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(map, { pb, k -> pb.writeDouble(k) }, { pb, v -> pb.writeCollection(v) { _, s -> pb.writeString(s) } })
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        map = buffer.readMap({ pb -> pb.readDouble() }, { pb -> pb.readList { pb.readString() }.toMutableList() }).toMutableMap()
    }

    fun check(storm: ParticleStorm, particle: SnowstormParticle?, previousTime: Double, newTime: Double) {
        val events = map.entries.filter { it.key in previousTime..newTime }.flatMap { it.value }
        events.forEach { event ->
            val event = storm.effect.events[event] ?: return
            event.run(storm, particle)
        }
    }
}

/**
 * An event trigger that takes the total distance travelled by the emitter and modulos it across [distance] to know
 * if we need to play it again.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class LoopingTravelDistanceEventTrigger(var distance: Double, var events: MutableList<String>): Encodable, Decodable {
    companion object {
        val CODEC = RecordCodecBuilder.create<LoopingTravelDistanceEventTrigger> { instance ->
            instance.group(
                PrimitiveCodec.DOUBLE.fieldOf("distance").forGetter { it.distance },
                PrimitiveCodec.STRING.listOf().fieldOf("events").forGetter { it.events }
            ).apply(instance, ::LoopingTravelDistanceEventTrigger)
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeDouble(distance)
        buffer.writeCollection(events) { _, s -> buffer.writeString(s) }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        distance = buffer.readDouble()
        events = buffer.readList { buffer.readString() }.toMutableList()
    }

    fun check(storm: ParticleStorm, particle: SnowstormParticle?, previousDistance: Double, currentDistance: Double) {
        if (previousDistance < distance && currentDistance >= distance) {
            events.mapNotNull { storm.effect.events[it] }.forEach { it.run(storm, particle) }
        }
    }
}