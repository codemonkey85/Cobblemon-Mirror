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
import com.bedrockk.molang.ast.BooleanExpression
import com.bedrockk.molang.ast.NumberExpression
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Effect details for the actual particles of a particle effect.
 *
 * @author Hiroku
 * @since January 4th, 2023
 */
class BedrockParticle(
    var texture: ResourceLocation = ResourceLocation.parse("minecraft:textures/particles/bubble.png"),
    var material: ParticleMaterial = ParticleMaterial.ALPHA,
    var uvMode: ParticleUVMode = StaticParticleUVMode(),
    var sizeX: Expression = NumberExpression(0.15),
    var sizeY: Expression = NumberExpression(0.15),
    var maxAge: Expression = NumberExpression(1.0),
    var killExpression: Expression = BooleanExpression(false),
    var updateExpressions: MutableList<Expression> = mutableListOf(),
    var renderExpressions: MutableList<Expression> = mutableListOf(),
    var motion: ParticleMotion = StaticParticleMotion(),
    var rotation: ParticleRotation = DynamicParticleRotation(),
    var viewDirection: ParticleViewDirection = FromMotionViewDirection(),
    var cameraMode: ParticleCameraMode = RotateXYZCameraMode(),
    var tinting: ParticleTinting = ExpressionParticleTinting(),
    var collision: ParticleCollision = ParticleCollision(),
    var environmentLighting: Boolean = false,
    var creationEvents: MutableList<SimpleEventTrigger> = mutableListOf(),
    var expirationEvents: MutableList<SimpleEventTrigger> = mutableListOf(),
    var timeline: EventTriggerTimeline = EventTriggerTimeline(mutableMapOf())
) {
    class ExpressionSet(
        val sizeX: Expression,
        val sizeY: Expression,
        val maxAge: Expression,
        val killExpression: Expression,
        val updateExpressions: MutableList<Expression>,
        val renderExpressions: MutableList<Expression>
    )

    /*
    				"creation_event": "event_sendspark",
				"expiration_event": ["event_spark", "event_afterspark", "event_capturestar"],
				"timeline": {
					"21.00": "event_spark"
				}
     */

    class EventSet(
        val creationEvents: MutableList<SimpleEventTrigger>,
        val expirationEvents: MutableList<SimpleEventTrigger>,
        val timeline: EventTriggerTimeline
    )

    // kill plane maybe
    companion object {
        val EXPRESSION_SET_CODEC = RecordCodecBuilder.create<ExpressionSet> { instance ->
            instance.group(
                EXPRESSION_CODEC.fieldOf("sizeX").forGetter { it.sizeX },
                EXPRESSION_CODEC.fieldOf("sizeY").forGetter { it.sizeY },
                EXPRESSION_CODEC.fieldOf("maxAge").forGetter { it.maxAge },
                EXPRESSION_CODEC.fieldOf("killExpression").forGetter { it.killExpression },
                EXPRESSION_CODEC.listOf().fieldOf("updateExpressions").forGetter { it.updateExpressions },
                EXPRESSION_CODEC.listOf().fieldOf("renderExpressions").forGetter { it.renderExpressions }
            ).apply(instance, ::ExpressionSet)
        }

        val EVENT_SET_CODEC = RecordCodecBuilder.create<EventSet> { instance ->
            instance.group(
                SimpleEventTrigger.CODEC.listOf().fieldOf("creationEvents").forGetter { it.creationEvents },
                SimpleEventTrigger.CODEC.listOf().fieldOf("expirationEvents").forGetter { it.expirationEvents },
                EventTriggerTimeline.CODEC.fieldOf("timeline").forGetter { it.timeline }
            ).apply(instance, ::EventSet)
        }

        val CODEC: Codec<BedrockParticle> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter { it.texture },
                PrimitiveCodec.STRING.fieldOf("material").forGetter { it.material.name },
                ParticleUVMode.codec.fieldOf("uvMode").forGetter { it.uvMode },
                EXPRESSION_SET_CODEC.fieldOf("expressionSet").forGetter {
                    ExpressionSet(
                        it.sizeX,
                        it.sizeY,
                        it.maxAge,
                        it.killExpression,
                        it.updateExpressions,
                        it.renderExpressions
                    )
                },
                ParticleMotion.codec.fieldOf("motion").forGetter { it.motion },
                ParticleRotation.codec.fieldOf("rotation").forGetter { it.rotation },
                ParticleViewDirection.codec.fieldOf("viewDirection").forGetter { it.viewDirection },
                ParticleCameraMode.codec.fieldOf("cameraMode").forGetter { it.cameraMode },
                ParticleTinting.codec.fieldOf("tinting").forGetter { it.tinting },
                ParticleCollision.CODEC.fieldOf("collision").forGetter { it.collision },
                PrimitiveCodec.BOOL.fieldOf("environmentLighting").forGetter { it.environmentLighting },
                EVENT_SET_CODEC.fieldOf("eventSet").forGetter {
                    EventSet(
                        it.creationEvents,
                        it.expirationEvents,
                        it.timeline
                    )
                }
            ).apply(instance) {
                    texture, materialStr, uvMode, expressionSet, motion, rotation, viewDirection, cameraMode, tinting, collision, environmentLighting, eventSet ->
                BedrockParticle(
                    texture = texture,
                    material = ParticleMaterial.valueOf(materialStr),
                    uvMode = uvMode,
                    sizeX = expressionSet.sizeX,
                    sizeY = expressionSet.sizeY,
                    maxAge = expressionSet.maxAge,
                    killExpression = expressionSet.killExpression,
                    updateExpressions = expressionSet.updateExpressions,
                    renderExpressions = expressionSet.renderExpressions,
                    motion = motion,
                    rotation = rotation,
                    viewDirection = viewDirection,
                    cameraMode = cameraMode,
                    tinting = tinting,
                    collision = collision,
                    environmentLighting = environmentLighting,
                    creationEvents = eventSet.creationEvents,
                    expirationEvents = eventSet.expirationEvents,
                    timeline = eventSet.timeline
                )
            }
        }
    }

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(texture)
        buffer.writeString(material.name)
        ParticleUVMode.writeToBuffer(buffer, uvMode)
        buffer.writeString(sizeX.getString())
        buffer.writeString(sizeY.getString())
        buffer.writeString(maxAge.getString())
        buffer.writeString(killExpression.getString())
        buffer.writeCollection(updateExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        buffer.writeCollection(renderExpressions) { pb, expression -> pb.writeString(expression.getString()) }
        ParticleMotion.writeToBuffer(buffer, motion)
        ParticleRotation.writeToBuffer(buffer, rotation)
        ParticleViewDirection.writeToBuffer(buffer, viewDirection)
        ParticleCameraMode.writeToBuffer(buffer, cameraMode)
        ParticleTinting.writeToBuffer(buffer, tinting)
        collision.writeToBuffer(buffer)
        buffer.writeBoolean(environmentLighting)
        buffer.writeCollection(creationEvents) { _, event -> event.encode(buffer) }
        buffer.writeCollection(expirationEvents) { _, event -> event.encode(buffer) }
        timeline.encode(buffer)
    }

    fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        texture = buffer.readIdentifier()
        material = ParticleMaterial.valueOf(buffer.readString())
        uvMode = ParticleUVMode.readFromBuffer(buffer)
        sizeX = MoLang.createParser(buffer.readString()).parseExpression()
        sizeY = MoLang.createParser(buffer.readString()).parseExpression()
        maxAge = MoLang.createParser(buffer.readString()).parseExpression()
        killExpression = MoLang.createParser(buffer.readString()).parseExpression()
        updateExpressions = buffer.readList { MoLang.createParser(it.readString()).parseExpression() }
        renderExpressions = buffer.readList { MoLang.createParser(it.readString()).parseExpression() }
        motion = ParticleMotion.readFromBuffer(buffer)
        rotation = ParticleRotation.readFromBuffer(buffer)
        viewDirection = ParticleViewDirection.readFromBuffer(buffer)
        cameraMode = ParticleCameraMode.readFromBuffer(buffer)
        tinting = ParticleTinting.readFromBuffer(buffer)
        collision.readFromBuffer(buffer)
        environmentLighting = buffer.readBoolean()
        creationEvents = buffer.readList { SimpleEventTrigger("").also { it.decode(buffer) } }
        expirationEvents = buffer.readList { SimpleEventTrigger("").also { it.decode(buffer) } }
        timeline.decode(buffer)
    }
}