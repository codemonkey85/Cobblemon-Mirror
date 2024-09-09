/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.berry

import com.cobblemon.mod.common.item.berry.BerryItem

//Dont make these ItemStacks, they do not have consistent hash codes, breaks stuff
data class BerryMutationRecipe(
    val berryOne: BerryItem,
    val berryTwo: BerryItem,
    val berryResult: BerryItem
)  {

}
