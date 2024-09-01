/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class AbilityRegistrySyncPacket(abilities: Collection<AbilityTemplate>) : DataRegistrySyncPacket<AbilityTemplate, AbilityRegistrySyncPacket>(abilities) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: AbilityTemplate) {
        buffer.writeString(entry.name)
        buffer.writeString(entry.displayName)
        buffer.writeString(entry.description)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): AbilityTemplate {
        return AbilityTemplate(
            name = buffer.readString(),
            displayName = buffer.readString(),
            description = buffer.readString()
        )
    }

    override fun synchronizeDecoded(entries: Collection<AbilityTemplate>) {
        Abilities.receiveSyncPacket(entries)
    }

    companion object {
        val ID = cobblemonResource("ability_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): AbilityRegistrySyncPacket = AbilityRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}