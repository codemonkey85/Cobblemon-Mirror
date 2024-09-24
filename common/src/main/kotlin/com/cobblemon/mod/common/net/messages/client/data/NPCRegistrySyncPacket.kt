/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

// We do not need to know every single attribute as a client, as such, we only sync the aspects that matter
class NPCRegistrySyncPacket(npcs: Collection<NPCClass>) : DataRegistrySyncPacket<NPCClass, NPCRegistrySyncPacket>(npcs) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: NPCClass) {
        try {
            buffer.writeIdentifier(entry.id)
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the NPC class {}", entry.id, e)
        }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): NPCClass? {
        val identifier = buffer.readIdentifier()
        val npc = NPCClass()
        npc.id = identifier
        return try {
            npc.decode(buffer)
            npc
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding the NPC class {}", identifier, e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<NPCClass>) {
        NPCClasses.reload(entries.associateBy { it.id })
    }

    companion object {
        val ID = cobblemonResource("npcs_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): NPCRegistrySyncPacket = NPCRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}