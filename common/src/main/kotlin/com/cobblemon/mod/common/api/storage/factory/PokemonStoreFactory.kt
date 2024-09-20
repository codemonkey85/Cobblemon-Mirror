/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.factory

import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.PokemonStoreManager
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.block.entity.PCBlockEntity
import java.util.UUID
import net.minecraft.core.RegistryAccess
import net.minecraft.server.level.ServerPlayer

/**
 * A provider for [PokemonStore]'s when given a player UUID. An implementation of this interface
 * does not need to always provide a value. In some cases you may only want to provide a store for
 * specific players, or under specific conditions, or only for parties, etc.
 *
 * If you have an implementation of this interface and want it to be used by Cobblemon, register
 * the factory using [PokemonStoreManager.registerFactory].
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
interface PokemonStoreFactory {
    fun getPlayerParty(playerID: UUID, registryAccess: RegistryAccess): PlayerPartyStore?
    fun getPC(playerID: UUID, registryAccess: RegistryAccess): PCStore?
    fun getPCForPlayer(player: ServerPlayer, pcBlockEntity: PCBlockEntity): PCStore? = getPC(player.uuid, player.registryAccess())

    fun <E : StorePosition, T : PokemonStore<E>> getCustomStore(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess): T?
    fun shutdown(registryAccess: RegistryAccess)
    fun onPlayerDisconnect(player: ServerPlayer)
}