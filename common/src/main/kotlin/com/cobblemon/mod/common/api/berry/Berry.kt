/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.berry.spawncondition.BerrySpawnCondition
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.berry.BerryYieldCalculationEvent
import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.item.berry.BerryItem
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.util.readBox
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readEnumSet
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readList
import com.cobblemon.mod.common.util.readMap
import com.cobblemon.mod.common.util.writeBox
import com.cobblemon.mod.common.util.writeCollection
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeEnumSet
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeMap
import com.google.gson.annotations.SerializedName
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import java.awt.Color
import java.util.EnumSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Represents the data behind a berry.
 *
 * @property identifier The [ResourceLocation] of this berry.
 * @property baseYield The [IntRange] possible for the berry tree before [bonusYield] is calculated.
 * @property preferredBiomeTags The [TagKey]s of the berries preffered biomes. Determines spawning and yield
 * @property growthTime The [IntRange] possible in minutes for how long the berry tree takes to grow frome age 0 to age 3
 * @property refreshRate The [IntRange] possible in minutes for how long the berry tree takes to grow from age 3 to age 5
 * @property favoriteMulches The types of mulches that benefit this berries yield
 * @property growthFactors An array of [GrowthFactor]s that will affect this berry. The client is not aware of these.
 * @property mutations A map of the partner berry as the key and the value as the resulting mutation with this berry.
 * @property growthPoints A collection of [GrowthPoint]s for the berry flowers and fruit.
 * @property sproutShapeBoxes A collection of [Box]es that make up the tree [VoxelShape] during the sprouting stages.
 * @property matureShapeBoxes A collection of [Box]es that make up the tree [VoxelShape] during the mature stages.
 * @property flavors The [Flavor] values.
 * @property tintIndexes Determines tints at specific indexes if any.
 * @property flowerModelIdentifier The [ResourceLocation] for the model of the berry in flower form.
 * @property flowerTexture The [ResourceLocation] for the texture of the berry in flower form. This is resolved into a [ModelPart] on the client.
 * @property fruitModelIdentifier The [ResourceLocation] for the model of the berry in flower form. This is resolved into a [ModelPart] on the client.
 * @property fruitTexture The [ResourceLocation] for the texture of the berry in flower form.
 * @property stageOnePositioning Transformation of the berry model for age 0
 *
 * @throws IllegalArgumentException if the any yield range argument is not a positive range.
 */
class Berry(
    identifier: ResourceLocation,
    val baseYield: IntRange,
    val preferredBiomeTags: List<TagKey<Biome>>,
    val growthTime: IntRange,
    val refreshRate: IntRange,
    val favoriteMulches: EnumSet<MulchVariant>,
    val growthFactors: Collection<GrowthFactor>,
    val spawnConditions: List<BerrySpawnCondition>,
    var growthPoints: Array<GrowthPoint>,
    val randomizedGrowthPoints: Boolean = true,
    val mutations: Map<ResourceLocation, ResourceLocation>,
    @SerializedName("sproutShape")
    private val sproutShapeBoxes: Collection<AABB>,
    @SerializedName("matureShape")
    private val matureShapeBoxes: Collection<AABB>,
    private val flavors: Map<Flavor, Int>,
    val tintIndexes: Map<Int, Color>,
    @SerializedName("flowerModel")
    val flowerModelIdentifier: ResourceLocation,
    val flowerTexture: ResourceLocation,
    @SerializedName("fruitModel")
    val fruitModelIdentifier: ResourceLocation,
    val fruitTexture: ResourceLocation,
    val stageOnePositioning: GrowthPoint,
    val weight: Float,
    val boneMealChance: Float
) {

    @Transient
    var identifier: ResourceLocation = identifier
        internal set

    @Transient
    private lateinit var shapedFlower: HashMap<Int, VoxelShape>

    @Transient
    private lateinit var shapedFruit: HashMap<Int, VoxelShape>

    /**
     * The [VoxelShape] of the tree during the sprouting stages.
     */
    @Transient
    lateinit var sproutShape: VoxelShape
        private set

    /**
     * The [VoxelShape] of the tree during the mature stages.
     */
    @Transient
    lateinit var matureShape: VoxelShape
        private set

    init {
        this.validate()
    }

    /**
     * Finds the [BerryItem] that uses this berry for data.
     *
     * @return The [BerryItem] if existing.
     */
    fun item(): BerryItem? = CobblemonItems.berries()[this.identifier]

    /**
     * Finds the [BerryBlock] that uses this berry for data.
     *
     * @return The [BerryBlock] if existing.
     */
    fun block(): BerryBlock? = CobblemonBlocks.berries()[this.identifier]

    /**
     * Query the value of a certain flavor.
     *
     * @param flavor The [Flavor] being queried.
     * @return The value if any or 0.
     */
    fun flavor(flavor: Flavor): Int = this.flavors[flavor] ?: 0

    fun dislikedBy(nature: Nature): Boolean {
        val dislikedFlavor = nature.dislikedFlavor ?: return false
        return flavor(dislikedFlavor) > 0
    }

    /**
     * Calculates the yield for a berry tree being planted or replanted after a life cycle.
     * Triggers [BerryYieldCalculationEvent].
     *
     * @param world The [World] the tree is present in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @param placer The [LivingEntity] tending to the tree, if any.
     * @return The total berry stack count.
     */
    fun calculateYield(world: Level, state: BlockState, pos: BlockPos, placer: LivingEntity? = null): Int {
        val base = this.baseYield.random()
        val bonus = this.bonusYield(world, state, pos)
        var yield = base + bonus.first
        val treeEntity = world.getBlockEntity(pos) as BerryBlockEntity
        if (BerryBlock.getMulch(treeEntity) == MulchVariant.RICH) {
            //I hope the upper bound isnt exclusive, especially when there's a method called nextBetweenExclusive
            // update: it was meant to be exclusive, which caused #1124. I've lowered the upper bound from 3 to 2.
            yield += world.random.nextIntBetweenInclusive(1, 2)
            treeEntity.decrementMulchDuration(world, pos, state)
        }
        val event = BerryYieldCalculationEvent(this, world, state, pos, placer, yield, bonus.second)
        CobblemonEvents.BERRY_YIELD.post(event) { yield = it.yield }
        return yield
    }

    /**
     * Calculates and returns the minimum possible yield by summing [baseYield] and [growthFactors] minimum values.
     *
     * @return The minimum possible yield.
     */
    fun minYield() = this.baseYield.first + this.growthFactors.sumOf { it.minYield() }

    /**
     * Calculates and returns the maximum possible yield by summing [baseYield] and [growthFactors] max values.
     *
     * @return The maximum possible yield.
     */
    fun maxYield() = this.baseYield.last + this.growthFactors.sumOf { it.maxYield() }

    /**
     * Checks if this berry can mutate with the given [partner].
     * For the berry the mutations may produce use [mutationWith].
     *
     * @param partner The [Berry] being checked as a possible mutation partner.
     * @return If a mutation is possible.
     */
    fun canMutateWith(partner: Berry) = this.mutationWith(partner) != null

    /**
     * Query for the possible mutation with the given [partner].
     *
     * @param partner The [Berry] being mutated with.
     * @return The resulting [Berry] if the mutation is possible.
     */
    fun mutationWith(partner: Berry): Berry? {
        val berryIdentifier = this.mutations[partner.identifier] ?: return null
        return Berries.getByIdentifier(berryIdentifier)
    }

    /**
     * Query for a possible partner to mutate into the given [resulting].
     *
     * @param resulting The [Berry] the mutation should result in.
     * @return The [Berry] that mutates with this berry into the given [resulting] if possible.
     */
    fun partnerForMutation(resulting: Berry): Berry? {
        return this.mutations.firstNotNullOfOrNull { (partner, result) ->
            if (result == resulting.identifier)
                Berries.getByIdentifier(partner)
            else
                null
        }
    }

    // A cheat since gson doesn't invoke init block
    internal fun validate() {
        if (this.baseYield.first < 0 || this.baseYield.last < 0) {
            throw IllegalArgumentException("A berry base yield must be a positive range")
        }
        if (this.growthTime.first < 0 || this.growthTime.last < 0) {
            throw IllegalArgumentException("The growth time must be a positive range")
        }
        if (this.refreshRate.first < 0 || this.refreshRate.last < 0) {
            throw IllegalArgumentException("The refresh rate must be a positive range")
        }
        /*
        if (this.lifeCycles.first < 0 || this.lifeCycles.last < 0) {
            throw IllegalArgumentException("A berry life cycle must be a positive range")
        }
         */
        this.growthFactors.forEach { it.validateArguments() }
        val maxYield = this.maxYield()
        if (this.growthPoints.size < maxYield) {
            throw IllegalArgumentException("Anchor points must have enough elements for the max possible yield of $maxYield you've provided ${this.growthPoints.size} points")
        }
        this.shapedFlower = hashMapOf()
        this.shapedFruit = hashMapOf()
        this.sproutShape = this.createAndUniteShapes(this.sproutShapeBoxes)
        this.matureShape = this.createAndUniteShapes(this.matureShapeBoxes)
    }

    internal fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.identifier)
        buffer.writeInt(this.baseYield.first)
        buffer.writeInt(this.baseYield.last)
        buffer.writeEnumSet(favoriteMulches, MulchVariant::class.java)
        //buffer.writeInt(this.lifeCycles.first)
        //buffer.writeInt(this.lifeCycles.last)
        buffer.writeInt(this.growthTime.first)
        buffer.writeInt(this.growthTime.last)
        buffer.writeInt(this.refreshRate.first)
        buffer.writeInt(this.refreshRate.last)
        buffer.writeCollection(this.growthPoints.toList())  { writer, value ->
            writer.writeDouble(value.position.x)
            writer.writeDouble(value.position.y)
            writer.writeDouble(value.position.z)
            writer.writeDouble(value.rotation.x)
            writer.writeDouble(value.rotation.y)
            writer.writeDouble(value.rotation.z)
        }
        buffer.writeBoolean(this.randomizedGrowthPoints)
        buffer.writeMap(this.mutations, { writer, key -> writer.writeIdentifier(key) }, { writer, value -> writer.writeIdentifier(value) })
        buffer.writeCollection(this.sproutShapeBoxes) { writer, value ->
            writer.writeBox(value)
        }
        buffer.writeCollection(this.matureShapeBoxes) { writer, value ->
            writer.writeBox(value)
        }
        buffer.writeMap(this.flavors, { writer, key -> writer.writeEnumConstant(key) }, { writer, value -> writer.writeInt(value) })
        buffer.writeMap(this.tintIndexes, { writer, key -> writer.writeInt(key) }, { writer, value -> writer.writeInt(value.rgb) })
        buffer.writeIdentifier(this.flowerModelIdentifier)
        buffer.writeIdentifier(this.flowerTexture)
        buffer.writeIdentifier(this.fruitModelIdentifier)
        buffer.writeIdentifier(this.fruitTexture)
        buffer.writeDouble(stageOnePositioning.position.x)
        buffer.writeDouble(stageOnePositioning.position.y)
        buffer.writeDouble(stageOnePositioning.position.z)
        buffer.writeDouble(stageOnePositioning.rotation.x)
        buffer.writeDouble(stageOnePositioning.rotation.y)
        buffer.writeDouble(stageOnePositioning.rotation.z)
        buffer.writeFloat(boneMealChance)
    }

    /**
     * Calculates the bonus yield for the berry tree.
     *
     * @param world The [World] the tree is present in.
     * @param state The [BlockState] of the tree.
     * @param pos The [BlockPos] of the tree.
     * @return The bonus yield, the growth factors that passed.
     */
    private fun bonusYield(world: Level, state: BlockState, pos: BlockPos): Pair<Int, Collection<GrowthFactor>> {
        var bonus = 0
        val passed = arrayListOf<GrowthFactor>()
        val treeEntity = world.getBlockEntity(pos) as? BerryBlockEntity ?: return 0 to passed
        val mulchVariant = BerryBlock.getMulch(treeEntity)
        val hasBiomeMulch = mulchVariant in favoriteMulches
        this.growthFactors.forEach { factor ->
            if (factor.isValid(world, state, pos)) {
                bonus += factor.yield()
                passed += factor
            } else if (hasBiomeMulch) {
                bonus += factor.yield()
            }
        }
        return bonus to passed
    }

    private fun createAndUniteShapes(boxes: Collection<AABB>): VoxelShape {
        var shape: VoxelShape? = null
        boxes.forEach { box ->
            val outerShape = shape // Kotlin is complaining about nullability of `shape` in the Shapes.or call

            shape = if (outerShape == null) {
                Block.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
            } else {
                Shapes.or(outerShape, Block.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ))
            }
        }
        return shape ?: Shapes.block()
    }

    companion object {

        internal fun decode(buffer: RegistryFriendlyByteBuf): Berry {
            val identifier = buffer.readIdentifier()
            val baseYield = IntRange(buffer.readInt(), buffer.readInt())
            val favMulchs = buffer.readEnumSet(MulchVariant::class.java)
            //val lifeCycles = IntRange(buffer.readInt(), buffer.readInt())
            val growthTime = IntRange(buffer.readInt(), buffer.readInt())
            val refreshRate = IntRange(buffer.readInt(), buffer.readInt())
            val growthPoints = buffer.readList { reader ->
                GrowthPoint(Vec3(reader.readDouble(), reader.readDouble(), reader.readDouble()), Vec3(reader.readDouble(), reader.readDouble(), reader.readDouble()))
            }.toTypedArray()
            val randomizedGrowthPoints = buffer.readBoolean()
            val mutations = buffer.readMap({ reader -> reader.readIdentifier() }, { reader -> reader.readIdentifier() })
            val sproutShapeBoxes = buffer.readList { it.readBox() }
            val matureShapeBoxes = buffer.readList { it.readBox() }
            val flavors = buffer.readMap({ reader -> reader.readEnumConstant(Flavor::class.java) }, { reader -> reader.readInt() })
            val tintIndexes = buffer.readMap({ reader -> reader.readInt() }, { reader -> Color(reader.readInt()) })
            val flowerModelIdentifier = buffer.readIdentifier()
            val flowerTexture = buffer.readIdentifier()
            val fruitModelIdentifier = buffer.readIdentifier()
            val fruitTexture = buffer.readIdentifier()
            val stageOneYPos = GrowthPoint(Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()))
            val boneMealChance = buffer.readFloat()
            return Berry(identifier, baseYield, emptyList(), growthTime, refreshRate, favMulchs, emptySet(), emptyList(), growthPoints, randomizedGrowthPoints, mutations, sproutShapeBoxes, matureShapeBoxes, flavors, tintIndexes, flowerModelIdentifier, flowerTexture, fruitModelIdentifier, fruitTexture, stageOneYPos, 0F, boneMealChance)
        }

    }

}