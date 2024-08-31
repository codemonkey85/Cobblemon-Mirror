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
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.InteractGoal

/**
 * An override of the [LookAtEntityGoal] so that Pokémon behaviours can be implemented.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
class PokemonLookAtEntityGoal(entity: PokemonEntity, targetType: Class<out LivingEntity>, range: Float) : InteractGoal(entity, targetType, range) {
    fun canLook() = (mob as PokemonEntity).let { pokemon ->
        val behaviour = pokemon.behaviour
        behaviour.moving.canLook && pokemon.pokemon.status?.status != Statuses.SLEEP && !pokemon.isBattling && behaviour.moving.looksAtEntities
    }

    override fun canUse() = super.canUse() && canLook()

    override fun canContinueToUse() = super.canContinueToUse() && canLook()

    override fun start() {
        super.start()
        (mob as PokemonEntity).setBehaviourFlag(PokemonBehaviourFlag.LOOKING, true)
    }

    override fun stop() {
        super.stop()
        (mob as PokemonEntity).setBehaviourFlag(PokemonBehaviourFlag.LOOKING, false)
    }
}