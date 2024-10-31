/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokeball.catching.effects.CaptureEffects
import com.cobblemon.mod.common.api.pokeball.catching.effects.FriendshipEarningBoostEffect
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.*
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import kotlin.math.roundToInt

/**
 * The data registry for [PokeBall]s.
 * All the pokeball fields are guaranteed to exist
 */
object PokeBalls : JsonDataRegistry<PokeBall> {

    override val id = cobblemonResource("pokeballs")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<PokeBalls>()

    // ToDo once datapack pokeball is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeBall> = TypeToken.get(PokeBall::class.java)
    override val resourcePath = "pokeballs"

    private val defaults = hashMapOf<ResourceLocation, PokeBall>()
    // ToDo datapack pokeball type here instead
    private val custom = hashMapOf<ResourceLocation, PokeBall>()

    val POKE_BALL
        get() = this.byName("poke_ball")
    val SLATE_BALL
        get() = this.byName("slate_ball")
    val AZURE_BALL
        get() = this.byName("azure_ball")
    val VERDANT_BALL
        get() = this.byName("verdant_ball")
    val ROSEATE_BALL
        get() = this.byName("roseate_ball")
    val CITRINE_BALL
        get() = this.byName("citrine_ball")
    val GREAT_BALL
        get() = this.byName("great_ball")
    val ULTRA_BALL
        get() = this.byName("ultra_ball")
    val MASTER_BALL
        get() = this.byName("master_ball")
    val SAFARI_BALL
        get() = this.byName("safari_ball")
    val FAST_BALL
        get() = this.byName("fast_ball")
    val LEVEL_BALL
        get() = this.byName("level_ball")
    val LURE_BALL
        get() = this.byName("lure_ball")
    val HEAVY_BALL
        get() = this.byName("heavy_ball")
    val LOVE_BALL
        get() = this.byName("love_ball")
    val FRIEND_BALL
        get() = this.byName("friend_ball")
    val MOON_BALL
        get() = this.byName("moon_ball")
    val SPORT_BALL
        get() = this.byName("sport_ball")
    val NET_BALL
        get() = this.byName("net_ball")
    val DIVE_BALL
        get() = this.byName("dive_ball")
    val NEST_BALL
        get() = this.byName("nest_ball")
    val REPEAT_BALL
        get() = this.byName("repeat_ball")
    val TIMER_BALL
        get() = this.byName("timer_ball")
    val LUXURY_BALL
        get() = this.byName("luxury_ball")
    val PREMIER_BALL
        get() = this.byName("premier_ball")
    val DUSK_BALL
        get() = this.byName("dusk_ball")
    val HEAL_BALL
        get() = this.byName("heal_ball")
    val QUICK_BALL
        get() = this.byName("quick_ball")
    val CHERISH_BALL
        get() = this.byName("cherish_ball")
    val PARK_BALL
        get() = this.byName("park_ball")
    val DREAM_BALL
        get() = this.byName("dream_ball")
    val BEAST_BALL
        get() = this.byName("beast_ball")
    val ANCIENT_POKE_BALL
        get() = this.byName("ancient_poke_ball")
    val ANCIENT_CITRINE_BALL
        get() = this.byName("ancient_citrine_ball")
    val ANCIENT_VERDANT_BALL
        get() = this.byName("ancient_verdant_ball")
    val ANCIENT_AZURE_BALL
        get() = this.byName("ancient_azure_ball")
    val ANCIENT_ROSEATE_BALL
        get() = this.byName("ancient_roseate_ball")
    val ANCIENT_SLATE_BALL
        get() = this.byName("ancient_slate_ball")
    val ANCIENT_IVORY_BALL
        get() = this.byName("ancient_ivory_ball")
    val ANCIENT_GREAT_BALL
        get() = this.byName("ancient_great_ball")
    val ANCIENT_ULTRA_BALL
        get() = this.byName("ancient_ultra_ball")
    val ANCIENT_HEAVY_BALL
        get() = this.byName("ancient_heavy_ball")
    val ANCIENT_LEADEN_BALL
        get() = this.byName("ancient_leaden_ball")
    val ANCIENT_GIGATON_BALL
        get() = this.byName("ancient_gigaton_ball")
    val ANCIENT_FEATHER_BALL
        get() = this.byName("ancient_feather_ball")
    val ANCIENT_WING_BALL
        get() = this.byName("ancient_wing_ball")
    val ANCIENT_JET_BALL
        get() = this.byName("ancient_jet_ball")
    val ANCIENT_ORIGIN_BALL
        get() = this.byName("ancient_origin_ball")

    init {
        createDefault("poke_ball")
        createDefault("slate_ball")
        createDefault("azure_ball")
        createDefault("verdant_ball")
        createDefault("roseate_ball")
        createDefault("citrine_ball")
        createDefault("great_ball", MultiplierModifier(1.5F))
        createDefault("ultra_ball", MultiplierModifier(2F))
        createDefault("master_ball", GuaranteedModifier())
        createDefault("safari_ball", CatchRateModifiers.SAFARI)
        createDefault("fast_ball", BaseStatModifier(Stats.SPEED, { it >= 100 }, 4F))
        createDefault("level_ball", CatchRateModifiers.LEVEL)
        createDefault("lure_ball", CatchRateModifiers.LURE)
        createDefault("heavy_ball", CatchRateModifiers.WEIGHT_BASED)
        createDefault("love_ball", CatchRateModifiers.LOVE)
        createDefault("friend_ball", effects = listOf(CaptureEffects.friendshipSetter(150)))
        createDefault("moon_ball", CatchRateModifiers.MOON_PHASES)
        createDefault("sport_ball", MultiplierModifier(1.5F))
        createDefault("net_ball", CatchRateModifiers.typeBoosting(3F, ElementalTypes.BUG, ElementalTypes.WATER))
        createDefault("dive_ball", CatchRateModifiers.SUBMERGED_IN_WATER, waterDragValue = 0.99F)
        createDefault("nest_ball", CatchRateModifiers.NEST)
        createDefault("repeat_ball", CatchRateModifiers.REPEAT)
        createDefault("timer_ball", CatchRateModifiers.turnBased { turn -> (1F * turn * (1229F / 4096F)).coerceAtMost(4F) })
        createDefault("luxury_ball", effects = listOf(FriendshipEarningBoostEffect(2F)))
        createDefault("premier_ball")
        createDefault("dusk_ball", CatchRateModifiers.LIGHT_LEVEL)
        createDefault("heal_ball", effects = listOf(CaptureEffects.FULL_RESTORE))
        createDefault("quick_ball", CatchRateModifiers.turnBased { turn -> if (turn == 1) 5F else 1F })
        createDefault("cherish_ball")
        createDefault("park_ball", CatchRateModifiers.PARK)
        createDefault("dream_ball", CatchRateModifiers.statusBoosting(4F, Statuses.SLEEP))
        createDefault("beast_ball", LabelModifier(5F, true, CobblemonPokemonLabels.ULTRA_BEAST)/*, LabelModifier(0.1F, false, CobblemonPokemonLabels.ULTRA_BEAST))*/)
        createDefault("ancient_poke_ball", ancient = true)
        createDefault("ancient_citrine_ball", ancient = true)
        createDefault("ancient_verdant_ball", ancient = true)
        createDefault("ancient_azure_ball", ancient = true)
        createDefault("ancient_roseate_ball", ancient = true)
        createDefault("ancient_slate_ball", ancient = true)
        createDefault("ancient_ivory_ball", ancient = true)
        createDefault("ancient_great_ball", MultiplierModifier(1.5F), ancient = true)
        createDefault("ancient_ultra_ball", MultiplierModifier(2F), ancient = true)
        createDefault("ancient_heavy_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_leaden_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_gigaton_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_feather_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_wing_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_jet_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_origin_ball", GuaranteedModifier(), ancient = true)
        // Luxury ball effect, low priority as it must be triggered before soothe bell as of gen 4
        CobblemonEvents.FRIENDSHIP_UPDATED.subscribe(priority = Priority.LOW) { event ->
            var increment = (event.newFriendship - event.pokemon.friendship).toFloat()
            if (increment <= 1F) {
                event.pokemon.caughtBall.effects.filterIsInstance<FriendshipEarningBoostEffect>()
                    .forEach { increment *= it.multiplier }
            }
            event.newFriendship = event.pokemon.friendship + increment.roundToInt()
        }
    }

    override fun reload(data: Map<ResourceLocation, PokeBall>) {
        this.custom.clear()
        // ToDo once datapack pokeball is implemented load them here, we will want datapacks to be able to override our default pokeballs too, however they will never be able to disable them
    }

    override fun sync(player: ServerPlayer) {
        // ToDo once datapack pokeball is implemented sync them here
    }

    /**
     * Gets a Pokeball from registry name.
     * @return the pokeball object if found otherwise null.
     */
    fun getPokeBall(name : ResourceLocation): PokeBall? = this.custom[name] ?: this.defaults[name]

    fun all() = this.defaults.filterKeys { !this.custom.containsKey(it) }.values + this.custom.values

    private fun createDefault(
        name: String,
        modifier: CatchRateModifier = MultiplierModifier(1F) { _, _ -> true },
        effects: List<CaptureEffect> = emptyList(),
        waterDragValue: Float = 0.8F,
        model2d: ResourceLocation = cobblemonResource(name),
        model3d: ResourceLocation = cobblemonResource("item/${name}_model"),
        throwPower: Float = 1.25f,
        ancient: Boolean = false
    ): PokeBall {
        val identifier = cobblemonResource(name)
        //val finalModifiers = if (appendUltraBeastPenalty) modifiers + listOf(LabelModifier(0.1F, true, CobblemonPokemonLabels.ULTRA_BEAST)) else modifiers
        val pokeball = PokeBall(identifier, modifier, effects, waterDragValue, model2d, model3d, throwPower, ancient)
        this.defaults[identifier] = pokeball
        return pokeball
    }

    private fun byName(name: String): PokeBall {
        val identifier = cobblemonResource(name)
        return this.custom[identifier] ?: this.defaults[identifier]!!
    }
}