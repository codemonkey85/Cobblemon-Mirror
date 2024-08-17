/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex.scanner

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB

//Handles the actual raycasting to figure out what pokemon we are looking at
object PokemonScanner {
    //This basically draws a box around the casting entity, finds all entities in the box, then finds the one that a ray emanating from the player hits first
    fun detectEntity(castingEntity: Entity): Entity? {
        val eyePos = castingEntity.getEyePosition(1.0F)
        val lookVec = castingEntity.getViewVector(1.0F)
        val maxDistance = RAY_LENGTH
        val boundingBoxSize = 12.0
        var closestEntity: Entity? = null
        var closestDistance = maxDistance

        // Define a large bounding box around the player
        val boundingBox = AABB(
            castingEntity.x - boundingBoxSize, castingEntity.y - boundingBoxSize, castingEntity.z - boundingBoxSize,
            castingEntity.x + boundingBoxSize, castingEntity.y + boundingBoxSize, castingEntity.z + boundingBoxSize
        )

        // Get all entities within the boundingBox
        val entities = castingEntity.level().getEntitiesOfClass(Entity::class.java, boundingBox) { it != castingEntity }

        for (entity in entities) {
            val entityBox: AABB = entity.boundingBox

            // Calculate the size of the bounding box
            val boxWidth = entityBox.xsize
            val boxHeight = entityBox.ysize
            val boxDepth = entityBox.zsize

            val boxVolume = boxWidth * boxHeight * boxDepth

            val minSize = 0.2 // Smallest bounding box volume (joltik at .2)
            val maxSize = 3.0 // Largest bounding box volume (wailord at 21.5)
            val minSizeScale = 2.0 // Maximum inflation for getting closer to smallest hitbox
            val maxSizeScale = 1.0 // No inflation for getting closer to largest hitbox
            val steepCoefficient = 20.0

            // Normalize the volume within the defined range
            val normalizedSize = (boxVolume - minSize) / (maxSize - minSize).coerceAtLeast(0.01)

            // Calculate the scaling factor using very steep exponential decay to make smaller hitboxes bigger
            val inflationFactor = maxSizeScale + (minSizeScale - maxSizeScale) * Math.exp(-steepCoefficient * normalizedSize)

            // Inflate the base bounding box
            val inflatedBox = entityBox.inflate(
                (inflationFactor - 1) * boxWidth / 2,
                (inflationFactor - 1) * boxHeight / 2,
                (inflationFactor - 1) * boxDepth / 2
            )

            val intersection = inflatedBox.clip(eyePos, eyePos.add(lookVec.scale(maxDistance)))

            if (intersection.isPresent) {
                val distanceToEntity = eyePos.distanceTo(intersection.get())
                if (distanceToEntity < closestDistance) {
                    closestEntity = entity
                    closestDistance = distanceToEntity
                }
            }
        }
        return closestEntity
    }

    fun findPokemon(castingEntity: Entity): PokemonEntity? {
        val targetedEntity = detectEntity(castingEntity)
        return targetedEntity as? PokemonEntity
    }

    fun isEntityInRange(castingEntity: Entity, targetEntity: Entity): Boolean {
        return targetEntity.position().distanceTo(castingEntity.position()) <= RAY_LENGTH
    }

    val RAY_LENGTH = 10.0
}