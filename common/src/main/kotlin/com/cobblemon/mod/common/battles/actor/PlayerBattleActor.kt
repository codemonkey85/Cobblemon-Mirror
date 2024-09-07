/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.actor

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.experience.BattleExperienceSource
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import java.util.*

class PlayerBattleActor(
    uuid: UUID,
    pokemonList: List<BattlePokemon>
) : BattleActor(uuid, pokemonList.toMutableList()), EntityBackedBattleActor<ServerPlayer> {

    override val entity: ServerPlayer?
        get() = this.uuid.getPlayer()

    /** The [SoundEvent] to play to the player during a battle. Will start playing as soon as the battle starts. */
    var battleTheme: SoundEvent? = null
        set(value) {
            if (field != value && this.battle.started)
                this.sendUpdate(BattleMusicPacket(value))
            field = value
        }

    override fun getName(): MutableComponent = this.entity?.name?.copy() ?: "Offline Player".red()
    override fun nameOwned(name: String): MutableComponent = battleLang("owned_pokemon", this.getName(), name)
    override val type = ActorType.PLAYER
    override fun getPlayerUUIDs() = setOf(uuid)
    override fun awardExperience(battlePokemon: BattlePokemon, experience: Int) {
        if (battle.isPvP && !Cobblemon.config.allowExperienceFromPvP) {
            return
        }

        val source = BattleExperienceSource(battle, battlePokemon.facedOpponents.toList())
        if (battlePokemon.effectedPokemon == battlePokemon.originalPokemon && experience > 0) {
            uuid.getPlayer()
                ?.let { battlePokemon.effectedPokemon.addExperienceWithPlayer(it, source, experience) }
                ?: run { battlePokemon.effectedPokemon.addExperience(source, experience) }
        }
    }

    override fun sendUpdate(packet: NetworkPacket<*>) {
        CobblemonNetwork.sendPacketToPlayers(getPlayerUUIDs().mapNotNull { it.getPlayer() }, packet)
    }
}
