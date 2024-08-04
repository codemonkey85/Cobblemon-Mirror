/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events

import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent
import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent
import com.cobblemon.mod.common.api.events.battles.BattleStartedPreEvent
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.berry.BerryHarvestEvent
import com.cobblemon.mod.common.api.events.berry.BerryMutationOfferEvent
import com.cobblemon.mod.common.api.events.berry.BerryMutationResultEvent
import com.cobblemon.mod.common.api.events.berry.BerryYieldCalculationEvent
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntityLoadEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveEvent
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveToWorldEvent
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.api.events.item.LeftoversCreatedEvent
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent
import com.cobblemon.mod.common.api.events.pokemon.*
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionAcceptedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionDisplayEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionTestedEvent
import com.cobblemon.mod.common.api.events.pokemon.interaction.ExperienceCandyUseEvent
import com.cobblemon.mod.common.api.events.pokemon.interaction.HeldItemUpdatedEvent
import com.cobblemon.mod.common.api.events.pokemon.interaction.PokemonInteractionGUICreationEvent
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.api.events.world.BigRootPropagatedEvent
import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.api.reactive.Observable.Companion.filter
import com.cobblemon.mod.common.api.reactive.Observable.Companion.map
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

@Suppress("unused")
object CobblemonEvents {

    @JvmField
    val DATA_SYNCHRONIZED = SimpleObservable<ServerPlayer>()
    @JvmField
    val SHOULDER_MOUNT = CancelableObservable<ShoulderMountEvent>()
    @JvmField
    val FRIENDSHIP_UPDATED = EventObservable<FriendshipUpdatedEvent>()
    @JvmField
    val POKEMON_FAINTED = EventObservable<PokemonFaintedEvent>()
    @JvmField
    val EVOLUTION_ACCEPTED = CancelableObservable<EvolutionAcceptedEvent>()
    @JvmField
    val EVOLUTION_DISPLAY = EventObservable<EvolutionDisplayEvent>()
    @JvmField
    val EVOLUTION_TESTED = EventObservable<EvolutionTestedEvent>()
    @JvmField
    val EVOLUTION_COMPLETE = EventObservable<EvolutionCompleteEvent>()
    @JvmField
    val POKEMON_NICKNAMED = CancelableObservable<PokemonNicknamedEvent>()
    @JvmField
    val HELD_ITEM_UPDATED = CancelableObservable<HeldItemUpdatedEvent>()

    @JvmField
    val THROWN_POKEBALL_HIT = CancelableObservable<ThrownPokeballHitEvent>()
    @JvmField
    val POKEMON_CATCH_RATE = EventObservable<PokemonCatchRateEvent>()
    @JvmField
    val POKE_BALL_CAPTURE_CALCULATED = EventObservable<PokeBallCaptureCalculatedEvent>()
    @JvmField
    val POKEMON_CAPTURED = EventObservable<PokemonCapturedEvent>()
    @JvmField
    val FOSSIL_REVIVED = EventObservable<FossilRevivedEvent>()
//    @JvmField
//    val EGG_HATCH = EventObservable<HatchEggEvent>()
    @JvmField
    val BATTLE_STARTED_PRE = CancelableObservable<BattleStartedPreEvent>()
    @JvmField
    val BATTLE_STARTED_POST = EventObservable<BattleStartedPostEvent>()
    @JvmField
    val BATTLE_FLED = EventObservable<BattleFledEvent>()
    @JvmField
    val BATTLE_VICTORY = EventObservable<BattleVictoryEvent>()
    @JvmField
    val BATTLE_FAINTED = EventObservable<BattleFaintedEvent>()

    @JvmField
    val POKEMON_SENT_PRE = CancelableObservable<PokemonSentPreEvent>()
    @JvmField
    val POKEMON_SENT_POST = EventObservable<PokemonSentPostEvent>()
    @JvmField
    val POKEMON_RECALLED = EventObservable<PokemonRecalledEvent>()

    @JvmField
    val TRADE_COMPLETED = EventObservable<TradeCompletedEvent>()

    @JvmField
    val LEVEL_UP_EVENT = EventObservable<LevelUpEvent>()

    @JvmField
    /** CLIENT ONLY! */
    val POKEMON_INTERACTION_GUI_CREATION = EventObservable<PokemonInteractionGUICreationEvent>()
    @JvmField
    val POKEMON_ENTITY_SAVE = EventObservable<PokemonEntitySaveEvent>()
    @JvmField
    val POKEMON_ENTITY_LOAD = CancelableObservable<PokemonEntityLoadEvent>()
    @JvmField
    val POKEMON_ENTITY_SAVE_TO_WORLD = CancelableObservable<PokemonEntitySaveToWorldEvent>()
    @JvmField
    val ENTITY_SPAWN = CancelableObservable<SpawnEvent<*>>()

    @JvmField
    val POKEMON_ENTITY_SPAWN = ENTITY_SPAWN
        .pipe(
            filter { it.entity is PokemonEntity },
            map {
                @Suppress("UNCHECKED_CAST")
                it as SpawnEvent<PokemonEntity>
            }
        )

    @JvmField
    val EXPERIENCE_GAINED_EVENT_PRE = CancelableObservable<ExperienceGainedPreEvent>()
    @JvmField
    val EXPERIENCE_GAINED_EVENT_POST = EventObservable<ExperienceGainedPostEvent>()
    @JvmField
    val EXPERIENCE_CANDY_USE_PRE = CancelableObservable<ExperienceCandyUseEvent.Pre>()
    @JvmField
    val EXPERIENCE_CANDY_USE_POST = EventObservable<ExperienceCandyUseEvent.Post>()

    @JvmField
    val POKEMON_RELEASED_EVENT_PRE = CancelableObservable<ReleasePokemonEvent.Pre>()
    @JvmField
    val POKEMON_RELEASED_EVENT_POST = EventObservable<ReleasePokemonEvent.Post>()

    @JvmField
    val LOOT_DROPPED = CancelableObservable<LootDroppedEvent>()
    @JvmField
    val STARTER_CHOSEN = CancelableObservable<StarterChosenEvent>()

    @JvmField
    val APRICORN_HARVESTED = EventObservable<ApricornHarvestEvent>()
    // Berries
    @JvmField
    val BERRY_HARVEST = EventObservable<BerryHarvestEvent>()
    @JvmField
    val BERRY_MUTATION_OFFER = EventObservable<BerryMutationOfferEvent>()
    @JvmField
    val BERRY_MUTATION_RESULT = EventObservable<BerryMutationResultEvent>()
    @JvmField
    val BERRY_YIELD = EventObservable<BerryYieldCalculationEvent>()
    @JvmField
    val LEFTOVERS_CREATED = CancelableObservable<LeftoversCreatedEvent>()
    @JvmField
    val BIG_ROOT_PROPAGATED = CancelableObservable<BigRootPropagatedEvent>()
    @JvmField
    val HELD_ITEM_PRE = CancelableObservable<HeldItemEvent.Pre>()
    @JvmField
    val HELD_ITEM_POST = EventObservable<HeldItemEvent.Post>()
}