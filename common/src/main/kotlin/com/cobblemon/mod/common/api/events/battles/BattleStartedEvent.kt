/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.events.Cancelable
import net.minecraft.network.chat.MutableComponent

/**
 * Event fired before a [PokemonBattle] is started. Canceling this event prevents the battle from being
 * created and launched.
 *
 * @property reason The text used to inform the participants why the battle was canceled. Keep null for a default error.
 *
 * @author Segfault Guy
 * @since March 26th 2023
 */
data class BattleStartedPreEvent (override val battle: PokemonBattle, var reason: MutableComponent? = null) : BattleEvent, Cancelable()

/**
 * Event fired after a [PokemonBattle] starts.
 *
 * @author Segfault Guy
 * @since March 26th 2023
 */
data class BattleStartedPostEvent (override val battle: PokemonBattle) : BattleEvent