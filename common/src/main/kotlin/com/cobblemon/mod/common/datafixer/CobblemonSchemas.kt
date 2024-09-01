/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.datafixer.fix.EvolutionProxyNestingFix
import com.cobblemon.mod.common.datafixer.fix.IvEvToIdentifierFix
import com.cobblemon.mod.common.datafixer.fix.TeraTypeFix
import com.cobblemon.mod.common.datafixer.fix.TradeableMissingFix
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mojang.datafixers.DSL
import com.mojang.datafixers.DSL.TypeReference
import com.mojang.datafixers.DataFixer
import com.mojang.datafixers.DataFixerBuilder
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.types.templates.TypeTemplate
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import net.minecraft.util.datafix.fixes.References
import java.util.concurrent.Executors
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate", "unused")
object CobblemonSchemas {

    private val RESULT: DataFixerBuilder.Result = this.create()

    const val DATA_VERSION = 0

    /**
     * The Cobblemon [DataFixer].
     */
    @JvmStatic
    val DATA_FIXER: DataFixer get() = RESULT.fixer()

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
        result.optimize(types, executor)
        return result
    }

    private fun appendSchemas(builder: DataFixerBuilder) {
        val schema0 = builder.addSchema(0, ::CobblemonRootSchema)
        builder.addFixer(EvolutionProxyNestingFix(schema0))
        builder.addFixer(IvEvToIdentifierFix(schema0))
        builder.addFixer(TeraTypeFix(schema0))
        builder.addFixer(TradeableMissingFix(schema0))
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

        companion object {
            private const val VERSION_KEY = "${Cobblemon.MODID}:data_version"
        }

    }

    private class CobblemonRootSchema(versionKey: Int, parent: Schema?) : Schema(versionKey, parent) {

        override fun registerTypes(
            schema: Schema,
            entityTypes: MutableMap<String, Supplier<TypeTemplate>>,
            blockEntityTypes: MutableMap<String, Supplier<TypeTemplate>>
        ) {
            schema.registerType(false, CobblemonTypeReferences.POKEMON, DSL::remainder)
            // Even thought these aren't used yet might as well, we need 1 recursive type present to prevent a crash anyhow.
            schema.registerType(true, References.ENTITY) { DSL.taggedChoiceLazy("id", DSL.string(), entityTypes) }
        }

        // If we ever decide to target something do it here
        override fun registerEntities(schema: Schema): MutableMap<String, Supplier<TypeTemplate>> = hashMapOf()

        // If we ever decide to target something do it here
        override fun registerBlockEntities(schema: Schema): MutableMap<String, Supplier<TypeTemplate>> = hashMapOf()

    }

}