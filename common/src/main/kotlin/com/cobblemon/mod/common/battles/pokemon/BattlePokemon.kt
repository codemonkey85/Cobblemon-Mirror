/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.pokemon

import com.bedrockk.molang.runtime.struct.VariableStruct
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.battles.actor.MultiPokemonBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.interpreter.ContextManager
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.BattleUpdateTeamPokemonPacket
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.battleLang
import java.util.UUID
import net.minecraft.network.chat.MutableComponent

open class BattlePokemon(
    val originalPokemon: Pokemon,
    val effectedPokemon: Pokemon = originalPokemon,
    val postBattleEntityOperation: (PokemonEntity) -> Unit = {}
) {
    lateinit var actor: BattleActor
    companion object {
        fun safeCopyOf(pokemon: Pokemon): BattlePokemon = BattlePokemon(
            originalPokemon = pokemon,
            effectedPokemon = pokemon.clone(),
            postBattleEntityOperation = { entity -> entity.discard() }
        )

        fun playerOwned(pokemon: Pokemon): BattlePokemon = BattlePokemon(
            originalPokemon = pokemon,
            effectedPokemon = pokemon,
            postBattleEntityOperation = { entity ->
                entity.effects.wipe()
            }
        )
    }

    val uuid: UUID
        get() = effectedPokemon.uuid
    val health: Int
        get() = effectedPokemon.currentHealth
    val maxHealth: Int
        get() = effectedPokemon.hp
    val ivs: IVs
        get() = effectedPokemon.ivs
    val nature: Nature
        get() = effectedPokemon.nature
    val moveSet: MoveSet
        get() = effectedPokemon.moveSet
    val statChanges = mutableMapOf<Stat, Int>()
    var gone = false
    // etc

    val entity: PokemonEntity?
        get() = effectedPokemon.entity

    var willBeSwitchedIn = false

    /** A set of all the BattlePokemon that they faced during the battle (for exp purposes) */
    val facedOpponents = mutableSetOf<BattlePokemon>()

    /**
     * The [HeldItemManager] backing this [BattlePokemon].
     */
    val heldItemManager: HeldItemManager by lazy { HeldItemProvider.provide(this) }

    val contextManager = ContextManager()

    open fun getName(): MutableComponent {
        val displayPokemon = getIllusion()?.effectedPokemon ?: effectedPokemon
        return if (actor is PokemonBattleActor || actor is MultiPokemonBattleActor) {
            displayPokemon.getDisplayName()
        } else {
            battleLang("owned_pokemon", actor.getName(), displayPokemon.getDisplayName())
        }
    }

    fun sendUpdate() {
        actor.sendUpdate(BattleUpdateTeamPokemonPacket(effectedPokemon))
    }

    fun isSentOut() = actor.battle.activePokemon.any { it.battlePokemon == this }
    fun canBeSentOut() =
            if (actor.request?.side?.pokemon?.get(0)?.reviving == true) {
                !isSentOut() && !willBeSwitchedIn && health <= 0
            } else {
                !isSentOut() && !willBeSwitchedIn && health > 0
            }

    fun writeVariables(struct: VariableStruct) {
        effectedPokemon.writeVariables(struct)
    }

    fun getIllusion(): BattlePokemon? = this.actor.activePokemon.find { it.battlePokemon == this }?.illusion
}