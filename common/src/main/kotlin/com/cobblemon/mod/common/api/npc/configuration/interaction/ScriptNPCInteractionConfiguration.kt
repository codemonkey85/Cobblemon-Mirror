/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration.interaction

import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * An [NPCInteractConfiguration] which runs a referenced MoLang script from [CobblemonScripts].
 *
 * @author Hiroku
 * @since July 5th, 2024
 */
class ScriptNPCInteractionConfiguration : NPCInteractConfiguration {
    override val type: String = "script"
    var script: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "scripts/test.molang")

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(script)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        script = buffer.readIdentifier()
    }

    override fun interact(npc: NPCEntity, player: ServerPlayer): Boolean {
        val script = CobblemonScripts.scripts[script] ?: return false
        val context = mapOf(
            "npc" to npc.struct,
            "player" to player.asMoLangValue()
        )
        script.resolve(npc.runtime, context)
        return true
    }

    override fun writeToNBT(compoundTag: CompoundTag) {
        compoundTag.putString(DataKeys.NPC_INTERACT_SCRIPT, script.toString())
    }

    override fun readFromNBT(compoundTag: CompoundTag) {
        script = ResourceLocation.parse(compoundTag.getString(DataKeys.NPC_INTERACT_SCRIPT))
    }

    override fun isDifferentTo(other: NPCInteractConfiguration) = other !is ScriptNPCInteractionConfiguration || other.script != script
}