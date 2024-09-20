package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.api.storage.player.client.ClientGeneralPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

object PlayerInstancedDataStoreTypes {
    val GENERAL = register(PlayerInstancedDataStoreType(
        cobblemonResource("general"),
        ClientGeneralPlayerData::decode,
        ClientGeneralPlayerData::runAction
    ))
    val POKEDEX = register(PlayerInstancedDataStoreType(
        cobblemonResource("pokedex"),
        ClientPokedexManager::decode,
        ClientPokedexManager::runAction,
        ClientPokedexManager::runIncremental
    ))

    val types = mutableMapOf<ResourceLocation, PlayerInstancedDataStoreType>()
    fun register(type: PlayerInstancedDataStoreType): PlayerInstancedDataStoreType {
        types[type.id] = type
        return type
    }

    fun getTypeById(id: ResourceLocation) = types[id]
}