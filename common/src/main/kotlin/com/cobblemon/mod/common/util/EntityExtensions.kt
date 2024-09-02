/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes

fun Entity.effectiveName() = this.displayName ?: this.name

fun Entity.setPositionSafely(pos: Vec3): Boolean {
    // TODO: Rework this function. Best way to do it would be to define a vertical and horizontal min/max shift based on the Pokemon's hitbox.
    // Then loop through horizontal/vertical shifts, and detect collisions in three categories: suffocation, damaging blocks, and general collision
    // The closest position with the least severe collision types will be selected to move the Pokemon to
    // The throw could be cancelled if there are no viable locations without severe problems

    // Optional: use getBlockCollisions iterator and VoxelShapes.combineAndSimplify to create a single cube to represent collision area
    // Use that cube to "push" the Pokemon out of the wall at an angle
    // Note: may not work well with L shape wall collisions
//    val collisions = world.getBlockCollisions(this, box).iterator()
//    if (collisions.hasNext()) {
//        var collisionShape = collisions.next()
//        while (collisions.hasNext()) {
//            collisionShape = VoxelShapes.union(collisionShape, collisions.next())
//            println(collisionShape)
//        }
//    } else {
//        setPosition(pos)
//        return true
//    }

    var result = pos
    val eyes = pos.with(Direction.Axis.Y, pos.y + this.eyeHeight)

    var box = boundingBox.move(pos)
    val conflicts = mutableSetOf<Direction>()

    if (!level().getBlockCollisions(this, box).iterator().hasNext()) {
        setPos(pos)
        return true
    }

    for (target in BlockPos.betweenClosedStream(box)) {
        val blockState = this.level().getBlockState(target)
        val collides = !blockState.isAir &&
                blockState.isSuffocating(this.level(), target) &&
                Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level(), target)
                    .move(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    Shapes.create(box),
                    BooleanOp.AND
                )
        if (collides) {
            val x = eyes.toBlockPos()
            for (direction in Direction.entries) {
                if (conflicts.contains(direction)) continue

                val conflict = target.toVec3d()
                if (x.offset(direction.normal) == target) {
                    conflicts.add(direction)
                    when (direction) {
                        Direction.UP -> return false
                        Direction.NORTH -> {
                            result = result.add(Vec3(0.0, 0.0, 1 + (conflict.z - box.minZ + (1.0 / 8.0))))
                        }
                        Direction.SOUTH -> {
                            result = result.add(Vec3(0.0, 0.0, (conflict.z - box.maxZ) - (1.0 / 8.0)))
                        }
                        Direction.WEST -> {
                            result = result.add(Vec3(1 + (conflict.x - box.minX  + (1.0 / 8.0)), 0.0, 0.0))
                        }
                        Direction.EAST -> {
                            result = result.add(Vec3((conflict.x - box.maxX) - (1.0 / 8.0), 0.0, 0.0))
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    box = boundingBox.move(result)
    if (!level().getBlockCollisions(this, box).iterator().hasNext()) {
        this.setPos(result)
        return true
    } else {
        val yChanges = listOf(1.0, -1.0, 2.0, -2.0)
        var previousChange = 0.0

        // If the Pokemon still collides with blocks after horizontal shifting, try vertical shifting from the shifted position
        for (yChange in yChanges) {
            box = box.move(0.0, yChange - previousChange, 0.0)
            previousChange = yChange
            if (level().getBlockCollisions(this, box).iterator().hasNext()) {
                continue
            } else {
                val roundedY = (result.y + yChange).toInt()
                box = box.move(0.0, roundedY - result.y, 0.0)
                // If the rounded position actually collides again, then don't round at all.
                if (level().getBlockCollisions(this, box).iterator().hasNext()) {
                    result = result.add(0.0, yChange, 0.0)
                    this.setPos(result)
                    return true
                } else {
                    result = Vec3(result.x, roundedY.toDouble(), result.z)
                    this.setPos(result)
                    return true
                }
            }
        }

        // If vertical shifting from the new position still collides, try vertical shifting from the original pos
        previousChange = 0.0
        box = boundingBox.move(pos)
        for (yChange in yChanges) {
            box = box.move(0.0, yChange - previousChange, 0.0)
            previousChange = yChange
            if (level().getBlockCollisions(this, box).iterator().hasNext()) {
                continue
            } else {
                val roundedY = (result.y + yChange).toInt()
                box = box.move(0.0, roundedY - result.y, 0.0)
                // If the rounded position actually collides again, then don't round at all.
                if (level().getBlockCollisions(this, box).iterator().hasNext()) {
                    result = result.add(0.0, yChange, 0.0)
                    this.setPos(result)
                    return true
                } else {
                    result = Vec3(result.x, roundedY.toDouble(), result.z)
                    this.setPos(result)
                    return true
                }
            }
        }
    }

    if (conflicts.size >= 3) {
        this.setPos(pos)
    }

    // This final check guarantees that the sendout will return to the original position if the Pokemon will suffocate in the new one
    // This will only happen if the horizontal shift moved the Pokemon into a suffocating position, and there was no valid vertical shift
    val resultEyes = result.with(Direction.Axis.Y, result.y + this.eyeHeight)
    val resultEyeBox = AABB.ofSize(resultEyes, bbWidth.toDouble(), 1.0E-6, bbWidth.toDouble())
    var collides = false

    for (target in BlockPos.betweenClosedStream(resultEyeBox)) {
        val blockState = this.level().getBlockState(target)
        collides = !blockState.isAir &&
                blockState.isSuffocating(this.level(), target) &&
                Shapes.joinIsNotEmpty(
                    blockState.getCollisionShape(this.level(), target)
                        .move(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    Shapes.create(box),
                    BooleanOp.AND
                )
        if (collides) break
    }
    if (collides) {
        this.setPos(pos)
        return true
    } else {
        this.setPos(result)
        return true
    }
}

fun Entity.isStandingOnSandOrRedSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.level().getBlockState(blockPosition().below(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
        if (sandBlock == Blocks.RED_SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
    }
    return false
}

fun Entity.isDusk(): Boolean {
    val time = level().dayTime % 24000
    return time in 12000..13000
}

fun Entity.isStandingOnSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.level().getBlockState(blockPosition().below(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
    }
    return false
}

fun Entity.isStandingOnRedSand(): Boolean {
    val redSandDepth = 2 // Define the depth you want to check
    for (i in 1..redSandDepth) {
        val redSandBlockState = this.level().getBlockState(blockPosition().below(i))
        val redSandBlock = redSandBlockState.block
        if (redSandBlock == Blocks.RED_SAND && !redSandBlockState.isAir && redSandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(i))) {
            return true
        }
    }
    return false
}

fun Entity.distanceTo(pos: BlockPos): Double {
    val difference = pos.toVec3d().subtract(this.position())
    return difference.length()
}

fun Entity.closestPosition(positions: Iterable<BlockPos>, filter: (BlockPos) -> Boolean = { true }): BlockPos? {
    var closest: BlockPos? = null
    var closestDistance = Double.MAX_VALUE

    val iterator = positions.iterator()
    while (iterator.hasNext()) {
        val position = iterator.next()
        if (filter(position)) {
            val distance = distanceTo(position)
            if (distance < closestDistance) {
                closest = BlockPos(position)
                closestDistance = distance
            }
        }
    }

    return closest
}

fun Entity.getIsSubmerged() = isInLava || isUnderWater

fun <T> SynchedEntityData.update(data: EntityDataAccessor<T>, mutator: (T) -> T) {
    val value = get(data)
    val newValue = mutator(value)
    if (value != newValue) {
        set(data, newValue)
    }
}