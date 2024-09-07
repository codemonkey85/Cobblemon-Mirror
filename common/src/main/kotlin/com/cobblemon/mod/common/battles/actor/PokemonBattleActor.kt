/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ai.RandomBattleAI
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import java.util.UUID
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

class PokemonBattleActor(
    uuid: UUID,
    val pokemon: BattlePokemon,
    override val fleeDistance: Float,
    artificialDecider: BattleAI = RandomBattleAI()
) : AIBattleActor(uuid, listOf(pokemon), artificialDecider), EntityBackedBattleActor<PokemonEntity>, FleeableBattleActor {

    override val initialPos: Vec3?
    init {
        initialPos = entity?.position()
    }
    override fun getName() = pokemon.effectedPokemon.species.translatedName
    override fun nameOwned(name: String): MutableComponent = Component.literal(name)
    override fun getWorldAndPosition(): Pair<ServerLevel, Vec3>? {
        // This isn't a great solution, but basically capturing a Pokémon
        // removes the entity from the world, which sure does look similar
        // to an entity perishing -> which is grounds for flee triggering.
        val ownerPlayer = pokemon.effectedPokemon.getOwnerPlayer()
        if (ownerPlayer != null) {
            return ownerPlayer.serverLevel() to ownerPlayer.position()
        }

        val entity = this.entity ?: return null
        val world = entity.level() as? ServerLevel ?: return null
        return world to entity.position()
    }

    override fun sendUpdate(packet: NetworkPacket<*>) {
        super.sendUpdate(packet)
        if (packet is BattleEndPacket) {
            // Do some shit
            val entity = entity ?: return
            entity.battleId = null
        }
    }

    override val entity: PokemonEntity?
        get() = pokemon.entity

    override val type = ActorType.WILD
}