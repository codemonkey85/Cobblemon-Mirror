/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules

import com.cobblemon.mod.common.api.spawning.rules.component.SpawnRuleComponent
import com.cobblemon.mod.common.api.text.text
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * A bundling of [SpawnRuleComponent]s.
 *
 * @author Hiroku
 * @since September 30th, 2023
 */
class SpawnRule {

    lateinit var id: ResourceLocation
    val displayName: Component = "Spawn Rule".text()
    var enabled: Boolean = true
//    val pool: String? = null Kinda difficult to see how this would get used in practice.
    val components: MutableList<SpawnRuleComponent> = mutableListOf()
}