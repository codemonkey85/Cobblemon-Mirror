/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

/**
 * A collection of the Cobblemon [TagKey]s related to the [Registries.ENTITY_TYPE].
 *
 * @author Licious
 * @since March 6th, 2023
 */
@Suppress("HasPlatformType", "unused")
object CobblemonEntityTypeTags {


    /**
     * A tag that is used by Cobblemon to identify entities name tags can't be used on.
     */
    @JvmField
    val CANNOT_HAVE_NAME_TAG = create("cannot_have_name_tag")

    @JvmField
    val BOATS = create("boats")

    private fun create(path: String) = TagKey.create(Registries.ENTITY_TYPE, cobblemonResource(path))

}