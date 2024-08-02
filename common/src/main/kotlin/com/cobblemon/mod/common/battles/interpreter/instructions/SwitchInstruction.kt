/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.dispatch.*
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.effects.IllusionEffect
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import com.cobblemon.mod.common.util.swap
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import java.util.concurrent.CompletableFuture

/**
 * Format: |switch|POKEMON|DETAILS|HP STATUS
 *
 * POKEMON has switched in (if there was an old Pokémon at that position, it is switched out).
 * POKEMON|DETAILS represents all the information that can be used to tell Pokémon apart.
 * The switched Pokémon has HP health points and STATUS status.
 * @author Deltric
 * @since January 22nd, 2022
 */
class SwitchInstruction(val instructionSet: InstructionSet, val battleActor: BattleActor, val publicMessage: BattleMessage, val privateMessage: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {

        val (pnx, _) = publicMessage.pnxAndUuid(0) ?: return
        val (actor, activePokemon) = battle.getActorAndActiveSlotFromPNX(pnx)
        val entity = if (actor is EntityBackedBattleActor<*>) actor.entity else null

        val imposter = instructionSet.getNextInstruction<TransformInstruction>(this)?.expectedTarget != null
        val illusion = publicMessage.battlePokemonFromOptional(battle, "is")
        val pokemon = publicMessage.battlePokemon(0, battle) ?: return

        if (!battle.started) {
            activePokemon.battlePokemon = pokemon
            activePokemon.illusion = illusion
            val pokemonEntity = pokemon.entity?.let {
                // If a Pokémon entity is being recalled with an animation,
                // wrap up the animation and recall the Pokémon immediately.
                if (it.beamMode == 3) {
                    pokemon.effectedPokemon.recall()
                    return@let null
                }
                pokemon.entity
            }

            if (pokemonEntity == null && entity != null) {
                val targetPos = battleActor.getSide().getOppositeSide().actors.filterIsInstance<EntityBackedBattleActor<*>>().firstOrNull()?.entity?.position()?.let { pos ->
                    val offset = pos.subtract(entity.position())
                    val idealPos = entity.position().add(offset.scale(0.33))
                    idealPos
                } ?: entity.position()

                actor.stillSendingOutCount++
                pokemon.effectedPokemon.sendOutWithAnimation(
                    source = entity,
                    battleId = battle.battleId,
                    level = entity.level() as ServerLevel,
                    doCry = false,
                    position = targetPos,
                    illusion = illusion?.let { IllusionEffect(it.effectedPokemon) }
                ).thenApply {
                    actor.stillSendingOutCount--
                }
            }
            else if (pokemonEntity != null) {
                illusion?.let { IllusionEffect(it.effectedPokemon).start(pokemonEntity) }
            }
        } else {
            battle.dispatchInsert {
                pokemon.sendUpdate()

                if (activePokemon.battlePokemon == pokemon) {
                    return@dispatchInsert emptySet() // Already switched in, Showdown does this if the pokemon is going to die before it can switch
                }

                activePokemon.battlePokemon?.let { oldPokemon ->
                    if (publicMessage.effect()?.id == "batonpass") oldPokemon.contextManager.swap(pokemon.contextManager, BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
                    oldPokemon.contextManager.clear(BattleContext.Type.VOLATILE, BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
                    battle.majorBattleActions[oldPokemon.uuid] = publicMessage
                }
                battle.majorBattleActions[pokemon.uuid] = publicMessage

                setOf(
                    BattleDispatch {
                        if (entity != null) {
                            createEntitySwitch(battle, actor, entity, pnx, activePokemon, pokemon, illusion, imposter)
                        } else {
                            createNonEntitySwitch(battle, actor, pnx, activePokemon, pokemon, illusion)
                        }
                    }
                )
            }
        }
    }

    companion object{
        fun createEntitySwitch(
            battle: PokemonBattle,
            actor: BattleActor,
            entity: LivingEntity,
            pnx: String,
            activePokemon: ActiveBattlePokemon,
            newPokemon: BattlePokemon,
            illusion: BattlePokemon? = null,
            imposter: Boolean = false
        ): DispatchResult {
            val pokemonEntity = activePokemon.battlePokemon?.entity
            // If we can't find the entity for some reason then we're going to skip the recall animation
            val sendOutFuture = CompletableFuture<Unit>()
            // skip if handled by respective EntityEffect
            val doCry = illusion == null && !imposter
            (pokemonEntity?.recallWithAnimation() ?: CompletableFuture.completedFuture(Unit)).thenApply {
                // Queue actual swap and send-in after the animation has ended
                actor.pokemonList.swap(actor.activePokemon.indexOf(activePokemon), actor.pokemonList.indexOf(newPokemon))
                activePokemon.battlePokemon = newPokemon
                activePokemon.illusion = illusion
                battle.sendSidedUpdate(actor, BattleSwitchPokemonPacket(pnx, newPokemon, true, illusion), BattleSwitchPokemonPacket(pnx, newPokemon, false, illusion))
                if (newPokemon.entity != null) {
                    illusion?.let { IllusionEffect(it.effectedPokemon).start(newPokemon.entity!!) }
                    if (doCry) newPokemon.entity?.cry()
                    sendOutFuture.complete(Unit)
                } else {
                    val lastPosition = activePokemon.position
                    // Send out at previous Pokémon's location if it is known, otherwise actor location
                    val world = lastPosition?.first ?: entity.level() as ServerLevel
                    val pos = lastPosition?.second ?: entity.position()
                    newPokemon.effectedPokemon.sendOutWithAnimation(
                        source = entity,
                        battleId = battle.battleId,
                        level = world,
                        position = pos,
                        doCry = doCry,
                        illusion = illusion?.let { IllusionEffect(it.effectedPokemon) }
                    ).thenAccept { sendOutFuture.complete(Unit) }
                }
            }

            return UntilDispatch { sendOutFuture.isDone }
        }

        fun createNonEntitySwitch(battle: PokemonBattle, actor: BattleActor, pnx: String, activePokemon: ActiveBattlePokemon, newPokemon: BattlePokemon, illusion: BattlePokemon? = null): DispatchResult {
            actor.pokemonList.swap(actor.activePokemon.indexOf(activePokemon), actor.pokemonList.indexOf(newPokemon))
            activePokemon.battlePokemon = newPokemon
            activePokemon.illusion = illusion
            battle.sendSidedUpdate(actor, BattleSwitchPokemonPacket(pnx, newPokemon, true, illusion), BattleSwitchPokemonPacket(pnx, newPokemon, false, illusion))
            return WaitDispatch(1.5F)
        }

    }

}