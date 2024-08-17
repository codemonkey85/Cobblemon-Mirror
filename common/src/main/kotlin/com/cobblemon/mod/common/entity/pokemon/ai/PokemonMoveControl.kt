/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getWaterAndLavaIn
import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.resolveFloat
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class PokemonMoveControl(val pokemonEntity: PokemonEntity) : MoveControl(pokemonEntity) {
    companion object {
        const val VERY_CLOSE = 2.500000277905201E-3
    }

    val runtime = MoLangRuntime().also {
        it.environment.query.addStandardFunctions().addFunctions(pokemonEntity.struct.functions)
    }

    override fun tick() {
        if (pokemonEntity.pokemon.status?.status == Statuses.SLEEP || pokemonEntity.isDeadOrDying) {
            pokemonEntity.speed = 0F
            pokemonEntity.yya = 0F
            return
        }

        val behaviour = pokemonEntity.behaviour
        val mediumSpeed = runtime.resolveFloat(if (pokemonEntity.getCurrentPoseType() in setOf(PoseType.FLY, PoseType.HOVER)) {
            behaviour.moving.fly.flySpeedHorizontal
        } else if (pokemonEntity.isEyeInFluid(FluidTags.WATER) || pokemonEntity.isEyeInFluid(FluidTags.LAVA)) {
            behaviour.moving.swim.swimSpeed
        } else {
           behaviour.moving.walk.walkSpeed
        })

        val baseSpeed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED).toFloat() * this.speedModifier.toFloat()
        val adjustedSpeed = baseSpeed * mediumSpeed

        if (operation == Operation.STRAFE) {
            var movingDistanceTotal = Mth.sqrt(strafeForwards * strafeForwards + strafeRight * strafeRight)
            if (movingDistanceTotal < 1.0f) {
                movingDistanceTotal = 1.0f
            }

            movingDistanceTotal = adjustedSpeed / movingDistanceTotal

            val adjustedForward = strafeForwards * movingDistanceTotal
            val adjustedStrafe = strafeRight * movingDistanceTotal

            val xComponent = -Mth.sin(mob.yRot.toRadians())
            val zComponent = Mth.cos(mob.yRot.toRadians())
            val xMovement = adjustedForward * zComponent - adjustedStrafe * xComponent
            val zMovement = adjustedStrafe * zComponent + adjustedForward * xComponent
            if (!isWalkable(xMovement, zMovement)) {
                strafeForwards = 1.0f
                strafeRight = 0.0f
            }
            mob.speed = adjustedSpeed
            mob.setZza(strafeForwards)
            mob.setXxa(strafeRight)
            operation = Operation.WAIT
        } else if (operation == Operation.MOVE_TO) {
            // Don't instantly move to WAIT for fluid movements as they overshoot their mark.
            if (!pokemonEntity.isFlying() && !pokemonEntity.isSwimming) {
                operation = Operation.WAIT
            }
            var xDist = wantedX - mob.x
            var zDist = wantedZ - mob.z
            var yDist = wantedY - mob.y

            if (xDist * xDist + yDist * yDist + zDist * zDist < VERY_CLOSE) {
                // If we're close enough, pull up stumps here.
                mob.setZza(0F)
                mob.yya = 0F
                // If we're super close and we're fluid movers, forcefully stop moving so you don't overshoot
                if ((pokemonEntity.isFlying() || pokemonEntity.isSwimming)) {
                    operation = Operation.WAIT
                    mob.deltaMovement = Vec3.ZERO
                }
                return
            }

            val horizontalDistanceFromTarget = xDist * xDist + zDist * zDist
            val closeHorizontally = horizontalDistanceFromTarget < VERY_CLOSE
            if (!closeHorizontally) {
                val angleToTarget = Mth.atan2(zDist, xDist).toDegrees() - 90.0f
                val currentMovingAngle = mob.yRot
                val steppedAngle = Mth.approachDegrees(currentMovingAngle, angleToTarget,  100 * mediumSpeed)
                mob.yRot = steppedAngle
            }

            val (inWater, inLava) = mob.level().getWaterAndLavaIn(mob.boundingBox)
            val inFluid = inWater || inLava
            var verticalHandled = false
            val blockPos = mob.blockPosition()
            val blockState = mob.level().getBlockState(blockPos)
            val voxelShape = blockState.getCollisionShape(mob.level(), blockPos)

            if (pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) || inFluid) {
                verticalHandled = true
                mob.yya = 0F
                mob.speed = 0F
                // Refinement is to prevent the entity from spinning around trying to get to a super precise location.
                val refine: (Double) -> Double = { if (abs(it) < 0.05) 0.0 else it }

                val fullDistance = Vec3(
                    xDist,
                    refine(yDist + 0.05), // + 0.05 for dealing with swimming out of water, they otherwise get stuck on the lip
                    zDist
                )

                val direction = fullDistance.normalize()

                val scale = min(adjustedSpeed.toDouble(), fullDistance.length())

                mob.deltaMovement = direction.scale(scale)

                xDist = fullDistance.x
                zDist = fullDistance.z
                yDist = fullDistance.y
            } else {
                // division is to slow the speed down a bit so they don't overshoot when they get there.
                val forwardSpeed = min(adjustedSpeed, max(horizontalDistanceFromTarget.toFloat() / 2, 0.15F))
                mob.speed = forwardSpeed
            }

            if (!verticalHandled) {
                val tooBigToStep = yDist > pokemonEntity.behaviour.moving.stepHeight
                val xComponent = -Mth.sin(mob.yRot.toRadians()).toDouble()
                val zComponent = Mth.cos(mob.yRot.toRadians()).toDouble()

                val motion = Vec3(xComponent, 0.0, zComponent).normalize()
                val offset = motion.scale(mob.speed.toDouble())
                val closeEnoughToJump = !mob.isFree(offset.x, 0.0, offset.z)// sqrt(xDist * xDist + zDist * zDist) - 0.5 <  entity.width / 2 + entity.movementSpeed * 4

                if (tooBigToStep &&
                    closeEnoughToJump ||
                    !voxelShape.isEmpty && mob.y < voxelShape.max(Direction.Axis.Y) + blockPos.y.toDouble() &&
                    !blockState.`is`(BlockTags.DOORS) &&
                    !blockState.`is`(BlockTags.FENCES)
                ) {
                    mob.jumpControl.jump()
                    operation = Operation.JUMPING
                }
            }

            if (closeHorizontally && abs(yDist) < VERY_CLOSE) {
                operation = Operation.WAIT
            }
        } else if (operation == Operation.JUMPING) {
            mob.speed = adjustedSpeed
            mob.yya = 0F
            if (mob.onGround() || pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                operation = Operation.WAIT
            }
        } else {
            mob.setZza(0.0f)
            mob.yya = 0F
        }

        if (operation == Operation.WAIT && !mob.navigation.isInProgress) {
            if (mob.onGround() && behaviour.moving.walk.canWalk && pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                pokemonEntity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            }

            // Float
            if (mob.isEyeInFluid(FluidTags.WATER) && behaviour.moving.swim.canSwimInWater) {
                pokemonEntity.yya = 0.2F
            }
        }
    }

    private fun isWalkable(xMovement: Float, zMovement: Float): Boolean {
        val entityNavigation = mob.navigation
        val pathNodeMaker = entityNavigation.nodeEvaluator
        return pathNodeMaker.getPathType(
            mob,
            BlockPos(
                Mth.floor(mob.x + xMovement.toDouble()),
                mob.blockY,
                Mth.floor(mob.z + zMovement.toDouble())
            )
        ) == PathType.WALKABLE
    }
}