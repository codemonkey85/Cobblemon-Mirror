/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.*
import java.util.UUID
import net.minecraft.world.item.ItemStack
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

/**
 * A packet that initializes a trade with a player. Information about the other party is included.
 *
 * Handled by [com.cobblemon.mod.common.client.net.trade.TradeStartedHandler].
 *
 * @author Hiroku
 * @since March 13th, 2023
 */
class TradeStartedPacket(
    val traderId: UUID,
    val traderName: MutableComponent,
    val traderParty: List<TradeablePokemon?>
) : NetworkPacket<TradeStartedPacket> {
    class TradeablePokemon(
        val pokemonId: UUID,
        val species: ResourceLocation,
        val aspects: Set<String>,
        val level: Int,
        val gender: Gender,
        val heldItem: ItemStack,
        val tradeable: Boolean
    ) {
        companion object {
            fun decode(buffer: RegistryFriendlyByteBuf) = TradeablePokemon(
                buffer.readUUID(),
                buffer.readIdentifier(),
                buffer.readList { it.readString() }.toSet(),
                buffer.readSizedInt(IntSize.U_SHORT),
                Gender.values()[buffer.readSizedInt(IntSize.U_BYTE)],
                buffer.readItemStack(),
                buffer.readBoolean()
            )
        }

        constructor(pokemon: Pokemon): this(
            pokemon.uuid,
            pokemon.species.resourceIdentifier,
            pokemon.aspects,
            pokemon.level,
            pokemon.gender,
            pokemon.heldItem().copy(),
            pokemon.tradeable
        )

        fun encode(buffer: RegistryFriendlyByteBuf) {
            buffer.writeUUID(pokemonId)
            buffer.writeIdentifier(species)
            buffer.writeCollection(aspects) { _, v -> buffer.writeString(v) }
            buffer.writeSizedInt(IntSize.U_SHORT, level)
            buffer.writeSizedInt(IntSize.U_BYTE, gender.ordinal)
            buffer.writeItemStack(heldItem)
            buffer.writeBoolean(tradeable)
        }

        fun asRenderablePokemon() = RenderablePokemon(
            species = PokemonSpecies.getByIdentifier(species)!!,
            aspects = aspects
        )
    }

    companion object {
        val ID = cobblemonResource("trade_started")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeStartedPacket(
            buffer.readUUID(),
            buffer.readText().copy(),
            buffer.readList { buffer.readNullable { TradeablePokemon.decode(buffer) } }
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(traderId)
        buffer.writeText(traderName)
        buffer.writeCollection(traderParty) { _, v -> buffer.writeNullable(v) { _, v2 -> v2.encode(buffer) } }
    }
}