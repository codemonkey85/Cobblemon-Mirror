/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.worldgen

import com.cobblemon.mod.common.world.predicate.CobblemonBlockPredicates
import net.minecraft.core.registries.Registries
import net.neoforged.neoforge.registries.RegisterEvent

object CobblemonForgeBlockPredicateType {
    fun register(event: RegisterEvent) {
        event.register(Registries.BLOCK_PREDICATE_TYPE) {
            CobblemonBlockPredicates.touch()
        }
    }
}