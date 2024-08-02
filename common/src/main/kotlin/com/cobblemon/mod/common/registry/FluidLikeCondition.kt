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
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid

/**
 * A tag condition for fluids. Built off of [RegistryLikeTagCondition].
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
class FluidTagCondition(tag: TagKey<Fluid>) : RegistryLikeTagCondition<Fluid>(tag)

/**
 * An identifier condition for fluids. Built off of [RegistryLikeIdentifierCondition].
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
class FluidIdentifierCondition(identifier: ResourceLocation) : RegistryLikeIdentifierCondition<Fluid>(identifier)