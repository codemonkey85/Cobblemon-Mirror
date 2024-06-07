package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import org.joml.Vector4f
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

data class PokemonItemComponent(
    val species: Species,
    val aspects: Set<String>,
    val tint: Vector4f? = null
) {
    companion object {
        val CODEC: Codec<PokemonItemComponent> = RecordCodecBuilder.create { builder -> builder.group(
            Species.CODEC.fieldOf("species").forGetter(PokemonItemComponent::species),
            Codec.STRING.listOf().fieldOf("aspects").forGetter { it.aspects.toList() },
            Codec.FLOAT.listOf().optionalFieldOf("tint").forGetter { Optional.ofNullable(it.tint?.let { listOf(it.x, it.y, it.z, it.w) }) }
        ).apply(builder) { species, aspects, tint -> PokemonItemComponent(species, aspects.toSet(), tint.getOrNull()?.let { Vector4f(it[0], it[1], it[2], it[3]) } ) } }

        val PACKET_CODEC: PacketCodec<ByteBuf, PokemonItemComponent> = PacketCodecs.codec(CODEC)
    }
}