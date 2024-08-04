/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.registry

import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import net.minecraft.world.level.block.Block
import net.minecraft.tags.TagKey
import net.minecraft.resources.ResourceLocation

/**
 * A tag condition for blocks. Built off of [RegistryLikeTagCondition].
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
class BlockTagCondition(tag: TagKey<Block>) : RegistryLikeTagCondition<Block>(tag)
/**
 * An identifier condition for blocks. Built off of [RegistryLikeIdentifierCondition].
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
class BlockIdentifierCondition(identifier: ResourceLocation) : RegistryLikeIdentifierCondition<Block>(identifier)