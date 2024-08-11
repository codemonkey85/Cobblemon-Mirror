package com.cobblemon.mod.common.pokedex.scanner

import java.util.UUID

object PlayerScanningDetails {
    //Maybe make some of this stuff fixed size caches
    //Maps a player UUID to the UUID of the entity being scanned
    val playerToEntityMap: MutableMap<UUID, UUID> = mutableMapOf()
    //Maps a player UUID to the time (the number of the server tick) they started scanning their current pokemon
    val playerToTickMap: MutableMap<UUID, Int> = mutableMapOf()
}