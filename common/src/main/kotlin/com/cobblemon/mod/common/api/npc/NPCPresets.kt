/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.ai.SleepDepth
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.entity.EntityDimensionsAdapter
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.npc.configuration.NPCInteractConfiguration
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.item.Item
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB

object NPCPresets : JsonDataRegistry<NPCPreset> {

    override val id = cobblemonResource("npc_preset")
    override val type = PackType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(EntityDimensions::class.java, EntityDimensionsAdapter)
        .registerTypeAdapter(AABB::class.java, BoxAdapter)
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(TimeRange::class.java, IntRangesAdapter(TimeRange.timeRanges) { TimeRange(*it) })
        .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
        .registerTypeAdapter(SleepDepth::class.java, SleepDepth.adapter)
        .registerTypeAdapter(DropEntry::class.java, DropEntryAdapter)
        .registerTypeAdapter(CompoundTag::class.java, NbtCompoundAdapter)
        .registerTypeAdapter(NPCPartyProvider::class.java, NPCPartyProviderAdapter)
        .registerTypeAdapter(NPCInteractConfiguration::class.java, NPCInteractConfigurationAdapter)
        .registerTypeAdapter(MoValue::class.java, MoValueAdapter)
        .registerTypeAdapter(Component::class.java, TextAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(Either::class.java, ResourceLocation::class.java, ExpressionLike::class.java).type, NPCScriptAdapter)
        .disableHtmlEscaping()
        .enableComplexMapKeySerialization()
        .create()

    override val typeToken: TypeToken<NPCPreset> = TypeToken.get(NPCPreset::class.java)
    override val resourcePath = "npcs"
    override val observable = SimpleObservable<NPCPresets>()
    private val npcPresetsByIdentifier = mutableMapOf<ResourceLocation, NPCPreset>()

    override fun sync(player: ServerPlayer) {
        // TODO probably do want to sync the presets
    }

    override fun reload(data: Map<ResourceLocation, NPCPreset>) {
        npcPresetsByIdentifier.clear()
        npcPresetsByIdentifier.putAll(data)
        observable.emit(this)
    }

    fun getPreset(identifier: ResourceLocation): NPCPreset? {
        return npcPresetsByIdentifier[identifier]
    }
}