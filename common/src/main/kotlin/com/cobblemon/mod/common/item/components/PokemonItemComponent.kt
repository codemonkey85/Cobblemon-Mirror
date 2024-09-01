/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.joml.Vector4f
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class PokemonItemComponent(
    val species: Species,
    val aspects: Set<String>,
    val tint: Vector4f? = null
) {
    companion object {
        val CODEC: Codec<PokemonItemComponent> = RecordCodecBuilder.create { builder -> builder.group(
            Species.BY_IDENTIFIER_CODEC.fieldOf("species").forGetter(PokemonItemComponent::species),
            Codec.STRING.listOf().fieldOf("aspects").forGetter { it.aspects.toList() },
            Codec.FLOAT.listOf().optionalFieldOf("tint").forGetter { Optional.ofNullable(it.tint?.let { listOf(it.x, it.y, it.z, it.w) }) }
        ).apply(builder) { species, aspects, tint -> PokemonItemComponent(species, aspects.toSet(), tint.getOrNull()?.let { Vector4f(it[0], it[1], it[2], it[3]) } ) } }

        val PACKET_CODEC: StreamCodec<ByteBuf, PokemonItemComponent> = ByteBufCodecs.fromCodec(CODEC)
    }
}