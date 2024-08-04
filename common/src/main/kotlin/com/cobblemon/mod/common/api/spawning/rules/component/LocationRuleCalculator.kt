/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asDimensionTypeMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asWorldMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType

/**
 * An early rule that filters possible locations that would go into a [SpawningContext].
 *
 * @author Hiroku
 * @since October 2nd, 2023
 */
class LocationRuleCalculator : SpawnRuleComponent {
    @Transient
    val runtime = MoLangRuntime().setup()
    @Transient
    private val reusableX = DoubleValue(0.0)
    @Transient
    private val reusableY = DoubleValue(0.0)
    @Transient
    private val reusableZ = DoubleValue(0.0)
    @Transient
    private val reusableContext = StringValue("")
    @Transient
    private lateinit var reusableWorldValue: ObjectValue<Holder<Level>>
    @Transient
    private lateinit var reusableDimensionTypeValue: ObjectValue<Holder<DimensionType>>

    val allow = "true".asExpressionLike()

    override fun isAllowedPosition(
        world: ServerLevel,
        pos: BlockPos,
        contextCalculator: SpawningContextCalculator<*, *>
    ): Boolean {
        reusableX.value = pos.x.toDouble()
        reusableY.value = pos.y.toDouble()
        reusableZ.value = pos.z.toDouble()
        reusableContext.value = contextCalculator.name

        if (!this::reusableWorldValue.isInitialized) {
            reusableWorldValue = world.registryAccess().registryOrThrow(Registries.DIMENSION).getHolder(world.dimension()).get().asWorldMoLangValue()
        } else {
            reusableWorldValue.obj = world.registryAccess().registryOrThrow(Registries.DIMENSION).getHolder(world.dimension()).get()
        }

        if (!this::reusableDimensionTypeValue.isInitialized) {
            reusableDimensionTypeValue = world.dimensionTypeRegistration().asDimensionTypeMoLangValue()
        } else {
            reusableDimensionTypeValue.obj = world.dimensionTypeRegistration()
        }

        runtime.environment.setSimpleVariable("x", reusableX)
        runtime.environment.setSimpleVariable("y", reusableY)
        runtime.environment.setSimpleVariable("z", reusableZ)
        runtime.environment.setSimpleVariable("context", reusableContext)
        runtime.environment.setSimpleVariable("world", reusableWorldValue)
        runtime.environment.setSimpleVariable("dimension_type", reusableDimensionTypeValue)
        return runtime.resolveBoolean(allow)
    }
}