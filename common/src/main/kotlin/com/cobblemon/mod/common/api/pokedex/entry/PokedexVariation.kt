package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.readIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

abstract class PokedexVariation : Encodable {
    abstract val type: ResourceLocation

    companion object {
        fun decodeAll(buf: RegistryFriendlyByteBuf): PokedexVariation {
            val typeId = buf.readIdentifier()
            val result = PokedexVariationTypes.getById(typeId)?.decoder?.invoke(buf)
                ?: throw RuntimeException("Unknown dex data type: $typeId")
            return result
        }
    }

}