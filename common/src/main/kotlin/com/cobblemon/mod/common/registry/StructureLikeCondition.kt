/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.registry

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeTagCondition
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.levelgen.structure.Structure

/**
 * A [RegistryLikeCondition] that expects a [TagKey] attached to the [Structure] registry.
 *
 * @property tag The tag to check for the structure to match.
 */
class StructureTagCondition(tag: TagKey<Structure>) : RegistryLikeTagCondition<Structure>(tag)

/**
 * A [RegistryLikeCondition] that expects an [ResourceLocation] to match.
 *
 * @property identifier The identifier for the structure being referenced.
 */
class StructureIdentifierCondition(identifier: ResourceLocation) : RegistryLikeIdentifierCondition<Structure>(identifier)