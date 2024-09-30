/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3

class PokemonInBattleMovementGoal(val entity: PokemonEntity, val range: Int) : Goal() {

    var battlePos : Vec3? = null
    var ticksSinceStart: Int = 0
    var ticksMax = 5
    override fun canUse(): Boolean {
        return entity.isBattling && getClosestPokemonEntity() != null && entity.getCurrentPoseType() != PoseType.SLEEP
    }

    override fun start() {
        super.start()
        battlePos = entity.position()
        entity.navigation.stop()
    }

    override fun stop() {
        super.stop()
        entity.isNoGravity = false
    }

    private fun getClosestPokemonEntity(): PokemonEntity? {
        entity.battleId?.let { BattleRegistry.getBattle(it) }?.let { battle ->
            return battle.sides.find { it -> it.activePokemon.any { it.battlePokemon?.effectedPokemon == entity.pokemon } }?.
            getOppositeSide()?.activePokemon?.mapNotNull { it.battlePokemon?.entity }?.minByOrNull { it.distanceTo(entity) }
        }
        return null
    }

    override fun tick() {
        val closestPokemonEntity = getClosestPokemonEntity()
        if (closestPokemonEntity != null) {
            entity.lookControl.setLookAt(closestPokemonEntity.x, closestPokemonEntity.eyeY, closestPokemonEntity.z)
        }

        if( ticksSinceStart <= 2 * ticksMax) {
            ++ticksSinceStart
        }

        if (ticksSinceStart == ticksMax) {
            // kick start swimming/flying after letting them fall for a bit
            if(!entity.onGround()) {
                if (entity.pokemon.species.behaviour.moving.fly.canFly) {
                    // Let flyers fly in battle if they're in the air
                    entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
                }
                entity.navigation.moveTo(
                    battlePos!!.x,
                    battlePos!!.y,
                    battlePos!!.z,
                    0.7
                )
            }
//            entity.isNoGravity = true
        } else if (ticksSinceStart == ticksMax * 2) {
            // halt them in place
            // TODO: Swimmers continue to drift upward/downward. Need to find a way to halt vertical motion
            // I believe the fluid is pushing them around
            entity.navigation.stop()
        }
    }
}