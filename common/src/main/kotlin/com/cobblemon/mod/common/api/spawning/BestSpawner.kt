/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.entity.Despawner
import com.cobblemon.mod.common.api.spawning.condition.*
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.context.FishingSpawningContext
import com.cobblemon.mod.common.api.spawning.context.GroundedSpawningContext
import com.cobblemon.mod.common.api.spawning.context.LavafloorSpawningContext
import com.cobblemon.mod.common.api.spawning.context.SeafloorSpawningContext
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.SubmergedSpawningContext
import com.cobblemon.mod.common.api.spawning.context.SurfaceSpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.GroundedSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.LavafloorSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SeafloorSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SubmergedSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.SurfaceSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.detail.NPCSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawner
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.preset.BasicSpawnDetailPreset
import com.cobblemon.mod.common.api.spawning.preset.BestSpawnerConfig
import com.cobblemon.mod.common.api.spawning.preset.PokemonSpawnDetailPreset
import com.cobblemon.mod.common.api.spawning.prospecting.SpawningProspector
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector
import com.cobblemon.mod.common.api.spawning.spawner.AreaSpawner
import com.cobblemon.mod.common.api.spawning.spawner.FixedAreaSpawner
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawner
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.TickingSpawner
import com.cobblemon.mod.common.entity.pokemon.CobblemonAgingDespawner
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

/**
 * A grouping of all the overarching behaviours of the Best Spawner system. This is a convenient accessor to
 * the configuration and many other properties used by the spawner.
 *
 * The Best Spawner (in world spawning) works in distinct stages that are:
 * - Prospecting (see: [SpawningProspector])
 * - Context resolving (see: [AreaContextResolver])
 * - Spawn selection (see: [SpawningSelector])
 * - Spawn action (see: [SpawnAction])
 *
 * In the case of more specialized use, the creation of a [SpawningContext] that motivates most of the spawn
 * process can be created manually, skipping the first two steps.
 *
 * An individually spawnable entity is defined as a [SpawnDetail]. A processor handling this process is a [Spawner].
 * Various subclasses exist for more specialized cases. A spawner that is constantly ticking and will spawn things
 * without prompts is a [TickingSpawner], and one of those which occurs within a defined area is a [AreaSpawner]. If
 * that area is unmoving then it is a [FixedAreaSpawner] whereas if it is actively following the player it is a
 * [PlayerSpawner].
 *
 * Spawning is coordinated and ticked using a [SpawnerManager], and all the current managers are accessible from
 * [BestSpawner.spawnerManagers].
 *
 * Spawners and contexts are often put under the effects of [SpawningInfluence]s which can be used to make temporary
 * or lasting changes to spawning for whatever component they are attached to (whether that is a spawner or a context).
 * This pairs strongly with edits to the influence builders inside the [PlayerSpawnerFactory]. The range of effects
 * an influence can have is significant.
 *
 * Broad configuration of this spawning system is found in [BestSpawner.config].
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
object BestSpawner {
    var config = BestSpawnerConfig()
    val spawnerManagers = mutableListOf<SpawnerManager>(CobblemonWorldSpawnerManager)
    var defaultPokemonDespawner: Despawner<PokemonEntity> = CobblemonAgingDespawner(getAgeTicks = { it.ticksLived })
    lateinit var fishingSpawner: FishingSpawner

    fun loadConfig() {
        LOGGER.info("Starting the Best Spawner...")
        SpawningCondition.register(BasicSpawningCondition.NAME, BasicSpawningCondition::class.java)
        SpawningCondition.register(AreaSpawningCondition.NAME, AreaSpawningCondition::class.java)
        SpawningCondition.register(SubmergedSpawningCondition.NAME, SubmergedSpawningCondition::class.java)
        SpawningCondition.register(GroundedSpawningCondition.NAME, GroundedSpawningCondition::class.java)
        SpawningCondition.register(SurfaceSpawningCondition.NAME, SurfaceSpawningCondition::class.java)
        SpawningCondition.register(SeafloorSpawningCondition.NAME, SeafloorSpawningCondition::class.java)
        SpawningCondition.register(FishingSpawningCondition.NAME, FishingSpawningCondition::class.java)

        LOGGER.info("Loaded ${SpawningCondition.conditionTypes.size} spawning condition types.")
        SpawningContextCalculator.register(GroundedSpawningContextCalculator)
        SpawningContextCalculator.register(SeafloorSpawningContextCalculator)
        SpawningContextCalculator.register(LavafloorSpawningContextCalculator)
        SpawningContextCalculator.register(SubmergedSpawningContextCalculator)
        SpawningContextCalculator.register(SurfaceSpawningContextCalculator)

        SpawningContext.register(name = "grounded", clazz = GroundedSpawningContext::class.java, defaultCondition = GroundedSpawningCondition.NAME)
        SpawningContext.register(name = "seafloor", clazz = SeafloorSpawningContext::class.java, defaultCondition = SeafloorSpawningCondition.NAME)
        SpawningContext.register(name = "lavafloor", clazz = LavafloorSpawningContext::class.java, defaultCondition = GroundedSpawningCondition.NAME)
        SpawningContext.register(name = "submerged", clazz = SubmergedSpawningContext::class.java, defaultCondition = SubmergedSpawningCondition.NAME)
        SpawningContext.register(name = "surface", clazz = SurfaceSpawningContext::class.java, defaultCondition = SurfaceSpawningCondition.NAME)
        SpawningContext.register(name = "fishing", clazz = FishingSpawningContext::class.java, defaultCondition = FishingSpawningCondition.NAME)

        LOGGER.info("Loaded ${SpawningContext.contexts.size} spawning context types.")

        SpawnDetail.registerSpawnType(name = PokemonSpawnDetail.TYPE, PokemonSpawnDetail::class.java)
        SpawnDetail.registerSpawnType(name = NPCSpawnDetail.TYPE, NPCSpawnDetail::class.java)
        LOGGER.info("Loaded ${SpawnDetail.spawnDetailTypes.size} spawn detail types.")

        config = BestSpawnerConfig.load()

        SpawnDetailPresets.registerPresetType(BasicSpawnDetailPreset.NAME, BasicSpawnDetailPreset::class.java)
        SpawnDetailPresets.registerPresetType(PokemonSpawnDetailPreset.NAME, PokemonSpawnDetailPreset::class.java)
    }

    fun onServerStarted() {
        spawnerManagers.forEach(SpawnerManager::onServerStarted)
        fishingSpawner = FishingSpawner()
    }
}