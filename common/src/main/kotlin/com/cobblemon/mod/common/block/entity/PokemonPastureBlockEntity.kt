/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.pasture.PastureLinkManager
import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import com.cobblemon.mod.common.api.pokemon.breeding.EggPatterns
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.block.NestBlock
import com.cobblemon.mod.common.block.PastureBlock
import com.cobblemon.mod.common.breeding.BreedingLogicManager
import com.cobblemon.mod.common.breeding.SimpleBreedingLogic
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonPasturedPacket
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.blockPositionsAsList
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.Vec3i
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random

class PokemonPastureBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(CobblemonBlockEntities.PASTURE, pos, state) {
    open class Tethering(
        val minRoamPos: BlockPos,
        val maxRoamPos: BlockPos,
        val playerId: UUID,
        val playerName: String,
        val tetheringId: UUID,
        val pokemonId: UUID,
        val pcId: UUID,
        val entityId: Int
    ) {
        fun getPokemon() = Cobblemon.storage.getPC(pcId)[pokemonId]
        val box = AABB(minRoamPos.toVec3d(), maxRoamPos.toVec3d())
        open fun canRoamTo(pos: BlockPos) = box.contains(pos.center)

        fun toDTO(player: ServerPlayer): OpenPasturePacket.PasturePokemonDataDTO? {
            val pokemon = getPokemon() ?: return null
            return OpenPasturePacket.PasturePokemonDataDTO(
                pokemonId = pokemonId,
                playerId = playerId,
                displayName = if (playerId == player.uuid) pokemon.getDisplayName() else lang(
                    "ui.pasture.owned_name",
                    pokemon.getDisplayName(),
                    playerName
                ),
                species = pokemon.species.resourceIdentifier,
                aspects = pokemon.aspects,
                heldItem = pokemon.heldItem(),
                level = pokemon.level,
                entityKnown = (player.level()
                    .getEntity(entityId) as? PokemonEntity)?.tethering?.tetheringId == tetheringId
            )
        }
    }

    companion object {
        internal val TICKER = BlockEntityTicker<PokemonPastureBlockEntity> { world, _, _, blockEntity ->
            if (world.isClientSide) return@BlockEntityTicker
            blockEntity.ticksUntilCheck--
            if (blockEntity.ticksUntilCheck <= 0) {
                blockEntity.checkPokemon()
                blockEntity.tryBreed()
            }
            blockEntity.togglePastureOn(blockEntity.getInRangeViewerCount(world, blockEntity.blockPos) > 0)
        }
        val DITTO_DEX_NUM = 132
    }

    var ticksUntilCheck = Cobblemon.config.pastureBlockUpdateTicks
    val tetheredPokemon = mutableListOf<Tethering>()
    var minRoamPos: BlockPos
    var maxRoamPos: BlockPos
    var ownerId: UUID? = null
    var ownerName: String = ""
    //Maps a male pokemon to all of the female pokemon it can breed with
    val breedingSets: MutableMap<Tethering, MutableSet<Tethering>> = mutableMapOf()
    //Basically here to decide whether to calc all breeding sets when updating the block entity
    //We add breeding sets when pokemon are tethered but we need to calc them on world load too,
    var initializedBreedingSets = false

    init {
        val radius = Cobblemon.config.pastureMaxWanderDistance
        minRoamPos = pos.subtract(Vec3i(radius, radius, radius))
        maxRoamPos = pos.offset(Vec3i(radius, radius, radius))
    }

    fun getMaxTethered() = Cobblemon.config.defaultPasturedPokemonLimit

    fun canAddPokemon(player: ServerPlayer, pokemon: Pokemon, maxPerPlayer: Int): Boolean {
        val forThisPlayer = tetheredPokemon.count { it.playerId == player.uuid }
        // Shouldn't be possible, client should've prevented it
        if (forThisPlayer >= maxPerPlayer || tetheredPokemon.size >= getMaxTethered() || pokemon.isFainted()) {
            return false
        }
        val radius = Cobblemon.config.pastureMaxWanderDistance.toDouble()
        val bottom = blockPos.toVec3d().multiply(1.0, 0.0, 1.0)

        val pokemonWithinPastureWander = player.level()
            .getEntitiesOfClass(PokemonEntity::class.java, AABB.ofSize(bottom, radius, 99999.0, radius)) { true }
            .count()
        val chunkDiameter = (radius / 16) * 2 // Diameter
        if (pokemonWithinPastureWander >= Cobblemon.config.pastureMaxPerChunk * chunkDiameter * chunkDiameter) {
            player.sendPacket(ClosePasturePacket())
            player.sendSystemMessage(lang("pasture.too_many_nearby").red(), true)
            return false
        }

        return true
    }


    fun tether(player: ServerPlayer, pokemon: Pokemon, directionToBehind: Direction): Boolean {
        val world = level ?: return false
        val entity = PokemonEntity(world, pokemon = pokemon)
        entity.refreshDimensions()
        val width = entity.boundingBox.xsize

        val idealPlace = blockPos.offset(directionToBehind.normal.multiply(ceil(width).toInt() + 1))
        var box = entity.getDimensions(Pose.STANDING).makeBoundingBox(idealPlace.center.subtract(0.0, 0.5, 0.0))

        for (i in 0..5) {
            box = box.move(directionToBehind.normal.x.toDouble(), 0.0, directionToBehind.normal.z.toDouble())
            val fixedPosition = makeSuitableY(world, idealPlace.offset(directionToBehind.normal), entity, box)
            if (fixedPosition != null) {
                entity.setPos(fixedPosition.center.subtract(0.0, 0.5, 0.0))
                val pc = Cobblemon.storage.getPC(player.uuid)
                entity.beamMode = 2
                afterOnServer(seconds = SendOutPokemonHandler.SEND_OUT_DURATION) {
                    entity.beamMode = 0
                }
                if (world.addFreshEntity(entity)) {
                    val tethering = Tethering(
                        minRoamPos = minRoamPos,
                        maxRoamPos = maxRoamPos,
                        playerId = player.uuid,
                        playerName = player.gameProfile.name,
                        tetheringId = UUID.randomUUID(),
                        pokemonId = pokemon.uuid,
                        pcId = pc.uuid,
                        entityId = entity.id
                    )
                    calcBreedingSets(tethering)
                    pokemon.tetheringId = tethering.tetheringId
                    tetheredPokemon.add(tethering)
                    entity.tethering = tethering
                    tethering.toDTO(player)?.let { player.sendPacket(PokemonPasturedPacket(it)) }
                    setChanged()
                    CobblemonCriteria.PASTURE_USE.trigger(player, pokemon)
                    return true
                } else {
                    Cobblemon.LOGGER.warn("Couldn't spawn pastured Pokémon for some reason")
                }
                break
            }
        }

        return false
    }

    private fun togglePastureOn(on: Boolean) {
        val pastureBlock = blockState.block as PastureBlock

        val level = level
        if (level != null && !level.isClientSide) {
            val world = level
            val posBottom = pastureBlock.getBasePosition(blockState, blockPos)
            val stateBottom = world.getBlockState(posBottom)

            val posTop = pastureBlock.getPositionOfOtherPart(stateBottom, posBottom)
            val stateTop = world.getBlockState(posTop)

            try {
                if (stateBottom.getValue(PastureBlock.ON) != on) {
                    world.setBlockAndUpdate(posTop, stateTop.setValue(PastureBlock.ON, on))
                    world.setBlockAndUpdate(posBottom, stateBottom.setValue(PastureBlock.ON, on))
                }
            } catch (exception: IllegalArgumentException) {
                if (world.getBlockState(blockPos.above()).block is PastureBlock) {
                    world.setBlockAndUpdate(blockPos.above(), Blocks.AIR.defaultBlockState())
                } else {
                    world.setBlockAndUpdate(blockPos.below(), Blocks.AIR.defaultBlockState())
                }
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState())
                world.addFreshEntity(
                    ItemEntity(
                        world, blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5,
                        ItemStack(CobblemonBlocks.PASTURE)
                    )
                )
            }
        }
    }

    fun isSafeFloor(world: Level, pos: BlockPos, entity: PokemonEntity): Boolean {
        val state = world.getBlockState(pos)
        return if (state.isAir) {
            false
        } else if (state.entityCanStandOn(world, pos, entity) || state.entityCanStandOnFace(
                world,
                pos,
                entity,
                Direction.DOWN
            )
        ) {
            true
        } else if ((entity.behaviour.moving.swim.canWalkOnWater || entity.behaviour.moving.swim.canSwimInWater) && state.fluidState.`is`(FluidTags.WATER)) {
            true
        } else {
            (entity.behaviour.moving.swim.canWalkOnLava || entity.behaviour.moving.swim.canSwimInLava) && state.fluidState.`is`(FluidTags.LAVA)
        }
    }

    // Place the tether block like this: https://gyazo.com/7c163bccfde238688e9a2c600c27aace
    // You'll find you can't place pokemon into the tether. It's because of this function somehow
    fun makeSuitableY(world: Level, pos: BlockPos, entity: PokemonEntity, box: AABB): BlockPos? {
        if (world.collidesWithSuffocatingBlock(entity, box)) {
            for (i in 1..15) {
                val newBox = box.move(0.5, i.toDouble(), 0.5)

                if (!world.collidesWithSuffocatingBlock(entity, newBox) && isSafeFloor(world, pos.offset(0, i - 1, 0), entity)) {
                    return pos.offset(0, i, 0)
                }
            }
        } else {
            for (i in 1..15) {
                val newBox = box.move(0.5, -i.toDouble(), 0.5)

                if (world.collidesWithSuffocatingBlock(entity, newBox) && isSafeFloor(world, pos.offset(0, -i, 0), entity)) {
                    return pos.offset(0, -i + 1, 0)
                }
            }
        }

        return null
    }

    fun checkPokemon() {
        val deadLinks = mutableListOf<UUID>()
        tetheredPokemon.forEach {
            val pokemon = it.getPokemon()
            if (pokemon == null) {
                deadLinks.add(it.pokemonId)
            } else if (pokemon.tetheringId == null || pokemon.tetheringId != it.tetheringId) {
                deadLinks.add(it.pokemonId)
            }
        }
        deadLinks.forEach(::releasePokemon)
        ticksUntilCheck = Cobblemon.config.pastureBlockUpdateTicks
        setChanged()
    }

    //Dear god this method needs to be cleaned up
    fun tryBreed() {
        val bredPokemon = mutableSetOf<Pokemon>()
        //We should probably find a way to minimize how often this gets called
        //Maybe cache the result and update it infrequently?
        val nests = findUnusedNests()
        if (nests.isNotEmpty()) {
            breedingSets.forEach { father, mothers ->
                if (father.getPokemon()?.breedingCooldown == 0) {
                    mothers.forEach { mother ->
                        if (mother.getPokemon()?.breedingCooldown == 0) {
                            val motherPoke = mother.getPokemon()!!
                            val fatherPoke = father.getPokemon()!!
                            if (BreedingLogicManager.canBreed(motherPoke, fatherPoke) && nests.isNotEmpty()) {
                                val breedResult = BreedingLogicManager.breed(motherPoke, fatherPoke)

                                if (breedResult.successful) {
                                    // add cooldown to the parents
                                    motherPoke.breedingCooldown = 240

                                    //FIXME: Reimplement or scrap
                                    /*
                                    // add heart particles <3 <3 <3
                                    val fatherPokemon = father.getPokemon()?.entity
                                    val motherPokemon = mother.getPokemon()?.entity
                                    // Spawn heart particles for father
                                    if (fatherPokemon != null && motherPokemon != null) {
                                        /*//this.getWorld().addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
                                        fatherPokemon.getWorld()?.addParticle(ParticleTypes.HEART,
                                                            fatherPokemon.getParticleX(1.0),
                                                            fatherPokemon.getRandomBodyY() + 0.5,
                                                            fatherPokemon.getParticleZ(1.0),
                                                            0.02 * fatherPokemon.random.nextGaussian(), // X velocity (slight variance)
                                                            0.02 * fatherPokemon.random.nextGaussian(), // Y velocity (upwards with variance)
                                                            0.02 * fatherPokemon.random.nextGaussian()) // Z velocity (slight variance))

                                        motherPokemon.getWorld()?.addParticle(ParticleTypes.HEART,
                                                motherPokemon.getParticleX(1.0),
                                                motherPokemon.getRandomBodyY() + 0.5,
                                                motherPokemon.getParticleZ(1.0),
                                                0.02 * motherPokemon.random.nextGaussian(), // X velocity (slight variance)
                                                0.02 * motherPokemon.random.nextGaussian(), // Y velocity (upwards with variance)
                                                0.02 * motherPokemon.random.nextGaussian()) // Z velocity (slight variance))*/
                                        (level as ServerLevel).addParticle(
                                                ParticleTypes.HEART,
                                                fatherPokemon.getParticleX(1.0), fatherPokemon.getRandomBodyY() + 0.5, fatherPokemon.getParticleZ(1.0), // Slightly raise the Y position for upward effect
                                                4 + kotlin.random.Random.nextInt(2),
                                                0.2 * fatherPokemon.random.nextGaussian(), // X velocity (slight variance)
                                                0.1 * fatherPokemon.random.nextGaussian(), // Y velocity (upwards with variance)
                                                0.2 * fatherPokemon.random.nextGaussian(), // Z velocity (slight variance)
                                                0.02 // Speed of particles
                                        )


                                        // Spawn heart particles for mother
                                        (world as ServerWorld).spawnParticles(
                                                ParticleTypes.HEART,
                                                motherPokemon.getParticleX(1.0), motherPokemon.getRandomBodyY() + 0.5, motherPokemon.getParticleZ(1.0), // Slightly raise the Y position for upward effect
                                                4 + kotlin.random.Random.nextInt(2),
                                                0.2 * motherPokemon.random.nextGaussian(), // X velocity (slight variance)
                                                0.1 * motherPokemon.random.nextGaussian(), // Y velocity (upwards with variance)
                                                0.2 * motherPokemon.random.nextGaussian(), // Z velocity (slight variance)
                                                0.02 // Speed of particles
                                        )
                                    }
                                    */
                                    println(breedResult.pokemon)
                                    val nestTaken = nests.random()
                                    val nestStal = level?.getBlockState(nestTaken)
                                    val blockEntity = level?.getBlockEntity(nestTaken) as? NestBlockEntity
                                    if (blockEntity != null && breedResult.pokemon != null) {
                                        //Most params here need to be gotten from the form when implemented properly
                                        blockEntity.egg = Egg(
                                                breedResult.pokemon,
                                                EggPatterns.patternMap.keys.random(),
                                                Integer.toHexString(breedResult.pokemon.species.primaryType.hue),
                                                breedResult.pokemon.species.secondaryType?.let {Integer.toHexString(it.hue)} ?: "FFFFFF",
                                                20*10
                                        )
                                        blockEntity.setChanged()
                                        level?.sendBlockUpdated(nestTaken, level?.getBlockState(nestTaken), level?.getBlockState(nestTaken), Block.UPDATE_CLIENTS)

                                        // remove the nest used from the nest consideration pool
                                        nests.remove(nestTaken)

                                        bredPokemon.add(fatherPoke)
                                        bredPokemon.add(motherPoke)
                                    }


                                }

                            }
                        }
                    }
                }
                bredPokemon.forEach {
                    it.breedingCooldown = 20
                }
            }
        }

    }

    //If new mon is passed, only calc/add breeding sets with that parent
    //Otherwise, find all combos in the block (like on world load)
    fun calcBreedingSets(newMon: Tethering? = null) {
        initializedBreedingSets = true
        if (newMon != null) {
            for (t in tetheredPokemon) {
                addIfBreedingPair(newMon, t)
            }
        }
        else {
            tetheredPokemon.forEachIndexed { i, mon ->
                val listLen = tetheredPokemon.size
                if (i != listLen - 1) {
                    tetheredPokemon.subList(i + 1, listLen).forEach {
                        addIfBreedingPair(mon, it)
                    }
                }

            }
        }

    }

    fun addIfBreedingPair(monOne: Tethering, monTwo: Tethering) {
        val tetheredMon = monOne.getPokemon()
        val mother =  if (tetheredMon?.gender == Gender.FEMALE ||
            tetheredMon?.species?.nationalPokedexNumber == DITTO_DEX_NUM)
            monOne
        else monTwo
        val father = (if (tetheredMon == mother) monTwo else monOne)
        if (SimpleBreedingLogic.canBreed(mother.getPokemon()!!, father.getPokemon()!!)) {
            val fatherSet: MutableSet<Tethering> = breedingSets.getOrDefault(father, mutableSetOf())
            fatherSet.add(mother)
            breedingSets[father] = fatherSet
        }
    }

    fun findUnusedNests(): MutableSet<BlockPos> {
        val res = mutableSetOf<BlockPos>()
        val cube = Shapes.box(
            (this.blockPos.x - 2).toDouble(), (this.blockPos.y - 1).toDouble(), (this.blockPos.z - 2).toDouble(),
            (this.blockPos.x + 3).toDouble(), (this.blockPos.y + 3).toDouble(), (this.blockPos.z + 3).toDouble()
        )
        cube.blockPositionsAsList().forEach {
            val state = level?.getBlockState(it)
            if (state?.block is NestBlock) {
                val entity = level?.getBlockEntity(it) as NestBlockEntity
                //Cobblemon.LOGGER.warn("Nest at ${it.toString()}")
                if (entity.egg == null) {
                    res.add(it)
                }

            }
        }
        return res
    }

    fun onBroken() {
        if (level is ServerLevel) {
            tetheredPokemon.toList().forEach { releasePokemon(it.pokemonId) }
            PastureLinkManager.removeAt(level as ServerLevel, blockPos)
        }
    }

    fun releasePokemon(pokemonId: UUID) {
        val tethering = tetheredPokemon.find { it.pokemonId == pokemonId } ?: return
        tethering.getPokemon()?.tetheringId = null
        tetheredPokemon.remove(tethering)
        setChanged()
    }

    fun releaseAllPokemon(playerId: UUID): List<UUID> {
        val unpastured = mutableListOf<UUID>()
        tetheredPokemon.filter { it.playerId == playerId }.forEach {
            it.getPokemon()?.tetheringId = null
            tetheredPokemon.remove(it)
            unpastured.add(it.pokemonId)
        }
        setChanged()
        return unpastured
    }

    private fun getInRangeViewerCount(world: Level, pos: BlockPos, range: Double = 5.0): Int {
        val box = AABB(
            pos.x.toDouble() - range,
            pos.y.toDouble() - range,
            pos.z.toDouble() - range,
            (pos.x + 1).toDouble() + range,
            (pos.y + 1).toDouble() + range,
            (pos.z + 1).toDouble() + range
        )

        return world.getEntities(EntityTypeTest.forClass(ServerPlayer::class.java), box, this::isPlayerViewing).size
    }

    private fun isPlayerViewing(player: ServerPlayer): Boolean {
        val pastureLink = PastureLinkManager.getLinkByPlayer(player)
        return pastureLink != null && pastureLink.pos == blockPos && pastureLink.dimension == ResourceLocation.tryParse(
            player.level().dimensionTypeRegistration().registeredName // todo (techdaan): confirm this is good
        )
    }

    override fun loadAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(nbt, registryLookup)
        val list = nbt.getList(DataKeys.TETHER_POKEMON, CompoundTag.TAG_COMPOUND.toInt())
        this.ownerId = if (nbt.hasUUID(DataKeys.TETHER_OWNER_ID)) nbt.getUUID(DataKeys.TETHER_OWNER_ID) else null
        this.ownerName = nbt.getString(DataKeys.TETHER_OWNER_NAME).takeIf { it.isNotEmpty() } ?: ""
        for (tetheringNBT in list) {
            tetheringNBT as CompoundTag
            val tetheringId = tetheringNBT.getUUID(DataKeys.TETHERING_ID)
            val pokemonId = tetheringNBT.getUUID(DataKeys.POKEMON_UUID)
            val pcId = tetheringNBT.getUUID(DataKeys.PC_ID)
            val playerId = tetheringNBT.getUUID(DataKeys.TETHERING_PLAYER_ID)
            val entityId = tetheringNBT.getInt(DataKeys.TETHERING_ENTITY_ID)
            tetheredPokemon.add(
                Tethering(
                    minRoamPos = minRoamPos,
                    maxRoamPos = maxRoamPos,
                    playerId = playerId,
                    playerName = ownerName,
                    tetheringId = tetheringId,
                    pokemonId = pokemonId,
                    pcId = pcId,
                    entityId = entityId
                )
            )
        }
        this.minRoamPos = NbtUtils.readBlockPos(nbt, DataKeys.TETHER_MIN_ROAM_POS).get()
        this.maxRoamPos = NbtUtils.readBlockPos(nbt, DataKeys.TETHER_MAX_ROAM_POS).get()
        if (!initializedBreedingSets) {
            calcBreedingSets()
        }
    }

    override fun saveAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(nbt, registryLookup)
        val list = ListTag()
        for (tethering in tetheredPokemon) {
            val tetheringNBT = CompoundTag()
            tetheringNBT.putUUID(DataKeys.TETHERING_ID, tethering.tetheringId)
            tetheringNBT.putUUID(DataKeys.TETHERING_PLAYER_ID, tethering.playerId)
            tetheringNBT.putUUID(DataKeys.POKEMON_UUID, tethering.pokemonId)
            tetheringNBT.putUUID(DataKeys.PC_ID, tethering.pcId)
            tetheringNBT.putInt(DataKeys.TETHERING_ENTITY_ID, tethering.entityId)
            list.add(tetheringNBT)
        }
        nbt.put(DataKeys.TETHER_POKEMON, list)
        nbt.put(DataKeys.TETHER_MIN_ROAM_POS, NbtUtils.writeBlockPos(minRoamPos))
        nbt.put(DataKeys.TETHER_MAX_ROAM_POS, NbtUtils.writeBlockPos(maxRoamPos))
        ownerId?.let { nbt.putUUID(DataKeys.TETHER_OWNER_ID, it) }
        nbt.putString(DataKeys.TETHER_OWNER_NAME, this.ownerName)
    }
}