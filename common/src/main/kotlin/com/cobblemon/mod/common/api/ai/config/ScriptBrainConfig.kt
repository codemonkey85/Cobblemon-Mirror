/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.ai.ActivityConfigurationContext
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.activityRegistry
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class ScriptBrainConfig : BrainConfig {
    val condition = "true".asExpressionLike()
    val script = cobblemonResource("dummy")

    override fun configure(entity: LivingEntity, context: BrainConfigurationContext) {
        val runtime = MoLangRuntime().setup()
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return

        val struct = createBrainStruct(entity, context)
        val script = CobblemonScripts.scripts[this.script]
            ?: run {
                Cobblemon.LOGGER.error("Tried loading script $script as part of an entity brain but that script does not exist")
                return
            }
        runtime.resolve(script, mapOf("brain" to struct))
    }

    fun createBrainStruct(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): QueryStruct {
        return QueryStruct(hashMapOf()).addStandardFunctions()
            .addFunction("entity") { (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()) }
            .addFunction("create_activity") { params ->
                val name = params.getString(0).asIdentifierDefaultingNamespace()
                val activity = entity.level().activityRegistry.get(name) ?: return@addFunction run {
                    Cobblemon.LOGGER.error("Tried loading activity $name as part of an entity brain but that activity does not exist")
                    DoubleValue.ZERO
                }
                val existingActivityBuilder = brainConfigurationContext.activities.find { it.activity == activity }
                if (existingActivityBuilder != null) {
                    return@addFunction createActivityStruct(existingActivityBuilder)
                } else {
                    val activityConfigurationContext = ActivityConfigurationContext(activity)
                    brainConfigurationContext.activities.add(activityConfigurationContext)
                    return@addFunction createActivityStruct(activityConfigurationContext)
                }
            }
            .addFunction("set_core_activities") { params ->
                brainConfigurationContext.coreActivities = params.params.map { (it as ObjectValue<ActivityConfigurationContext>).obj.activity }.toSet()
                return@addFunction DoubleValue.ONE
            }
            .addFunction("set_default_activity") { params ->
                brainConfigurationContext.defaultActivity = params.get<ObjectValue<ActivityConfigurationContext>>(0).obj.activity
                return@addFunction DoubleValue.ONE
            }
    }

    fun createActivityStruct(activityConfigurationContext: ActivityConfigurationContext): ObjectValue<ActivityConfigurationContext> {
        val struct = ObjectValue(activityConfigurationContext)
        struct.addStandardFunctions()
            .addFunction("add_task") { params ->
                val priority = params.getInt(0)
                val task = params.get(1) as ObjectValue<BehaviorControl<in LivingEntity>>
                activityConfigurationContext.tasks.add(Pair(priority, task.obj))
            }
        return struct
    }
}