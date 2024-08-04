/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent
import com.cobblemon.mod.common.api.events.battles.BattleStartedPreEvent
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeExpiredPacket
import com.cobblemon.mod.common.util.getPlayer
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.server.level.ServerPlayer

object BattleRegistry {

    class BattleChallenge(
        val challengeId: UUID,
        val challengedPlayerUUID: UUID,
        val selectedPokemonId: UUID,
        var expiryTimeSeconds: Int = 60
    ) {
        val challengedTime = Instant.now()
        fun isExpired() = Instant.now().isAfter(challengedTime.plusSeconds(expiryTimeSeconds.toLong()))
    }

    val gson = GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapter(ShowdownMoveset::class.java, ShowdownMovesetAdapter)
        .create()
    private val battleMap = ConcurrentHashMap<UUID, PokemonBattle>()
    // Challenger to challenge
    val pvpChallenges = mutableMapOf<UUID, BattleChallenge>()

    fun onServerStarted() {
        battleMap.clear()
        pvpChallenges.clear()
    }

    fun removeChallenge(challengerId: UUID, challengeId: UUID? = null) {
        val existing = pvpChallenges[challengerId] ?: return
        if (existing.challengeId != challengeId) {
            return
        }
        pvpChallenges.remove(challengerId)
        existing.challengedPlayerUUID.getPlayer()?.sendPacket(BattleChallengeExpiredPacket(existing.challengeId))
    }

    /**
     * Packs a team into the showdown format
     *
     * Example from https://gitlab.com/cable-mc/cobblemon-showdown/-/blob/master/sim/TEAMS.md#packed-format
     *  NICKNAME|SPECIES|ITEM|ABILITY|MOVES|NATURE|EVS|GENDER|IVS|SHINY|LEVEL|HAPPINESS,POKEBALL,HIDDENPOWERTYPE,GIGANTAMAX,DYNAMAXLEVEL,TERATYPE
     *
     * @return a string of the packed team
     */
    fun List<BattlePokemon>.packTeam() : String {
        val team = mutableListOf<String>()
        for (pokemon in this) {
            val pk = pokemon.effectedPokemon
            val packedTeamBuilder = StringBuilder()
            // If no nickname, write species first and leave next blank
            packedTeamBuilder.append("${pk.showdownId()}|")
            // Species, left empty if no nickname
            packedTeamBuilder.append("|")

            // REQUIRES OUR SHOWDOWN
            packedTeamBuilder.append("${pk.uuid}|")
            packedTeamBuilder.append("${pk.currentHealth}|")
            val showdownStatus = if (pk.status != null) pk.status!!.status.showdownName else ""
            packedTeamBuilder.append("$showdownStatus|")
            // If a temporary status is on the Pokémon, provide a duration.
            if (pk.status?.status in listOf(Statuses.SLEEP, Statuses.FROZEN)) {
                packedTeamBuilder.append("2|")
            } else {
                packedTeamBuilder.append("-1|")
            }

            // Held item, empty if none
            val heldItemID = HeldItemProvider.provideShowdownId(pokemon) ?: ""
            packedTeamBuilder.append("$heldItemID|")
            // Ability, our showdown has edits here to trust whatever we tell it, this was needed to support more than 4 abilities.
            packedTeamBuilder.append("${pk.ability.name.replace("_", "")}|")
            // Moves
            packedTeamBuilder.append(
                "${
                    pk.moveSet.getMoves().joinToString(",") { move -> move.name.replace("_", "") }
                }|"
            )
            // Additional move info
            packedTeamBuilder.append(
                "${
                    pk.moveSet.getMoves().joinToString(",") { move -> move.currentPp.toString() + "/" + move.maxPp.toString() }
                }|"
            )
            // Nature
            val battleNature = pk.effectiveNature
            packedTeamBuilder.append("${battleNature.name.path}|")
            // EVs
            val evsInOrder = Stats.PERMANENT.map { pk.evs.getOrDefault(it) }.joinToString(separator = ",")
            packedTeamBuilder.append("$evsInOrder|")
            // Gender
            packedTeamBuilder.append("${pk.gender.showdownName}|")
            // IVs
            val ivsInOrder = Stats.PERMANENT.map { pk.ivs.getOrDefault(it) }.joinToString(separator = ",")
            packedTeamBuilder.append("$ivsInOrder|")
            // Shiny
            packedTeamBuilder.append("${if (pk.shiny) "S" else ""}|")
            // Level
            packedTeamBuilder.append("${pk.level}|")

            // Misc
            // Happiness
            packedTeamBuilder.append("${pk.friendship},")
            // Caught Ball
            // This is safe to do as all our pokeballs that have showdown item equivalents are the same IDs they use for the pokeball attribute
            val pokeball = pokemon.effectedPokemon.caughtBall.name.path.replace("_", "")
            packedTeamBuilder.append("$pokeball,")
            // Hidden Power Type
            packedTeamBuilder.append(",")
            // Gigantamax
            packedTeamBuilder.append("${if (pk.gmaxFactor) "G" else ""},")
            // DynamaxLevel
            // 0 - 9, empty == 10
            packedTeamBuilder.append("${if (pk.dmaxLevel < 10) pk.dmaxLevel else ""},")
            // Teratype
            packedTeamBuilder.append("${pokemon.effectedPokemon.teraType.showdownId()},")

            team.add(packedTeamBuilder.toString())
        }
        return team.joinToString("]")
    }

    private fun startShowdown(battle: PokemonBattle) {

        // Build request message
        val messages = mutableListOf<String>()
        messages.add(">start { \"format\": ${battle.format.toFormatJSON()} }")

        /*
         * Showdown IDs are like p1, p2, p3, etc. Showdown uses these keys to identify who is doing what to whom.
         *
         * "But why are these showdown IDs so weird"
         *
         * I'll tell you, Jimmy.
         * https://gitlab.com/cable-mc/pokemon-Cobblemon-showdown/-/blob/master/sim/SIM-PROTOCOL.md#user-content-identifying-pok%C3%A9mon
         *
         * See the lines about multi battles and free for alls. The same side of the battle will share 'parity' (even or odd) across
         * all participants. So side 1 will be 1, 3, 5, ... while side 2 will be 2, 4, 6, ...
         *
         * That isn't how our code works, as we have the BattleSide thing, but it's how Showdown works so we need to play along a bit.
         */

        var actorIndex = 1
        for (actor in battle.side1.actors) {
            actor.showdownId = "p$actorIndex"
            actorIndex += 2
        }

        actorIndex = 2
        for (actor in battle.side2.actors) {
            actor.showdownId = "p$actorIndex"
            actorIndex += 2
        }

        for (actor in battle.actors) {
            repeat(battle.format.battleType.slotsPerActor) {
                actor.activePokemon.add(ActiveBattlePokemon(actor))
            }
            val entities = actor.pokemonList.mapNotNull { it.entity }
            entities.forEach { it.battleId = battle.battleId }
        }

        // -> Add the players and team
        for (actor in battle.actors.sortedBy { it.showdownId }) {
            messages.add(""">player ${actor.showdownId} {"name":"${actor.uuid}","team":"${actor.pokemonList.packTeam()}"}""")
        }

        // -> Set team size
        for (actor in battle.actors.sortedBy { it.showdownId }) {
            messages.add(">${actor.showdownId} team ${actor.pokemonList.count()}")
        }

        // Compiles the request and sends it off
        ShowdownService.service.startBattle(battle, messages.toTypedArray())
    }

    fun startBattle(
        battleFormat: BattleFormat,
        side1: BattleSide,
        side2: BattleSide,
        silent: Boolean = false
    ): BattleStartResult {
        val battle = PokemonBattle(battleFormat, side1, side2)
        if (silent) return SuccessfulBattleStart(battle)

        val preBattleEvent = BattleStartedPreEvent(battle)
        CobblemonEvents.BATTLE_STARTED_PRE.postThen(preBattleEvent) {
            battleMap[battle.battleId] = battle
            startShowdown(battle)
            CobblemonEvents.BATTLE_STARTED_POST.post(BattleStartedPostEvent(battle))
            return SuccessfulBattleStart(battle)
        }
        return ErroredBattleStart(mutableSetOf(BattleStartError.canceledByEvent(preBattleEvent.reason)))
    }

    fun closeBattle(battle: PokemonBattle) {
        battle.onEndHandlers.forEach { it(battle) }
        battleMap.remove(battle.battleId)
    }

    fun getBattle(id: UUID) : PokemonBattle? {
        return battleMap[id]
    }

    fun getBattleByParticipatingPlayer(serverPlayerEntity: ServerPlayer) : PokemonBattle? {
        return battleMap.values.find { it.getActor(serverPlayerEntity) != null }
    }

    fun getBattleByParticipatingPlayerId(playerId: UUID): PokemonBattle? {
        return battleMap.values.find { playerId in it.playerUUIDs }
    }

    fun tick() {
        battleMap.forEachValue(Long.MAX_VALUE) { it.tick() }
    }
}