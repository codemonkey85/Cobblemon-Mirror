package com.cobblemon.mod.common.api.pokedex.entry

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

enum class PokedexVariationTypes(
    val typeId: ResourceLocation,
    val decoder: (RegistryFriendlyByteBuf) -> PokedexVariation,
    val type: KClass<out PokedexVariation>
) {
    FORM(BasicPokedexVariation.ID, BasicPokedexVariation.Companion::decode, BasicPokedexVariation::class);

    companion object {
        fun getById(id: ResourceLocation): PokedexVariationTypes? {
            return entries.filter {
                it.typeId == id
            }.firstOrNull()
        }
    }
}