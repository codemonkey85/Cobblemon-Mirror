/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.helditem

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A registry responsible for providing [HeldItemManager]s.
 *
 * @author Licious
 * @since December 30th, 2022
 */
object HeldItemProvider {

    private val managers = PrioritizedList<HeldItemManager>()

    /**
     * Finds a [HeldItemManager] if any whose [HeldItemManager.showdownId] is not null for the provided [pokemon].
     *
     * @param pokemon The [Pokemon] being queried.
     * @return The [HeldItemManager] that can provide for the given [pokemon], if non match returns the [HeldItemManager.EMPTY].
     */
    fun provide(pokemon: BattlePokemon): HeldItemManager = this.managers.firstOrNull { manager -> manager.showdownId(pokemon) != null } ?: HeldItemManager.EMPTY

    /**
     * Finds the first non-null showdownId provided by a [HeldItemManager] in [managers].
     *
     * @param pokemon The [Pokemon] being queried.
     * @return The showdownId string that [pokemon] is holding, otherwise null.
     */
    fun provideShowdownId(pokemon: BattlePokemon) = this.managers.firstNotNullOfOrNull { manager -> manager.showdownId(pokemon) }

    /**
     * Registers a new [HeldItemManager].
     *
     * @param manager The [HeldItemManager] being registered.
     * @param priority The [Priority] to register it under.
     */
    fun register(manager: HeldItemManager, priority: Priority = Priority.NORMAL) {
        this.managers.add(priority, manager)
    }

    /**
     * Unregisters a [HeldItemManager].
     *
     * @param manager The [HeldItemManager] being unregistered.
     * @param priority The [Priority] to specifically remove the instance from, if null it will remove from all.
     */
    @Suppress("unused")
    fun unregister(manager: HeldItemManager, priority: Priority? = null) {
        if (priority != null) {
            this.managers.remove(priority, manager)
            return
        }
        this.managers.remove(manager)
    }

    /**
     * A read-only copy of the registered [HeldItemManager]s ordered following the backing priority of [PrioritizedList].
     *
     * @return The read-only copy of the registered [HeldItemManager]s.
     */
    fun managers(): List<HeldItemManager> = this.managers.toList()

}