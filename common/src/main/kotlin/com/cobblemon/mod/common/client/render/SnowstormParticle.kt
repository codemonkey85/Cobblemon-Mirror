/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.ModAPI
import com.cobblemon.mod.common.api.snowstorm.ParticleMaterial
import com.cobblemon.mod.common.api.snowstorm.UVDetails
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.abs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.ParticleTextureSheet.NO_RENDER
import net.minecraft.client.particle.ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import org.joml.AxisAngle4d
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3f

class SnowstormParticle(
    val storm: ParticleStorm,
    world: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    initialVelocity: Vec3d,
    var invisible: Boolean = false
) : Particle(world, x, y, z) {
    companion object {
        const val MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION = 0.005
    }

    val sprite = getSpriteFromAtlas()

    val particleTextureSheet: ParticleTextureSheet
    var angularVelocity = 0.0
    var colliding = false

    var texture = storm.effect.particle.texture

    var variableStruct = (storm.runtime.environment.structs["variable"] as VariableStruct);

    val random1 = variableStruct.map["particle_random_1"]
    val random2 = variableStruct.map["particle_random_2"]
    val random3 = variableStruct.map["particle_random_3"]
    val random4 = variableStruct.map["particle_random_4"]

    var localX = x - storm.getX()
    var localY = y - storm.getY()
    var localZ = z - storm.getZ()
    var rotatedLocal = Vector3d(localX, localY, localZ)

    var prevLocalX = localX
    var prevLocalY = localY
    var prevLocalZ = localZ
    var prevRotatedLocal = Vector3d(localX, localY, localZ)

    val currentRotation = AxisAngle4d(0.0, 0.0, 1.0, 0.0)

    var oldAxisRotation = AxisAngle4d(0.0, 0.0, 1.0, 0.0)
    var axisRotation = AxisAngle4d(0.0, 0.0, 1.0, 0.0)


    val uvDetails = UVDetails()

    var viewDirection = Vec3d.ZERO
    var originPos = Vec3d(storm.getX(), storm.getY(), storm.getZ())

    fun getX() = x
    fun getY() = y
    fun getZ() = z

    fun getVelocityX() = velocityX
    fun getVelocityY() = velocityY
    fun getVelocityZ() = velocityZ

    fun getSpriteFromAtlas(): Sprite {
        val atlas = MinecraftClient.getInstance().particleManager.particleAtlasTexture

//        val field = atlas::class.java.getDeclaredField("sprites")
//        field.isAccessible = true
//        val map = field.get(atlas) as Map<Identifier, Sprite>
//        println(map.keys.joinToString { it.toString() })
        val sprite = atlas.getSprite(storm.effect.particle.texture)
//        println(storm.effect.particle.texture)
        return sprite
    }

    private fun applyRandoms() {
        variableStruct.setDirectly("particle_random_1", random1)
        variableStruct.setDirectly("particle_random_2", random2)
        variableStruct.setDirectly("particle_random_3", random3)
        variableStruct.setDirectly("particle_random_4", random4)
    }

    init {
        setVelocity(initialVelocity.x, initialVelocity.y, initialVelocity.z)
        angle = -storm.effect.particle.rotation.getInitialRotation(storm.runtime).toFloat()
        prevAngle = angle
        angularVelocity = storm.effect.particle.rotation.getInitialAngularVelocity(storm.runtime)
        velocityMultiplier = 1F
        maxAge = (storm.runtime.resolveDouble(storm.effect.particle.maxAge) * 20).toInt()
        storm.particles.add(this)
        gravityStrength = 0F
        particleTextureSheet = if (invisible) NO_RENDER else PARTICLE_SHEET_TRANSLUCENT
        storm.effect.particle.creationEvents.forEach { it.trigger(storm, this) }
//            when (storm.effect.particle.material) {
//            ParticleMaterial.ALPHA -> ParticleMaterials.ALPHA
//            ParticleMaterial.OPAQUE -> ParticleMaterials.OPAQUE
//            ParticleMaterial.BLEND -> ParticleMaterials.BLEND
//            ParticleMaterial.ADD -> ParticleMaterials.ADD
//        }
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (Cobblemon.implementation.modAPI != ModAPI.FORGE) {
           if (!MinecraftClient.getInstance().worldRenderer.frustum.isVisible(boundingBox)) {
               return
           }
        }

        applyRandoms()
        setParticleAgeInRuntime()
        storm.effect.curves.forEach { it.apply(storm.runtime) }
        storm.runtime.execute(storm.effect.particle.renderExpressions)

//        // TODO need to implement the other materials but not sure exactly what they are GL wise
        when (storm.effect.particle.material) {
            // Alpha is the usual effect of "Cutout", this needs a shader but fabric fucking sucks so... Ignoring it.
            ParticleMaterial.ALPHA -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
            ParticleMaterial.OPAQUE -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ZERO)
            ParticleMaterial.BLEND -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
            ParticleMaterial.ADD -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE)
        }

        vertexConsumer as BufferBuilder

        val vec3d = camera.pos

        val interpLocalX = MathHelper.lerp(tickDelta.toDouble(), prevLocalX, localX)
        val interpLocalY = MathHelper.lerp(tickDelta.toDouble(), prevLocalY, localY)
        val interpLocalZ = MathHelper.lerp(tickDelta.toDouble(), prevLocalZ, localZ)

        val pos = if (storm.effect.space.localRotation) {
            val interpRotation = MathHelper.lerp(tickDelta.toDouble(), 0.0, currentRotation.angle)
            val vec = Vector3d(interpLocalX, interpLocalY, interpLocalZ)
            oldAxisRotation.transform(vec)
            currentRotation.get(AxisAngle4d()).also { it.angle = interpRotation }.transform(vec)
        } else {
            Vector3d(interpLocalX, interpLocalY, interpLocalZ)
        }

        val f = (pos.x + originPos.x - vec3d.getX()).toFloat()
        val g = (pos.y + originPos.y - vec3d.getY()).toFloat()
        val h = (pos.z + originPos.z - vec3d.getZ()).toFloat()
        val quaternion = storm.effect.particle.cameraMode.getRotation(
            matrixWrapper = storm.matrixWrapper,
            prevAngle = prevAngle,
            angle = angle,
            deltaTicks = tickDelta,
            particlePosition = Vec3d(x, y, z),
            cameraPosition = camera.pos,
            cameraAngle = camera.rotation,
            cameraYaw = camera.yaw,
            cameraPitch = camera.pitch,
            viewDirection = viewDirection
        )

        val xSize = storm.runtime.resolveDouble(storm.effect.particle.sizeX).toFloat() / 1.5.toFloat()
        val ySize = storm.runtime.resolveDouble(storm.effect.particle.sizeY).toFloat() / 1.5.toFloat()

        val particleVertices = arrayOf(
            Vector3f(-xSize, -ySize, 0.0f),
            Vector3f(-xSize, ySize, 0.0f),
            Vector3f(xSize, ySize, 0.0f),
            Vector3f(xSize, -ySize, 0.0f)
        )

        for (k in 0..3) {
            val vertex = particleVertices[k]
            vertex.rotate(quaternion)
            vertex.add(f, g, h)
        }

        val uvs = storm.effect.particle.uvMode.get(storm.runtime, age / 20.0, maxAge / 20.0, uvDetails)
        val colour = storm.effect.particle.tinting.getTint(storm.runtime)

        val spriteURange = sprite.maxU - sprite.minU
        val spriteVRange = sprite.maxV - sprite.minV

        val minU = uvs.startU * spriteURange + sprite.minU
        val maxU = uvs.endU * spriteURange + sprite.minU
        val minV = uvs.startV * spriteVRange + sprite.minV
        val maxV = uvs.endV * spriteVRange + sprite.minV

        val p = if (storm.effect.particle.environmentLighting) getBrightness(tickDelta) else (15 shl 20 or (15 shl 4))
        vertexConsumer
            .vertex(particleVertices[0].x.toDouble(), particleVertices[0].y.toDouble(), particleVertices[0].z.toDouble())
            .texture(maxU, maxV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[1].x.toDouble(), particleVertices[1].y.toDouble(), particleVertices[1].z.toDouble())
            .texture(maxU, minV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[2].x.toDouble(), particleVertices[2].y.toDouble(), particleVertices[2].z.toDouble())
            .texture(minU, minV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
        vertexConsumer
            .vertex(particleVertices[3].x.toDouble(), particleVertices[3].y.toDouble(), particleVertices[3].z.toDouble())
            .texture(minU, maxV)
            .color(colour.x, colour.y, colour.z, colour.w)
            .light(p)
            .next()
    }

    fun runExpirationEvents() {
        storm.effect.particle.expirationEvents.forEach { it.trigger(storm, this)}
    }

    override fun tick() {
        if (storm.effect.space.localPosition) {
            originPos = storm.matrixWrapper.getOrigin()
        }

        applyRandoms()
        setParticleAgeInRuntime()
        storm.effect.curves.forEach { it.apply(storm.runtime) }
        storm.runtime.execute(storm.effect.particle.updateExpressions)
        angularVelocity = storm.effect.particle.rotation.getAngularVelocity(storm.runtime, -angle.toDouble(), angularVelocity) / 20

        if (age >= maxAge || storm.runtime.resolveBoolean(storm.effect.particle.killExpression)) {
            runExpirationEvents()
            markDead()
            return
        } else {
            val velocity = storm.effect.particle.motion.getVelocity(storm.runtime, this, Vec3d(velocityX, velocityY, velocityZ))
            velocityX = velocity.x
            velocityY = velocity.y
            velocityZ = velocity.z
            prevAngle = angle
            // Subtract because Bedrock particles are counter-clockwise and Java Edition is clockwise.
            angle = prevAngle - angularVelocity.toFloat()
        }

        viewDirection = storm.effect.particle.viewDirection.getDirection(
            runtime = storm.runtime,
            lastDirection = viewDirection,
            currentVelocity = Vec3d(velocityX, velocityY, velocityZ)
        ).normalize()

        prevPosX = x
        prevPosY = y
        prevPosZ = z

        prevLocalX = localX
        prevLocalY = localY
        prevLocalZ = localZ

        oldAxisRotation = axisRotation
        prevRotatedLocal = oldAxisRotation.transform(Vector3d(prevLocalX, prevLocalY, prevLocalZ))

        storm.matrixWrapper.matrix.getRotation(axisRotation)
        rotatedLocal = axisRotation.transform(Vector3d(prevLocalX, prevLocalY, prevLocalZ))
        Quaterniond().rotateTo(prevRotatedLocal, rotatedLocal).get(currentRotation)

        age++

        this.move(velocityX, velocityY, velocityZ)

        storm.effect.particle.timeline.check(storm, this, (age - 1) / 20.0, age / 20.0)
    }

    override fun move(dx: Double, dy: Double, dz: Double) {
        val collision = storm.effect.particle.collision
        val radius = storm.runtime.resolveDouble(collision.radius)
        boundingBox = Box.of(Vec3d(x, y, z), radius, radius, radius)
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) {
            updatePosition()
            return
        }

        var dx = dx
        var dy = dy
        var dz = dz

        if (storm.runtime.resolveBoolean(collision.enabled) && radius > 0.0 && !storm.effect.space.isLocalSpace) {
            collidesWithWorld = true

            val newMovement = checkCollision(Vec3d(dx, dy, dz))

            if (dead) {
                return
            }

            dx = newMovement.x
            dy = newMovement.y
            dz = newMovement.z

//            if (collidesWithWorld && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < 10000) {
//                val vec3d = Entity.adjustMovementForCollisions(
//                    null,
//                    Vec3d(dx, dy, dz),
//                    boundingBox,
//                    world,
//                    listOf()
//                )
//
//            }

            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                boundingBox = boundingBox.offset(dx, dy, dz)
                localX += dx
                localY += dy
                localZ += dz
            }

//            if (abs(dy) >= 9.999999747378752E-6 && abs(dy) < 9.999999747378752E-6) {
//                field_21507 = true
//            }
//            onGround = dy != dy && e < 0.0
//            if (d != dx) {
//                velocityX = 0.0
//            }
//            if (dz != dz) {
//                velocityZ = 0.0
//            }
        } else {
            collidesWithWorld = false
            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                localX += dx
                localY += dy
                localZ += dz
            }
        }
        updatePosition()
    }

    fun updatePosition() {
        val localVector = if (storm.effect.space.localRotation) storm.transformDirection(Vec3d(localX, localY, localZ)) else Vec3d(localX, localY, localZ)
        x = localVector.x + originPos.x
        y = localVector.y + originPos.y
        z = localVector.z + originPos.z
    }

    private fun checkCollision(movement: Vec3d): Vec3d {
        val collision = storm.effect.particle.collision
        var box = boundingBox
        val bounciness = storm.runtime.resolveDouble(collision.bounciness)
        val friction = storm.runtime.resolveDouble(collision.friction)
        val expiresOnContact = collision.expiresOnContact

        val collisions = world.getBlockCollisions(null, box.stretch(movement))
        if (collisions.none()) {
            colliding = false
            return movement
        } else if (expiresOnContact) {
            runExpirationEvents()
            markDead()
            return movement
        }

//        println("Collisions with Y values: ${collisionProvider.map { it.boundingBox.center.y }.distinct().joinToString() }")

        var xMovement = movement.x
        var yMovement = movement.y
        var zMovement = movement.z

        var bouncing = false
        var sliding = false

        if (yMovement != 0.0) {
//            // If it would have avoided collisions if not for the Y movement, then it's bouncing off a vertical-normal surface
//            val originalCollisionYs = collisionProvider.map { it.boundingBox.center.y }.distinct()
//            val yCollisions = world.getBlockCollisions(null, box.stretch(movement.multiply(1.0, 0.0, 1.0))).toList()
////            println("Compared to new Y values: ${yCollisions.map { it.boundingBox.center.y }.distinct().joinToString()}")
//            if (yCollisions.none { it.boundingBox.center.y in originalCollisionYs }) {
//                yMovement = 0.0
//                if (bounciness > 0.0 && abs(movement.y) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
//                    velocityY *= -1 * bounciness
//                    yMovement = -1 * bounciness * movement.y
//                    bouncing = true
//                } else if (friction > 0.0) {
//                    sliding = true
//                    velocityY = 0.0
//                } else {
//                    velocityY = 0.0
//                }
//            } else {
//            }


            yMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, box, collisions, yMovement)
            if (yMovement != 0.0) {
                box = box.offset(0.0, 0.0, zMovement)
            } else {
                if (bounciness > 0.0 && abs(movement.y) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityY *= -1 * bounciness
                    yMovement = -1 * bounciness * movement.y
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityY = 0.0
                } else {
                    velocityY = 0.0
                }
            }
        }

        val mostlyIsZMovement = abs(xMovement) < abs(zMovement)
        if (mostlyIsZMovement && zMovement != 0.0) {
            zMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, collisions, zMovement)
            if (zMovement != 0.0) {
                box = box.offset(0.0, 0.0, zMovement)
            } else {
                if (bounciness > 0.0 && abs(movement.z) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityZ *= -1 * bounciness
                    zMovement = -1 * bounciness * movement.z
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }

        if (xMovement != 0.0) {
            xMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.X, box, collisions, xMovement)
            if (!mostlyIsZMovement && xMovement != 0.0) {
                box = box.offset(xMovement, 0.0, 0.0)
            } else {
                if (bounciness > 0.0 && abs(movement.x) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityX *= -1 * bounciness
                    xMovement = -1 * bounciness * movement.x
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }

        if (!mostlyIsZMovement && zMovement != 0.0) {
            zMovement = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, collisions, zMovement)
            if (zMovement != 0.0) {
            } else {
                if (bounciness > 0.0 && abs(movement.z) > MAXIMUM_DISTANCE_CHANGE_PER_TICK_FOR_FRICTION) {
                    velocityZ *= -1 * bounciness
                    zMovement = -1 * bounciness * movement.z
                    bouncing = true
                } else if (friction > 0.0) {
                    sliding = true
                    velocityZ = 0.0
                } else {
                    velocityZ = 0.0
                }
            }
        }

        var newMovement = Vec3d(xMovement, yMovement, zMovement)

        if (sliding && !bouncing) {
            // If it's moving slower than the friction per second, time to stop
            newMovement = if (newMovement.length() * 20 < friction) {
                Vec3d.ZERO
            } else {
                newMovement.subtract(newMovement.normalize().multiply(friction / 20))
            }

            var velocity = Vec3d(velocityX, velocityY, velocityZ)
            if (velocity.length() * 20 < friction) {
                setVelocity(0.0, 0.0, 0.0)
            } else {
                velocity = velocity.subtract(velocity.normalize().multiply(friction / 20))
                setVelocity(velocity.x, velocity.y, velocity.z)
            }
        }

        return newMovement
    }


    private fun setParticleAgeInRuntime() {
        variableStruct.setDirectly("particle_age", DoubleValue(age / 20.0))
        variableStruct.setDirectly("particle_lifetime", DoubleValue(maxAge / 20.0))
    }

    override fun getType() = particleTextureSheet

    override fun markDead() {
        super.markDead()
        storm.particles.remove(this)
    }
}