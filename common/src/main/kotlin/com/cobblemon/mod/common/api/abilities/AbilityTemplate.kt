/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.registry.RegistryElement
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.simplify
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

/**
 * The representation of the fixed data of an ability.
 *
 * This is used to create [Ability].
 *
 * @property rating Rating from -1(Detrimental) to +5(Essential).
 * @property suppressWeather If this ability suppresses weather.
 * @property flags The [AbilityFlag]s of this ability.
 * @property displayName The display name of this ability.
 * @property description The description of this ability.
 */
@Suppress("MemberVisibilityCanBePrivate")
class AbilityTemplate(
    val rating: Float,
    val suppressWeather: Boolean,
    val flags: Set<AbilityFlag>,
    val displayName: Component,
    val description: Component,
) : RegistryElement<AbilityTemplate>, ShowdownIdentifiable {

    init {
        assert(this.rating in MIN_RATING..MAX_RATING) { "AbilityTemplate rating must be between -1 and 5" }
    }

    /**
     * Uses this data to create a new [Ability].
     *
     * @param forced The [Ability.forced] state.
     * @return The created [Ability].
     */
    fun create(forced: Boolean = false): Ability = Ability(this, forced)

    override fun showdownId(): String {
        return ShowdownIdentifiable.EXCLUSIVE_REGEX.replace(this.resourceLocation().simplify(), "")
    }

    override fun registry(): Registry<AbilityTemplate> = CobblemonRegistries.ABILITY

    override fun resourceKey(): ResourceKey<AbilityTemplate> = this.registry().getResourceKey(this)
        .orElseThrow { IllegalStateException("Unregistered AbilityTemplate") }

    override fun isTaggedBy(tag: TagKey<AbilityTemplate>): Boolean = this.registry()
        .getHolder(this.resourceKey())
        .orElseThrow { IllegalStateException("Unregistered AbilityTemplate") }
        .`is`(tag)

    companion object {

        private const val MIN_RATING = -1F
        private const val MAX_RATING = 5F

        @JvmStatic
        val DIRECT_CODEC: Codec<AbilityTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.floatRange(MIN_RATING, MAX_RATING).fieldOf("rating").forGetter(AbilityTemplate::rating),
                Codec.BOOL.optionalFieldOf("suppressWeather", false).forGetter(AbilityTemplate::suppressWeather),
                AbilityFlag.CODEC.listOf().optionalFieldOf("flags", emptyList())
                    .xmap(
                        { it.toSet() },
                        { it.toMutableList() }
                    )
                    .forGetter(AbilityTemplate::flags),
                ComponentSerialization.CODEC.fieldOf("displayName").forGetter(AbilityTemplate::displayName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(AbilityTemplate::description),
            ).apply(instance, ::AbilityTemplate)
        }
        @JvmStatic
        val CODEC: Codec<Holder<AbilityTemplate>> = RegistryFileCodec.create(CobblemonRegistries.ABILITY_KEY, DIRECT_CODEC)
        @JvmStatic
        val LIST_CODEC: Codec<HolderSet<AbilityTemplate>> = RegistryCodecs.homogeneousList(CobblemonRegistries.ABILITY_KEY, DIRECT_CODEC)

    }

}