/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.pokedex.PokedexManager
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes

class DexDataNbtBackend : NbtBackedPlayerData<PokedexManager>("pokedex", PlayerInstancedDataStoreTypes.POKEDEX) {
    override val codec = PokedexManager.CODEC
    override val defaultData = DexDataJsonBackend.defaultDataFunc
}