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
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.api.snowstorm.ParticleEmitterAction
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.particle.SnowstormParticleOptions
import com.cobblemon.mod.common.util.math.geometry.transformDirection
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.NoRenderParticle
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

/**
 * An instance of a bedrock particle effect.
 *
 * @author Hiroku
 * @since January 2nd, 2022
 */
class ParticleStorm(
    val effect: BedrockParticleOptions,
    val matrixWrapper: MatrixWrapper,
    val world: ClientLevel,
    val sourceVelocity: () -> Vec3 = { Vec3.ZERO },
    val sourceAlive: () -> Boolean = { true },
    val sourceVisible: () -> Boolean = { true },
    val onDespawn: () -> Unit = {},
    val runtime: MoLangRuntime = MoLangRuntime(),
    val entity: Entity? = null
): NoRenderParticle(world, matrixWrapper.getOrigin().x, matrixWrapper.getOrigin().y, matrixWrapper.getOrigin().z) {
    fun spawn() {
        if (entity != null) {
            runtime.environment.query
                .addFunction("entity_width") { DoubleValue(entity.boundingBox.xsize) }
                .addFunction("entity_height") { DoubleValue(entity.boundingBox.ysize) }
                .addFunction("entity_size") { DoubleValue(entity.boundingBox.run { if (xsize > ysize) xsize else ysize }) }
                .addFunction("entity_radius") { DoubleValue(entity.boundingBox.run { if (xsize > ysize) xsize else ysize } / 2) }
                .addFunction("entity_scale") {
                    val pokeEntity = entity as? PokemonEntity
                    val pokemon = pokeEntity?.pokemon
                    //Use form data if available, species as fall back
                    val baseScale = pokemon?.form?.baseScale ?: pokemon?.species?.baseScale ?: 1.0F
                    val pokemonScale = pokemon?.scaleModifier ?: 1.0F
                    val entityScale = pokeEntity?.scale ?: 1.0F
                    DoubleValue(baseScale * pokemonScale * entityScale)
                }
            if (entity is PosableEntity) {
                runtime.environment.query.addFunction("entity") { entity.struct }
            }
            // TODO replace with a generified call to if (entity is MoLangEntity) entity.applyVariables(env) or w/e
            runtime.environment.setSimpleVariable("entity_width", DoubleValue(entity.boundingBox.xsize))
            runtime.environment.setSimpleVariable("entity_height", DoubleValue(entity.boundingBox.ysize))
            val longerDiameter = entity.boundingBox.run { if (xsize > ysize) xsize else ysize }
            runtime.environment.setSimpleVariable("entity_size", DoubleValue(longerDiameter))
            runtime.environment.setSimpleVariable("entity_radius", DoubleValue(longerDiameter / 2))
            runtime.environment.setSimpleVariable("entity_scale", DoubleValue((entity as? PokemonEntity)?.scale ?: 1.0))
        }
        Minecraft.getInstance().particleEngine.add(this)
    }

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    fun getPrevX() = xo
    fun getPrevY() = yo
    fun getPrevZ() = zo

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

    val particleEffect = SnowstormParticleOptions(effect)

    init {
        runtime.execute(effect.emitter.startExpressions)
        effect.emitter.creationEvents.forEach { it.trigger(this, null) }
    }

    override fun getLifetime(): Int {
        return if (stopped) 0 else Int.MAX_VALUE
    }

    override fun remove() {
        super.remove()
        if (!despawned) {
            effect.emitter.expirationEvents.forEach { it.trigger(this, null) }
            despawned = true
            onDespawn()
        }
    }

    override fun tick() {
        setLifetime(getLifetime())
        super.tick()

        if (!hasPlayedOnce) {
            age = 0
            hasPlayedOnce = true
        }

        if (!sourceAlive() && !stopped) {
            stopped = true
            remove()
        }

        if (stopped || !sourceVisible()) {
            return
        }

        val pos = matrixWrapper.getOrigin()
        xo = x
        yo = y
        zo = z

        x = pos.x
        y = pos.y
        z = pos.z

        val oldDistanceTravelled = distanceTravelled
        distanceTravelled += Vec3(x - xo, y - yo, z - zo).length().toFloat()

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

    fun getNextParticleSpawnPosition(): Vec3 {
        runtime.environment.setSimpleVariable("particle_random_1", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_2", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_3", DoubleValue(Random.Default.nextDouble()))
        runtime.environment.setSimpleVariable("particle_random_4", DoubleValue(Random.Default.nextDouble()))

        val newPosition = transformPosition(effect.emitter.shape.getNewParticlePosition(runtime, entity))
        return newPosition
    }

    fun getNextParticleVelocity(nextParticlePosition: Vec3): Vec3 {
        val center = transformPosition(effect.emitter.shape.getCenter(runtime, entity))
        val initialVelocity = effect.particle.motion.getInitialVelocity(runtime, storm = this, particlePos = nextParticlePosition, emitterPos = center)
        return initialVelocity
            .scale(1 / 20.0)
            .add(if (effect.space.localVelocity) sourceVelocity() else Vec3.ZERO)
    }

    fun spawnParticle() {
        val newPosition = getNextParticleSpawnPosition()
        val velocity = getNextParticleVelocity(newPosition)

        contextStorm = this
        world.addParticle(particleEffect, newPosition.x, newPosition.y, newPosition.z, velocity.x, velocity.y, velocity.z)
        contextStorm = null
    }

    fun transformPosition(position: Vec3): Vec3 = matrixWrapper.transformPosition(position)

    fun transformDirection(direction: Vec3): Vec3 = matrixWrapper.matrix.transformDirection(direction)
}