/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.WorldSlice
import com.cobblemon.mod.common.api.spawning.context.calculators.AreaSpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.context.calculators.AreaSpawningInput
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.core.BlockPos

/**
 * Interface responsible for drawing a list of spawn contexts from a slice of the world,
 * given a list of all the context calculators that should be considered in order. As soon
 * as one of the context calculators returns true for [AreaSpawningContextCalculator.fits],
 * no other context calculator will be considered.
 *
 * The default method body of this interface checks every single block in the slice
 * and composes a single context per position, at most. This is almost certainly fine,
 * but this interface exists, so you can override it if you want.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface AreaContextResolver {
    fun resolve(
        spawner: Spawner,
        contextCalculators: List<AreaSpawningContextCalculator<*>>,
        slice: WorldSlice
    ): List<AreaSpawningContext> {
        var pos = BlockPos.MutableBlockPos(1, 2, 3)
        val input = AreaSpawningInput(spawner, pos, slice)
        val contexts = mutableListOf<AreaSpawningContext>()

        var x = slice.baseX
        var y = slice.baseY
        var z = slice.baseZ

        while (x < slice.baseX + slice.length) {
            while (y < slice.baseY + slice.height) {
                while (z < slice.baseZ + slice.width) {
                    pos.set(x, y, z)
                    val vec = pos.toVec3d()
                    if (slice.nearbyEntityPositions.none { it.closerThan(vec, config.minimumDistanceBetweenEntities) && it != slice.cause.entity }) {
                        val fittedContextCalculator = contextCalculators
                            .firstOrNull { calc -> calc.fits(input) && input.spawner.influences.none { !it.isAllowedPosition(input.world, input.position, calc) } }
                        if (fittedContextCalculator != null) {
                            val context = fittedContextCalculator.calculate(input)
                            if (context != null) {
                                contexts.add(context)
                                // The position BlockPos has been used in a context, editing the same one
                                // will cause entities to spawn at the wrong location (buried in walls, usually).
                                // I made it so that built-in context calculators explicitly take a copy of the
                                // BlockPos but it'd still be exposed in custom contexts so fixing it here too so
                                // custom context calculators don't have to remember to do it. - Hiroku
                                pos = BlockPos.MutableBlockPos(1, 2, 3)
                                input.position = pos
                            }
                        }
                    }
                    z++
                }
                y++
                z = slice.baseZ
            }
            x++
            y = slice.baseY
            z = slice.baseZ
        }

        return contexts
    }

//    fun resolveFishing(
//            spawner: Spawner,
//            contextCalculators: List<AreaSpawningContextCalculator<*>>,
//            slice: WorldSlice
//    ): List<AreaSpawningContext> {
//        var pos = BlockPos.Mutable(1, 2, 3)
//        val input = AreaSpawningInput(spawner, pos, slice)
//        val contexts = mutableListOf<AreaSpawningContext>()
//
//        var x = slice.baseX
//        var y = slice.baseY
//        var z = slice.baseZ
//
//        while (x < slice.baseX + slice.length) {
//            while (y < slice.baseY + slice.height) {
//                while (z < slice.baseZ + slice.width) {
//                    pos.set(x, y, z)
//                    val vec = pos.toVec3d()
//                    if (slice.nearbyEntityPositions.none { it.isInRange(vec, 50.0) && it != slice.cause.entity }) {
//                        val fittedContextCalculator = contextCalculators
//                                .firstOrNull { calc -> calc.fits(input) && input.spawner.influences.none { !it.isAllowedPosition(input.world, input.position, calc) } }
//                        if (fittedContextCalculator != null) {
//                            val context = fittedContextCalculator.calculate(input)
//                            if (context != null) {
//                                contexts.add(context)
//                                // The position BlockPos has been used in a context, editing the same one
//                                // will cause entities to spawn at the wrong location (buried in walls, usually)
//                                // I made it so that our context calculators specifically take a copy of the
//                                // BlockPos but it'd still be exposed in custom contexts so fixing it here too.
//                                pos = BlockPos.Mutable(1, 2, 3)
//                                input.position = pos
//                            }
//                        }
//                    }
//                    z++
//                }
//                y++
//                z = slice.baseZ
//            }
//            x++
//            y = slice.baseY
//            z = slice.baseZ
//        }
//
//        return contexts
//    }
}