/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

class SpawnPokeballPacket(
    val pokeBall: PokeBall,
    val aspects: Set<String>,
    vanillaSpawnPacket: ClientboundAddEntityPacket
) : SpawnExtraDataEntityPacket<SpawnPokeballPacket, EmptyPokeBallEntity>(vanillaSpawnPacket) {

    override val id: ResourceLocation = ID

    override fun encodeEntityData(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.pokeBall.name)
        buffer.writeCollection(aspects) { _, aspect -> buffer.writeString(aspect)}
    }

    override fun applyData(entity: EmptyPokeBallEntity) {
        entity.pokeBall = this.pokeBall
        entity.aspects = this.aspects
    }

    override fun checkType(entity: Entity): Boolean = entity is EmptyPokeBallEntity

    companion object {
        val ID = cobblemonResource("spawn_empty_pokeball_entity")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnPokeballPacket {
            val pokeBall = PokeBalls.getPokeBall(buffer.readIdentifier())!!
            val aspects = buffer.readList { it.readString() }.toSet()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnPokeballPacket(pokeBall, aspects, vanillaPacket)
        }
    }
}