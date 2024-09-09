/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.gamerules

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.mixin.invoker.BooleanRuleInvoker
import net.minecraft.world.level.GameRules

object CobblemonGameRules {

    @JvmField
    val BATTLE_INVULNERABILITY: GameRules.Key<GameRules.BooleanValue> = Cobblemon.implementation.registerGameRule("battleInvulnerability", GameRules.Category.PLAYER, BooleanRuleInvoker.`cobblemon$create`(false))
    @JvmField
    val DO_POKEMON_SPAWNING: GameRules.Key<GameRules.BooleanValue> = Cobblemon.implementation.registerGameRule("doPokemonSpawning", GameRules.Category.SPAWNING, BooleanRuleInvoker.`cobblemon$create`(true))
    @JvmField
    val DO_POKEMON_LOOT: GameRules.Key<GameRules.BooleanValue> = Cobblemon.implementation.registerGameRule("doPokemonLoot", GameRules.Category.DROPS, BooleanRuleInvoker.`cobblemon$create`(true))
    @JvmField
    val MOB_TARGET_IN_BATTLE: GameRules.Key<GameRules.BooleanValue> = Cobblemon.implementation.registerGameRule("mobTargetInBattle", GameRules.Category.MOBS, BooleanRuleInvoker.`cobblemon$create`(true))
    @JvmField
    val SHINY_STARTERS: GameRules.Key<GameRules.BooleanValue> = Cobblemon.implementation.registerGameRule("doShinyStarters", GameRules.Category.MISC, BooleanRuleInvoker.`cobblemon$create`(false))

}