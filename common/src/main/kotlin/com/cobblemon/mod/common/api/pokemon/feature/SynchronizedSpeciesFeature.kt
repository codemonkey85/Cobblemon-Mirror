/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.serialization.BufferSerializer

/**
 * A species feature that can be synchronized to the client. These must be managed by a [SynchronizedSpeciesFeatureProvider].
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
interface SynchronizedSpeciesFeature : SpeciesFeature, BufferSerializer