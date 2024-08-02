/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.stat.CobblemonStatProvider
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A provider for various things stat related.
 * This should only provide stats with the type of [Stat.Type.PERMANENT].
 * The base implementation can be found in [CobblemonStatProvider].
 * To replace the base implementation see [Cobblemon.statProvider].
 *
 * @author Licious
 * @since November 6th, 2022
 */
interface StatProvider {

    /**
     * The [StatTypeAdapter] implementation.
     */
    val typeAdapter: StatTypeAdapter

    /**
     * Collects all the stats currently implemented.
     *
     * @return A collection of stats.
     */
    fun all(): Collection<Stat>

    /**
     * Collects all stats of the given type.
     *
     * @param type The [Stat.Type] being queried.
     * @return A collection of stats that match the given [type].
     */
    fun ofType(type: Stat.Type): Collection<Stat>

    /**
     * Populate a [Species] with base stats during initialization.
     * This should only provide stats with the type of [Stat.Type.PERMANENT].
     *
     * @param species The [Species] requesting provision.
     */
    fun provide(species: Species)

    /**
     * Populate a [FormData] with base stats during initialization.
     * This should only provide stats with the type of [Stat.Type.PERMANENT].
     *
     * @param form The [FormData] requesting provision.
     */
    fun provide(form: FormData)

    /**
     * Generates an empty IVs stat holder.
     *
     * @return The [EVs] generated.
     */
    fun createEmptyEVs(): EVs

    /**
     * Generates an empty IVs stat holder.
     *
     * @param minPerfectIVs The minimal amount of perfect IVs.
     * @return The [IVs] generated.
     */
    fun createEmptyIVs(minPerfectIVs: Int): IVs

    /**
     * Creates a literal representation of the stats in showdown format.
     *
     * @param species The [Species] being created in a showdown format.
     * @param form The form being used if any, if it's still writing the base species this will be null.
     * @return The literal representation.
     */
    fun toShowdown(species: Species, form: FormData?): String

    /**
     * Resolves the value of a stat for Pokémon.
     *
     * @param pokemon The [Pokemon] being queried.
     * @param stat The [Stat] being queried.
     * @return The stat numerical value.
     */
    fun getStatForPokemon(pokemon: Pokemon, stat: Stat): Int

    /**
     * Provides the [Stat] for the given identifier.
     *
     * @param identifier The identifier being queried.
     * @return The [Stat] if existing otherwise null.
     */
    fun fromIdentifier(identifier: ResourceLocation): Stat?

    /**
     * Provides the [Stat] for the given identifier.
     *
     * @throws IllegalArgumentException if the identifier isn't associate with any stat.
     * @param identifier The identifier being queried.
     * @return The [Stat] if existing otherwise throws exception.
     */
    fun fromIdentifierOrThrow(identifier: ResourceLocation): Stat

    /**
     * Decode a [Stat] from the given [buffer].
     *
     * @param buffer The [ByteBuf].
     * @return The decoded [Stat].
     */
    fun decode(buffer: RegistryFriendlyByteBuf): Stat

    /**
     * Encode the given [stat] to the [buffer].
     *
     * @param buffer The [ByteBuf].
     * @param stat The [Stat] being encoded.
     */
    fun encode(buffer: RegistryFriendlyByteBuf, stat: Stat)

}