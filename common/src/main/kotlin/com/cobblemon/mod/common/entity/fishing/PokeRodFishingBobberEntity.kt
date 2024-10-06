/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.fishing

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.ModAPI
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.client.sound.EntitySoundTracker
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.toBlockPos
import kotlin.math.sqrt
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.FluidTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3


class PokeRodFishingBobberEntity(type: EntityType<out PokeRodFishingBobberEntity>, world: Level) : FishingHook(type, world) {

    private val velocityRandom = RandomSource.create()
    private var caughtFish = false
    private var outOfOpenWaterTicks = 0
    private var removalTimer = 0
    private var hookCountdown = 0
    private var waitCountdown = 0
    private var fishTravelCountdown = 0
    private var fishAngle = 0f
    var inOpenWater = true
    private var hookedEntity: Entity? = null
    var state = State.FLYING
    private var luckOfTheSeaLevel = 0
    private var lureLevel = 0
    private var typeCaught = TypeCaught.ITEM
    private var chosenBucket = Cobblemon.bestSpawner.config.buckets[0] // default to first rarity bucket
    private val pokemonSpawnChance = 85 // chance a Pokemon will be fished up % out of 100
    private val castingSound = CobblemonSounds.FISHING_ROD_CAST
    var pokeRodId: ResourceLocation? = null
    var lineColor: String = "000000" // default line color is black
    var usedRod: ResourceLocation? = null
    var bobberBait: ItemStack = ItemStack.EMPTY
    var isCast = false
    var lastSpinAngle: Float = 0f
    var randomPitch: Float = 0f
    var randomYaw: Float = 0f
    var lastBobberPos: Vec3? = null
    var rodItem: ItemStack? = null

    constructor(thrower: Player, pokeRodId: ResourceLocation, bait: ItemStack, world: Level, luckOfTheSea: Int, lure: Int, rodItemStack: ItemStack) : this(CobblemonEntities.POKE_BOBBER, world) {
        owner = thrower
        rodItem = rodItemStack
        luckOfTheSeaLevel = luckOfTheSea
        lureLevel = lure
        this.pokeRodId = pokeRodId
        this.bobberBait = bait
        entityData.set(POKEROD_ID, pokeRodId.toString())
        entityData.set(POKEBOBBER_BAIT, bobberBait)
        entityData.set(HOOK_ENTITY_ID, 0)
        entityData.set(CAUGHT_FISH, false)

        this.usedRod = pokeRodId

        val throwerPitch = thrower.xRot
        val throwerYaw = thrower.yRot
        val cosYaw = Mth.cos(-throwerYaw * 0.017453292f - 3.1415927f)
        val sinYaw = Mth.sin(-throwerYaw * 0.017453292f - 3.1415927f)
        val cosPitch = -Mth.cos(-throwerPitch * 0.017453292f)
        val sinPitch = Mth.sin(-throwerPitch * 0.017453292f)
        val posX = thrower.x - sinYaw.toDouble() * 0.3
        val posY = thrower.eyeY
        val posZ = thrower.z - cosYaw.toDouble() * 0.3
        this.moveTo(posX, posY, posZ, throwerYaw, throwerPitch)
        var vec3d = Vec3((-sinYaw).toDouble(), Mth.clamp(-(sinPitch / cosPitch), -5.0f, 5.0f).toDouble(), (-cosYaw).toDouble())
        val m = vec3d.length()
        vec3d = vec3d.multiply(0.6 / m + random.triangle(0.5, 0.0103365), 0.6 / m + random.triangle(0.5, 0.0103365), 0.6 / m + random.triangle(0.5, 0.0103365))
        deltaMovement = vec3d
        yRot = (Mth.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        xRot = (Mth.atan2(vec3d.y, vec3d.horizontalDistance()) * 57.2957763671875).toFloat()
        yRotO = yRot
        xRotO = xRot
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(HOOK_ENTITY_ID, 0)
        builder.define(CAUGHT_FISH, false)
        builder.define(POKEROD_ID, "")
        builder.define(POKEBOBBER_BAIT, ItemStack.EMPTY)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        if (HOOK_ENTITY_ID == data) {
            val i = entityData.get(HOOK_ENTITY_ID) as Int
            this.hookedEntity = if (i > 0) level().getEntity(i - 1) else null
        }

        if (CAUGHT_FISH == data) {
            this.caughtFish = (entityData.get(CAUGHT_FISH) as Boolean)
            if (this.caughtFish) {
                this.setDeltaMovement(deltaMovement.x, (-0.4f * Mth.nextFloat(this.velocityRandom, 0.3f, 0.5f)).toDouble(), deltaMovement.z)
            }
        }

        super.onSyncedDataUpdated(data)
    }

    fun calculateMinMaxCountdown(weight: Float): Pair<Int, Int> {
        // Constants for the target min and max values at weight extremes
        val minAtMaxWeight = 20
        val maxAtMaxWeight = 40
        val minAtMinWeight = 15
        val maxAtMinWeight = 20

        // Calculate factors for min and max based on the weight
        val minFactor = ((minAtMaxWeight - minAtMinWeight) / 100f) * weight + minAtMinWeight
        val maxFactor = ((maxAtMaxWeight - maxAtMinWeight) / 100f) * weight + maxAtMinWeight

        // Ensure min and max are within the needed bounds
        val min = minFactor.toInt().coerceIn(minAtMinWeight, minAtMaxWeight)
        val max = maxFactor.toInt().coerceIn(maxAtMinWeight, maxAtMaxWeight)

        return Pair(min, max)
    }

    fun chooseAdjustedSpawnBucket(buckets: List<SpawnBucket>, luckOfTheSeaLevel: Int): SpawnBucket {
        val baseIncreases = listOf(5.0F, 1.0F, 0.2F)  // Base increases for the first three buckets beyond the first
        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index == 0) {
                // Placeholder, will be recalculated
                0.0F
            } else {
                val increase = if (index < baseIncreases.size) baseIncreases[index] else baseIncreases.last() + (index - baseIncreases.size + 1) * 0.15F
                bucket.weight + increase * luckOfTheSeaLevel
            }
        }.toMutableList()

        // Recalculate the first bucket's weight to ensure the total is 100%
        val totalAdjustedWeight = adjustedWeights.sum() - adjustedWeights[0]  // Corrected to ensure the list contains Floats
        adjustedWeights[0] = 100.0F - totalAdjustedWeight + buckets[0].weight

        // Random selection based on adjusted weights
        val weightSum = adjustedWeights.sum()
        val chosenSum = kotlin.random.Random.nextDouble(weightSum.toDouble()).toFloat()  // Ensure usage of Random from kotlin.random package
        var sum = 0.0F
        adjustedWeights.forEachIndexed { index, weight ->
            sum += weight
            if (sum >= chosenSum) {
                return buckets[index]
            }
        }

        return buckets.first()  // Fallback
    }

    fun isOpenOrWaterAround(pos: BlockPos): Boolean {
        var positionType = PositionType.INVALID
        for (i in -1..2) {
            val positionType2 = this.getPositionType(pos.offset(-2, i, -2), pos.offset(2, i, 2))
            when (positionType2) {
                PositionType.INVALID -> return false
                PositionType.ABOVE_WATER -> if (positionType == PositionType.INVALID) {
                    return false
                }

                PositionType.INSIDE_WATER -> if (positionType == PositionType.ABOVE_WATER) {
                    return false
                }

                else -> return false
            }
            positionType = positionType2
        }
        return true
    }

    private fun getPositionType(start: BlockPos, end: BlockPos): PositionType? {
        return BlockPos.betweenClosedStream(start, end)
                .map { pos -> this.getPositionType(pos) }
                .reduce { positionType, positionType2 ->
                    if (positionType == positionType2) positionType else PositionType.INVALID
                }.orElse(PositionType.INVALID)
    }

    private fun getPositionType(pos: BlockPos): PositionType {
        val blockState = level().getBlockState(pos)
        return if (!blockState.isAir && !blockState.`is`(Blocks.LILY_PAD)) {
            val fluidState = blockState.fluidState
            if (fluidState.`is`(FluidTags.WATER) && fluidState.isSource && blockState.getCollisionShape(level(), pos).isEmpty) PositionType.INSIDE_WATER else PositionType.INVALID
        } else {
            PositionType.ABOVE_WATER
        }
    }

    enum class PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    private fun setPlayerFishHook(fishingBobber: FishingHook?) {
        val playerEntity = this.playerOwner
        if (playerEntity != null) {
            playerEntity.fishing = fishingBobber
        }
    }

    enum class State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }

    // todo maybe custom behavior for fishing logic
    private fun tickFishingLogic(pos: BlockPos) {
        val serverWorld = level() as ServerLevel
        var i = 1
        val blockPos = pos.above()

        if (random.nextFloat() < 0.25f && level().isRainingAt(blockPos)) {
            ++i
        }
        if (random.nextFloat() < 0.5f && !level().canSeeSky(blockPos)) {
            --i
        }
        if (this.hookCountdown > 0) {
            --this.hookCountdown
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0
                this.fishTravelCountdown = 0
                //this.CAUGHT_FISH = false
                entityData.set(CAUGHT_FISH, false)
            }
        } else if (this.fishTravelCountdown > 0) { // create a fish trail that leads to the bobber visually
            this.fishTravelCountdown -= i
            if (this.fishTravelCountdown > 0) {
                this.fishAngle += random.triangle(0.0, 9.188).toFloat()
                val f = this.fishAngle * (Math.PI.toFloat() / 180)
                val g = Mth.sin(f)
                val h = Mth.cos(f)
                val offsetX = this.x + (g * this.fishTravelCountdown.toFloat() * 0.1f).toDouble()
                val offsetY = (Mth.floor(this.y).toFloat() + 1.0f).toDouble()
                val j = this.z + (h * this.fishTravelCountdown.toFloat() * 0.1f).toDouble()
                val blockState = serverWorld.getBlockState(BlockPos.containing(offsetX, offsetY - 1.0, j))
                //val blockState = serverWorld.getBlockState(BlockPos.containing(offsetX, (Mth.floor(this.y).toFloat() + 1.0f).toDouble().also { offsetY = it } - 1.0, this.z + (h * this.fishTravelCountdown.toFloat() * 0.1f).toDouble().also { j = it }))
                if (blockState.`is`(Blocks.WATER)) {
                    if (random.nextFloat() < 0.15f) {
                        // random bubble particles that spawn around
                        //serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.x, this.y, this.z, 3, g.toDouble(), 0.1, h.toDouble(), 0.0)
                        serverWorld.sendParticles(ParticleTypes.BUBBLE, offsetX, offsetY - 0.1, j, 1, g.toDouble(), 0.1, h.toDouble(), 0.0)
                    }
                    val k = g * 0.04f
                    val l = h * 0.04f

                    // todo the fish trail that leads to the bobber
                    serverWorld.sendParticles(ParticleTypes.FISHING, offsetX, offsetY, j, 0, l.toDouble(), 0.01, -k.toDouble(), 1.0)
                    serverWorld.sendParticles(ParticleTypes.FISHING, offsetX, offsetY, j, 0, -l.toDouble(), 0.01, k.toDouble(), 1.0)
                    // create tiny splash particles for fishing trail
                    //particleEntityHandler(this, Identifier.of("cobblemon","bob_splash"))
                }
            } else {
                //playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f)
                // play bobber hook notification sound
                level().playSound(null, this.blockPosition(), CobblemonSounds.FISHING_NOTIFICATION, SoundSource.BLOCKS, 1.0F, 1.0F)

                // create tiny splash particle when there is a bite
                particleEntityHandler(this, ResourceLocation.fromNamespaceAndPath("cobblemon", "bob_splash"))

                val m = this.y + 0.5
                serverWorld.sendParticles(ParticleTypes.BUBBLE, this.x, m, this.z, (1.0f + this.bbWidth * 20.0f).toInt(), this.bbWidth.toDouble(), 0.0, this.bbWidth.toDouble(), 0.2)
                serverWorld.sendParticles(ParticleTypes.FISHING, this.x, m, this.z, (1.0f + this.bbWidth * 20.0f).toInt(), this.bbWidth.toDouble(), 0.0, this.bbWidth.toDouble(), 0.2)

                // check for chance to catch pokemon based on the bait
                if (Mth.nextInt(random, 0, 100) < getPokemonSpawnChance(bobberBait)) {
                    this.typeCaught = TypeCaught.POKEMON

                    val buckets = Cobblemon.bestSpawner.config.buckets

                    // choose a spawn bucket according to weights no matter how many there are
                    chosenBucket = chooseAdjustedSpawnBucket(buckets, luckOfTheSeaLevel)
                    val reactionMinMax = calculateMinMaxCountdown(chosenBucket.weight)

                    // set the hook reaction time to be based off the rarity of the bucket chosen
                    this.hookCountdown = Mth.nextInt(random, reactionMinMax.first, reactionMinMax.second)
                }
                else {
                    // todo caught item
                    this.typeCaught = TypeCaught.ITEM
                    this.hookCountdown = Mth.nextInt(random, 20, 40)

                }
                entityData.set(CAUGHT_FISH, true)
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i
            var f = 0.15f
            if (this.waitCountdown < 20) {
                f += (20 - this.waitCountdown).toFloat() * 0.05f
            } else if (this.waitCountdown < 40) {
                f += (40 - this.waitCountdown).toFloat() * 0.02f
            } else if (this.waitCountdown < 60) {
                f += (60 - this.waitCountdown).toFloat() * 0.01f
            }
            if (random.nextFloat() < f) {
                val g = Mth.nextFloat(this.random, 0.0f, 360.0f) * 0.017453292f
                val h = Mth.nextFloat(this.random, 25.0f, 60.0f)
                val d = this.x + (Mth.sin(g) * h).toDouble() * 0.1 // X
                val e = (Mth.floor(this.y).toFloat() + 1.0f).toDouble() // Y
                val j = this.z + (Mth.cos(g) * h).toDouble() * 0.1 // randomized Z value
                val blockState = serverWorld.getBlockState(BlockPos.containing(d, e - 1.0, j))
                if (blockState.`is`(Blocks.WATER)) {
                    serverWorld.sendParticles(ParticleTypes.SPLASH, d, e, j, 2 + random.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0)
                }
            }
            if (this.waitCountdown <= 0) {
                this.fishAngle = Mth.nextFloat(random, 0.0f, 360.0f)
                this.fishTravelCountdown = Mth.nextInt(random, 20, 80)
            }
        } else {
            if (isCast != true) {
                // When bobber lands on the water for the first time
                level().playSound(null, this.blockPosition(), CobblemonSounds.FISHING_BOBBER_LAND, SoundSource.NEUTRAL, 1.0F, 1.0F)

                // create tiny splash particle
                particleEntityHandler(this, ResourceLocation.fromNamespaceAndPath("cobblemon", "bob_splash"))

                isCast = true
            }

            // set the time it takes to wait for a hooked item or pokemon
            this.waitCountdown = Mth.nextInt(random, 100, 600)
            this.waitCountdown -= this.lureLevel * 20 * 5

            if (this.waitCountdown < 0)
                this.waitCountdown = 0
            else {
                // check for the bait on the hook and see if the waitCountdown is reduced
                if (checkReduceBiteTime(bobberBait))
                    this.waitCountdown = alterBiteTimeAttempt(this.waitCountdown, this.bobberBait)
            }
        }
    }

    fun stopCastingAudio () {
        if (!this.level().isClientSide) return
        owner?.let { EntitySoundTracker.stop(it.id, this.castingSound.location) }
    }

    // client instantiation
    override fun recreateFromPacket(clientboundAddEntityPacket: ClientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket)
        val owner = this.owner
        if (this.state == State.FLYING && owner != null) {  // starts casting sound when instantiated on client
            val rand = this.level().random
            val toPlay = EntityBoundSoundInstance(this.castingSound, SoundSource.PLAYERS, 1.0F, 1.0f / (rand.nextFloat() * 0.4f + 0.8f), owner, rand.nextLong())
            EntitySoundTracker.play(owner.id, toPlay)
        }
    }

    // client destruction
    override fun onClientRemoval() {
        stopCastingAudio()
        super.onClientRemoval()
    }

    private fun removeIfInvalid(player: Player): Boolean {
        val itemStack = player.mainHandItem
        val itemStack2 = player.offhandItem
        val bl = BuiltInRegistries.ITEM[this.usedRod] == itemStack.item //(itemStack.item is PokerodItem) // todo make this work again so the line breaks when you swap items
        val bl2 = BuiltInRegistries.ITEM[this.usedRod] == itemStack2.item //(itemStack2.item is PokerodItem) // todo make this work again so the line breaks when you swap items
        if (player.isRemoved || !player.isAlive || !bl && !bl2 || this.distanceToSqr(player) > 1024.0) {
            discard()
            isCast = false
            return true
        }
        return false
    }

    private fun checkForCollision() {
        // todo (techdaan): ensure getHitResultOnMoveVector is correct
        val hitResult = ProjectileUtil.getHitResultOnMoveVector(this) { entity: Entity -> this.canHitEntity(entity) }
        onHit(hitResult)
    }

    override fun onHitEntity(entityHitResult: EntityHitResult) {
        if (!level().isClientSide) {
            this.updateHookedEntityId(entityHitResult.entity)
        }
    }

    private fun updateHookedEntityId(entity: Entity?) {
        this.hookedEntity = entity
        entityData.set(HOOK_ENTITY_ID, if (entity == null) 0 else entity.id + 1)
    }

    override fun tick() {
        velocityRandom.setSeed(getUUID().leastSignificantBits xor level().gameTime)
        val playerEntity = this.playerOwner
        if (playerEntity == null) {
            discard()
        } else if (level().isClientSide || !removeIfInvalid(playerEntity)) {
            if (this.onGround()) {
                ++removalTimer
                if (removalTimer >= 1200) {
                    discard()
                    return
                }
            } else {
                removalTimer = 0
            }
            var f = 0.0f
            val blockPos = blockPosition()
            val fluidState = level().getFluidState(blockPos)
            if (fluidState.`is`(FluidTags.WATER)) {
                f = fluidState.getHeight(level(), blockPos)
            }
            val bl = f > 0.0f

            // pause audio if the bobber is not moving anymore
            if (lastBobberPos == position()) {
                // stop audio for the rod casting if the position has not changed
                stopCastingAudio()
            }
            lastBobberPos = position()

            if (state == State.FLYING) {
                if (hookedEntity != null) {
                    deltaMovement = Vec3.ZERO
                    state = State.HOOKED_IN_ENTITY
                    return
                }
                if (bl) {
                    deltaMovement = deltaMovement.multiply(0.3, 0.2, 0.3)
                    state = State.BOBBING
                    return
                }
                checkForCollision()
            } else {
                if (state == State.HOOKED_IN_ENTITY) {
                    if (hookedEntity != null) {
                        if (!hookedEntity!!.isRemoved && hookedEntity!!.level().dimension() === level().dimension()) {
                            this.setPos(hookedEntity!!.x, hookedEntity!!.getY(0.8), hookedEntity!!.z)
                        } else {
                            updateHookedEntityId(null as Entity?)
                            state = State.FLYING
                        }
                    }
                    return
                }
                if (state == State.BOBBING) {
                    // stop casting audio once it lands in water
                    stopCastingAudio()

                    val vec3d = deltaMovement
                    var d = this.y + vec3d.y - blockPos.y.toDouble() - f.toDouble()
                    if (Math.abs(d) < 0.01) {
                        d += Math.signum(d) * 0.1
                    }
                    this.setDeltaMovement(vec3d.x * 0.9, vec3d.y - d * random.nextFloat().toDouble() * 0.2, vec3d.z * 0.9)
                    inOpenWater = if (hookCountdown <= 0 && fishTravelCountdown <= 0) {
                        true
                    } else {
                        inOpenWater && outOfOpenWaterTicks < 10 && isOpenOrWaterAround(blockPos)
                    }
                    if (bl) {
                        outOfOpenWaterTicks = Math.max(0, outOfOpenWaterTicks - 1)
                        if (caughtFish) {
                            deltaMovement = deltaMovement.add(0.0, -0.1 * velocityRandom.nextFloat().toDouble() * velocityRandom.nextFloat().toDouble(), 0.0)
                        }
                        if (!level().isClientSide) {
                            tickFishingLogic(blockPos)
                        }
                    } else {
                        outOfOpenWaterTicks = Math.min(10, outOfOpenWaterTicks + 1)
                    }
                }
            }
            if (!fluidState.`is`(FluidTags.WATER)) {
                deltaMovement = deltaMovement.add(0.0, -0.03, 0.0)
            }
            move(MoverType.SELF, deltaMovement)
            this.updateRotation()
            if (state == State.FLYING && (this.onGround() || horizontalCollision)) {
                deltaMovement = Vec3.ZERO
            }
            deltaMovement = deltaMovement.scale(0.92)
            reapplyPosition()
        }
    }

    override fun retrieve(usedItem: ItemStack): Int {
        // when reelimg in prematurely stop casting audio if it is playing
        stopCastingAudio()

        val playerEntity = this.playerOwner
        isCast = false

        return if (!level().isClientSide && playerEntity != null && !removeIfInvalid(playerEntity)) {
            isCast = false
            var i = 0
            if (this.hookedEntity != null) {
                pullEntity(this.hookedEntity!!)
                (playerEntity as ServerPlayer?)?.let { CriteriaTriggers.FISHING_ROD_HOOKED.trigger(it, usedItem, this, emptyList()) }
                level().broadcastEntityEvent(this, 31.toByte())
                i = if (this.hookedEntity is ItemEntity) 3 else 5
            } else if (this.hookCountdown > 0) {
                // check if thing caught was an item
                if (this.typeCaught == TypeCaught.ITEM) {
                    val owner = owner
                    if (owner != null) {
                        val lootContextParameterSet = LootParams.Builder(level() as ServerLevel)
                            .withParameter(LootContextParams.ORIGIN, position())
                            .withParameter(LootContextParams.TOOL, usedItem)
                            .withParameter(LootContextParams.THIS_ENTITY, this)
                            .also { if (Cobblemon.implementation.modAPI != ModAPI.FABRIC) it.withParameter(LootContextParams.ATTACKING_ENTITY, owner) }
                            .create(LootContextParamSets.FISHING)
                        val lootTable = level().server!!.reloadableRegistries().getLootTable(LOOT_TABLE_ID)
                        val list: List<ItemStack> = lootTable.getRandomItems(lootContextParameterSet)
                        CriteriaTriggers.FISHING_ROD_HOOKED.trigger(playerEntity as ServerPlayer, usedItem, this, list)
                        val var7: Iterator<*> = list.iterator()
                        while (var7.hasNext()) {
                            val itemStack = var7.next() as ItemStack
                            val itemEntity = ItemEntity(level(), this.x, this.y, this.z, itemStack)
                            val d = playerEntity.getX() - this.x
                            val e = playerEntity.getY() - this.y
                            val f = playerEntity.getZ() - this.z
                            itemEntity.setDeltaMovement(
                                d * 0.1,
                                e * 0.1 + sqrt(sqrt(d * d + e * e + f * f)) * 0.08,
                                f * 0.1
                            )
                            level().addFreshEntity(itemEntity)
                            playerEntity.level().addFreshEntity(
                                ExperienceOrb(
                                    playerEntity.level(),
                                    playerEntity.getX(),
                                    playerEntity.getY() + 0.5,
                                    playerEntity.getZ() + 0.5,
                                    random.nextInt(6) + 1
                                )
                            )
                            if (itemStack.`is`(ItemTags.FISHES)) {
                                playerEntity.awardStat(Stats.FISH_CAUGHT, 1)
                            }
                        }
                        i = 1
                    }
                } else { // logic for spawning Pokemon using rarity
                    val bobberOwner = playerOwner as ServerPlayer

                    // decrememnt the bait count on the rod itself when reeling in a pokemon
                    PokerodItem.consumeBait(rodItem!!)

                    // spawn the pokemon from the chosen bucket at the bobber's location
                    spawnPokemonFromFishing(bobberOwner, chosenBucket, bobberBait)

                    val serverWorld = level() as ServerLevel

                    val g = Mth.nextFloat(random, 0.0f, 360.0f) * (Math.PI.toFloat() / 180)
                    val h = Mth.nextFloat(random, 25.0f, 60.0f)
                    val partX = this.x + (Mth.sin(g) * h).toDouble() * 0.1
                    serverWorld.sendParticles(ParticleTypes.SPLASH, partX, this.y, this.z, 6 + random.nextInt(4), 0.0, 0.2, 0.0, 0.0)

                    playerEntity.level().addFreshEntity(ExperienceOrb(playerEntity.level(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, random.nextInt(6) + 1))
                }
            }
            if (this.onGround()) {
                i = 2
            }
            discard()
            i
        } else {
            isCast = false
            0
        }
    }

    // calculate the trajectory for the reeled in pokemon
    fun lobPokemonTowardsTarget(player: Player, entity: Entity) {
        val rad = Math.toRadians(player.yRot.toDouble())
        val targetDirection = Vec3(-Math.sin(rad), 0.0, Math.cos(rad))
        val targetPos = player.position().add(targetDirection.scale(5.0))

        val delta = targetPos.subtract(entity.position())
        val horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z)

        // Introduce a damping factor that reduces the velocity as the distance increases
        val dampingFactor = 1 - (horizontalDistance / 80).coerceIn(0.0, 0.8) // increase end of coerceIn to dampen more

        val verticalVelocity = 0.30 // Base vertical velocity for a gentle arc
        val horizontalVelocityFactor = 0.13 // Base horizontal velocity factor

        // Apply the damping factor to both horizontal and vertical velocities
        val adjustedHorizontalVelocity = horizontalDistance * horizontalVelocityFactor * dampingFactor
        val adjustedVerticalVelocity = (verticalVelocity + (horizontalDistance * 0.05)) * dampingFactor

        // Calculate the final velocities
        val velocityX = delta.x / horizontalDistance * adjustedHorizontalVelocity
        val velocityZ = delta.z / horizontalDistance * adjustedHorizontalVelocity
        val velocityY = adjustedVerticalVelocity

        val tossVelocity = Vec3(velocityX, velocityY, velocityZ)
        entity.deltaMovement = tossVelocity
    }

    fun spawnPokemonFromFishing(player: Player, chosenBucket: SpawnBucket, bobberBait: ItemStack) {
        var hookedEntityID: Int? = null
        
        val spawner = BestSpawner.fishingSpawner

        val spawnCause = FishingSpawnCause(
            spawner = spawner,
            bucket = chosenBucket,
            entity = player,
            rodStack = player.mainHandItem // Crab, you should probably parse in the rod item connected to the bobber so we can check enchants in spawn conditions
        )

        val result = spawner.run(spawnCause, level() as ServerLevel, position().toBlockPos())

        if (result == null) {
        // This has a chance to fail, if the position has no suitability for a fishing context
        //  it could also just be a miss which
        //   means two attempts to spawn in the same location can have differing results (which is expected for
        //   randomness).
            player.sendSystemMessage("Not even a nibble".red())
        }

        var spawnedPokemon: PokemonEntity? = null
        val resultingSpawn = result?.get()

        if (resultingSpawn is EntitySpawnResult) {
            for (entity in resultingSpawn.entities) {
                //pokemon.uuid = it.uuid
                hookedEntityID = entity.id
                spawnedPokemon = (entity as PokemonEntity)

                // create accessory splash particle when you fish something up
                particleEntityHandler(this, ResourceLocation.fromNamespaceAndPath("cobblemon","accessory_fish_splash"))

                if (spawnedPokemon.pokemon.species.weight.toDouble() < 900.0) { // if weight value of Pokemon is less than 200 lbs (in hectograms) which we store weight as) then reel it in to the player
                    // play sound for small splash when this weight class is fished up
                    level().playSound(null, this.blockPosition(), CobblemonSounds.FISHING_SPLASH_SMALL, SoundSource.BLOCKS, 1.0F, 1.0F)

                    // create small splash particle for small pokemon
                    particleEntityHandler(this, ResourceLocation.fromNamespaceAndPath("cobblemon","small_fish_splash"))

                    // Example of applying the new velocity
                    lobPokemonTowardsTarget(player, entity)
                }
                else { // it is a big lad and you cannot reel it in
                    // create big splash particle for large pokemon
                    particleEntityHandler(this, ResourceLocation.fromNamespaceAndPath("cobblemon","big_fish_splash"))

                    level().playSound(null, this.blockPosition(), CobblemonSounds.FISHING_SPLASH_BIG, SoundSource.BLOCKS, 1.0F, 1.0F)

                }
            }
        }

        if (hookedEntityID != null) {
            hookedEntity = level().getEntity(hookedEntityID)
        }

        if (spawnedPokemon != null) {
            BattleBuilder.pve((player as ServerPlayer), spawnedPokemon).ifErrored { it.sendTo(player) { it.red() } }
        }
    }

    fun checkBaitSuccessRate(successChance: Double): Boolean {
        return Math.random() <= successChance
    }


    // function to return true of false if the given bait affects time to expect a bite
    fun checkReduceBiteTime(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromBaitItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.BITE_TIME }
    }

    // function to return true of false if the given bait to make it so a Pokemon is always reeled in
    fun checkPokemonFishRate(stack: ItemStack): Boolean {
        val bait = FishingBaits.getFromBaitItemStack(stack) ?: return false
        return bait.effects.any { it.type == FishingBait.Effects.POKEMON_CHANCE }
    }

    // check if the bite time is reduced based on the bait bonus
    fun alterBiteTimeAttempt(waitCountdown: Int, stack: ItemStack): Int {
        val bait = FishingBaits.getFromBaitItemStack(stack) ?: return waitCountdown
        val effect = bait.effects.filter { it.type == FishingBait.Effects.BITE_TIME }.random()
        if (!checkBaitSuccessRate(effect.chance)) return waitCountdown
        return if (waitCountdown - waitCountdown * (effect.value) <= 0)
            1 // return min value
        else
            (waitCountdown - waitCountdown * (effect.value)).toInt()
    }

    // check the chance of a pokemon to spawn and if it is affected by bait
    fun getPokemonSpawnChance(stack: ItemStack): Int {
        val bait = FishingBaits.getFromBaitItemStack(stack) ?: return this.pokemonSpawnChance
        val effectList = bait.effects.filter { it.type == FishingBait.Effects.POKEMON_CHANCE }
        if (effectList.isEmpty()) return this.pokemonSpawnChance
        val effect = effectList.random()
        return if (effect.chance >= 0 && effect.chance <= 100) {
            ((effect.chance) * 100).toInt()
        } else this.pokemonSpawnChance
    }

    // Particle Stuff
    private fun particleEntityHandler(entity: Entity, particle: ResourceLocation) {
        val spawnSnowstormParticlePacket = SpawnSnowstormParticlePacket(particle, entity.position())
        spawnSnowstormParticlePacket.sendToPlayersAround(entity.x, entity.y, entity.z, 64.0, entity.level().dimension())
    }

    enum class TypeCaught {
        ITEM,
        POKEMON
    }

    companion object {
        val POKEROD_ID = SynchedEntityData.defineId(PokeRodFishingBobberEntity::class.java, EntityDataSerializers.STRING)
        val POKEBOBBER_BAIT = SynchedEntityData.defineId(PokeRodFishingBobberEntity::class.java, EntityDataSerializers.ITEM_STACK)
        val HOOK_ENTITY_ID = SynchedEntityData.defineId(PokeRodFishingBobberEntity::class.java, EntityDataSerializers.INT)
        private val CAUGHT_FISH = SynchedEntityData.defineId(PokeRodFishingBobberEntity::class.java, EntityDataSerializers.BOOLEAN)
        private val LOOT_TABLE_ID = ResourceKey.create(Registries.LOOT_TABLE, cobblemonResource("fishing/pokerod"))
    }
}
