/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.datafixer.fix.BlockPosUpdateFix
import com.cobblemon.mod.common.datafixer.fix.EvolutionProxyNestingFix
import com.cobblemon.mod.common.datafixer.fix.IvEvToIdentifierFix
import com.cobblemon.mod.common.datafixer.fix.TeraTypeFix
import com.cobblemon.mod.common.datafixer.fix.TradeableMissingFix
import com.cobblemon.mod.common.datafixer.schema.CobblemonRootSchema
import com.cobblemon.mod.common.datafixer.schema.CobblemonSchemaV2
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mojang.datafixers.DSL.TypeReference
import com.mojang.datafixers.DataFixer
import com.mojang.datafixers.DataFixerBuilder
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix
import java.util.concurrent.Executors

@Suppress("MemberVisibilityCanBePrivate", "unused")
object CobblemonSchemas {

    private val RESULT: DataFixerBuilder.Result = this.create()

    const val DATA_VERSION = 1

    /**
     * The Cobblemon [DataFixer].
     */
    @JvmStatic
    val DATA_FIXER: DataFixer get() = RESULT.fixer()

    const val VERSION_KEY = "${Cobblemon.MODID}:data_version"

    /**
     * Wraps the given [Codec] with the Cobblemon [DataFixer].
     *
     * @param T The [Codec] element type.
     * @param codec The [Codec] being wrapped.
     * @param typeReference The [TypeReference] used for this wrap.
     * @return The new generated [Codec].
     */
    fun <T> wrapCodec(codec: Codec<T>, typeReference: TypeReference): Codec<T> = CobblemonDataFixerCodec(codec, typeReference)

    private fun create(): DataFixerBuilder.Result {
        val builder = DataFixerBuilder(DATA_VERSION)
        this.appendSchemas(builder)
        val types = CobblemonTypeReferences.types()
        val result = builder.build()
        if (types.isEmpty()) {
            return result
        }
        val executor = Executors.newSingleThreadExecutor(
            ThreadFactoryBuilder()
                .setNameFormat("${Cobblemon.MODID} Datafixer Bootstrap")
                .setDaemon(true)
                .setPriority(1)
                .build()
        )
        result.optimize(types, executor).join()
        return result
    }

    private fun appendSchemas(builder: DataFixerBuilder) {
        builder.addSchema(0, ::CobblemonRootSchema)
        val schema1 = builder.addSchema(1, ::CobblemonSchemaV2)
        builder.addFixer(EvolutionProxyNestingFix(schema1))
        builder.addFixer(IvEvToIdentifierFix(schema1))
        builder.addFixer(TeraTypeFix(schema1))
        builder.addFixer(TradeableMissingFix(schema1))
        builder.addFixer(ItemStackComponentizationFix(schema1))
        builder.addFixer(BlockPosUpdateFix(schema1))
    }

    private class CobblemonDataFixerCodec<R>(private val baseCodec: Codec<R>, private val typeReference: TypeReference) : Codec<R> {

        override fun <T> encode(input: R, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            return this.baseCodec.encode(input, ops, prefix)
                .flatMap { encoded -> ops.mergeToMap(encoded, ops.createString(VERSION_KEY), ops.createInt(DATA_VERSION)) }
        }

        override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<R, T>> {
            val inputVersion = ops.get(input, VERSION_KEY)
                .flatMap(ops::getNumberValue)
                .map(Number::toInt)
                .result()
                // If none always do op unlike vanilla.
                .orElse(DATA_VERSION - 1)
            val dynamicWithoutVersion = Dynamic(ops, ops.remove(input, VERSION_KEY))
            val dataFixedDynamic = DATA_FIXER.update(this.typeReference, dynamicWithoutVersion, inputVersion, DATA_VERSION)
            return this.baseCodec.decode(dataFixedDynamic)
        }
    }



}