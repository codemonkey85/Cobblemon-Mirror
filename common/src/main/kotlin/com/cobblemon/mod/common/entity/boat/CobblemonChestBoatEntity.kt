/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.boat

import com.cobblemon.mod.common.CobblemonEntities
import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.HasCustomInventoryScreen
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.ContainerEntity
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.storage.loot.LootTable

@Suppress("unused")
class CobblemonChestBoatEntity(entityType: EntityType<CobblemonChestBoatEntity>, world: Level) : CobblemonBoatEntity(entityType, world), HasCustomInventoryScreen, ContainerEntity {

    constructor(world: Level) : this(CobblemonEntities.CHEST_BOAT, world)

    // This exists cause super passes in vanilla boat entity type
    constructor(world: Level, x: Double, y: Double, z: Double) : this(CobblemonEntities.CHEST_BOAT, world) {
        this.setPos(x, y, z)
        this.xo = x
        this.yo = y
        this.zo = z
    }

    private var inventory = this.emptyInventory()
    private var lootTableId: ResourceKey<LootTable>? = null
    private var lootTableSeed = 0L

    override fun openCustomInventoryScreen(player: Player) {
        player.openMenu(this)
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player)
            PiglinAi.angerNearbyPiglins(player, true)
        }
    }

    override fun clearContent() = this.clearItemStacks()

    override fun getContainerSize(): Int = INVENTORY_SLOTS

    override fun getItem(slot: Int): ItemStack = this.getChestVehicleItem(slot)

    override fun removeItem(slot: Int, amount: Int): ItemStack = this.removeChestVehicleItem(slot, amount)

    override fun removeItemNoUpdate(slot: Int): ItemStack = this.removeChestVehicleItemNoUpdate(slot)

    override fun setItem(slot: Int, stack: ItemStack) = this.setChestVehicleItem(slot, stack)

    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean = this.isChestVehicleStillValid(player)

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu? {
        if (this.lootTableId != null && player.isSpectator) {
            return null
        }
        this.unpackChestVehicleLootTable(playerInventory.player)
        return ChestMenu.threeRows(syncId, playerInventory, this)
    }

    override fun getLootTable() = lootTableId

    override fun setLootTable(lootTable: ResourceKey<LootTable>?) {
        this.lootTableId = lootTable
    }

    override fun getLootTableSeed(): Long = this.lootTableSeed

    override fun setLootTableSeed(lootTableSeed: Long) {
        this.lootTableSeed = lootTableSeed
    }

    override fun getItemStacks(): NonNullList<ItemStack> = this.inventory

    override fun clearItemStacks() {
        this.inventory = this.emptyInventory()
    }

    override fun getDropItem(): Item = this.boatType.chestBoatItem

    private fun emptyInventory(): NonNullList<ItemStack> = NonNullList.withSize(INVENTORY_SLOTS, ItemStack.EMPTY)

    companion object {

        private const val INVENTORY_SLOTS = 27

    }

}