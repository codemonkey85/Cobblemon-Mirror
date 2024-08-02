/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types.tera.elemental

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class ElementalTypeTeraType(val type: ElementalType) : TeraType {
    override val legalAsStatic: Boolean = true

    override val id: ResourceLocation = cobblemonResource(this.type.name)

    override val displayName: Component = this.type.displayName

    override fun showdownId(): String = this.type.name
}