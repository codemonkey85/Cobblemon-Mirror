package com.cobblemon.mod.common.api.dex.entry

import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

enum class ExtraDexDataTypes(
    val typeId: ResourceLocation,
    val decoder: (RegistryFriendlyByteBuf) -> ExtraDexData,
    val type: KClass<out ExtraDexData>
) {
    FORM(FormDexData.ID, FormDexData::decode, FormDexData::class);

    companion object {
        fun getById(id: ResourceLocation): ExtraDexDataTypes? {
            return entries.filter {
                it.typeId == id
            }.firstOrNull()
        }
    }
}