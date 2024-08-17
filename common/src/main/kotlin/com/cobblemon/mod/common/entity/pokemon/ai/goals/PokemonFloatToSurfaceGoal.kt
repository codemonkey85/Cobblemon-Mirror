/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.ai.goal.Goal
import java.util.*

class PokemonFloatToSurfaceGoal(val pokemonEntity: PokemonEntity) : Goal() {
    companion object {
        private val FLAGS = EnumSet.of(Flag.JUMP)
    }

    override fun getFlags(): EnumSet<Flag> = FLAGS

    override fun canUse(): Boolean {
        val canSwimInWater = pokemonEntity.behaviour.moving.swim.canSwimInWater
        val canSwimInLava = pokemonEntity.behaviour.moving.swim.canSwimInLava
        val canBreatheUnderlava = pokemonEntity.behaviour.moving.swim.canBreatheUnderlava
        val canBreatheUnderwater = pokemonEntity.behaviour.moving.swim.canBreatheUnderwater

        if (!this.pokemonEntity.navigation.isDone) {
            return false
        }

        if (this.pokemonEntity.isInLava && !canBreatheUnderlava) {
            return true
        } else if (canSwimInWater && !canBreatheUnderwater && this.pokemonEntity.isInWater && this.pokemonEntity.getFluidHeight(FluidTags.WATER) > this.pokemonEntity.fluidJumpThreshold) {
            return true
        }

        return false
    }

    override fun requiresUpdateEveryTick() = true

    override fun tick() {
        if (this.pokemonEntity.random.nextFloat() < 0.8f) {
            this.pokemonEntity.jumpControl.jump()
        }
    }
}