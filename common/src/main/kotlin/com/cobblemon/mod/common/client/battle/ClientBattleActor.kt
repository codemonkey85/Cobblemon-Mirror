/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
import net.minecraft.network.chat.MutableComponent
class ClientBattleActor(
    /** The showdown pIndexing, p0, p2, etc*/
    val showdownId: String,
    val displayName: MutableComponent,
    val uuid: UUID,
    val type: ActorType
) {
    lateinit var side: ClientBattleSide

    var pokemon = mutableListOf<Pokemon>()
    val activePokemon = mutableListOf<ActiveClientBattlePokemon>()
}