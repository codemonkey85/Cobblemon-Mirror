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
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.goal.Goal

class PokemonInBattleMovementGoal(val entity: PokemonEntity, val range: Int) : Goal() {
    override fun canUse(): Boolean {
        return entity.isBattling && getClosestPokemonEntity() != null && entity.getCurrentPoseType() != PoseType.SLEEP
    }

    override fun start() {
        super.start()
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
    }
}