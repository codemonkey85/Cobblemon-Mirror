/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.npc.NPCBattleActor
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.getBattleTheme
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.party
import java.util.UUID
import com.cobblemon.mod.common.util.update
import com.cobblemon.mod.common.util.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Iterable
import kotlin.collections.MutableSet
import kotlin.collections.all
import kotlin.collections.filterIsInstance
import kotlin.collections.first
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.listOf
import kotlin.collections.mapNotNull
import kotlin.collections.mutableSetOf
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.collections.set
import kotlin.collections.sortedBy

object BattleBuilder {
    @JvmOverloads
    fun pvp1v1(
        player1: ServerPlayer,
        player2: ServerPlayer,
        leadingPokemonPlayer1: UUID? = null,
        leadingPokemonPlayer2: UUID? = null,
        battleFormat: BattleFormat = BattleFormat.GEN_9_SINGLES,
        cloneParties: Boolean = false,
        healFirst: Boolean = false,
        partyAccessor: (ServerPlayer) -> PartyStore = { it.party() }
    ): BattleStartResult {
        val team1 = partyAccessor(player1).toBattleTeam(clone = cloneParties, checkHealth = !healFirst, leadingPokemonPlayer1).sortedBy { it.health <= 0 }
        val team2 = partyAccessor(player2).toBattleTeam(clone = cloneParties, checkHealth = !healFirst, leadingPokemonPlayer2).sortedBy { it.health <= 0 }

        val player1Actor = PlayerBattleActor(player1.uuid, team1)
        val player2Actor = PlayerBattleActor(player2.uuid, team2)

        val errors = ErroredBattleStart()

        for ((player, actor) in arrayOf(player1 to player1Actor, player2 to player2Actor)) {
            if (actor.pokemonList.filter { it.health > 0 }.size < battleFormat.battleType.slotsPerActor) {
                errors.participantErrors[actor] += BattleStartError.insufficientPokemon(
                    player = player,
                    requiredCount = battleFormat.battleType.slotsPerActor,
                    hadCount = actor.pokemonList.filter { it.health > 0 }.size
                )
            }

            if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
                errors.participantErrors[actor] += BattleStartError.alreadyInBattle(player)
            }
        }

        return if (errors.isEmpty) {
            var result: BattleStartResult = errors
            BattleRegistry.startBattle(
                battleFormat = battleFormat,
                side1 = BattleSide(player1Actor),
                side2 = BattleSide(player2Actor)
            ).ifSuccessful {
                player1Actor.battleTheme = player2.getBattleTheme()
                player2Actor.battleTheme = player1.getBattleTheme()
                result = SuccessfulBattleStart(it)
            }
            result
        } else {
            errors
        }
    }

    @JvmOverloads
    fun pvp2v2(
            players: List<ServerPlayer> = emptyList(),
            leadingPokemon: List<UUID> = emptyList(),
            battleFormat: BattleFormat = BattleFormat.GEN_9_MULTI,
            cloneParties: Boolean = false,
            healFirst: Boolean = false,
            partyAccessor: (ServerPlayer) -> PartyStore = { it.party() }
    ): BattleStartResult {
        val teams = players.mapIndexed { index, it -> partyAccessor(it).toBattleTeam(clone = cloneParties, checkHealth = !healFirst, leadingPokemon[index]) }
        val playerActors = teams.mapIndexed { index, team -> PlayerBattleActor(players[index].uuid, team)}.toMutableList()

        val errors = ErroredBattleStart()

        if (players.size != BattleRegistry.MAX_TEAM_MEMBER_COUNT * 2) {
            playerActors.forEach {actor ->
                errors.participantErrors[actor] += BattleStartError.incorrectActorCount(
                    requiredCount = BattleRegistry.MAX_TEAM_MEMBER_COUNT * 2,
                    hadCount = players.size
                )
            }
        }

        for ((player, actor) in players.zip(playerActors)) {
            if (actor.pokemonList.size < battleFormat.battleType.slotsPerActor) {
                errors.participantErrors[actor] += BattleStartError.insufficientPokemon(
                        player = player,
                        requiredCount = battleFormat.battleType.slotsPerActor,
                        hadCount = actor.pokemonList.size
                )
            }

            if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
                errors.participantErrors[actor] += BattleStartError.alreadyInBattle(player)
            }
        }

        // Rearrange actors so that Showdown's player arrangement hopefully matches the player arrangement in the world
        val playersPositions = players.map { p -> p.position() }
        val side1Center = playersPositions.subList(0, players.size / 2).fold(Vec3(0.0, 0.0, 0.0)) { acc, vec3 -> acc.add(vec3.scale(1.0 / (players.size / 2))) }
        val side2Center = playersPositions.subList(players.size / 2, players.size).fold(Vec3(0.0, 0.0, 0.0)) { acc, vec3 -> acc.add(vec3.scale(1.0 / (players.size / 2))) }

        if ((side2Center.x - side1Center.x)*(playersPositions[1].z - side1Center.z) - (side2Center.z - side1Center.z)*(playersPositions[1].x - side1Center.x) < 0) {
            // swap
            playerActors.swap(0,1)
        }

        if ((side1Center.x - side2Center.x)*(playersPositions[3].z - side2Center.z) - (side1Center.z - side2Center.z)*(playersPositions[3].x - side2Center.x) < 0) {
            // swap
            playerActors.swap(2,3)
        }

        return if (errors.isEmpty) {
            BattleRegistry.startBattle(
                    battleFormat = battleFormat,
                    side1 = BattleSide(playerActors[0], playerActors[1]),
                    side2 = BattleSide(playerActors[2], playerActors[3])
            ).ifSuccessful {
                // TODO: less hard coding
                playerActors[0].battleTheme = players[2].getBattleTheme()
                playerActors[1].battleTheme = players[2].getBattleTheme()

                playerActors[2].battleTheme = players[0].getBattleTheme()
                playerActors[3].battleTheme = players[0].getBattleTheme()
            }
            errors
        } else {
            errors
        }
    }



    /**
     * Attempts to create a PvE battle against the given Pokémon.
     *
     * @param player The player battling the wild Pokémon.
     * @param pokemonEntity The Pokémon to battle.
     * @param leadingPokemon The Pokémon in the player's party to send out first. If null, it uses the first in the party.
     * @param battleFormat The format to use for the battle. By default it is [BattleFormat.GEN_9_SINGLES].
     * @param cloneParties Whether the player's party should be cloned so that damage will not affect their party afterwards. Defaults to false.
     * @param healFirst Whether the player's Pokémon should be healed before the battle starts. Defaults to false.
     * @param fleeDistance How far away the player must get to flee the Pokémon. If the value is -1, it cannot be fled.
     * @param party The party of the player to use for the battle. This does not need to be their actual party. Defaults to it though.
     */
    @JvmOverloads
    fun pve(
        player: ServerPlayer,
        pokemonEntity: PokemonEntity,
        leadingPokemon: UUID? = null,
        battleFormat: BattleFormat = BattleFormat.GEN_9_SINGLES,
        cloneParties: Boolean = false,
        healFirst: Boolean = false,
        fleeDistance: Float = Cobblemon.config.defaultFleeDistance,
        party: PartyStore = player.party()
    ): BattleStartResult {
        val playerTeam = party.toBattleTeam(clone = cloneParties, checkHealth = !healFirst, leadingPokemon = leadingPokemon).sortedBy { it.health <= 0 }
        val playerActor = PlayerBattleActor(player.uuid, playerTeam)
        val wildActor = PokemonBattleActor(pokemonEntity.pokemon.uuid, BattlePokemon(pokemonEntity.pokemon), fleeDistance)
        val errors = ErroredBattleStart()

        if(playerTeam.isNotEmpty() && playerTeam[0].health <= 0){
            errors.participantErrors[playerActor] += BattleStartError.insufficientPokemon(
                    player = player,
                    requiredCount = battleFormat.battleType.slotsPerActor,
                    hadCount = playerActor.pokemonList.size
            )
        }

        if (playerActor.pokemonList.size < battleFormat.battleType.slotsPerActor) {
            errors.participantErrors[playerActor] += BattleStartError.insufficientPokemon(
                player = player,
                requiredCount = battleFormat.battleType.slotsPerActor,
                hadCount = playerActor.pokemonList.size
            )
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.participantErrors[playerActor] += BattleStartError.alreadyInBattle(playerActor)
        }

        if (pokemonEntity.battleId != null) {
            errors.participantErrors[wildActor] += BattleStartError.alreadyInBattle(wildActor)
        }

        return if (errors.isEmpty) {
            var result: BattleStartResult = errors
            BattleRegistry.startBattle(
                battleFormat = battleFormat,
                side1 = BattleSide(playerActor),
                side2 = BattleSide(wildActor)
            ).ifSuccessful {
                if (!cloneParties) {
                    pokemonEntity.battleId = it.battleId
                }
                playerActor.battleTheme = pokemonEntity.getBattleTheme()
                result = SuccessfulBattleStart(it)
            }
            result
        } else {
            errors
        }
    }

    /**
     * Attempts to create a PvE battle against the given Pokémon.
     *
     * @param player The player battling the wild Pokémon.
     * @param pokemonEntity The Pokémon to battle.
     * @param leadingPokemon The Pokémon in the player's party to send out first. If null, it uses the first in the party.
     * @param battleFormat The format to use for the battle. By default it is [BattleFormat.GEN_9_SINGLES].
     * @param cloneParties Whether the player's party should be cloned so that damage will not affect their party afterwards. Defaults to false.
     * @param healFirst Whether the player's Pokémon should be healed before the battle starts. Defaults to false.
     * @param fleeDistance How far away the player must get to flee the Pokémon. If the value is -1, it cannot be fled.
     * @param party The party of the player to use for the battle. This does not need to be their actual party. Defaults to it though.
     */
    @JvmOverloads
    fun pvn(
        player: ServerPlayer,
        npcEntity: NPCEntity,
        leadingPokemon: UUID? = null,
        battleFormat: BattleFormat = BattleFormat.GEN_9_SINGLES,
        cloneParties: Boolean = false,
        healFirst: Boolean = false,
        party: PartyStore = player.party()
    ): BattleStartResult {
        val playerTeam = party.toBattleTeam(clone = cloneParties, checkHealth = !healFirst, leadingPokemon = leadingPokemon)
        val playerActor = PlayerBattleActor(player.uuid, playerTeam)

        val party = npcEntity.getBattleConfiguration().party?.provide(npcEntity, listOf(player)) ?: TODO("Deal with this issue!")
        val npcActor = NPCBattleActor(npcEntity, party)
        val errors = ErroredBattleStart()

        if (playerActor.pokemonList.size < battleFormat.battleType.slotsPerActor) {
            errors.participantErrors[playerActor] += BattleStartError.insufficientPokemon(
                player = player,
                requiredCount = battleFormat.battleType.slotsPerActor,
                hadCount = playerActor.pokemonList.size
            )
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.participantErrors[playerActor] += BattleStartError.alreadyInBattle(playerActor)
        }

//        if (npcEntity.battleIds.get().isPresent) {
//            errors.participantErrors[npcActor] += BattleStartError.alreadyInBattle(npcActor)
//        }

        return if (errors.isEmpty) {
            var result: BattleStartResult = errors
            BattleRegistry.startBattle(
                battleFormat = battleFormat,
                side1 = BattleSide(playerActor),
                side2 = BattleSide(npcActor)
            ).ifSuccessful { battle ->
                // TODO NPC battle themes
//                playerActor.battleTheme = pokemonEntity.getBattleTheme()
                npcEntity.entityData.update(NPCEntity.BATTLE_IDS) { it + battle.battleId }
                result = SuccessfulBattleStart(battle)
            }
            result
        } else {
            errors
        }
    }
}

abstract class BattleStartResult {
    open fun ifSuccessful(action: (PokemonBattle) -> Unit): BattleStartResult {
        return this
    }

    open fun ifErrored(action: (ErroredBattleStart) -> Unit): BattleStartResult {
        return this
    }
}
class SuccessfulBattleStart(
    val battle: PokemonBattle
) : BattleStartResult() {
    override fun ifSuccessful(action: (PokemonBattle) -> Unit): BattleStartResult {
        action(battle)
        return this
    }
}

interface BattleStartError {

    fun getMessageFor(entity: Entity): MutableComponent

    companion object {
        fun alreadyInBattle(player: ServerPlayer) = AlreadyInBattleError(player.uuid, player.effectiveName())
        fun alreadyInBattle(pokemonEntity: PokemonEntity) = AlreadyInBattleError(pokemonEntity.uuid, pokemonEntity.effectiveName())
        fun alreadyInBattle(actor: BattleActor) = AlreadyInBattleError(actor.uuid, actor.getName())

        fun targetIsBusy(targetName: MutableComponent) = BusyError(targetName)
        fun insufficientPokemon(
            player: ServerPlayer,
            requiredCount: Int,
            hadCount: Int
        ) = InsufficientPokemonError(player, requiredCount, hadCount)
        fun incorrectActorCount(
            requiredCount: Int,
            hadCount: Int
        ) = IncorrectActorCountError(requiredCount, hadCount)
        fun canceledByEvent(reason: MutableComponent?) = CanceledError(reason)
    }
}

enum class CommonBattleStartError : BattleStartError {

}

class CanceledError(
    val reason: MutableComponent?
): BattleStartError {
    override fun getMessageFor(entity: Entity) = reason ?: battleLang("error.canceled")
}

class InsufficientPokemonError(
    val player: ServerPlayer,
    val requiredCount: Int,
    val hadCount: Int
) : BattleStartError {
    override fun getMessageFor(entity: Entity): MutableComponent {
        return if (player == entity) {
            val key = if (hadCount == 0) "no_pokemon" else "insufficient_pokemon.personal"
            battleLang(
                "error.$key",
                hadCount,
                requiredCount
            )
        } else {
            battleLang(
                "error.insufficient_pokemon",
                player.effectiveName(),
                hadCount,
                requiredCount
            )
        }
    }
}

class IncorrectActorCountError(
        val requiredCount: Int,
        val hadCount: Int
) : BattleStartError {
    override fun getMessageFor(entity: Entity): MutableComponent {
        return battleLang(
            "error.incorrect_actor_count",
            requiredCount,
            hadCount
        )
    }
}
class AlreadyInBattleError(
    val actorUUID: UUID,
    val name: Component
): BattleStartError {
    override fun getMessageFor(entity: Entity): MutableComponent {
        return if (actorUUID == entity.uuid) {
            battleLang("error.in_battle.personal")
        } else {
            battleLang("error.in_battle", name)
        }
    }
}
class BusyError(
    val targetName: MutableComponent
): BattleStartError {
    override fun getMessageFor(entity: Entity) = battleLang("errors.busy", targetName)
}

open class BattleActorErrors : HashMap<BattleActor, MutableSet<BattleStartError>>() {
    override operator fun get(key: BattleActor): MutableSet<BattleStartError> {
        return super.get(key) ?: mutableSetOf<BattleStartError>().also { this[key] = it }
    }
}

open class ErroredBattleStart(
    val generalErrors: MutableSet<BattleStartError> = mutableSetOf(),
    val participantErrors: BattleActorErrors = BattleActorErrors()
) : BattleStartResult() {
    override fun ifErrored(action: (ErroredBattleStart) -> Unit): BattleStartResult {
        action(this)
        return this
    }

    inline fun <reified T : BattleStartError> forError(action: (T) -> Unit): ErroredBattleStart {
        errors.filterIsInstance<T>().forEach { action(it) }
        return this
    }

    fun sendTo(entity: Entity, transformer: (MutableComponent) -> (MutableComponent) = { it }) {
        errors.forEach { entity.sendSystemMessage(transformer(it.getMessageFor(entity))) }
    }

    inline fun <reified T : BattleStartError> ifHasError(action: () -> Unit): ErroredBattleStart {
        if (errors.filterIsInstance<T>().isNotEmpty()) {
            action()
        }
        return this
    }

    val isEmpty: Boolean
        get() = generalErrors.isEmpty() && participantErrors.values.all { it.isEmpty() }

    fun isPlayerToBlame(player: ServerPlayer) = generalErrors.isEmpty()
        && participantErrors.size == 1
        && participantErrors.entries.first().let { it.key.uuid == player.uuid }

    fun isSomePlayerToBlame() = generalErrors.isEmpty() && participantErrors.isNotEmpty()

    val playersToBlame: Iterable<ServerPlayer>
        get() = participantErrors.keys.mapNotNull { it.uuid.getPlayer() }

    val actorsToBlame: Iterable<BattleActor>
        get() = participantErrors.keys

    val errors: Iterable<BattleStartError>
        get() = generalErrors + participantErrors.flatMap { it.value }
}