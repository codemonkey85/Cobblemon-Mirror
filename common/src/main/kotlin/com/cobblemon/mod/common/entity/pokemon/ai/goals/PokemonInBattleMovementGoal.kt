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
    var hasMovedToPos = false
    override fun canUse(): Boolean {
        return entity.isBattling && entity.getCurrentPoseType() != PoseType.SLEEP
    }

    override fun start() {
        super.start()
        battlePos = entity.position()
        entity.navigation.stop()
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

        if (entity.exposedSpecies.behaviour.moving.fly.canFly) {
            // Flyer logic
            if (!hasMovedToPos) {
                entity.navigation.moveTo(battlePos!!.x, battlePos!!.y, battlePos!!.z, 1.0)
                hasMovedToPos = true
            }
            if (entity.ticksLived > 1 && !entity.onGround() && !entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING)) {
                // Let flyers fly in battle if they're in the air
                entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
            }
        }
    }
}