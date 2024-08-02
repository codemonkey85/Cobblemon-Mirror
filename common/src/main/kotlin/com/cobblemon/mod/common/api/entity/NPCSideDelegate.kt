/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.entity

import com.cobblemon.mod.common.entity.npc.NPCEntity

/**
 * A delegate for some actions that can be different for an NPC on client versus server.
 *
 * @author Hiroku
 * @since August 25th, 2023
 */
interface NPCSideDelegate : EntitySideDelegate<NPCEntity>