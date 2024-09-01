/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerPlayer

/**
 * A [CatchRateModifier] that resolves the catch rate based on an ongoing battle.
 *
 * @property calculator Responsible for resolving the catch rate dynamically based on the given params.
 *
 * @author Licious
 * @since May 7th, 2022
 */
open class BattleModifier(
    private val calculator: (player: ServerPlayer, playerPokemon: Iterable<ActiveBattlePokemon>, pokemon: Pokemon) -> Float
) : CatchRateModifier {

    override fun isGuaranteed(): Boolean = false

    override fun value(thrower: LivingEntity, pokemon: Pokemon): Float {
        val player = thrower as? ServerPlayer ?: return 1F
        val team = BattleRegistry
            .getBattleByParticipatingPlayer(player)
            ?.actors?.firstOrNull { actor -> actor is PlayerBattleActor && actor.uuid == player.uuid }?.activePokemon
            ?: return 1F
        return this.calculator(player, team, pokemon)
    }

    override fun behavior(thrower: LivingEntity, pokemon: Pokemon): CatchRateModifier.Behavior = CatchRateModifier.Behavior.MULTIPLY

    override fun isValid(thrower: LivingEntity, pokemon: Pokemon): Boolean = true

    override fun modifyCatchRate(currentCatchRate: Float, thrower: LivingEntity, pokemon: Pokemon): Float = this.behavior(thrower, pokemon).mutator(currentCatchRate, this.value(thrower, pokemon))

    open fun modifyCatchRate(currentCatchRate: Float, player: ServerPlayer, playerPokemon: Iterable<ActiveBattlePokemon>, pokemon: Pokemon): Float = this.calculator.invoke(player, playerPokemon, pokemon)

}
