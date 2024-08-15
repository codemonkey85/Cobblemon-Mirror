/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.mojang.brigadier.suggestion.SuggestionsBuilder

internal abstract class PropertiesCompletionProvider {

    protected val providers = hashSetOf<SuggestionHolder>()

    /**
     * Attempts to suggest a key for a property from the provided partial key.
     *
     * @param partialKey The partial key attempting to fill.
     * @param excludedKeys The keys that should not be checked for, this should be used when you want to avoid repeating keys.
     * @param builder The [SuggestionsBuilder] for the context of the query.
     * @return he [builder] with any appended suggestions.
     */
    open fun suggestKeys(partialKey: String, excludedKeys: Collection<String>, builder: SuggestionsBuilder): SuggestionsBuilder {
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
        return builder
    }

    /**
     * Attempts to suggest a value for a property from the provided examples.
     *
     * @param possibleKey The potential key that may exist for a property.
     * @param currentValue The current literal value being input.
     * @param builder The [SuggestionsBuilder] for the context of the query.
     * @return The [builder] with any appended suggestions.
     */
    open fun suggestValues(possibleKey: String, currentValue: String, builder: SuggestionsBuilder): SuggestionsBuilder {
        val suggestionHolder = this.providers.firstOrNull { provider -> provider.keys.contains(possibleKey) } ?: return builder
        suggestionHolder.suggestions.forEach { suggestion ->
            if (!suggestion.startsWith(currentValue))
                return@forEach
            val substring = suggestion.substringAfter(currentValue)
            builder.suggest(builder.remaining + substring)
        }
        return builder
    }

    /**
     * Provides all the keys from the registered providers.
     *
     * @return Every possible key to be suggested.
     */
    open fun keys() = this.providers.flatMap { it.keys }

    /**
     * Adds a new possible suggestion to this registry.
     *
     * @param key The key.
     * @param suggestions The suggestions.
     */
    open fun inject(key: String, suggestions: Collection<String>) {
        this.inject(setOf(key), suggestions)
    }

    /**
     * Adds a new possible suggestion to this registry.
     *
     * @param keys The different possible keys.
     * @param suggestions The suggestions.
     */
    open fun inject(keys: Set<String>, suggestions: Collection<String>) {
        this.providers += SuggestionHolder(
            keys,
            suggestions
        )
    }

    internal data class SuggestionHolder(
        val keys: Set<String>,
        val suggestions: Collection<String>
    )

}