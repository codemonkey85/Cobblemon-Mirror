/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.simplify
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.CompletableFuture

/**
 * A data registry responsible for providing tab completion for Pokemon properties.
 * This will handle both our defaults and dynamically allow clients to get tab completion for server side custom properties.
 *
 * @author Licious
 * @since October 27th, 2022
 */
internal object PropertiesCompletionProvider : DataRegistry {

    override val id = cobblemonResource("properties_tab_completion")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<PropertiesCompletionProvider>()
    private val providers = hashSetOf<SuggestionHolder>()

    override fun reload(manager: ResourceManager) {
        // We do not have sort of datapack support for this
        this.reload()
    }

    override fun sync(player: ServerPlayer) {
        PropertiesCompletionRegistrySyncPacket(this.providers).sendToPlayer(player)
    }

    // We only have this because we do not need to have a ResourceManager for a reload to exist, this is invoked each time a custom property is added
    fun reload() {
        this.providers.clear()
        this.addDefaults()
        this.addCustom()
    }

    /**
     * Adds a new possible suggestion to this registry.
     *
     * @param keys The different possible keys.
     * @param suggestions The suggestions.
     */
    fun inject(keys: Iterable<String>, suggestions: Collection<String>) {
        this.providers += SuggestionHolder(keys.toList(), suggestions)
    }

    /**
     * Provides all the keys from the registered providers.
     *
     * @return Every possible key to be suggested.
     */
    fun keys() = this.providers.flatMap { it.keys }

    /**
     * Attempts to suggest a key for a property from the provided partial key.
     *
     * @param partialKey The partial key attempting to fill.
     * @param excludedKeys The keys that should not be checked for, this should be used when you want to avoid repeating keys.
     * @param builder The [SuggestionsBuilder] for the context of the query.
     * @return The suggestions
     */
    fun suggestKeys(partialKey: String, excludedKeys: Collection<String>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        var matches = 0
        var exactMatch = false
        this.providers.forEach { provider ->
            if (provider.keys.none { key -> excludedKeys.contains(key) }) {
                provider.keys.forEach { key ->
                    if (key.startsWith(partialKey)) {
                        val substring = key.substringAfter(partialKey)
                        builder.suggest(builder.remaining + substring)
                        matches++
                        if (substring.isEmpty()) {
                            exactMatch = true
                        }
                    }
                }
            }
        }
        // If only 1 match happened and it was the exact value already input then we suggest the assigner character
        if (matches == 1 && exactMatch) {
            builder.suggest("${builder.remaining}=")
        }
        return builder.buildFuture()
    }

    /**
     * Attempts to suggest a value for a property from the provided examples.
     *
     * @param possibleKey The potential key that may exist for a property.
     * @param currentValue The current literal value being input.
     * @param builder The [SuggestionsBuilder] for the context of the query.
     * @return The suggestions.
     */
    fun suggestValues(possibleKey: String, currentValue: String, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val suggestionHolder = this.providers.firstOrNull { provider -> provider.keys.contains(possibleKey) } ?: return Suggestions.empty()
        suggestionHolder.suggestions.forEach { suggestion ->
            if (!suggestion.startsWith(currentValue))
                return@forEach
            val substring = suggestion.substringAfter(currentValue)
            builder.suggest(builder.remaining + substring)
        }
        return builder.buildFuture()
    }

    private fun addDefaults() {
        this.inject(setOf("level", "lvl", "l"), setOf("1", "${Cobblemon.config.maxPokemonLevel}") )
        this.inject(setOf("shiny", "s"), setOf("yes", "no"))
        this.inject(setOf("gender"), Gender.values().map { it.name.lowercase() })
        this.inject(setOf("friendship"), setOf("0", Cobblemon.config.maxPokemonFriendship.toString()))
        this.inject(setOf("pokeball"), PokeBalls.all().map { it.name.simplify() })
        this.inject(setOf("nature"), Natures.all().map { it.name.simplify() })
        this.inject(setOf("ability"), Abilities.all().map { if (it.name.asIdentifierDefaultingNamespace().namespace == Cobblemon.MODID) it.name.asIdentifierDefaultingNamespace().path else it.name })
        this.inject(setOf("dmax"), setOf("0", Cobblemon.config.maxDynamaxLevel.toString()))
        this.inject(setOf("gmax"), setOf("yes", "no"))
        this.inject(setOf("type", "elemental_type"), ElementalTypes.all().map { it.name })
        this.inject(setOf("tera_type", "tera"), TeraTypes.map { it.id.simplify() })
        this.inject(setOf("tradeable"), setOf("yes", "no"))
        this.inject(setOf("originaltrainer", "ot"), setOf(""))
        this.inject(setOf("originaltrainertype", "ottype"), setOf("None", "Player", "NPC"))

        Stats.PERMANENT.forEach{ stat ->
            val statName = stat.toString().lowercase()
            this.inject(setOf("${statName}_iv"), setOf("0", IVs.MAX_VALUE.toString()))
            this.inject(setOf("${statName}_ev"), setOf("0", EVs.MAX_STAT_VALUE.toString()))
        }

        this.inject(setOf("status"), Statuses.getPersistentStatuses().map { it.name.simplify() })
    }

    private fun addCustom() {
        CustomPokemonProperty.properties.forEach { property ->
            // We won't tab complete properties that have no key attached to them as it would be fairly hard to determine which one to suggest
            if (property.needsKey) {
                this.inject(property.keys, property.examples())
            }
        }
    }

    internal data class SuggestionHolder(
        val keys: Collection<String>,
        val suggestions: Collection<String>
    )

}
