/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.math.CatmullRomCurve
import com.cobblemon.mod.common.util.math.CubedBezierCurve
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.codecs.UnboundedMapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import kotlin.math.floor

/**
 * A type of interpolating curve used in MoLang.
 *
 * @author Hiroku
 * @since January 21st, 2023
 */
interface MoLangCurve : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<MoLangCurve, CurveType>(
        keyFromString = CurveType::valueOf,
        keyFromValue = { it.type },
        stringFromKey = { it.name }
    ) {
        init {
            registerSubtype(CurveType.LINEAR, LinearMoLangCurve::class.java, LinearMoLangCurve.CODEC)
            registerSubtype(CurveType.CATMULL_ROM, CatmullRomMoLangCurve::class.java, CatmullRomMoLangCurve.CODEC)
            registerSubtype(CurveType.BEZIER, BezierMoLangCurve::class.java, BezierMoLangCurve.CODEC)
            registerSubtype(CurveType.BEZIER_CHAIN, BezierChainMoLangCurve::class.java, BezierChainMoLangCurve.CODEC)
        }
    }

    var name: String
    val type: CurveType
    var input: Expression
    fun resolve(runtime: MoLangRuntime, inputValue: Double): Double
    fun apply(runtime: MoLangRuntime) {
        runtime.environment.setSimpleVariable(name, DoubleValue(resolve(runtime, runtime.resolveDouble(input))))
    }
}

class LinearMoLangCurve(
    override var name: String = "variable",
    override var input: Expression = NumberExpression(0.0),
    var horizontalRange: Expression = NumberExpression(1.0),
    var nodes: List<Double> = emptyList()
) : MoLangCurve {
    companion object {
        val CODEC: Codec<LinearMoLangCurve> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.STRING.fieldOf("name").forGetter { it.name },
                EXPRESSION_CODEC.fieldOf("input").forGetter { it.input },
                EXPRESSION_CODEC.fieldOf("horizontalRange").forGetter { it.horizontalRange },
                PrimitiveCodec.DOUBLE.listOf().fieldOf("nodes").forGetter { it.nodes }
            ).apply(instance) { _, name, input, horizontalRange, nodes ->
                LinearMoLangCurve(name, input, horizontalRange, nodes)
            }
        }
    }

    override val type: CurveType = CurveType.LINEAR
    override fun resolve(runtime: MoLangRuntime, inputValue: Double): Double {
        val range = runtime.resolveDouble(horizontalRange)
        val spaceBetweenNodes = range / (nodes.size - 1)
        val rangeIndex = floor(inputValue / spaceBetweenNodes).toInt()
        if (rangeIndex < 0) {
            return nodes[0]
        } else if (rangeIndex + 1 >= nodes.size) {
            return nodes.last()
        } else {
            val leftNode = nodes[rangeIndex]
            val rightNode = nodes[rangeIndex + 1]
            val t = inputValue.mod(spaceBetweenNodes) / spaceBetweenNodes

            return leftNode + (rightNode - leftNode) * t
        }
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        name = buffer.readString()
        input = MoLang.createParser(buffer.readString()).parseExpression()
        horizontalRange = MoLang.createParser(buffer.readString()).parseExpression()
        nodes = buffer.readList { buffer.readDouble() }
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(name)
        buffer.writeString(input.getString())
        buffer.writeString(horizontalRange.getString())
        buffer.writeCollection(nodes) { pb, node -> pb.writeDouble(node) }
    }
}

class CatmullRomMoLangCurve(
    override var name: String = "variable",
    override var input: Expression = NumberExpression(0.5),
    var horizontalRange: Expression = NumberExpression(1.0),
    var nodes: List<Double> = emptyList()
) : MoLangCurve {

    var curve: CatmullRomCurve

    init {
        curve = CatmullRomCurve(nodes)
    }

    companion object {
        val CODEC: Codec<CatmullRomMoLangCurve> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.STRING.fieldOf("name").forGetter { it.name },
                EXPRESSION_CODEC.fieldOf("input").forGetter { it.input },
                EXPRESSION_CODEC.fieldOf("horizontalRange").forGetter { it.horizontalRange },
                PrimitiveCodec.DOUBLE.listOf().fieldOf("nodes").forGetter { it.nodes }
            ).apply(instance) { _, name, input, horizontalRange, nodes -> CatmullRomMoLangCurve(name, input, horizontalRange, nodes) }
        }
    }

    override val type = CurveType.CATMULL_ROM
    override fun resolve(runtime: MoLangRuntime, inputValue: Double): Double {

        val horizontalRange = runtime.resolveDouble(horizontalRange)
        val segments = nodes.size - 3
        val position = (inputValue / horizontalRange) * segments
        val pso = (position + 1) / (segments + 2)

        return curve.getY(pso)
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        name = buffer.readString()
        input = MoLang.createParser(buffer.readString()).parseExpression()
        horizontalRange = MoLang.createParser(buffer.readString()).parseExpression()
        nodes = buffer.readList { buffer.readDouble() }

        curve = CatmullRomCurve(nodes)
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(name)
        buffer.writeString(input.getString())
        buffer.writeString(horizontalRange.getString())
        buffer.writeCollection(nodes) { pb, node -> pb.writeDouble(node) }
    }
}

class BezierChainMoLangCurve(
    override var name: String = "variable",
    override var input: Expression = NumberExpression(0.5),
    var nodes: Map<Double, BezierChainNode> = emptyMap()
) : MoLangCurve {
    var nodePairs: List<BezierChainNodePair> = emptyList()

    fun deriveNodePairs() {
        val nodePairs = mutableListOf<BezierChainNodePair>()
        val sortedNodes = nodes.entries.sortedBy { it.key }
        for (i in 1 until sortedNodes.size) {
            val node1 = sortedNodes[i - 1]
            val node2 = sortedNodes[i]

            nodePairs.add(BezierChainNodePair(node1.key, node2.key, node1.value, node2.value))
        }
        this.nodePairs = nodePairs
    }

    init {
        deriveNodePairs()
    }

    class BezierChainNode(var value: Double, var slope: Double) {
        fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
            buffer.writeDouble(value)
            buffer.writeDouble(slope)
        }

        fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
            value = buffer.readDouble()
            slope = buffer.readDouble()
        }

        companion object {
            val CODEC = RecordCodecBuilder.create<BezierChainNode> { instance ->
                instance.group(
                    PrimitiveCodec.DOUBLE.fieldOf("value").forGetter { it.value },
                    PrimitiveCodec.DOUBLE.fieldOf("slope").forGetter { it.slope }
                ).apply(instance, ::BezierChainNode)
            }
        }
    }

    class BezierChainNodePair(
        val startTime: Double,
        val endTime: Double,
        node1: BezierChainNode,
        node2: BezierChainNode
    ) {
        val curve: CubedBezierCurve
        init {
            val v1 = node1.value
            val v2 = node1.value + node1.slope / 3
            val v3 = node2.value - node2.slope / 3
            val v4 = node2.value
            curve = CubedBezierCurve(v1, v2, v3, v4)
        }
    }

    class BezierChainPointData(val time: Double, val value: Double, val slope: Double)

    companion object {
        val CODEC: Codec<BezierChainMoLangCurve> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.STRING.fieldOf("name").forGetter { it.name },
                EXPRESSION_CODEC.fieldOf("input").forGetter { it.input },
                UnboundedMapCodec(PrimitiveCodec.DOUBLE, BezierChainNode.CODEC).fieldOf("nodes").forGetter { it.nodes }
            ).apply(instance) { _, name, input, nodes -> BezierChainMoLangCurve(name, input, nodes) }
        }
    }

    override val type: CurveType = CurveType.BEZIER_CHAIN
    override fun resolve(runtime: MoLangRuntime, inputValue: Double): Double {
        val position = inputValue.coerceIn(0.0, 1.0)
        val nodePair = nodePairs.last { position >= it.startTime }
        val curve = nodePair.curve
        val t = (position - nodePair.startTime) / (nodePair.endTime - nodePair.startTime)
        return curve.getY(t)
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        name = buffer.readString()
        input = MoLang.createParser(buffer.readString()).parseExpression()
        nodes = buffer.readMap({ buffer.readDouble() }, { BezierChainNode(0.0, 0.0).also { it.readFromBuffer(buffer) } })
        deriveNodePairs()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(name)
        buffer.writeString(input.getString())
        buffer.writeMap(nodes, { pb, key -> pb.writeDouble(key) }, { pb, value -> value.writeToBuffer(buffer) })
    }
}

class BezierMoLangCurve(
    override var name: String = "variable",
    override var input: Expression = NumberExpression(0.5),
    var horizontalRange: Expression = NumberExpression(1.0),
    var v0: Double = 0.0,
    var v1: Double = 0.0,
    var v2: Double = 0.0,
    var v3: Double = 0.0
) : MoLangCurve {
    var curve: CubedBezierCurve

    init {
        curve = CubedBezierCurve(v0, v1, v2, v3)
    }

    companion object {
        val CODEC: Codec<BezierMoLangCurve> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.STRING.fieldOf("name").forGetter { it.name },
                EXPRESSION_CODEC.fieldOf("input").forGetter { it.input },
                EXPRESSION_CODEC.fieldOf("horizontalRange").forGetter { it.horizontalRange },
                PrimitiveCodec.DOUBLE.fieldOf("v0").forGetter { it.v0 },
                PrimitiveCodec.DOUBLE.fieldOf("v1").forGetter { it.v1 },
                PrimitiveCodec.DOUBLE.fieldOf("v2").forGetter { it.v2 },
                PrimitiveCodec.DOUBLE.fieldOf("v3").forGetter { it.v3 }
            ).apply(instance) { _, name, input, horizontalRange, v0, v1, v2, v3 -> BezierMoLangCurve(name, input, horizontalRange, v0, v1, v2, v3) }
        }
    }

    override val type = CurveType.BEZIER
    override fun resolve(runtime: MoLangRuntime, inputValue: Double): Double {
        val horizontalRange = runtime.resolveDouble(horizontalRange)
        val position = inputValue / horizontalRange
        return curve.getY(position)
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        name = buffer.readString()
        input = MoLang.createParser(buffer.readString()).parseExpression()
        horizontalRange = MoLang.createParser(buffer.readString()).parseExpression()
        v0 = buffer.readDouble()
        v1 = buffer.readDouble()
        v2 = buffer.readDouble()
        v3 = buffer.readDouble()
        curve = CubedBezierCurve(v0, v1, v2, v3)
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(name)
        buffer.writeString(input.getString())
        buffer.writeString(horizontalRange.getString())
        buffer.writeDouble(v0)
        buffer.writeDouble(v1)
        buffer.writeDouble(v2)
        buffer.writeDouble(v3)
    }
}