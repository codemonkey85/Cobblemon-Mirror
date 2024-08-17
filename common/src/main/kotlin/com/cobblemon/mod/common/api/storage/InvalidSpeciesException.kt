/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

import net.minecraft.resources.ResourceLocation

/**
 * An exception when trying to deserialize a Pokemon instance with an identifier that doesn't seem to be in the registry.
 *
 * @author Hiroku
 * @since October 9th, 2023
 */
class InvalidSpeciesException(val identifier: ResourceLocation) : IllegalStateException("Invalid species: $identifier")