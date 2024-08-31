/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types.tera.gimmick

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class StellarTeraType : TeraType {
    override val id: ResourceLocation = ID

    override val legalAsStatic: Boolean = false

    override val displayName: Component = LANG

    override fun showdownId(): String = ID.path

    companion object {
        val ID = cobblemonResource("stellar")
        private val LANG = Component.translatable("${Cobblemon.MODID}.terra_type.stellar")
    }
}