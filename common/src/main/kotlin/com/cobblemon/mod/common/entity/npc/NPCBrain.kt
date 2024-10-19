/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc

import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonActivities.NPC_BATTLING
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.entity.ai.AttackAngryAtTask
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.entity.ai.GetAngryAtAttackerTask
import com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import com.cobblemon.mod.common.entity.npc.ai.*
import com.cobblemon.mod.common.util.asExpression
import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink
import net.minecraft.world.entity.ai.behavior.RunOne
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead
import net.minecraft.world.entity.schedule.Activity

object NPCBrain {
    fun configure(npcEntity: NPCEntity, npcClass: NPCClass, brain: Brain<out NPCEntity>) {
        val brainConfigurationContext = BrainConfigurationContext()
        brainConfigurationContext.apply(npcEntity, npcClass.ai)
        return

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
            )),
            Pair.of(1, FollowWalkTargetTask()),
            Pair.of(0, SwitchToBattleTask.create()),
            Pair.of(1, AttackAngryAtTask.create()),
            Pair.of(1, MoveToAttackTargetTask.create()),
            Pair.of(1, MeleeAttackTask.create(2F, 30L)),
            Pair.of(1, HealUsingHealingMachineTask()),
            Pair.of(1, GoToHealingMachineTask.create(
                horizontalSearchRange = "14".asExpression(),
                verticalSearchRange = "5".asExpression(),
                speedMultiplier = "0.33".asExpression(),
                completionRange = "1".asExpression()
            ))
        ))
        brain.addActivity(
            NPC_BATTLING, ImmutableList.of(
            Pair.of(0, SwitchFromBattleTask.create(Activity.IDLE)),
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



//        val brainConfigurationContext = BrainConfigurationContext()
////        val brainStruct = createNPCBrainStruct(npcEntity, brain, brainConfigurationContext)
//        // run some scripts at some point
//
//        // apply the brain
//        brainConfigurationContext.activities.forEach {
////            brain.addActivity(it.activity, ImmutableList.copyOf(it.tasks))
//        }
//        brain.setCoreActivities(brainConfigurationContext.coreActivities)
//        brain.setDefaultActivity(brainConfigurationContext.defaultActivity)
//        brain.schedule = brainConfigurationContext.schedule
    }
}