package com.cobblemon.mod.common.pokedex.scanner

import java.util.UUID

object PlayerScanningDetails {
    //Maps a player UUID to the UUID of the entity being scanned
    val playerToEntityMap: MutableMap<UUID, UUID> = mutableMapOf()
}