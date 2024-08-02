/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
class ActiveBattlePokemon(val actor: BattleActor, var battlePokemon: BattlePokemon? = null): Targetable {
    val battle = actor.battle
    fun getSide() = actor.getSide()
    var position: Pair<ServerLevel, Vec3>? = null

    var illusion: BattlePokemon? = null

    override fun getAllActivePokemon() = battle.activePokemon
    override fun getActorShowdownId() = actor.showdownId
    override fun getActorPokemon() = actor.activePokemon
    override fun getSidePokemon() = getSide().activePokemon
    override fun getFormat() = battle.format
    override fun isAllied(other: Targetable) = getSide() == (other as ActiveBattlePokemon).getSide()
    override fun hasPokemon() = battlePokemon != null
    fun isGone() = battlePokemon?.gone ?: true
    fun isAlive() = (battlePokemon?.health ?: 0) > 0
}