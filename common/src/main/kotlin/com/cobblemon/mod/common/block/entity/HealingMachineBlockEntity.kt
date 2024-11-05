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
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.block.HealingMachineBlock
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.*
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.hashMapOf
import kotlin.collections.hashSetOf
import kotlin.collections.isNotEmpty
import kotlin.collections.set
import kotlin.math.floor
import net.minecraft.world.phys.AABB

@Suppress("MemberVisibilityCanBePrivate", "unused")
class HealingMachineBlockEntity(
    blockPos: BlockPos,
    blockState: BlockState
) : BlockEntity(CobblemonBlockEntities.HEALING_MACHINE, blockPos, blockState) {
    var currentUser: UUID? = null
        private set

    var healTimeLeft: Int = 0
    var healingCharge: Float = 0.0F
    val isInUse: Boolean
        get() = currentUser != null
    var infinite: Boolean = false

    var currentSignal = 0
        private set

    var maxCharge: Float = 6F

    private var dataSnapshot: DataSnapshot? = null
    /**
     * Represents the PokéBalls occupying this entity.
     * The key is the equivalent party slot in index form.
     */
    private val pokeBalls: MutableMap<Int, PokeBall> = hashMapOf()

    init {
        maxCharge = (Cobblemon.config.maxHealerCharge).coerceAtLeast(6F)
        this.updateRedstoneSignal()
        this.updateBlockChargeLevel()
    }

    /**
     * Resolves the currently occupying PokéBalls.
     * The key is the equivalent party index of the [currentUser].
     * The value is the [Pokemon.caughtBall] of the Pokémon in said party index.
     *
     * @return The PokéBalls in this healing machine.
     */
    fun pokeBalls(): Map<Int, PokeBall> = this.pokeBalls

    fun setUser(user: UUID, party: PartyStore) {
        this.clearData()

        this.pokeBalls.clear()
        party.toGappyList().forEachIndexed { index, pokemon ->
            if (pokemon != null) {
                this.pokeBalls[index] = pokemon.caughtBall
            }
        }
        this.currentUser = user
        this.healTimeLeft = 24

        markUpdated()
    }

    fun canHeal(party: PartyStore): Boolean {
        if (Cobblemon.config.infiniteHealerCharge || this.infinite) {
            return true
        }
        val neededHealthPercent = party.getHealingRemainderPercent()
        return this.healingCharge >= neededHealthPercent
    }

    fun activate(user: UUID, party: PartyStore) {
        if (!Cobblemon.config.infiniteHealerCharge && this.healingCharge != maxCharge) {
            val neededHealthPercent = party.getHealingRemainderPercent()
            this.healingCharge = (healingCharge - neededHealthPercent).coerceIn(0F..maxCharge)
            this.updateRedstoneSignal()
        }
        this.setUser(user, party)
        alreadyHealing.add(user)
        updateBlockChargeLevel(HealingMachineBlock.MAX_CHARGE_LEVEL + 1)
        val world = level ?: return
        if (!world.isClientSide) world.playSoundServer(
            position = blockPos.toVec3d(),
            sound = CobblemonSounds.HEALING_MACHINE_ACTIVE,
            volume = 1F,
            pitch = 1F
        )
    }

    fun completeHealing() {
        val currentUser = currentUser ?: return clearData()
        val player = currentUser.getPlayer()
        if (player != null) {
            val party = player.party()
            party.heal()
            player.sendSystemMessage(lang("healingmachine.healed").green(), true)
        } else {
            val npc = level
                ?.getEntities(null, AABB.ofSize(blockPos.toVec3d(), 10.0, 10.0, 10.0)) { it.uuid == currentUser && it is NPCEntity }
                ?.firstOrNull() as? NPCEntity
            val party = npc?.party
            if (party != null) {
                party.heal()
                npc.sendSystemMessage(lang("healingmachine.healed").green()) // An NPC can read text, right?
            }
        }
        updateBlockChargeLevel()
        clearData()
    }

    override fun loadAdditional(
        compoundTag: CompoundTag,
        registryLookup: HolderLookup.Provider
    ) {
        super.loadAdditional(compoundTag, registryLookup)

        this.pokeBalls.clear()

        if (compoundTag.hasUUID(DataKeys.HEALER_MACHINE_USER)) {
            this.currentUser = compoundTag.getUUID(DataKeys.HEALER_MACHINE_USER)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_POKEBALLS)) {
            val pokeBallsTag = compoundTag.getCompound(DataKeys.HEALER_MACHINE_POKEBALLS)
            // Keep around for compat with old format
            var index = 0
            for (key in pokeBallsTag.allKeys) {
                val pokeBallId = pokeBallsTag.getString(key)
                if (pokeBallId.isEmpty()) {
                    continue
                }
                val actualIndex = key.toIntOrNull() ?: index
                val pokeBall = PokeBalls.getPokeBall(ResourceLocation.parse(pokeBallId))
                if (pokeBall != null) {
                    this.pokeBalls[actualIndex] = pokeBall
                }
                index++
            }
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_TIME_LEFT)) {
            this.healTimeLeft = compoundTag.getInt(DataKeys.HEALER_MACHINE_TIME_LEFT)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_CHARGE)) {
            this.healingCharge = compoundTag.getFloat(DataKeys.HEALER_MACHINE_CHARGE).coerceIn(0F..maxCharge)
        }
        if (compoundTag.contains(DataKeys.HEALER_MACHINE_INFINITE)) {
            this.infinite = compoundTag.getBoolean(DataKeys.HEALER_MACHINE_INFINITE)
        }
    }

    override fun saveAdditional(
        compoundTag: CompoundTag,
        registryLookup: HolderLookup.Provider
    ) {
        super.saveAdditional(compoundTag, registryLookup)

        if (this.currentUser != null) {
            compoundTag.putUUID(DataKeys.HEALER_MACHINE_USER, this.currentUser!!)
        } else {
            compoundTag.remove(DataKeys.HEALER_MACHINE_USER)
        }

        if (this.pokeBalls().isNotEmpty()) {
            val pokeBallsTag = CompoundTag()
            this.pokeBalls().forEach { (index, pokeBall) ->
                pokeBallsTag.putString(index.toString(), pokeBall.name.toString())
            }
            compoundTag.put(DataKeys.HEALER_MACHINE_POKEBALLS, pokeBallsTag)
        } else {
            compoundTag.remove(DataKeys.HEALER_MACHINE_POKEBALLS)
        }

        compoundTag.putInt(DataKeys.HEALER_MACHINE_TIME_LEFT, this.healTimeLeft)
        compoundTag.putFloat(DataKeys.HEALER_MACHINE_CHARGE, this.healingCharge)
        compoundTag.putBoolean(DataKeys.HEALER_MACHINE_INFINITE, this.infinite)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(provider: HolderLookup.Provider): CompoundTag {
        return super.saveWithFullMetadata(provider)
    }

    override fun setRemoved() {
        this.snapshotAndClearData()
        super.setRemoved()
    }

    override fun clearRemoved() {
        this.restoreSnapshot()
        super.clearRemoved()
    }

    private fun updateRedstoneSignal() {
        if (Cobblemon.config.infiniteHealerCharge || this.infinite) {
            this.currentSignal = MAX_REDSTONE_SIGNAL
        }
        val remainder = ((this.healingCharge / maxCharge) * 100).toInt() / 10
        this.currentSignal = remainder.coerceAtMost(MAX_REDSTONE_SIGNAL)
    }

    private fun updateBlockChargeLevel(level: Int? = null) {
        val world = this.level ?: return
        if (!world.isClientSide) {
            val chargeLevel = (level
                ?: if (Cobblemon.config.infiniteHealerCharge || this.infinite) HealingMachineBlock.MAX_CHARGE_LEVEL
                else floor((healingCharge / maxCharge) * HealingMachineBlock.MAX_CHARGE_LEVEL).toInt()
                    ).coerceIn(0..HealingMachineBlock.MAX_CHARGE_LEVEL + 1)

            val state = world.getBlockState(blockPos)
            if (state.block is HealingMachineBlock) {
                val currentCharge = state.getValue(HealingMachineBlock.CHARGE_LEVEL).toInt()
                if (chargeLevel != currentCharge)
                    world.setBlockAndUpdate(blockPos, state.setValue(HealingMachineBlock.CHARGE_LEVEL, chargeLevel))
            }
        }
    }

    private fun markUpdated() {
        this.setChanged()
        level!!.sendBlockUpdated(blockPos, blockState, blockState, 3)
    }

    private fun snapshotAndClearData() {
        this.dataSnapshot = DataSnapshot(
            this.currentUser,
            this.pokeBalls(),
            this.healTimeLeft
        )
        this.clearData()
    }

    private fun clearData() {
        this.currentUser?.let(alreadyHealing::remove)
        this.currentUser = null
        this.pokeBalls.clear()
        this.healTimeLeft = 0
        markUpdated()
    }

    private fun restoreSnapshot() {
        this.dataSnapshot?.let {
            pokeBalls.clear()
            currentUser = it.currentUser
            pokeBalls.putAll(it.pokeBalls)
            healTimeLeft = it.healTimeLeft
        }
    }

    private data class DataSnapshot(
        val currentUser: UUID?,
        val pokeBalls: Map<Int, PokeBall>,
        val healTimeLeft: Int
    )

    companion object {
        private val alreadyHealing = hashSetOf<UUID>()
        const val MAX_REDSTONE_SIGNAL = 10

        internal val TICKER = BlockEntityTicker<HealingMachineBlockEntity> { world, _, _, blockEntity ->
            if (world.isClientSide) return@BlockEntityTicker

            // Healing progression
            if (blockEntity.isInUse) {
                if (blockEntity.healTimeLeft > 0) {
                    blockEntity.healTimeLeft--
                } else {
                    blockEntity.completeHealing()
                }
            } else {
                // Recharging
                if (blockEntity.healingCharge < blockEntity.maxCharge) {
                    val chargePerTick = (Cobblemon.config.chargeGainedPerTick).coerceAtLeast(0F)
                    blockEntity.healingCharge =
                        (blockEntity.healingCharge + chargePerTick).coerceIn(0F..blockEntity.maxCharge)
                    blockEntity.updateBlockChargeLevel()
                    blockEntity.updateRedstoneSignal()
                    blockEntity.markUpdated()
                }
            }
        }

        fun isUsingHealer(player: Player) = this.alreadyHealing.contains(player.uuid)

    }
}