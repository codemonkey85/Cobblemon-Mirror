/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.network.chat.Component

/**
 * A predefined variable that will be declared and filled in on any NPC that extends from the class or preset
 * that specifies this variable. This gives the client a cleaner way to represent a variable that should exist
 * on the NPC.
 *
 * @author Hiroku
 * @since August 14th, 2023
 */
class NPCConfigVariable(
    val variableName: String = "variable",
    val displayName: Component = "Variable".asTranslated(),
    val description: Component = "A variable that can be used in the NPC's configuration.".asTranslated(),
    val type: NPCVariableType = NPCVariableType.NUMBER,
    val defaultValue: String = "0",
) {
    enum class NPCVariableType {
        NUMBER,
        STRING,
        BOOLEAN
    }

    fun apply(npc: NPCEntity, value: MoValue) {
        npc.config.setDirectly(variableName, value)
    }

    fun applyDefault(npc: NPCEntity) {
        val value = when (type) {
            NPCVariableType.STRING -> StringValue(defaultValue)
            NPCVariableType.BOOLEAN -> DoubleValue(defaultValue.toBoolean())
            else -> DoubleValue(defaultValue.toDouble())
        }
        npc.config.set(variableName.split(".").iterator(), value)
    }
}