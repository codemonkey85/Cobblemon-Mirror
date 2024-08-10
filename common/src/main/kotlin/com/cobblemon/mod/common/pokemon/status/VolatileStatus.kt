/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.status

import com.cobblemon.mod.common.api.pokemon.status.Status
import net.minecraft.util.Identifier

/**
 * Represents a status that only remains during a battle.
 *
 * @author Deltric
 */
open class VolatileStatus(
    name: Identifier,
    showdownName: String,
    applyMessage: String,
    removeMessage: String,
) : Status(name, showdownName, applyMessage, removeMessage)