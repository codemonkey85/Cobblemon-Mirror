/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.predicate

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.tags.RegistryBasedCondition
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

data class NbtItemPredicate(
    val item: RegistryBasedCondition<Item>,
    val nbt: Optional<NbtPredicate>
) {

    @Suppress("DEPRECATION")
    constructor(item: RegistryLikeCondition<Item>, nbt: NbtPredicate?) : this(RegistryBasedCondition.fromLegacy(item), Optional.ofNullable(nbt))

    fun test(item: ItemStack): Boolean {
        return this.item.fits(item.item, BuiltInRegistries.ITEM) && this.nbt.map { it.matches(item) }.orElse(true)
    }

    companion object {
        @JvmStatic
        val CODEC: Codec<NbtItemPredicate> = RecordCodecBuilder.create { instance ->
            instance.group(
                RegistryBasedCondition.codec(Registries.ITEM).fieldOf("item").forGetter(NbtItemPredicate::item),
                NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(NbtItemPredicate::nbt),
            ).apply(instance, ::NbtItemPredicate)
        }
    }
}