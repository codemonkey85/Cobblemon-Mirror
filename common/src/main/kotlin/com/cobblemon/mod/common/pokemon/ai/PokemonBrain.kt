/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.entity.ai.AttackAngryAtTask
import com.cobblemon.mod.common.entity.ai.ChooseLandWanderTargetTask
import com.cobblemon.mod.common.entity.ai.FleeFromAttackerTask
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.entity.ai.GetAngryAtAttackerTask
import com.cobblemon.mod.common.entity.ai.LookAroundTaskWrapper
import com.cobblemon.mod.common.entity.ai.LookAtMobTaskWrapper
import com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import com.cobblemon.mod.common.entity.ai.SwapActivityTask
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.*
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.toDF
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.mojang.datafixers.util.Pair
import net.minecraft.util.TimeUtil
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.behavior.*
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity

// brain sensors / memory definitions split from PokemonEntity
// to better represent vanillas layout.
object PokemonBrain {

    private val ADULT_FOLLOW_RANGE = UniformInt.of(5, 16)
    private val AVOID_MEMORY_DURATION = TimeUtil.rangeOfSeconds(5, 20)

    val SENSORS: Collection<SensorType<out Sensor<in PokemonEntity>>> = listOf(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.HURT_BY,
        SensorType.NEAREST_PLAYERS,
        CobblemonSensors.POKEMON_DISTURBANCE,
        CobblemonSensors.POKEMON_DEFEND_OWNER,
        CobblemonSensors.POKEMON_DROWSY,
        CobblemonSensors.POKEMON_ADULT,
        SensorType.IS_IN_WATER,
        CobblemonSensors.NEARBY_GROWABLE_CROPS

//            CobblemonSensors.BATTLING_POKEMON,
//            CobblemonSensors.NPC_BATTLING
    )

    val MEMORY_MODULES: List<MemoryModuleType<*>> = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        CobblemonMemories.POKEMON_FLYING,
        CobblemonMemories.NEAREST_VISIBLE_ATTACKER,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.NEAREST_PLAYERS,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.ANGRY_AT,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        CobblemonMemories.POKEMON_DROWSY,
        CobblemonMemories.POKEMON_BATTLE,
        MemoryModuleType.HOME,
        CobblemonMemories.REST_PATH_COOLDOWN,
        CobblemonMemories.TARGETED_BATTLE_POKEMON,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.IS_IN_WATER,
        MemoryModuleType.DISTURBANCE_LOCATION,
        CobblemonMemories.NEARBY_GROWABLE_CROPS,
        MemoryModuleType.AVOID_TARGET,
        CobblemonMemories.POKEMON_SLEEPING
    )

    fun makeBrain(pokemon: Pokemon, brain: Brain<out PokemonEntity>): Brain<*> {
        brain.addActivity(
            Activity.CORE,
            ImmutableList.copyOf(coreTasks(pokemon))
        )
        brain.addActivity(
            Activity.IDLE,
            ImmutableList.copyOf(idleTasks(pokemon))
        )
        brain.addActivity(
            CobblemonActivities.BATTLING_ACTIVITY,
            ImmutableList.copyOf(battlingTasks())
        )
        brain.addActivity(
            Activity.FIGHT,
            ImmutableList.copyOf(fightTasks(pokemon))
        )
        brain.addActivity(
            Activity.AVOID,
            ImmutableList.copyOf(avoidTasks(pokemon))
        )
        brain.addActivity(
            CobblemonActivities.POKEMON_SLEEPING_ACTIVITY,
            ImmutableList.copyOf(sleepingTasks())
        )
//        brain.addActivityAndRemoveMemoryWhenStopped(
//            Activity.AVOID,
//            10,
//            ImmutableList.of(
//                SetWalkTargetAwayFrom.entity(
//                    MemoryModuleType.AVOID_TARGET,
//                    1.5f,
//                    15,
//                    false
//                ),
//                makeRandomWalkTask(),
//                SetEntityLookTargetSometimes.create(
//                    8.0f,
//                    UniformInt.of(30, 60)
//                ),
//            ),
//            MemoryModuleType.AVOID_TARGET
//        )
        brain.addActivityWithConditions(
            CobblemonActivities.POKEMON_GROW_CROP,
            ImmutableList.copyOf(growingPlantTasks(pokemon)),
            ImmutableSet.of(Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT))
        )

        brain.setCoreActivities(setOf(Activity.CORE))
        brain.setDefaultActivity(Activity.IDLE)
        brain.useDefaultActivity()

        return brain
    }

    fun updateActivities(pokemon:PokemonEntity) {
        pokemon.brain.setActiveActivityToFirstValid(
            ImmutableList.of(
                Activity.CORE,
                Activity.AVOID,
                Activity.IDLE,
                CobblemonActivities.BATTLING_ACTIVITY,
                CobblemonActivities.POKEMON_SLEEPING_ACTIVITY,
                CobblemonActivities.POKEMON_GROW_CROP
            )
        )
    }

    private fun coreTasks(pokemon: Pokemon) = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        if (!pokemon.form.behaviour.moving.swim.canBreatheUnderwater) {
            add(0 toDF StayAfloatTask(0.8F))
        }

        if (pokemon.form.behaviour.combat.willDefendSelf) {
            add(0 toDF GetAngryAtAttackerTask.create())
        } else {
            add(0 toDF FleeFromAttackerTask.create())
        }

        add(0 toDF StopBeingAngryIfTargetDead.create())
        add(0 toDF HandleBattleActivityGoal.create())
        add(0 toDF FollowWalkTargetTask())

        add(0 toDF SwapActivityTask.possessing(CobblemonMemories.POKEMON_BATTLE, CobblemonActivities.BATTLING_ACTIVITY))
    }

    private fun fightTasks(pokemon: Pokemon) = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        add(0 toDF MoveToAttackTargetTask.create())
        add(0 toDF PokemonMeleeTask.create(20))
        add(0 toDF SwapActivityTask.lacking(MemoryModuleType.ATTACK_TARGET, Activity.IDLE))
    }

    private fun avoidTasks(pokemon: Pokemon) = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        add(0 toDF SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 0.55F, 15, false))
        add(0 toDF makeRandomWalkTask())
        add(0 toDF SetEntityLookTargetSometimes.create(8.0f, UniformInt.of(30, 60)))
        add(0 toDF SwapActivityTask.lacking(MemoryModuleType.AVOID_TARGET, Activity.IDLE))
    }

    private fun idleTasks(pokemon: Pokemon) = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        add(0 toDF WakeUpTask.create() )
        if (pokemon.form.behaviour.moving.canLook) {
            if (pokemon.form.behaviour.moving.looksAtEntities) {
                add(0 toDF LookAtMobTaskWrapper.create(15F))
            }

            add(0 toDF LookAroundTaskWrapper(45, 90))
        }

        add(0 toDF ChooseLandWanderTargetTask.create(pokemon.form.behaviour.moving.wanderChance, horizontalRange = 10, verticalRange = 5, walkSpeed = 0.33F, completionRange = 1))
        add(0 toDF GoToSleepTask.create())
        add(0 toDF FindRestingPlaceTask.create(16, 8))
        add(0 toDF EatGrassTask())
        add(0 toDF AttackAngryAtTask.create())
        add(0 toDF MoveToOwnerTask.create(completionRange = 4, maxDistance = 14F, teleportDistance = 24F))
        add(0 toDF WalkTowardsParentSpeciesTask.create(ADULT_FOLLOW_RANGE, 0.4f))
        if (pokemon.form.behaviour.combat.willDefendOwner) {
            add(0 toDF DefendOwnerTask.create())
        }
        add(0 toDF SwapActivityTask.possessing(MemoryModuleType.ATTACK_TARGET, Activity.FIGHT))
        add(0 toDF SwapActivityTask.possessing(MemoryModuleType.AVOID_TARGET, Activity.AVOID))

//        add(0 toDF HuntPlayerTask()) // commenting this out to test other things
        add(1 toDF FertilizerTask())

        if (pokemon.species.primaryType.name.equals("fire", true)) {
            add(1 toDF IgniteTask())
        }
        if (pokemon.form.behaviour.moving.swim.canBreatheUnderwater) {
            add(0 toDF ChooseWaterWanderTargetTask.create(pokemon.form.behaviour.moving.wanderChance, horizontalRange = 10, verticalRange = 5, swimSpeed = 0.33F, completionRange = 1))
            add(1 toDF JumpOutOfWaterTask())
        }
        if (pokemon.form.behaviour.moving.fly.canFly) {
            add(0 toDF ChooseFlightWanderTargetTask.create(pokemon.form.behaviour.moving.wanderChance, horizontalRange = 10, verticalRange = 5, flySpeed = 0.33F, completionRange = 1))
        }
        /*if (pokemon.isPlayerOwned()) {
            add(1 toDF DefendOwnerTask())
        }*/

    }

    private fun makeRandomWalkTask(): RunOne<PokemonEntity> {
        return RunOne(ImmutableList.of(Pair.of(RandomStroll.stroll(0.4f), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4f, 3), 2), Pair.of(DoNothing(30, 60), 1)))
    }

    private fun battlingTasks() = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        add(0 toDF LookAtTargetedBattlePokemonTask.create())
        add(0 toDF LookAtTargetSink(Int.MAX_VALUE - 1, Int.MAX_VALUE - 1))

        add(0 toDF SwapActivityTask.lacking(CobblemonMemories.POKEMON_BATTLE, Activity.IDLE))
    }

    private fun sleepingTasks() = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        add(1 toDF WakeUpTask.create())
    }

    private fun growingPlantTasks(pokemon: Pokemon) = buildList<Pair<Int, BehaviorControl<in PokemonEntity>>> {
        if (pokemon.species.primaryType.name.equals("grass", true)){
            add(1 toDF FertilizerTask())
        }
    }

    fun onCaptureFailed(pokemonEntity: PokemonEntity, capturer: Entity) {
        if (pokemonEntity.pokemon.isWild() && pokemonEntity.battleId == null && capturer is LivingEntity) {
            pokemonEntity.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET)
            pokemonEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET)
            pokemonEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, capturer, AVOID_MEMORY_DURATION.sample(pokemonEntity.random).toLong())
        }
    }
}