package com.cobblemon.mod.common.api.dex.entry

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.readIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

abstract class ExtraDexData : Encodable {
    abstract val type: ResourceLocation

    companion object {
        fun decodeAll(buf: RegistryFriendlyByteBuf): ExtraDexData {
            val typeId = buf.readIdentifier()
            val result = ExtraDexDataTypes.getById(typeId)?.decoder?.invoke(buf)
                ?: throw RuntimeException("Unknown dex data type: $typeId")
            return result
        }
    }

}