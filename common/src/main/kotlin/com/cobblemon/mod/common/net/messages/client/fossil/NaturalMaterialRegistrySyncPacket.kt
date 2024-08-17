/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.fossil

import com.cobblemon.mod.common.api.fossil.NaturalMaterial
import com.cobblemon.mod.common.api.fossil.NaturalMaterials
import com.cobblemon.mod.common.net.messages.client.data.DataRegistrySyncPacket
import com.cobblemon.mod.common.registry.ItemTagCondition
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class NaturalMaterialRegistrySyncPacket(naturalMaterials: List<NaturalMaterial>) : DataRegistrySyncPacket<NaturalMaterial, NaturalMaterialRegistrySyncPacket>(naturalMaterials) {
    companion object {
        val ID = cobblemonResource("natural_materials")
        fun decode(buffer: RegistryFriendlyByteBuf) = NaturalMaterialRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }


    override val id = ID
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: NaturalMaterial) {
        buffer.writeNullable(entry.item) {pb, type -> pb.writeIdentifier(entry.item!!)}
        buffer.writeNullable(entry.tag) { pb, type -> pb.writeString(NaturalMaterials.gson.toJson("#" + entry.tag?.tag?.location.toString()) ) }
        buffer.writeNullable(entry.returnItem) { pb, type -> pb.writeIdentifier(entry.returnItem!!) }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): NaturalMaterial {
        return NaturalMaterial (
                content = 0, // Server handles incrementing of the fossil machine
                item = buffer.readNullable { pb -> pb.readIdentifier() },
                tag = buffer.readNullable { pb -> NaturalMaterials.gson.fromJson(buffer.readString(), ItemTagCondition::class.java) },
                returnItem = buffer.readNullable { pb -> pb.readIdentifier() }
        )
    }

    override fun synchronizeDecoded(entries: Collection<NaturalMaterial>) {
        NaturalMaterials.reload(mapOf(cobblemonResource("natural_materials") to entries.toList())  )
    }
}