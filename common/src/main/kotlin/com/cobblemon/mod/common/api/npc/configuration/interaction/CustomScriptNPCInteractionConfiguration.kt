/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration.interaction

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

/**
 * An [NPCInteractConfiguration] which has a MoLang script defined in the configuration directly
 * rather than a reference to a separately made script.
 *
 * @author Hiroku
 * @since July 5th, 2024
 */
class CustomScriptNPCInteractionConfiguration : NPCInteractConfiguration {
    override val type: String = "custom_script"
    var script: ExpressionLike = "1".asExpressionLike()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(script.getString())
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        script = buffer.readString().asExpressionLike()
    }

    override fun writeToNBT(compoundTag: CompoundTag) {
        compoundTag.putString(DataKeys.NPC_INTERACT_CUSTOM_SCRIPT, script.getString())
    }

    override fun readFromNBT(compoundTag: CompoundTag) {
        script = compoundTag.getString(DataKeys.NPC_INTERACT_CUSTOM_SCRIPT).asExpressionLike()
    }

    override fun interact(npc: NPCEntity, player: ServerPlayer): Boolean {
        val context = mapOf(
            "npc" to npc.struct,
            "player" to player.asMoLangValue()
        )
        script.resolve(npc.runtime, context)
        return true
    }

    override fun isDifferentTo(other: NPCInteractConfiguration): Boolean {
        return other !is CustomScriptNPCInteractionConfiguration || other.script.getString() != script.getString()
    }
}