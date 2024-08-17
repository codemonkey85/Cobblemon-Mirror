/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.conditional.RegistryLikeAdapter
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import com.cobblemon.mod.common.registry.BlockIdentifierCondition
import com.cobblemon.mod.common.registry.BlockTagCondition
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block

object BlockLikeConditionAdapter : RegistryLikeAdapter<Block> {
    override val registryLikeConditions = mutableListOf(
        RegistryLikeTagCondition.resolver(Registries.BLOCK, ::BlockTagCondition),
        RegistryLikeIdentifierCondition.resolver(::BlockIdentifierCondition)
    )
}