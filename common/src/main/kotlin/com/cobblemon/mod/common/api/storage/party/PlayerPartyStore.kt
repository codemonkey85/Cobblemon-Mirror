/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.party

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PassiveEvolution
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.pokemon.OriginalTrainerType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution
import com.cobblemon.mod.common.util.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import java.util.*
import kotlin.math.ceil
import kotlin.math.round
import kotlin.random.Random

/**
 * A [PartyStore] used for a single player. This uses the player's UUID as the store's UUID, and is declared as its own
 * class so that the purpose of this store is clear in practice. It also automatically adds the player's UUID as an
 * observer UUID as per [PartyStore.observerUUIDs]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
open class PlayerPartyStore(
    /** The UUID of the player this store is for. */
    val playerUUID: UUID,
    /** The UUID of the store. This is the same as [playerUUID] by default, but can be changed to allow for multiple parties. */
    storageUUID: UUID = playerUUID
) : PartyStore(storageUUID) {

    private var secondsSinceFriendshipUpdate = 0

    constructor(playerUUID: UUID): this(playerUUID, playerUUID)

    override fun initialize() {
        super.initialize()
        observerUUIDs.add(playerUUID)
    }

    open fun getOverflowPC(): PCStore? {
        return Cobblemon.storage.getPC(playerUUID)
    }

    override fun add(pokemon: Pokemon): Boolean {
        if (pokemon.originalTrainerType == OriginalTrainerType.NONE) {
            pokemon.setOriginalTrainer(playerUUID)
        }
        pokemon.refreshOriginalTrainer()

        return if (super.add(pokemon)) {
            pokemon.getOwnerPlayer()?.let { CobblemonCriteria.PARTY_CHECK.trigger(it, this) }
            true
        } else {
            val player = playerUUID.getPlayer()
            val pc = getOverflowPC()

            if (pc == null || !pc.add(pokemon)) {
                if (pc == null) {
                    player?.sendSystemMessage(lang("overflow_no_pc"))
                } else {
                    player?.sendSystemMessage(lang("overflow_no_space", pc.name))
                }
                false
            } else {
                player?.sendSystemMessage(lang("overflow_to_pc", pokemon.species.translatedName, pc.name))
                true
            }
        }
    }

    /**
     * Called on the party every second for routine party updates
     * ex: Passive healing, statuses, etc
     */
    fun onSecondPassed(player: ServerPlayer) {
        // Passive healing and passive statuses require the player be out of battle
        if (BattleRegistry.getBattleByParticipatingPlayer(player) == null) {
            val random = Random.Default
            for (pokemon in this) {
                // Awake from fainted
                if (pokemon.isFainted()) {
                    pokemon.faintedTimer -= 1
                    if (pokemon.faintedTimer <= -1) {
                        val php = ceil(pokemon.hp * Cobblemon.config.faintAwakenHealthPercent)
                        pokemon.currentHealth = php.toInt()
                        player.sendSystemMessage(Component.translatable("cobblemon.party.faintRecover", pokemon.getDisplayName()))
                    }
                }
                // Passive healing while less than full health
                else if (pokemon.currentHealth < pokemon.hp) {
                    pokemon.healTimer--
                    if (pokemon.healTimer <= -1) {
                        pokemon.healTimer = Cobblemon.config.healTimer
                        val healAmount = 1.0.coerceAtLeast(pokemon.hp.toDouble() * Cobblemon.config.healPercent)
                        pokemon.currentHealth = pokemon.currentHealth + round(healAmount).toInt()
                    }
                }

                // Statuses
                val status = pokemon.status
                if (status != null && !player.isSleeping) {
                    if (status.isExpired()) {
                        status.status.onStatusExpire(player, pokemon, random)
                        pokemon.status = null
                    } else {
                        status.status.onSecondPassed(player, pokemon, random)
                        status.tickTimer()
                    }
                }

                // Passive evolutions
                pokemon.lockedEvolutions.filterIsInstance<PassiveEvolution>().forEach { it.attemptEvolution(pokemon) }
                val removeList = mutableListOf<Evolution>()
                pokemon.evolutionProxy.server().forEach {
                    if (!it.test(pokemon) && it is LevelUpEvolution && !it.permanent)
                        removeList.add(it)
                }
                removeList.forEach { pokemon.evolutionProxy.server().remove(it) }
            }
            // Friendship
            // ToDo expand this down the line just a very basic implementation for the first releases
            if (++this.secondsSinceFriendshipUpdate == 120) {
                this.secondsSinceFriendshipUpdate = 0
                this.forEach { pokemon ->
                    if (pokemon.friendship < 160) {
                        if (pokemon.entity != null || pokemon.state is ShoulderedState) {
                            pokemon.incrementFriendship(1)
                        }
                    }
                }
            }
        }

        // Shoulder validation code
        if (player.shoulderEntityLeft.isPokemonEntity() && !validateShoulder(player.shoulderEntityLeft, true)) {
            player.respawnEntityOnShoulder(player.shoulderEntityLeft)
        }
        if (player.shoulderEntityRight.isPokemonEntity() && !validateShoulder(player.shoulderEntityRight, false)) {
            player.respawnEntityOnShoulder(player.shoulderEntityRight)
        }

        forEach {
            val state = it.state
            if (state is ShoulderedState && !state.isStillShouldered(player)) {
                it.recall()
            }
        }
    }

    fun validateShoulder(shoulderEntity: CompoundTag, isLeft: Boolean): Boolean {
        val pokemon = find { it.uuid == shoulderEntity.getCompound("Pokemon").getUUID(DataKeys.POKEMON_UUID) }
        if (pokemon == null || (pokemon.state as? ShoulderedState)?.isLeftShoulder != isLeft) {
            return false
        }
        return true
    }

    override fun swap(position1: PartyPosition, position2: PartyPosition) {
        super.swap(position1, position2)

        //Make it so we can check what's in the Player's party
        val pokemon1 = get(position1)
        val pokemon2 = get(position2)
        if (pokemon1 != null && pokemon2 != null) {
            val player = pokemon1.getOwnerPlayer()
            if (player != null) {
                CobblemonCriteria.PARTY_CHECK.trigger(player, this)
            }
        } else if (pokemon1 != null || pokemon2 != null) {
            var player = pokemon1?.getOwnerPlayer()
            if (player != null) {
                CobblemonCriteria.PARTY_CHECK.trigger(player, this)
            } else {
                player = pokemon2!!.getOwnerPlayer()
                CobblemonCriteria.PARTY_CHECK.trigger(player!!, this)
            }
        }
    }

    override fun set(position: PartyPosition, pokemon: Pokemon) {
        super.set(position, pokemon)
        pokemon.getOwnerPlayer()?.let { CobblemonCriteria.PARTY_CHECK.trigger(it, this) }
    }
}