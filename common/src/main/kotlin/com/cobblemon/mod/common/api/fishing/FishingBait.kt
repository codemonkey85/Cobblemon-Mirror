/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitEffectFunctionRegistryEvent
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

data class FishingBait(
    val item: ResourceLocation,
    val effects: List<Effect>,
) {
    fun toItemStack(itemRegistry: Registry<Item>) = item.let(itemRegistry::get)?.let { ItemStack(it) } ?: ItemStack.EMPTY

    data class Effect(
        val type: ResourceLocation,
        val subcategory: ResourceLocation?,
        val chance: Double = 0.0,
        val value: Double = 0.0
    ) {
        fun toNbt(): CompoundTag {
            val nbt = CompoundTag()
            nbt.putString("Type", type.toString())
            subcategory?.let { nbt.putString("Subcategory", it.toString()) }
            nbt.putDouble("Chance", chance)
            nbt.putDouble("Value", value)
            return nbt
        }

        companion object {
            fun fromNbt(nbt: CompoundTag): Effect {
                val type = ResourceLocation.parse(nbt.getString("Type"))
                val subcategory = if (nbt.contains("Subcategory")) ResourceLocation.parse(nbt.getString("Subcategory")) else null
                val chance = nbt.getDouble("Chance")
                val value = nbt.getDouble("Value")
                return Effect(type, subcategory, chance, value)
            }
        }
    }

    fun toNbt(): CompoundTag {
        val nbt = CompoundTag()
        nbt.putString("Item", item.toString())
        val effectsList = ListTag()
        effects.forEach { effectsList.add(it.toNbt()) }
        nbt.put("Effects", effectsList)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: CompoundTag): FishingBait {
            val item = ResourceLocation.parse(nbt.getString("Item"))
            val effectsList = nbt.getList("Effects", 10) // 10 is the type for NbtCompound
            val effects = mutableListOf<Effect>()
            for (i in 0 until effectsList.size) {
                effects.add(Effect.fromNbt(effectsList.getCompound(i)))
            }
            return FishingBait(item, effects)
        }

        val BLANK_BAIT = FishingBait(
            cobblemonResource("blank"),
            emptyList()
        )
    }

    object Effects {
        private val EFFECT_FUNCTIONS: MutableMap<ResourceLocation, (PokemonEntity, Effect) -> Unit> = mutableMapOf()
        val NATURE = cobblemonResource("nature")
        val IV = cobblemonResource("iv")
        val EV = cobblemonResource("ev")
        val BITE_TIME = cobblemonResource("bite_time")
        val GENDER_CHANCE = cobblemonResource("gender_chance")
        val LEVEL_RAISE = cobblemonResource("level_raise")
        val TERA = cobblemonResource("tera")
        val SHINY_REROLL = cobblemonResource("shiny_reroll")
        val HIDDEN_ABILITY_CHANCE = cobblemonResource("ha_chance")
        val POKEMON_CHANCE = cobblemonResource("pokemon_chance")
        val FRIENDSHIP = cobblemonResource("friendship")
        val INERT = cobblemonResource("inert")

        fun registerEffect(type: ResourceLocation, effect: (PokemonEntity, Effect) -> Unit) {
            EFFECT_FUNCTIONS[type] = effect
        }

        fun getEffectFunction(type: ResourceLocation): ((PokemonEntity, Effect) -> Unit)? {
            return EFFECT_FUNCTIONS[type]
        }

        fun setupEffects() {
            EFFECT_FUNCTIONS[NATURE] = { entity, effect -> FishingSpawnCause.alterNatureAttempt(entity, effect) }
            EFFECT_FUNCTIONS[IV] = { entity, effect -> FishingSpawnCause.alterIVAttempt(entity, effect) }
            EFFECT_FUNCTIONS[SHINY_REROLL] = { entity, effect -> FishingSpawnCause.shinyReroll(entity, effect) }
            EFFECT_FUNCTIONS[GENDER_CHANCE] = { entity, effect -> FishingSpawnCause.alterGenderAttempt(entity, effect) }
            EFFECT_FUNCTIONS[LEVEL_RAISE] = { entity, effect -> FishingSpawnCause.alterLevelAttempt(entity, effect) }
            EFFECT_FUNCTIONS[TERA] = { entity, effect -> FishingSpawnCause.alterTeraAttempt(entity, effect) }
            EFFECT_FUNCTIONS[HIDDEN_ABILITY_CHANCE] = { entity, _ -> FishingSpawnCause.alterHAAttempt(entity) }
            EFFECT_FUNCTIONS[FRIENDSHIP] = { entity, effect -> FishingSpawnCause.alterFriendshipAttempt(entity, effect) }
            CobblemonEvents.BAIT_EFFECT_REGISTRATION.post(BaitEffectFunctionRegistryEvent()) { event ->
                EFFECT_FUNCTIONS.putAll(event.functions)
            }
        }
    }
}


