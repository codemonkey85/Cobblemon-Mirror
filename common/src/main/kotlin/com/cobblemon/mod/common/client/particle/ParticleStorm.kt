/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.particle

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunction
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.species
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleEffect
import com.cobblemon.mod.common.api.snowstorm.ParticleEmitterAction
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.particle.SnowstormParticleEffect
import com.cobblemon.mod.common.util.math.geometry.transformDirection
import kotlin.random.Random
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.NoRenderParticle
import net.minecraft.client.util.ParticleUtil.spawnParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

/**
 * An instance of a bedrock particle effect.
 *
 * @author Hiroku
 * @since January 2nd, 2022
 */
class ParticleStorm(
    val effect: BedrockParticleEffect,
    val matrixWrapper: MatrixWrapper,
    val world: ClientWorld,
    val sourceVelocity: () -> Vec3d = { Vec3d.ZERO },
    val sourceAlive: () -> Boolean = { true },
    val sourceVisible: () -> Boolean = { true },
    val onDespawn: () -> Unit = {},
    val runtime: MoLangRuntime = MoLangRuntime(),
    val entity: Entity? = null
): NoRenderParticle(world, matrixWrapper.getOrigin().x, matrixWrapper.getOrigin().y, matrixWrapper.getOrigin().z) {
    fun spawn() {
        if (entity != null) {
            runtime.environment.getQueryStruct()
                .addFunction("entity_width") { DoubleValue(entity.boundingBox.xLength) }
                .addFunction("entity_height") { DoubleValue(entity.boundingBox.yLength) }
                .addFunction("entity_size") { DoubleValue(entity.boundingBox.run { if (xLength > yLength) xLength else yLength }) }
                .addFunction("entity_radius") { DoubleValue(entity.boundingBox.run { if (xLength > yLength) xLength else yLength } / 2) }
                .addFunction("entity_scale") {
                    val pokeEntity = entity as? PokemonEntity
                    val pokemon = pokeEntity?.pokemon
                    //Use form data if available, species as fall back
                    val baseScale = pokemon?.form?.baseScale ?: pokemon?.species?.baseScale ?: 1.0F
                    val pokemonScale = pokemon?.scaleModifier ?: 1.0F
                    val entityScale = pokeEntity?.scaleFactor ?: 1.0F
                    DoubleValue(baseScale * pokemonScale * entityScale)
                }
            // TODO replace with a generified call to if (entity is MoLangEntity) entity.applyVariables(env) or w/e
            runtime.environment.setSimpleVariable("entity_width", DoubleValue(entity.boundingBox.xLength))
            runtime.environment.setSimpleVariable("entity_height", DoubleValue(entity.boundingBox.yLength))
            val longerDiameter = entity.boundingBox.run { if (xLength > yLength) xLength else yLength }
            runtime.environment.setSimpleVariable("entity_size", DoubleValue(longerDiameter))
            runtime.environment.setSimpleVariable("entity_radius", DoubleValue(longerDiameter / 2))
            runtime.environment.setSimpleVariable("entity_scale", DoubleValue((entity as? PokemonEntity)?.scaleFactor ?: 1.0))
        }
        MinecraftClient.getInstance().particleManager.addParticle(this)
    }

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    fun getPrevX() = prevPosX
    fun getPrevY() = prevPosY
    fun getPrevZ() = prevPosZ

    val particles = mutableListOf<SnowstormParticle>()
    var started = false
    var stopped = false
    var despawned = false
    // The idea is that some instantaneous particle effects could teeeechnically be over before they start.
    var hasPlayedOnce = false

    var distanceTravelled = 0F

    companion object {
        var contextStorm: ParticleStorm? = null
    }

    val particleEffect = SnowstormParticleEffect(effect)

    init {
        runtime.execute(effect.emitter.startExpressions)
        effect.emitter.creationEvents.forEach { it.trigger(this, null) }
    }

    override fun getMaxAge(): Int {
        return if (stopped) 0 else Int.MAX_VALUE
    }

    override fun markDead() {
        super.markDead()
        if (!despawned) {
            effect.emitter.expirationEvents.forEach { it.trigger(this, null) }
            despawned = true
            onDespawn()
        }
    }

    override fun tick() {
        setMaxAge(getMaxAge())
        super.tick()

        if (!hasPlayedOnce) {
            age = 0
            hasPlayedOnce = true
        }

        if (!sourceAlive() && !stopped) {
            stopped = true
            markDead()
        }

        if (stopped || !sourceVisible()) {
            return
        }

        val pos = matrixWrapper.getOrigin()
        prevPosX = x
        prevPosY = y
        prevPosZ = z

        x = pos.x
        y = pos.y
        z = pos.z

        val oldDistanceTravelled = distanceTravelled
        distanceTravelled += Vec3d(x - prevPosX, y - prevPosY, z - prevPosZ).length().toFloat()

        effect.emitter.travelDistanceEvents.check(this, null, oldDistanceTravelled.toDouble(), distanceTravelled.toDouble())
        effect.emitter.loopingTravelDistanceEvents.forEach { it.check(this, null, oldDistanceTravelled.toDouble(), distanceTravelled.toDouble()) }
        effect.emitter.eventTimeline.check(this, null, (age - 1) / 20.0, age / 20.0)

        runtime.environment.setSimpleVariable("emitter_random_1", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_2", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_3", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_random_4", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("emitter_age", DoubleValue(age / 20.0))
        runtime.execute(effect.emitter.updateExpressions)

        when (effect.emitter.lifetime.getAction(runtime, started, age / 20.0)) {
            ParticleEmitterAction.GO -> {
                effect.curves.forEach { it.apply(runtime) }
                val toEmit = effect.emitter.rate.getEmitCount(runtime, started, particles.size)
                started = true
                repeat(times = toEmit) { spawnParticle() }
            }
            ParticleEmitterAction.NOTHING -> {}
            ParticleEmitterAction.STOP -> stopped = true
            ParticleEmitterAction.RESET -> started = false
        }
    }

    fun getNextParticleSpawnPosition(): Vec3d {
        runtime.environment.setSimpleVariable("particle_random_1", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_2", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_3", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_4", DoubleValue(Random.Default.nextDouble()))

        val newPosition = transformPosition(effect.emitter.shape.getNewParticlePosition(runtime, entity))
        return newPosition
    }

    fun getNextParticleVelocity(nextParticlePosition: Vec3d): Vec3d {
        val center = transformPosition(effect.emitter.shape.getCenter(runtime, entity))
        val initialVelocity = effect.particle.motion.getInitialVelocity(runtime, storm = this, particlePos = nextParticlePosition, emitterPos = center)
        return initialVelocity
            .multiply(1 / 20.0)
            .add(if (effect.space.localVelocity) sourceVelocity() else Vec3d.ZERO)
    }

    fun spawnParticle() {
        val newPosition = getNextParticleSpawnPosition()
        val velocity = getNextParticleVelocity(newPosition)

        contextStorm = this
        world.addParticle(particleEffect, newPosition.x, newPosition.y, newPosition.z, velocity.x, velocity.y, velocity.z)
        contextStorm = null
    }

    fun transformPosition(position: Vec3d): Vec3d = matrixWrapper.transformPosition(position)
    fun transformDirection(direction: Vec3d): Vec3d = matrixWrapper.matrix.transformDirection(direction)
}
