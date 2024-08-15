/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.registry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Environment
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

/**
 * Keys and access to Cobblemon [Registry].
 *
 * Keep in mind the registry access needs to be done at the appropriate time regardless of side.
 *
 * Treat these like you would any other built-in or dynamic registry.
 */
@Suppress("unused")
object CobblemonRegistries {

    @JvmStatic val ABILITY_KEY = this.create<AbilityTemplate>("ability")
    @JvmStatic val ABILITY get() = this.getRegistry(ABILITY_KEY)
    @JvmStatic val MOVE_KEY = this.create<MoveTemplate>("move")
    @JvmStatic val MOVE get() = this.getRegistry(MOVE_KEY)
    @JvmStatic val ELEMENTAL_TYPE_KEY = this.create<ElementalType>("elemental_type")
    @JvmStatic val ELEMENTAL_TYPE get() = this.getRegistry(ELEMENTAL_TYPE_KEY)
    @JvmStatic val SPECIES_KEY = this.create<Species>("species")
    @JvmStatic val SPECIES get() = this.getRegistry(SPECIES_KEY)

    private fun <T> create(name: String): ResourceKey<Registry<T>> = ResourceKey.createRegistryKey(cobblemonResource(name))

    internal fun register() {
        Cobblemon.implementation.registerBuiltInRegistry(ABILITY_KEY, false, Abilities::register)
        Cobblemon.implementation.registerBuiltInRegistry(MOVE_KEY, false, Moves::register)
        Cobblemon.implementation.registerDynamicRegistry(ELEMENTAL_TYPE_KEY, ElementalType.DIRECT_CODEC, ElementalType.DIRECT_CODEC)
        Cobblemon.implementation.registerDynamicRegistry(SPECIES_KEY, Species.DIRECT_CODEC, Species.PACKET_CODEC)
    }

    private fun <T> getRegistry(key: ResourceKey<Registry<T>>): Registry<T> {
        if (Cobblemon.implementation.environment() == Environment.CLIENT) {
            return Minecraft.getInstance().connection?.registryAccess()?.registryOrThrow(key)
                ?: throw IllegalStateException("Client isn't connected to a world, cannot fetch registry $key")
        }
        return Cobblemon.implementation.server()?.registryAccess()?.registryOrThrow(key)
            ?: throw IllegalStateException("Server hasn't started, cannot fetch registry $key")
    }

}