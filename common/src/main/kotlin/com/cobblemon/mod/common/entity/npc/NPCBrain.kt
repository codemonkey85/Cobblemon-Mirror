/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonActivities.NPC_BATTLING
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.entity.ai.AttackAngryAtTask
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.entity.ai.GetAngryAtAttackerTask
import com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import com.cobblemon.mod.common.entity.npc.ai.*
import com.cobblemon.mod.common.util.activityRegistry
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.itemRegistry
import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink
import net.minecraft.world.entity.ai.behavior.RunOne
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.entity.schedule.Schedule

object NPCBrain {
    fun configure(npcEntity: NPCEntity, npcClass: NPCClass, brain: Brain<out NPCEntity>) {
        brain.addActivity(
            Activity.CORE, ImmutableList.of(
                Pair.of(0, StayAfloatTask(0.8F)),
                Pair.of(0, GetAngryAtAttackerTask.create()),
                Pair.of(0, StopBeingAngryIfTargetDead.create())
            ))

        brain.addActivity(
            Activity.IDLE, ImmutableList.of(
            Pair.of(2, RunOne(
                ImmutableList.of(
                    Pair.of(LookAtTargetSink(45, 90), 2),
                    Pair.of(SetEntityLookTarget.create(15F), 2),
                    Pair.of(ChooseWanderTargetTask.create(horizontalRange = 10, verticalRange = 5, walkSpeed = 0.33F, completionRange = 1), 1)
                )
            )
            ),
            Pair.of(1, FollowWalkTargetTask()),
            Pair.of(0, SwitchToBattleTask.create()),
            Pair.of(1, AttackAngryAtTask.create()),
            Pair.of(1, MoveToAttackTargetTask.create()),
            Pair.of(1, MeleeAttackTask.create(2F, 30L)),
            Pair.of(1, HealUsingHealingMachineTask()),
            Pair.of(1, GoToHealingMachineTask.create(14, 5, 0.33F, 1))
        ))
        brain.addActivity(
            NPC_BATTLING, ImmutableList.of(
            Pair.of(0, SwitchFromBattleTask.create()),
            Pair.of(1, LookAtTargetSink(45, 90)),
            Pair.of(2, LookAtBattlingPokemonTask.create()),
        ))
        brain.addActivity(
            CobblemonActivities.NPC_ACTION_EFFECT, ImmutableList.of(
            Pair.of(0, FinishActionEffectTask.create())
        ))
        brain.addActivity(
            CobblemonActivities.NPC_CHATTING, ImmutableList.of(
            Pair.of(0, ExitSpeakersActivityTask.create()),
            Pair.of(0, LookAtSpeakerTask.create()),
            Pair.of(0, LookAtTargetSink(45, 90))
        ))
        brain.setCoreActivities(setOf(Activity.CORE))
        brain.setDefaultActivity(Activity.IDLE)
        brain.useDefaultActivity()



        val brainBuilder = BrainBuilder()
        val brainStruct = createNPCBrainStruct(npcEntity, brain, brainBuilder)
        // run some scripts at some point

        // apply the brain
        brainBuilder.activities.forEach {
            brain.addActivity(it.activity, ImmutableList.copyOf(it.tasks))
        }
        brain.setCoreActivities(brainBuilder.coreActivities)
        brain.setDefaultActivity(brainBuilder.defaultActivity)
        brain.schedule = brainBuilder.schedule
    }


    fun createNPCBrainStruct(npcEntity: NPCEntity, brain: Brain<out NPCEntity>, brainBuilder: BrainBuilder): QueryStruct {
        return QueryStruct(hashMapOf()).addStandardFunctions()
            .addFunction("npc") { npcEntity.struct }
            .addFunction("create_activity") { params ->
                val name = params.getString(0).asIdentifierDefaultingNamespace()
                val activity = npcEntity.level().activityRegistry.get(name) ?: return@addFunction run {
                    Cobblemon.LOGGER.error("Tried loading activity $name as part of an NPC brain but that activity does not exist")
                    DoubleValue.ZERO
                }
                val existingActivityBuilder = brainBuilder.activities.find { it.activity == activity }
                if (existingActivityBuilder != null) {
                    return@addFunction createNPCActivityStruct(existingActivityBuilder)
                } else {
                    val activityBuilder = ActivityBuilder(activity)
                    brainBuilder.activities.add(activityBuilder)
                    return@addFunction createNPCActivityStruct(activityBuilder)
                }
            }
            .addFunction("set_core_activities") { params ->
                brainBuilder.coreActivities = params.params.map { (it as ObjectValue<ActivityBuilder>).obj.activity }.toSet()
                return@addFunction DoubleValue.ONE
            }
            .addFunction("set_default_activity") { params ->
                brainBuilder.defaultActivity = params.get<ObjectValue<ActivityBuilder>>(0).obj.activity
                return@addFunction DoubleValue.ONE
            }
    }

    fun createNPCActivityStruct(activityBuilder: ActivityBuilder): ObjectValue<ActivityBuilder> {
        val struct = ObjectValue(activityBuilder)
        struct.addStandardFunctions()
            .addFunction("add_task") { params ->
                val priority = params.getInt(0)
                val task = params.get(1) as ObjectValue<BehaviorControl<NPCEntity>>
                activityBuilder.tasks.add(Pair(priority, task.obj))
            }
        return struct
    }

    class BrainBuilder {
        var defaultActivity = Activity.IDLE
        var coreActivities = setOf(Activity.CORE)
        val activities = mutableListOf<ActivityBuilder>()
        val schedule = Schedule.EMPTY
    }

    class ActivityBuilder(val activity: Activity) {
        val tasks = mutableListOf<Pair<Int, BehaviorControl<NPCEntity>>>()
    }
}