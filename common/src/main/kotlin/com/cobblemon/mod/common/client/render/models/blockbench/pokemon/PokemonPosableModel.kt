/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone

/**
 * A Pokemon-specific subclass of [PosableModel]. This is only specified so that if more properties
 * become necessary that are specific to Pokémon, we don't need to change heaps of imports.
 *
 * @author Hiroku
 * @since May 31st, 2024
 */
open class PokemonPosableModel(root: Bone) : PosableModel(root)