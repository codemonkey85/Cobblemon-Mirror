/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.cobblemon.mod.common.registry.ItemIdentifierCondition
import net.minecraft.resources.ResourceLocation

/**
 * An [EvolutionRequirement] for a [Pokemon.heldItem].
 *
 * @property itemCondition The [NbtItemPredicate] expected to match the [Pokemon.heldItem].
 * @author Licious
 * @since March 21st, 2022
 */
class HeldItemRequirement(val itemCondition: NbtItemPredicate) : EvolutionRequirement {

    constructor() : this(NbtItemPredicate(ItemIdentifierCondition(ResourceLocation.parse("air"))))

    override fun check(pokemon: Pokemon): Boolean = this.itemCondition.test(pokemon.heldItemNoCopy())

    companion object {
        const val ADAPTER_VARIANT = "held_item"
    }
}