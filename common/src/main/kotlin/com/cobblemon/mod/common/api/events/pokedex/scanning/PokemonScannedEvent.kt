package com.cobblemon.mod.common.api.events.pokedex.scanning

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

data class PokemonScannedEvent(val player: ServerPlayer, val scannedEntity: PokemonEntity) {
    val pokemon: Pokemon
        get() = scannedEntity.pokemon
}