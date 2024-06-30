/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.ai.SleepDepth
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.entity.EntityDimensionsAdapter
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.net.messages.client.data.NPCRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.datafixers.util.Either
import net.minecraft.block.Block
import net.minecraft.entity.EntityDimensions
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.world.biome.Biome

object NPCClasses : JsonDataRegistry<NPCClass> {

    override val id = cobblemonResource("npc")
    override val type = ResourceType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(EntityDimensions::class.java, EntityDimensionsAdapter)
        .registerTypeAdapter(Box::class.java, BoxAdapter)
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .registerTypeAdapter(TimeRange::class.java, IntRangesAdapter(TimeRange.timeRanges) { TimeRange(*it) })
        .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
        .registerTypeAdapter(SleepDepth::class.java, SleepDepth.adapter)
        .registerTypeAdapter(DropEntry::class.java, DropEntryAdapter)
        .registerTypeAdapter(NbtCompound::class.java, NbtCompoundAdapter)
        .registerTypeAdapter(NPCPartyProvider::class.java, NPCPartyProviderAdapter)
        .registerTypeAdapter(MoValue::class.java, MoValueAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(Either::class.java, Identifier::class.java, ExpressionLike::class.java).type, NPCScriptAdapter)
        .disableHtmlEscaping()
        .enableComplexMapKeySerialization()
        .create()

    override val typeToken: TypeToken<NPCClass> = TypeToken.get(NPCClass::class.java)
    override val resourcePath = "npcs"
    override val observable = SimpleObservable<NPCClasses>()
    private val npcClassesByIdentifier = hashMapOf<Identifier, NPCClass>()

    val classes: Collection<NPCClass>
        get() = this.npcClassesByIdentifier.values

    init {
        // NPC additions would be useful
//        SpeciesAdditions.observable.subscribe {
//            this.species.forEach(Species::initialize)
//            this.species.forEach(Species::resolveEvolutionMoves)
//        }
    }

    /**
     * Finds an NPC class by the pathname of their [Identifier].
     * This method exists for the convenience of finding Cobble default NPC classes.
     * This uses [getByIdentifier] using the [Cobblemon.MODID] as the namespace and the [name] as the path.
     *
     * @param name The path of the NPC class asset.
     * @return The [NPCClass] if existing.
     */
    fun getByName(name: String) = this.getByIdentifier(cobblemonResource(name))

    /**
     * Finds an [NPCClass] by its unique [Identifier].
     *
     * @param identifier The unique [NPCClass.resourceIdentifier] of the [NPCClass].
     * @return The [NPCClass] if existing.
     */
    fun getByIdentifier(identifier: Identifier) = this.npcClassesByIdentifier[identifier]

    /**
     * Counts the currently loaded NPC classes.
     *
     * @return The loaded NPC class amount.
     */
    fun count() = this.npcClassesByIdentifier.size

    /**
     * Picks a random [NPCClass].
     *
     * @throws [NoSuchElementException] if there are no NPC classes loaded.
     *
     * @return A randomly selected [Species].
     */
    fun random(): NPCClass = this.npcClassesByIdentifier.values.random()

    override fun reload(data: Map<Identifier, NPCClass>) {
        this.npcClassesByIdentifier.clear()
        data.forEach { (identifier, species) ->
            species.resourceIdentifier = identifier
            this.npcClassesByIdentifier[identifier] = species
        }
    }

    override fun sync(player: ServerPlayerEntity) {
        NPCRegistrySyncPacket(npcClassesByIdentifier.values.toList()).sendToPlayer(player)
    }
}