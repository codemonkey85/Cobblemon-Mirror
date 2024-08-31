/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.controller

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

/**
 * A riding controller is the internal control mechanism for determining how a pokemon
 * handles during riding. Controllers come into play once the pokemon being ridden has
 * established the path it's capable of. For instance, a land based controller could have
 * the pokemon experience drifting after movement, or generic movement. It's up to the
 * controller to decide how the pokemon behaves.
 *
 * @since x.x.x
 */
interface RideController : Encodable, Decodable {

    /** A reference key used to denote the individual controller */
    val key: ResourceLocation

    /**
     * Specifies the provider of poses which helps control updating the current pose on the pokemon
     * being ridden. Controllers are expected to define the pose types responsible for posing the entity
     * through all expected conditions. It should define a default pose, that will be used in all cases
     * where a pose does not meet the current conditions for the current mount. Typically, this will be
     * an idle pose.
     */
    val poseProvider: PoseProvider

    /**
     * Represents a condition that must be met for this controller to be active. For instance, this can be used
     * to evaluate things such as current velocity or block state of the entity.
     */
    val condition: (PokemonEntity) -> Boolean

    /**
     * Specifies the pose the ridden mount should be positioned in. This queries the pose provider using
     * the entity as a means of determining the conditions the entity is currently under. This allows for
     * dynamic pose updates.
     */
    fun pose(entity: PokemonEntity) : PoseType {
        return this.poseProvider.select(entity)
    }

    /**
     * Calculates the current speed of the mount.
     */
    fun speed(entity: PokemonEntity, driver: Player) : Float

    /**
     * Sets the rotation of the mount. This is typically based on the controlling driver and is manipulated as
     * necessary.
     */
    fun rotation(driver: LivingEntity) : Vec2

    /**
     * Manipulates the movement input as necessary by the controller. This is primarily used to apply limits
     * to the types of movements. For instance, we can apply limits to any sort of sideways movement input, so that
     * it would otherwise be slower than normal forward movement.
     */
    fun velocity(driver: Player, input: Vec3) : Vec3

    fun canJump(entity: PokemonEntity, driver: Player) : Boolean
    fun jumpForce(entity: PokemonEntity, driver: Player, jumpStrength: Int) : Vec3

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.key)
    }
}