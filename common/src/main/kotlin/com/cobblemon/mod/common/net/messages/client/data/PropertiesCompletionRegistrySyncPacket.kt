/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

internal class PropertiesCompletionRegistrySyncPacket(suggestions: Collection<PropertiesCompletionProvider.SuggestionHolder>) : DataRegistrySyncPacket<PropertiesCompletionProvider.SuggestionHolder, PropertiesCompletionRegistrySyncPacket>(suggestions) {

    override val id = ID

    override fun encodeEntry(
        buffer: RegistryFriendlyByteBuf,
        entry: PropertiesCompletionProvider.SuggestionHolder
    ) {
        buffer.writeCollection(entry.keys) { pb, value -> pb.writeString(value) }
        buffer.writeCollection(entry.suggestions) { pb, value -> pb.writeString(value) }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): PropertiesCompletionProvider.SuggestionHolder? {
        val keys = buffer.readList { pb -> pb.readString() }
        val suggestions = buffer.readList { pb -> pb.readString() }
        return PropertiesCompletionProvider.SuggestionHolder(keys, suggestions)
    }

    override fun synchronizeDecoded(entries: Collection<PropertiesCompletionProvider.SuggestionHolder>) {
        entries.forEach { suggestionHolder ->
            PropertiesCompletionProvider.inject(suggestionHolder.keys, suggestionHolder.suggestions)
        }
    }

    companion object {
        val ID = cobblemonResource("properties_completion_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): PropertiesCompletionRegistrySyncPacket = PropertiesCompletionRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

}