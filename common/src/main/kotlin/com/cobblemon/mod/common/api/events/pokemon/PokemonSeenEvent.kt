package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

data class PokemonSeenEvent(
    val playerId: UUID,
    val pokemon: Pokemon
) {
}