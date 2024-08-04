/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.placementmodifier

import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.block.BerryBlock
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlin.random.Random
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.network.RegistryFriendlyByteBuf

enum class BlockStateTransformerType {
    NONE,
    BERRY_TRANSFORM
}

interface BlockStateTransformer : CodecMapped {
    val type: BlockStateTransformerType
    fun transform(blockState: BlockState): BlockState

    companion object : ArbitrarilyMappedSerializableCompanion<BlockStateTransformer, BlockStateTransformerType>(
        keyFromString = { BlockStateTransformerType.valueOf(it.uppercase()) },
        stringFromKey = BlockStateTransformerType::name,
        keyFromValue = BlockStateTransformer::type
    ) {
        init {
            registerSubtype(BlockStateTransformerType.NONE, NoneBlockStateTransformer::class.java, NoneBlockStateTransformer.CODEC)
            registerSubtype(BlockStateTransformerType.BERRY_TRANSFORM, BerryTransformBlockStateTransformer::class.java, BerryTransformBlockStateTransformer.CODEC)
        }
    }
}

class NoneBlockStateTransformer : BlockStateTransformer {
    companion object {
        val CODEC: Codec<NoneBlockStateTransformer> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name }
            ).apply(instance) { _ -> NoneBlockStateTransformer() }
        }
    }

    override val type = BlockStateTransformerType.NONE
    override fun transform(blockState: BlockState) = blockState

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) = throw NotImplementedError("Not supposed to use this for block state transformers")
    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) = throw NotImplementedError("Not supposed to use this for block state transformers")
}

// We could totally add mulch to this
class BerryTransformBlockStateTransformer(val minAge: Int, val maxAge: Int, val wild: Boolean) : BlockStateTransformer {
    companion object {
        val CODEC: Codec<BerryTransformBlockStateTransformer> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.INT.fieldOf("minAge").forGetter { it.minAge },
                PrimitiveCodec.INT.fieldOf("maxAge").forGetter { it.maxAge },
                PrimitiveCodec.BOOL.fieldOf("isWild").forGetter { it.wild }
            ).apply(instance) { _, minAge, maxAge, isWild -> BerryTransformBlockStateTransformer(minAge, maxAge, isWild) }
        }
    }

    override val type = BlockStateTransformerType.NONE
    override fun transform(blockState: BlockState) = blockState
        .setValue(BerryBlock.AGE, Random.Default.nextInt(minAge, maxAge + 1))
        .setValue(BerryBlock.WAS_GENERATED, wild)

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) = throw NotImplementedError("Not supposed to use this for block state transformers")
    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) = throw NotImplementedError("Not supposed to use this for block state transformers")
}