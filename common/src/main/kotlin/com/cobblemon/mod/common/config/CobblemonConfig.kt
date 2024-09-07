/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.config

import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.pokeball.catching.calculators.CobblemonCaptureCalculator
import com.cobblemon.mod.common.util.adapters.CaptureCalculatorAdapter
import com.cobblemon.mod.common.util.adapters.IntRangeAdapter
import com.google.gson.GsonBuilder

class CobblemonConfig {
    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
            .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
            .registerTypeAdapter(CaptureCalculator::class.java, CaptureCalculatorAdapter)
            .create()
    }

    var lastSavedVersion: String = "0.0.1"

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 1, max = 1000)
    var maxPokemonLevel = 100

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 0, max = 1000)
    var maxPokemonFriendship = 255

    @NodeCategory(Category.Pokemon)
    var announceDropItems = true
    @NodeCategory(Category.Pokemon)
    var defaultDropItemMethod = ItemDropMethod.ON_ENTITY
    @NodeCategory(Category.Pokemon)
    @LastChangedVersion("1.4.0")
    var ambientPokemonCryTicks = 1080

    @NodeCategory(Category.Storage)
    @IntConstraint(min = 1, max = 1000)
    var defaultBoxCount = 30
    @NodeCategory(Category.Storage)
    @IntConstraint(min = 1, max = 120)
    var pokemonSaveIntervalSeconds = 30

    @NodeCategory(Category.Storage)
    var storageFormat = "nbt"

    @NodeCategory(Category.Storage)
    var preventCompletePartyDeposit = false

    @NodeCategory(Category.Storage)
    var mongoDBConnectionString = "mongodb://localhost:27017"
    @NodeCategory(Category.Storage)
    var mongoDBDatabaseName = "cobblemon"

    // TODO new types of constraint

    @NodeCategory(Category.Spawning)
    @IntConstraint(min = 1, max = 200)
    var maxVerticalCorrectionBlocks = 64

    @NodeCategory(Category.Spawning)
    @IntConstraint(min = 1, max = 1000)
    var minimumLevelRangeMax = 10

    @NodeCategory(Category.Spawning)
    var enableSpawning = true

    @NodeCategory(Category.Spawning)
    var minimumDistanceBetweenEntities = 8.0

    @NodeCategory(Category.Spawning)
    var maxNearbyBlocksHorizontalRange = 4

    @NodeCategory(Category.Spawning)
    var maxNearbyBlocksVerticalRange = 2

    @NodeCategory(Category.Spawning)
    var maxVerticalSpace = 8

    @NodeCategory(Category.Spawning)
    var worldSliceDiameter = 8

    @NodeCategory(Category.Spawning)
    var worldSliceHeight = 16

    @NodeCategory(Category.Spawning)
    var ticksBetweenSpawnAttempts = 20F

    @NodeCategory(Category.Spawning)
    var minimumSliceDistanceFromPlayer = 16F

    @NodeCategory(Category.Spawning)
    var maximumSliceDistanceFromPlayer = 16 * 4F

    @NodeCategory(Category.Spawning)
    var exportSpawnConfig = false

    @NodeCategory(Category.Spawning)
    var savePokemonToWorld = true

    @NodeCategory(Category.Starter)
    var exportStarterConfig = false

    @NodeCategory(Category.Battles)
    var autoUpdateShowdown = true

    @NodeCategory(Category.Battles)
    var defaultFleeDistance = 16F * 2

    @NodeCategory(category = Category.Battles)
    var allowExperienceFromPvP = true

    @NodeCategory(category = Category.Battles)
    var experienceShareMultiplier = .5

    @NodeCategory(category = Category.Battles)
    var luckyEggMultiplier = 1.5

    @NodeCategory(category = Category.Battles)
    var allowSpectating = true

    @NodeCategory(category = Category.Pokemon)
    var experienceMultiplier = 2F

    @NodeCategory(category = Category.Spawning)
    var pokemonPerChunk = 1F

    @NodeCategory(Category.PassiveStatus)
    var passiveStatuses = mutableMapOf(
        Statuses.POISON.configEntry(),
        Statuses.POISON_BADLY.configEntry(),
        Statuses.PARALYSIS.configEntry(),
        Statuses.FROZEN.configEntry(),
        Statuses.SLEEP.configEntry(),
        Statuses.BURN.configEntry()
    )

    @NodeCategory(Category.Healing)
    var infiniteHealerCharge = false

    @NodeCategory(Category.Healing)
    var maxHealerCharge = 6.0f

    @NodeCategory(Category.Healing)
    var chargeGainedPerTick = 0.000333333f

    @NodeCategory(Category.Healing)
    var defaultFaintTimer = 300

    @NodeCategory(Category.Healing)
    var faintAwakenHealthPercent = 0.2f

    @NodeCategory(Category.Healing)
    var healPercent = 0.05

    @NodeCategory(Category.Healing)
    var healTimer = 60

    @NodeCategory(Category.Spawning)
    var baseApricornTreeGenerationChance = 0.1F

    @NodeCategory(Category.Pokemon)
    var displayEntityLevelLabel = true

    @NodeCategory(Category.Spawning)
    var shinyRate = 8192F

    @NodeCategory(Category.Pokemon)
    var captureCalculator: CaptureCalculator = CobblemonCaptureCalculator

    @NodeCategory(Category.Pokemon)
    var playerDamagePokemon = true

    @NodeCategory(Category.World)
    var appleLeftoversChance = 0.025

    @NodeCategory(Category.World)
    var maxRootsInArea = 5

    @NodeCategory(Category.World)
    var bigRootPropagationChance = 0.1

    @NodeCategory(Category.World)
    var energyRootChance = 0.25

    @NodeCategory(Category.Pokemon)
    @IntConstraint(min = 0, max = 10)
    var maxDynamaxLevel = 10

    @NodeCategory(Category.Spawning)
    var teraTypeRate = 20F

    @NodeCategory(Category.World)
    var defaultPasturedPokemonLimit = 16

    @NodeCategory(Category.World)
    var pastureBlockUpdateTicks = 40

    @NodeCategory(Category.World)
    var pastureMaxWanderDistance = 64

    @NodeCategory(Category.World)
    var pastureMaxPerChunk = 4F

    @NodeCategory(Category.World)
    var maxInsertedFossilItems = 2

    @NodeCategory(Category.Battles)
    var walkingInBattleAnimations = false

    @NodeCategory(Category.Battles)
    var battleWildMaxDistance = 12F

    @NodeCategory(Category.World)
    var tradeMaxDistance = 12F

    @NodeCategory(Category.Battles)
    var BattlePvPMaxDistance = 32F

    @NodeCategory(Category.Battles)
    var battleSpectateMaxDistance = 64F

    @NodeCategory(Category.World)
    var entityInteractionMaxDistance  = 64F

    @NodeCategory(Category.Debug)
    var enableDebugKeys = false
}
