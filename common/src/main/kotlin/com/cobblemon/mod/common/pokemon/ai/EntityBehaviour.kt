/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer


/**
 * Behavioural properties relating to how it treats other entities and how base Minecraft entities treat it.
 */
class EntityBehaviour {
    val avoidedByCreeper = false
//    val avoidedByPhantom = false
    val avoidedBySkeleton = false

    companion object {
        fun hasCreeperFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.species.behaviour.entityInteract.avoidedByCreeper }
        }

        fun hasSkeletonFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.species.behaviour.entityInteract.avoidedBySkeleton }
        }
    }
}