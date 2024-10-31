/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.ForcePassActionResponse
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import java.util.UUID

class ClientBattle(
    val battleId: UUID,
    val battleFormat: BattleFormat
) {
    var minimised = true
    var spectating = false

    val side1 = ClientBattleSide()
    val side2 = ClientBattleSide()
    var wildActor: ClientBattleActor? = null
        set(value) {
            field = value
            if (value != null) {
                val wildMonProps = value.activePokemon[0].battlePokemon?.properties
                wildMonProps?.let {props ->
                    val wildSpecies = PokemonSpecies.getByName(props.species!!)!!
                    //FIXME: Better knowledge checking based on aspects/form and such
                    knowledge = CobblemonClient.clientPokedexData.getKnowledgeForSpecies(wildSpecies.resourceIdentifier)
                } ?: PokedexEntryProgress.NONE
            }
            else {
                knowledge = PokedexEntryProgress.NONE
            }
        }
    var knowledge = PokedexEntryProgress.NONE

    val sides: Array<ClientBattleSide>
        get() = arrayOf(side1, side2)

    var pendingActionRequests = mutableListOf<SingleActionRequest>()
    val messages = ClientBattleMessageQueue()
    var mustChoose = false

    fun getFirstUnansweredRequest() = pendingActionRequests.firstOrNull { it.response == null }

    fun getLastAnsweredRequest() = pendingActionRequests.lastOrNull { it.response != null && it.response != PassActionResponse && it.response !is ForcePassActionResponse }

    fun cancelLastAnsweredRequest() {
        var index = pendingActionRequests.indexOfLast { it.response != null && it.response != PassActionResponse && it.response !is ForcePassActionResponse }
        if (index != -1) {
           while(index < pendingActionRequests.size) {
               val request = pendingActionRequests[index]
               if (request.response != PassActionResponse && request.response !is ForcePassActionResponse)
                   request.response = null
               index++
           }
        }
    }

    fun checkForFinishedChoosing() {
        if (getFirstUnansweredRequest() == null) {
            CobblemonNetwork.sendToServer(
                BattleSelectActionsPacket(
                    battleId = battleId,
                    pendingActionRequests.map { it.response!! }
                )
            )
            mustChoose = false
        }
    }

    fun getPokemonFromPNX(pnx: String): Pair<ClientBattleActor, ActiveClientBattlePokemon> {
        val actor = sides.flatMap { it.actors }.find { it.showdownId == pnx.substring(0, 2) }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown actor")
        val letter = pnx[2]
        val pokemon = actor.side.activeClientBattlePokemon.find { it.getLetter() == letter }
            ?: throw IllegalStateException("Invalid pnx: $pnx - unknown pokemon")
        return actor to pokemon
    }

    fun getParticipatingActor(uuid: UUID): ClientBattleActor? {
        return sides.flatMap { it.actors }.find { it.uuid == uuid }
    }

    /** Whether or not there is one side with at least one player, and the other only has wild Pokémon. */
    val isPvW: Boolean
        get() {
            val playerSide = sides.find { it.actors.any { it.type == ActorType.PLAYER } } ?: return false
            if (playerSide.actors.any { it.type != ActorType.PLAYER }) {
                return false
            }
            val otherSide = sides.find { it != playerSide }!!
            return otherSide.actors.all { it.type == ActorType.WILD }
        }

    fun findWildActor(): ClientBattleActor? {
        sides.forEach {
            it.actors.forEach { actor ->
                if (actor.type == ActorType.WILD) {
                    return actor
                }
            }
        }
        return null
    }
}