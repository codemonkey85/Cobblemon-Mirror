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
import net.minecraft.network.RegistryByteBuf

// We do not need to know every single attribute as a client, as such, we only sync the aspects that matter
class NPCRegistrySyncPacket(npcs: Collection<NPCClass>) : DataRegistrySyncPacket<NPCClass, NPCRegistrySyncPacket>(npcs) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryByteBuf, entry: NPCClass) {
        try {
            buffer.writeIdentifier(entry.resourceIdentifier)
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the NPC class {}", entry.resourceIdentifier, e)
        }
    }

    override fun decodeEntry(buffer: RegistryByteBuf): NPCClass? {
        val identifier = buffer.readIdentifier()
        val npc = NPCClass()
        npc.resourceIdentifier = identifier
        return try {
            npc.decode(buffer)
            npc
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding the NPC class {}", identifier, e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<NPCClass>) {
        NPCClasses.reload(entries.associateBy { it.resourceIdentifier })
    }

    companion object {
        val ID = cobblemonResource("npcs_sync")
        fun decode(buffer: RegistryByteBuf): NPCRegistrySyncPacket = NPCRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}