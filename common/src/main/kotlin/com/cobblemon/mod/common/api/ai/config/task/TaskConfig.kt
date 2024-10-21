/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

fun interface TaskConfig {
    companion object {
        val types = mutableMapOf<String, Class<out TaskConfig>>(
            "one_of" to OneOfTaskConfig::class.java,
            "wander" to WanderTaskConfig::class.java,
            "look_at_target" to LookAtTargetTaskConfig::class.java,
            "follow_walk_target" to FollowWalkTargetTaskConfig::class.java,
            "random" to RandomTaskConfig::class.java,
            "stay_afloat" to StayAfloatTaskConfig::class.java,
            "look_at_entities" to LookAtEntitiesTaskConfig::class.java,
            "do_nothing" to DoNothingTaskConfig::class.java,
            "get_angry_at_attacker" to GetAngryAtAttackerTaskConfig::class.java,
            "stop_being_angry_if_attacker_dead" to StopBeingAngryIfAttackerDeadTaskConfig::class.java,
            "switch_npc_to_battle" to SwitchToNPCBattleTaskConfig::class.java,
            "look_at_battling_pokemon" to LookAtBattlingPokemonTaskConfig::class.java,
            "switch_npc_from_battle" to SwitchFromNPCBattleTaskConfig::class.java,
            "go_to_healing_machine" to GoToHealingMachineTaskConfig::class.java,
            "heal_using_healing_machine" to HealUsingHealingMachineTaskConfig::class.java,
            "all_of" to AllOfTaskConfig::class.java,
            "attack_angry_at" to AttackAngryAtTaskConfig::class.java,
            "move_to_attack_target" to MoveToAttackTargetTaskConfig::class.java,
            "melee_attack" to MeleeAttackTaskConfig::class.java,
            "switch_from_fight" to SwitchFromFightTaskConfig::class.java,
            "switch_to_fight" to SwitchToFightTaskConfig::class.java,
            "switch_to_chatting" to SwitchToChattingTaskConfig::class.java,
            "switch_from_chatting" to SwitchFromChattingTaskConfig::class.java,
            "look_at_speaker" to LookAtSpeakerTaskConfig::class.java,
            "switch_to_action_effect" to SwitchToActionEffectTaskConfig::class.java,
            "switch_from_action_effect" to SwitchFromActionEffectTaskConfig::class.java,
        )

        val runtime = MoLangRuntime().setup()
    }

    val runtime: MoLangRuntime
        get() = Companion.runtime

    fun createTasks(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): List<BehaviorControl<in LivingEntity>>
}