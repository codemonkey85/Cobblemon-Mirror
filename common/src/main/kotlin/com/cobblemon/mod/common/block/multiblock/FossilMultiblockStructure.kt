/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.multiblock

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.FossilRevivedEvent
import com.cobblemon.mod.common.api.fossil.Fossil
import com.cobblemon.mod.common.api.fossil.Fossils
import com.cobblemon.mod.common.api.fossil.NaturalMaterials
import com.cobblemon.mod.common.api.multiblock.MultiblockEntity
import com.cobblemon.mod.common.api.multiblock.MultiblockStructure
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.block.FossilAnalyzerBlock
import com.cobblemon.mod.common.block.MonitorBlock
import com.cobblemon.mod.common.block.RestorationTankBlock
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.RestorationTankBlockEntity
import com.cobblemon.mod.common.client.render.models.blockbench.fossil.FossilState
import com.cobblemon.mod.common.client.sound.CancellableSoundController
import com.cobblemon.mod.common.client.sound.CancellableSoundInstance
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.giveOrDropItemStack
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.readBlockPosWithFallback
import com.cobblemon.mod.common.util.server
import java.util.UUID
import kotlin.math.ceil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

class FossilMultiblockStructure (
    val monitorPos: BlockPos,
    val analyzerPos: BlockPos,
    val tankBasePos: BlockPos,
    animAge: Int = -1,
    animPartialTicks: Float = 0F
) : MultiblockStructure {

    override val controllerBlockPos = analyzerPos


    // TODO: API method for this
    var organicMaterialInside = 0
        private set

    var hasCreatedPokemon: Boolean = false
        private set
    var timeRemaining = -1
        private set
    var resultingFossil: Fossil? = null
        private set
    private var lastInteraction: Long = 0
    private var machineStartTime: Long = 0
    private var protectionTime: Int = -1
    private var fossilOwnerUUID: UUID? = null
    val fossilState = FossilState(animAge, animPartialTicks)
    var fossilInventory: MutableList<ItemStack> = mutableListOf<ItemStack>()
    var tankConnectorDirection: Direction? = null

    //Only updated clientside
    var fillLevel = 0
    override fun useWithoutItem(
        blockState: BlockState,
        world: Level,
        blockPos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        val stack = player.getItemInHand(InteractionHand.MAIN_HAND)

        if(stack.`is`(CobblemonItemTags.POKE_BALLS) || stack.item is PokeBallItem) {
            if (player !is ServerPlayer) {
                return InteractionResult.SUCCESS
            }
            if (this.hasCreatedPokemon) {
                if (this.fossilOwnerUUID != null && player.uuid != this.fossilOwnerUUID) {
                    var ownerName : String = "UNKNOWN_USER" // TODO: lang agnostic fallback
                    server()?.profileCache?.get(this.fossilOwnerUUID)?.orElse(null)?.name?.let {
                        ownerName = it
                    }
                    player.sendSystemMessage(lang("fossilmachine.protected", ownerName), true)
                    return InteractionResult.FAIL
                }


                val ballType = (stack.item as PokeBallItem).pokeBall
                if (!player.isCreative) {
                    stack?.shrink(1)
                }

                val pokemon = this.resultingFossil?.result?.create()

                if(pokemon != null) {
                    pokemon.caughtBall = ballType
                    player.party().add(pokemon)
                    this.fossilState.growthState = "Taken"
                    player.playSound(CobblemonSounds.FOSSIL_MACHINE_RETRIEVE_POKEMON, 1.0F, 1.0F)
                    CobblemonEvents.FOSSIL_REVIVED.post(FossilRevivedEvent(pokemon, player))
                }

                // Turn the monitor off
                val monitorState = world.getBlockState(monitorPos)
                if(monitorState.hasProperty(MonitorBlock.SCREEN) && !monitorState.equals(MonitorBlock.MonitorScreen.OFF)) {
                    world.setBlockAndUpdate(monitorPos, monitorState.setValue(MonitorBlock.SCREEN, MonitorBlock.MonitorScreen.OFF))
                }

                this.hasCreatedPokemon = false
                this.fossilOwnerUUID = null
                this.protectionTime = -1
                this.updateFossilType(world)
                this.syncToClient(world)
                this.markDirty(world)
                return InteractionResult.SUCCESS
            }
        }

        // Reclaim the last fossil from the machine if their hand is empty
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            if(!this.isRunning() && this.hasCreatedPokemon) {
                if (fossilInventory.isEmpty()) {
                    return InteractionResult.CONSUME
                }
                player.setItemInHand(InteractionHand.MAIN_HAND, fossilInventory.last())

                // remove last fossil in the fossil machine stack when grabbed out of the machine
                this.fossilInventory.removeAt(fossilInventory.size - 1)
                if(!world.isClientSide) {
                    world.playSound(null, analyzerPos, CobblemonSounds.FOSSIL_MACHINE_RETRIEVE_FOSSIL, SoundSource.BLOCKS)
                    this.updateFossilType(world)
                    this.syncToClient(world)
                    this.markDirty(world)
                }
            }
            return InteractionResult.CONSUME
        }

        // Check if the player is holding a fossil and if so insert it into the machine.
        if (Fossils.isFossilIngredient(stack)) {
            if (!this.isRunning() && !this.hasCreatedPokemon) {
                if (fossilInventory.size > Cobblemon.config.maxInsertedFossilItems) {
                    return InteractionResult.FAIL
                }
                if (player is ServerPlayer) {
                    val copyFossilStack = stack.copyWithCount(1)
                    if (!player.isCreative) {
                        stack?.shrink(1)
                    }
                    fossilOwnerUUID = player.uuid
                    fossilInventory.add(copyFossilStack)
                    this.updateFossilType(world)
                    world.playSound(null, analyzerPos, CobblemonSounds.FOSSIL_MACHINE_INSERT_FOSSIL, SoundSource.BLOCKS)
                    this.syncToClient(world)
                    this.markDirty(world)
                }
            }
            return InteractionResult.SUCCESS
        }

        // Check if the player is holding a natural material and if so, feed it to the machine.
        if (NaturalMaterials.isNaturalMaterial(stack)) {
            if (player is ServerPlayer
                    && !this.isRunning()
                    && !this.hasCreatedPokemon
                    && this.organicMaterialInside < MATERIAL_TO_START
                    && insertOrganicMaterial(ItemStack(stack.item, 1), world)) {
                this.lastInteraction = world.gameTime
                if (!player.isCreative) {
                    val returnItem = NaturalMaterials.getReturnItem(stack)
                    stack?.shrink(1)
                    player.giveOrDropItemStack(
                        ItemStack(
                            BuiltInRegistries.ITEM.get(
                                returnItem
                            )
                        ), false)
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide)
        }

        // pure client instances dont know what a valid fossil is so this is my janky workaround
        if (stack.`is`(CobblemonItemTags.FOSSILS)) return InteractionResult.SUCCESS

        return InteractionResult.PASS
    }

    fun spawn(world: Level, pos: BlockPos, directionToBehind: Direction, pokemon: Pokemon) : Boolean {
        val entity = PokemonEntity(world, pokemon = pokemon)
        entity.refreshDimensions()
        val width = entity.boundingBox.xsize

        val idealPlace = pos.offset(directionToBehind.normal.multiply(ceil(width / 2.0).toInt() + 1))
        var box = entity.getDimensions(Pose.STANDING).makeBoundingBox(idealPlace.center.subtract(0.0, 0.5, 0.0))

        for (i in 0..5) {
            box = box.move(directionToBehind.normal.x.toDouble(), 0.0, directionToBehind.normal.z.toDouble())
            val fixedPosition = makeSuitableY(world, idealPlace.offset(directionToBehind.normal), entity, box)
            if (fixedPosition != null) {
                entity.setPos(fixedPosition.center.subtract(0.0, 0.5, 0.0))
                // TODO: Find a correct way to set the new entity's Yaw rotation. (Face away from the machine)
                if (world.addFreshEntity(entity)) {
                    CobblemonEvents.FOSSIL_REVIVED.post(FossilRevivedEvent(pokemon, null))
                    return true
                } else {
                    Cobblemon.LOGGER.warn("Couldn't spawn resurrected Pokémon for some reason")
                }
                break
            }
        }
        return false
    }

    fun isSafeFloor(world: Level, pos: BlockPos, entity: PokemonEntity): Boolean {
        val state = world.getBlockState(pos)
        return if (state.isAir) {
            false
        } else if (state.entityCanStandOn(world, pos, entity) || state.entityCanStandOnFace(world, pos, entity, Direction.DOWN)) {
            true
        } else if ((entity.behaviour.moving.swim.canWalkOnWater || entity.behaviour.moving.swim.canSwimInWater) && state.fluidState.`is`(FluidTags.WATER)) {
            true
        } else {
            (entity.behaviour.moving.swim.canWalkOnLava || entity.behaviour.moving.swim.canSwimInLava) && state.fluidState.`is`(FluidTags.LAVA)
        }
    }

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

    @Deprecated("Deprecated in Java")
    override fun getAnalogOutputSignal(state: BlockState, world: Level?, pos: BlockPos?): Int {
        if(world == null || pos == null) {
            return 0
        }
        if(monitorPos == pos) {
            if(hasCreatedPokemon) {
                return 15
            }
            if(!isRunning()) {
                return 0
            }
            return Math.max(15 - timeRemaining * 15 / TIME_TO_TAKE, 1)
        }
        if(tankBasePos == pos || tankBasePos.above() == pos) {
            return organicMaterialInside * 15 / MATERIAL_TO_START
        }
        return 0
    }
    override fun onTriggerEvent(state: BlockState?, world: ServerLevel?, pos: BlockPos?, random: RandomSource?) {
        // instantiate the pokemon as a new entity and spawn it at the location of the machine
        if(this.protectionTime <= 0) {
            val wildPokemon: Pokemon = if (hasCreatedPokemon) resultingFossil?.result?.create() ?: return else return
            val direction = state?.getValue(HorizontalDirectionalBlock.FACING)?.opposite
            if(pos != null && direction != null && world != null) {
                val success = this.spawn(world, pos, direction, wildPokemon)
                if(success) {
                    this.fossilState.growthState = "Taken"
                    this.hasCreatedPokemon = false
                    this.fossilOwnerUUID = null
                    this.protectionTime = -1
                    world.playSound(null, tankBasePos, CobblemonSounds.FOSSIL_MACHINE_RETRIEVE_POKEMON, SoundSource.BLOCKS)
                    this.updateFossilType(world)
                    this.syncToClient(world)
                    this.markDirty(world)
                }
            }
        }
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player?) {
        val monitorEntity = world.getBlockEntity(monitorPos) as? MultiblockEntity
        val analyzerEntity = world.getBlockEntity(analyzerPos) as? MultiblockEntity
        val tankBaseEntity = world.getBlockEntity(tankBasePos) as? MultiblockEntity
        val tankTopEntity = world.getBlockEntity(tankBasePos.above()) as? MultiblockEntity
        val tankBaseBlockState = world.getBlockState(tankBaseEntity?.blockPos)
        val direction = tankBaseBlockState.getValue(HorizontalDirectionalBlock.FACING).opposite
        val wildPokemon: Pokemon? = if(hasCreatedPokemon) resultingFossil?.result?.create() else null

        monitorEntity?.multiblockStructure = null
        analyzerEntity?.multiblockStructure = null
        tankBaseEntity?.multiblockStructure = null
        tankTopEntity?.multiblockStructure = null
        monitorEntity?.masterBlockPos = null
        analyzerEntity?.masterBlockPos = null
        tankBaseEntity?.masterBlockPos = null
        tankTopEntity?.masterBlockPos = null

        // Drop fossils from machine as long as the machine is not started or near completion
        if (this.timeRemaining == -1 || this.timeRemaining >= 20) {
            this.fossilInventory.forEach {
                val stack = ItemStack(it.item, 1)
                Containers.dropItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
            }
        }
        if(tankBaseEntity is RestorationTankBlockEntity) {
            tankBaseEntity.inv.items.forEach {
                Containers.dropItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
            }
        }


        // if the machine is broken while the pokemon is done then spawn the pokemon at the location and make it a wild pokemon
        if (wildPokemon != null) {
            //world.createExplosion(this.createdPokemon?.entity, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 5F, World.ExplosionSourceType.TNT)

            // instantiate the pokemon as a new entity and spawn it at the location of the machine
            //var wildPokemon = this.createdPokemon?.sendOut(world as ServerWorld, pos.toVec3d())

            //world.spawnEntity(wildPokemon)
            this.spawn(world, pos, direction, wildPokemon)

        }
        this.protectionTime = -1
        this.updateFossilType(world)
        this.stopMachine(world)
        this.syncToClient(world)
        this.markDirty(world)
    }

    override fun setRemoved(world: Level) {
        if(world.isClientSide) {
            CancellableSoundController.stopSound(this.tankBasePos, CobblemonSounds.FOSSIL_MACHINE_ACTIVE_LOOP.location)
        }
    }

    override fun tick(world: Level) {
        if (protectionTime > 0) protectionTime--
        if (protectionTime == 0) {
            protectionTime = -1
            this.fossilOwnerUUID = null
            this.updateProgress(world)
            this.syncToClient(world)
            this.markDirty(world)
            world.playSound(null, tankBasePos, CobblemonSounds.FOSSIL_MACHINE_UNPROTECTED, SoundSource.BLOCKS)
        }

        if (this.hasCreatedPokemon) {
            return
        }

        if (world.isClientSide && this.isRunning() && (world.gameTime - this.machineStartTime) % 160L == 0L) {
            if(world.isClientSide) {
                CancellableSoundController.playSound(CancellableSoundInstance(CobblemonSounds.FOSSIL_MACHINE_ACTIVE_LOOP,
                        tankBasePos, true, 1.0f, 1.0f, ))
            }
        }

        if (this.timeRemaining == -1 && this.organicMaterialInside >= MATERIAL_TO_START && this.resultingFossil != null) {
            this.startMachine(world)
            return
        }

        if (this.timeRemaining >= 0) {
            this.timeRemaining--
        }

        if (this.timeRemaining % TIME_PER_STAGE == 0) {
            this.updateProgress(world)
            this.syncToClient(world)
            this.markDirty(world)
        }

        if (this.timeRemaining == 0) {
            world.playSound(null, tankBasePos, CobblemonSounds.FOSSIL_MACHINE_FINISHED, SoundSource.BLOCKS)
            fossilInventory.clear()
            this.hasCreatedPokemon = true
            if(this.fossilOwnerUUID != null) {
                protectionTime = PROTECTION_TIME
            }

            this.stopMachine(world)
        }
    }

    override fun syncToClient(world: Level) {
        val tankBaseEntity = world.getBlockEntity(tankBasePos) as? MultiblockEntity
        val analyzerEntity = world.getBlockEntity(controllerBlockPos) as? MultiblockEntity
        val monitorEntity = world.getBlockEntity(monitorPos) as? MultiblockEntity

        if(tankBaseEntity != null)
            world.sendBlockUpdated(tankBasePos, tankBaseEntity.blockState, tankBaseEntity.blockState, Block.UPDATE_CLIENTS)
        if(analyzerEntity != null)
            world.sendBlockUpdated(analyzerPos, analyzerEntity.blockState, analyzerEntity.blockState, Block.UPDATE_CLIENTS)
        if(monitorEntity != null)
            world.sendBlockUpdated(monitorPos, monitorEntity.blockState, monitorEntity.blockState, Block.UPDATE_CLIENTS)
    }

    override fun markDirty(world: Level) {
        val entities = listOf(
            world.getBlockEntity(analyzerPos),
            world.getBlockEntity(tankBasePos),
            world.getBlockEntity(tankBasePos.above()),
            world.getBlockEntity(monitorPos)
        )
        entities.forEach {
            it?.setChanged()
        }
    }

    fun startMachine(world: Level) {
        this.timeRemaining = TIME_TO_TAKE
        this.machineStartTime = world.gameTime

        world.playSound(null, tankBasePos, CobblemonSounds.FOSSIL_MACHINE_ACTIVATE, SoundSource.BLOCKS)
        if(world.isClientSide) {
            CancellableSoundController.playSound(CancellableSoundInstance(CobblemonSounds.FOSSIL_MACHINE_ACTIVE_LOOP,
                    tankBasePos, true, 1.0f, 1.0f, ))
        }

        this.updateOnStatus(world)
        this.updateProgress(world)
        this.syncToClient(world)
        this.markDirty(world)
    }

    fun stopMachine(world: Level){
        this.fossilState.growthState = "Fully Grown"
        this.timeRemaining = -1
        this.organicMaterialInside = 0

        fossilInventory.clear()

        if(world.isClientSide) {
            CancellableSoundController.stopSound(tankBasePos, CobblemonSounds.FOSSIL_MACHINE_ACTIVE_LOOP.location)
        }

        this.updateOnStatus(world)
        this.updateProgress(world)
        this.syncToClient(world)
        this.markDirty(world)
    }

    fun updateOnStatus(world: Level) {
        val upperTankPos = tankBasePos.above()
        val analyzerState = world.getBlockState(analyzerPos)
        val tankState = world.getBlockState(tankBasePos.above())
        if (analyzerState.hasProperty(FossilAnalyzerBlock.ON)) {
            world.setBlockAndUpdate(analyzerPos, analyzerState.setValue(FossilAnalyzerBlock.ON, timeRemaining >= 0))
        }
        if (tankState.hasProperty(RestorationTankBlock.ON)) {
            world.setBlockAndUpdate(upperTankPos, tankState.setValue(RestorationTankBlock.ON, timeRemaining >= 0))
        }
    }

    fun updateProgress(world: Level) {
        val monitorState = world.getBlockState(monitorPos)
        if (monitorState.hasProperty(MonitorBlock.SCREEN)) {
            val screenID = if (protectionTime > 0F) {
                MonitorBlock.MonitorScreen.GREEN_PROGRESS_9
            } else if (timeRemaining <= 0) {
                MonitorBlock.MonitorScreen.OFF
            } else {
                getProgressScreen((TIME_TO_TAKE - timeRemaining) / TIME_PER_STAGE)
            }
            world.setBlockAndUpdate(monitorPos, monitorState.setValue(MonitorBlock.SCREEN, screenID))
        }
    }

    fun getProgressScreen(progress:Int) : MonitorBlock.MonitorScreen {
        return when (progress) {
            0 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_1
            1 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_2
            2 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_3
            3 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_4
            4 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_5
            5 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_6
            6 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_7
            7 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_8
            8 -> MonitorBlock.MonitorScreen.BLUE_PROGRESS_9
            else -> MonitorBlock.MonitorScreen.OFF
        }
    }

    /**
     * Checks for a resulting fossil type inside the machine.
     * @param world The world to check in.
     * @return The resulting fossil type if found, otherwise null.
     */
    fun updateFossilType(world: Level) {
        if (fossilInventory.isEmpty()) {
            if (this.resultingFossil == null) {
                return
            }
            this.resultingFossil = null
        } else {
            this.resultingFossil = Fossils.getFossilByItemStacks(fossilInventory)
        }
    }

    /**
     * Checks if the machine is currently running.
     */
    fun isRunning(): Boolean {
        return this.timeRemaining > 0
    }

    //Returns false if material wasnt inserted
    fun insertOrganicMaterial(stack: ItemStack, world: Level): Boolean {
        var natureValue = NaturalMaterials.getContent(stack)
        if (timeRemaining > 0 || this.organicMaterialInside >= MATERIAL_TO_START || natureValue == null) {
            return false
        }
        natureValue *= stack.count

        if (natureValue <= 0 && organicMaterialInside == 0) return false
        val oldFillStage = organicMaterialInside * 8 / MATERIAL_TO_START

        // to prevent over/under filling the tank causing a crash
        if ((organicMaterialInside + natureValue) > MATERIAL_TO_START) {
            organicMaterialInside = MATERIAL_TO_START
        } else if ((organicMaterialInside + natureValue) < 0) {
            organicMaterialInside = 0
        } else {
            organicMaterialInside += natureValue
        }
        if (this.organicMaterialInside >= MATERIAL_TO_START) {
            world.playSound(null, this.tankBasePos, CobblemonSounds.FOSSIL_MACHINE_DNA_FULL, SoundSource.BLOCKS, 1.0F, 1.0F)
        } else if (world.gameTime - this.lastInteraction < 10) {
            world.playSound(null, this.tankBasePos, CobblemonSounds.FOSSIL_MACHINE_INSERT_DNA_SMALL, SoundSource.BLOCKS, 1.0F, 1.0F)
        } else {
            world.playSound(null, this.tankBasePos, CobblemonSounds.FOSSIL_MACHINE_INSERT_DNA, SoundSource.BLOCKS, 1.0F, 1.0F)
        }
        this.markDirty(world)
        if (oldFillStage != (organicMaterialInside * 8 / MATERIAL_TO_START)) {
            this.syncToClient(world)
        }
        return true
    }

    // insert fossil to fossilInventory - returns false if failed
    fun insertFossil(stack: ItemStack, world: Level): Boolean {
        // if machine is running or fossil inventory is equal to 3 return false
        if (timeRemaining > 0 || this.fossilInventory.size == 3) {
            return false
        }
        val oldFillStage = this.fossilInventory.size

        //add fossil to the stack in the Compartment
        this.fossilInventory.add(stack)
        world.playSound(null, analyzerPos, CobblemonSounds.FOSSIL_MACHINE_INSERT_FOSSIL, SoundSource.BLOCKS)

        this.updateFossilType(world)
        this.markDirty(world)
        if (oldFillStage != this.fossilInventory.size) {
            this.syncToClient(world)
        }
        return true
    }

    override fun writeToNbt(registryLookup: HolderLookup.Provider): CompoundTag {
        val result = CompoundTag()
        result.put(DataKeys.MONITOR_POS, NbtUtils.writeBlockPos(monitorPos))
        result.put(DataKeys.ANALYZER_POS, NbtUtils.writeBlockPos(analyzerPos))
        result.put(DataKeys.TANK_BASE_POS, NbtUtils.writeBlockPos(tankBasePos))
        result.putInt(DataKeys.TIME_LEFT, timeRemaining)
        result.putInt(DataKeys.PROTECTED_TIME_LEFT, protectionTime)
        if(fossilOwnerUUID != null)
            result.putUUID(DataKeys.FOSSIL_OWNER, fossilOwnerUUID)
        result.putInt(DataKeys.ORGANIC_MATERIAL, organicMaterialInside)
        val fossilInv = ListTag()

        fossilInventory.forEach { fossilInv.add(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, it).orThrow) }
        result.put(DataKeys.FOSSIL_INVENTORY, fossilInv)
        result.putString(DataKeys.CONNECTOR_DIRECTION, tankConnectorDirection?.toString())

        if (this.resultingFossil != null) {
            result.putString(DataKeys.INSERTED_FOSSIL, this.resultingFossil!!.serializedName)
        }

        result.putBoolean(DataKeys.HAS_CREATED_POKEMON, hasCreatedPokemon)

        return result
    }

    companion object {
        val TICKER = BlockEntityTicker<FossilMultiblockEntity> { world, _, _, blockEntity ->
            if (blockEntity.multiblockStructure != null) {
                blockEntity.multiblockStructure!!.tick(world)
            }
        }

        const val TICKS_PER_MINUTE = 1200
        const val MATERIAL_TO_START = 128
        const val TIME_TO_TAKE = TICKS_PER_MINUTE * 12
        const val TIME_PER_STAGE = TIME_TO_TAKE / 8
        const val PROTECTION_TIME = TICKS_PER_MINUTE * 5

        fun fromNbt(nbt: CompoundTag, registryLookup: HolderLookup.Provider, animAge: Int = -1, partialTicks: Float = 0f): FossilMultiblockStructure {
            val monitorPos = nbt.readBlockPosWithFallback(DataKeys.MONITOR_POS)
            val compartmentPos = nbt.readBlockPosWithFallback(DataKeys.ANALYZER_POS)
            val tankBasePos = nbt.readBlockPosWithFallback(DataKeys.TANK_BASE_POS)

            val result = FossilMultiblockStructure(monitorPos, compartmentPos, tankBasePos, animAge, partialTicks)
            result.organicMaterialInside = nbt.getInt(DataKeys.ORGANIC_MATERIAL)
            result.timeRemaining = nbt.getInt(DataKeys.TIME_LEFT)
            result.protectionTime = if (nbt.contains(DataKeys.PROTECTED_TIME_LEFT)) nbt.getInt(DataKeys.PROTECTED_TIME_LEFT) else -1
            result.fossilOwnerUUID = if (nbt.contains(DataKeys.FOSSIL_OWNER)) nbt.getUUID(DataKeys.FOSSIL_OWNER) else null

            val fossilInv = if (nbt.contains(DataKeys.FOSSIL_INVENTORY)) { (nbt.get(DataKeys.FOSSIL_INVENTORY) as ListTag) } else ListTag()
            val actualFossilList = mutableListOf<ItemStack>()
            fossilInv.forEach { ItemStack.parse(registryLookup, it).ifPresent(actualFossilList::add) }
            result.fossilInventory = actualFossilList
            result.tankConnectorDirection = Direction.byName(nbt.getString(DataKeys.CONNECTOR_DIRECTION))

            if (nbt.contains(DataKeys.INSERTED_FOSSIL)) {
                val id = ResourceLocation.parse(nbt.getString(DataKeys.INSERTED_FOSSIL))
                val fossil = Fossils.getByIdentifier(id)

                if (fossil != null) {
                    result.resultingFossil = fossil
                } else {
                    Cobblemon.LOGGER.error("Loaded fossil structure with invalid fossil type: {}", id)
                }
            }

            if (nbt.contains(DataKeys.CREATED_POKEMON)) {
                // migration of instances that saved the created pokeon in the nbt
                result.hasCreatedPokemon = true
            } else if (nbt.contains(DataKeys.HAS_CREATED_POKEMON)){
                result.hasCreatedPokemon = nbt.getBoolean(DataKeys.HAS_CREATED_POKEMON)
            }
            result.fillLevel = result.organicMaterialInside * 8 / MATERIAL_TO_START
            return result
        }

    }
}