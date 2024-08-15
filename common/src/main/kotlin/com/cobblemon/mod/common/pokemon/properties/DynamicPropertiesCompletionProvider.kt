/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.registry.CobblemonRegistries
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
internal object DynamicPropertiesCompletionProvider : DataRegistry, PropertiesCompletionProvider() {

    override val id = cobblemonResource("properties_tab_completion")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<DynamicPropertiesCompletionProvider>()

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
        this.addCustom()
    }

    private fun addCustom() {
        CustomPokemonProperty.properties.forEach { property ->
            // We won't tab complete properties that have no key attached to them as it would be fairly hard to determine which one to suggest
            if (property.needsKey) {
                this.inject(property.keys.toSet(), property.examples())
            }
        }
    }

}
