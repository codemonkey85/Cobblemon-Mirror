/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.world.entity.ai.goal.Goal

class PokemonPointAtSpawnGoal(private val pokemonEntity: PokemonEntity) : Goal() {
    override fun canUse(): Boolean {
        val behaviour = pokemonEntity.behaviour
        return pokemonEntity.behaviour.idle.pointsAtSpawn &&
                behaviour.moving.canLook &&
                pokemonEntity.pokemon.status?.status != Statuses.SLEEP &&
                !pokemonEntity.isBattling
    }

    override fun tick() {
        if (!pokemonEntity.getBehaviourFlag(PokemonBehaviourFlag.LOOKING) && pokemonEntity.navigation.isDone) {
            pokemonEntity.lookAt(EntityAnchorArgument.Anchor.EYES, pokemonEntity.level().sharedSpawnPos.toVec3d())
        }
    }
}