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
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.block.PastureBlock
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonPasturedPacket
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.entity.EntityPose
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.TypeFilter
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.util.*
import kotlin.math.ceil

class PokemonPastureBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CobblemonBlockEntities.PASTURE, pos, state) {
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
        val box = Box(minRoamPos, maxRoamPos)
        open fun canRoamTo(pos: BlockPos) = box.contains(pos.toCenterPos())

        fun toDTO(player: ServerPlayerEntity): OpenPasturePacket.PasturePokemonDataDTO? {
            val pokemon = getPokemon() ?: return null
            return OpenPasturePacket.PasturePokemonDataDTO(
                pokemonId = pokemonId,
                playerId = playerId,
                displayName = if (playerId == player.uuid) pokemon.getDisplayName() else lang("ui.pasture.owned_name", pokemon.getDisplayName(), playerName),
                species = pokemon.species.resourceIdentifier,
                aspects = pokemon.aspects,
                heldItem = pokemon.heldItem(),
                level = pokemon.level,
                entityKnown = (player.getWorld().getEntityById(entityId) as? PokemonEntity)?.tethering?.tetheringId == tetheringId
            )
        }
    }

    companion object {
        internal val TICKER = BlockEntityTicker<PokemonPastureBlockEntity> { world, _, _, blockEntity ->
            if (world.isClient) return@BlockEntityTicker
            blockEntity.ticksUntilCheck--
            if (blockEntity.ticksUntilCheck <= 0) {
                blockEntity.checkPokemon()
            }
            blockEntity.togglePastureOn(blockEntity.getInRangeViewerCount(world, blockEntity.pos) > 0)
        }
    }

    var ticksUntilCheck = Cobblemon.config.pastureBlockUpdateTicks
    val tetheredPokemon = mutableListOf<Tethering>()
    var minRoamPos: BlockPos
    var maxRoamPos: BlockPos
    var ownerId: UUID? = null
    var ownerName: String = ""

    init {
        val radius = Cobblemon.config.pastureMaxWanderDistance
        minRoamPos = pos.subtract(Vec3i(radius, radius, radius))
        maxRoamPos = pos.add(Vec3i(radius, radius, radius))
    }

    fun getMaxTethered() = Cobblemon.config.defaultPasturedPokemonLimit

    fun canAddPokemon(player: ServerPlayerEntity, pokemon: Pokemon, maxPerPlayer: Int): Boolean {
        val forThisPlayer = tetheredPokemon.count { it.playerId == player.uuid }
        // Shouldn't be possible, client should've prevented it
        if (forThisPlayer >= maxPerPlayer || tetheredPokemon.size >= getMaxTethered() || pokemon.isFainted()) {
            return false
        }
        val radius = Cobblemon.config.pastureMaxWanderDistance.toDouble()
        val bottom = pos.toVec3d().multiply(1.0, 0.0, 1.0)

        val pokemonWithinPastureWander = player.world.getEntitiesByClass(PokemonEntity::class.java, Box.of(bottom, radius, 99999.0, radius)) { true }.count()
        val chunkDiameter = (radius / 16) * 2 // Diameter
        if (pokemonWithinPastureWander >= Cobblemon.config.pastureMaxPerChunk * chunkDiameter * chunkDiameter) {
            player.sendPacket(ClosePasturePacket())
            player.sendMessage(lang("pasture.too_many_nearby").red(), true)
            return false
        }

        return true
    }


    fun tether(player: ServerPlayerEntity, pokemon: Pokemon, directionToBehind: Direction): Boolean {
        val world = world ?: return false
        val entity = PokemonEntity(world, pokemon = pokemon)
        entity.calculateDimensions()
        val width = entity.boundingBox.xLength

        val idealPlace = pos.add(directionToBehind.vector.multiply(ceil(width).toInt() + 1))
        var box = entity.getDimensions(EntityPose.STANDING).getBoxAt(idealPlace.toCenterPos().subtract(0.0, 0.5, 0.0))

        for (i in 0..5) {
            box = box.offset(directionToBehind.vector.x.toDouble(), 0.0, directionToBehind.vector.z.toDouble())
            val fixedPosition = makeSuitableY(world, idealPlace.add(directionToBehind.vector.multiply(i + 1)), entity, box)
            if (fixedPosition != null) {
                entity.setPosition(fixedPosition.toCenterPos().subtract(0.0, 0.5, 0.0))
                val pc = Cobblemon.storage.getPC(player.uuid)
                entity.beamMode = 2
                afterOnServer(seconds = SendOutPokemonHandler.SEND_OUT_DURATION) {
                    entity.beamMode = 0
                }
                if (world.spawnEntity(entity)) {
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
                    pokemon.tetheringId = tethering.tetheringId
                    tetheredPokemon.add(tethering)
                    entity.tethering = tethering
                    tethering.toDTO(player)?.let { player.sendPacket(PokemonPasturedPacket(it)) }
                    markDirty()
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
        val pastureBlock = cachedState.block as PastureBlock

        if (world != null && !world!!.isClient) {
            val world = world!!
            val posBottom = pastureBlock.getBasePosition(cachedState, pos)
            val stateBottom = world.getBlockState(posBottom)

            val posTop = pastureBlock.getPositionOfOtherPart(stateBottom, posBottom)
            val stateTop = world.getBlockState(posTop)

            try {
                if (stateBottom.get(PastureBlock.ON) != on) {
                    world.setBlockState(posTop, stateTop.with(PastureBlock.ON, on))
                    world.setBlockState(posBottom, stateBottom.with(PastureBlock.ON, on))
                }
            } catch (exception: IllegalArgumentException) {
                if (world.getBlockState(pos.up()).block is PastureBlock) {
                    world.setBlockState(pos.up(), Blocks.AIR.defaultState)
                } else {
                    world.setBlockState(pos.down(), Blocks.AIR.defaultState)
                }
                world.setBlockState(pos, Blocks.AIR.defaultState)
                world.spawnEntity(ItemEntity(world, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5, ItemStack(CobblemonBlocks.PASTURE)))
            }
        }
    }

    fun isSafeFloor(world: World, pos: BlockPos, entity: PokemonEntity): Boolean {
        val state = world.getBlockState(pos)
        return if (state.isAir) {
            false
        } else if (state.hasSolidTopSurface(world, pos, entity) || state.isSolidSurface(world, pos, entity, Direction.DOWN)) {
            true
        } else if ((entity.behaviour.moving.swim.canWalkOnWater || entity.behaviour.moving.swim.canSwimInWater) && state.fluidState.isIn(FluidTags.WATER)) {
            true
        } else {
            (entity.behaviour.moving.swim.canWalkOnLava || entity.behaviour.moving.swim.canSwimInLava) && state.fluidState.isIn(FluidTags.LAVA)
        }
    }

    // Place the tether block like this: https://gyazo.com/7c163bccfde238688e9a2c600c27aace
    // You'll find you can't place pokemon into the tether. It's because of this function somehow
    fun makeSuitableY(world: World, pos: BlockPos, entity: PokemonEntity, box: Box): BlockPos? {
        if (world.canCollide(entity, box)) {
            for (i in 1..15) {
                val newBox = box.offset(0.5, i.toDouble(), 0.5)

                if (!world.canCollide(entity, newBox) && isSafeFloor(world, pos.add(0, i - 1, 0), entity)) {
                    return pos.add(0, i, 0)
                }
            }
        } else {
            for (i in 1..15) {
                val newBox = box.offset(0.5, -i.toDouble(), 0.5)

                if (world.canCollide(entity, newBox) && isSafeFloor(world, pos.add(0, -i, 0), entity)) {
                    return pos.add(0, -i + 1, 0)
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
        markDirty()
    }

    fun onBroken() {
        if (world is ServerWorld) {
            tetheredPokemon.toList().forEach { releasePokemon(it.pokemonId) }
            PastureLinkManager.removeAt(world as ServerWorld, pos)
        }
    }

    fun releasePokemon(pokemonId: UUID) {
        val tethering = tetheredPokemon.find { it.pokemonId == pokemonId } ?: return
        tethering.getPokemon()?.tetheringId = null
        tetheredPokemon.remove(tethering)
        markDirty()
    }

    fun releaseAllPokemon(playerId: UUID): List<UUID> {
        val unpastured = mutableListOf<UUID>()
        tetheredPokemon.filter { it.playerId == playerId }.forEach {
            it.getPokemon()?.tetheringId = null
            tetheredPokemon.remove(it)
            unpastured.add(it.pokemonId)
        }
        markDirty()
        return unpastured
    }

    private fun getInRangeViewerCount(world: World, pos: BlockPos, range: Double = 5.0): Int {
        val box = Box(
            pos.x.toDouble() - range,
            pos.y.toDouble() - range,
            pos.z.toDouble() - range,
            (pos.x + 1).toDouble() + range,
            (pos.y + 1).toDouble() + range,
            (pos.z + 1).toDouble() + range
        )

        return world.getEntitiesByType(TypeFilter.instanceOf(ServerPlayerEntity::class.java), box, this::isPlayerViewing).size
    }

    private fun isPlayerViewing(player: ServerPlayerEntity): Boolean {
        val pastureLink = PastureLinkManager.getLinkByPlayer(player)
        return pastureLink != null && pastureLink.pos == pos && pastureLink.dimension == player.world.dimensionKey.value
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        val list = nbt.getList(DataKeys.TETHER_POKEMON, NbtCompound.COMPOUND_TYPE.toInt())
        this.ownerId = if (nbt.containsUuid(DataKeys.TETHER_OWNER_ID)) nbt.getUuid(DataKeys.TETHER_OWNER_ID) else null
        this.ownerName = nbt.getString(DataKeys.TETHER_OWNER_NAME).takeIf { it.isNotEmpty() } ?: ""
        for (tetheringNBT in list) {
            tetheringNBT as NbtCompound
            val tetheringId = tetheringNBT.getUuid(DataKeys.TETHERING_ID)
            val pokemonId = tetheringNBT.getUuid(DataKeys.POKEMON_UUID)
            val pcId = tetheringNBT.getUuid(DataKeys.PC_ID)
            val playerId = tetheringNBT.getUuid(DataKeys.TETHERING_PLAYER_ID)
            val entityId = tetheringNBT.getInt(DataKeys.TETHERING_ENTITY_ID)
            tetheredPokemon.add(Tethering(minRoamPos = minRoamPos, maxRoamPos = maxRoamPos, playerId = playerId, playerName = ownerName, tetheringId = tetheringId, pokemonId = pokemonId, pcId = pcId, entityId = entityId))
        }
        this.minRoamPos = NbtHelper.toBlockPos(nbt.getCompound(DataKeys.TETHER_MIN_ROAM_POS))
        this.maxRoamPos = NbtHelper.toBlockPos(nbt.getCompound(DataKeys.TETHER_MAX_ROAM_POS))
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        val list = NbtList()
        for (tethering in tetheredPokemon) {
            val tetheringNBT = NbtCompound()
            tetheringNBT.putUuid(DataKeys.TETHERING_ID, tethering.tetheringId)
            tetheringNBT.putUuid(DataKeys.TETHERING_PLAYER_ID, tethering.playerId)
            tetheringNBT.putUuid(DataKeys.POKEMON_UUID, tethering.pokemonId)
            tetheringNBT.putUuid(DataKeys.PC_ID, tethering.pcId)
            tetheringNBT.putInt(DataKeys.TETHERING_ENTITY_ID, tethering.entityId)
            list.add(tetheringNBT)
        }
        nbt.put(DataKeys.TETHER_POKEMON, list)
        nbt.put(DataKeys.TETHER_MIN_ROAM_POS, NbtHelper.fromBlockPos(minRoamPos))
        nbt.put(DataKeys.TETHER_MAX_ROAM_POS, NbtHelper.fromBlockPos(maxRoamPos))
        ownerId?.let { nbt.putUuid(DataKeys.TETHER_OWNER_ID, it) }
        nbt.putString(DataKeys.TETHER_OWNER_NAME, this.ownerName)
    }
}