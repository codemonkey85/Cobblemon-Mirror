package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.text.Text
import com.cobblemon.mod.common.block.CookingPotBlock
import com.cobblemon.mod.common.gui.CookingPotScreenHandler
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.block.entity.ViewerCountManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity

class CookingPotBlockEntity(
        val blockPos: BlockPos,
        val blockState: BlockState
) : LockableContainerBlockEntity(CobblemonBlockEntities.COOKING_POT, blockPos, blockState) {

    var cookingPotInventory = CookingPotBlockInventory(this)
    var automationDelay: Int = AUTOMATION_DELAY
    var partialTicks = 0.0f

    val stateManager: ViewerCountManager = object : ViewerCountManager() {
        override fun onContainerOpen(world: World, pos: BlockPos, state: BlockState) {
            this@CookingPotBlockEntity.setOpen(state, true)
        }

        override fun onContainerClose(world: World, pos: BlockPos, state: BlockState) {
            this@CookingPotBlockEntity.setOpen(state, false)
        }

        override fun onViewerCountUpdate(world: World, pos: BlockPos, state: BlockState, oldViewerCount: Int, newViewerCount: Int) {}
        override fun isPlayerViewing(player: PlayerEntity): Boolean {
            if (player.currentScreenHandler is CookingPotScreenHandler) {
                val inventory = (player.currentScreenHandler as CookingPotScreenHandler).inventory
                return inventory === this@CookingPotBlockEntity
            }
            return false
        }
    }

    companion object {
        const val AUTOMATION_DELAY = 4
        const val FILTER_TM_NBT = "FilterTM"
    }

    override fun getPos(): BlockPos {
        return blockPos
    }

    override fun getCachedState(): BlockState {
        return blockState
    }

    fun setOpen(state: BlockState, open: Boolean) {
        world!!.setBlockState(pos, state.with(CookingPotBlock.COOKING, open), Block.NOTIFY_ALL)
    }

    fun tick() {
        if (!removed) {
            stateManager.updateViewerCount(world, pos, cachedState)
        }
    }

    override fun clear() {
        cookingPotInventory.clear()
    }

    override fun size(): Int {
        return this.cookingPotInventory.size()
    }

    override fun isEmpty(): Boolean {
        return this.cookingPotInventory.isEmpty()
    }

    override fun getStack(slot: Int): ItemStack {
        return this.cookingPotInventory.getStack(slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return this.cookingPotInventory.removeStack(slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        return this.cookingPotInventory.removeStack(slot)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        return this.cookingPotInventory.setStack(slot, stack)
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return this.cookingPotInventory.canPlayerUse(player)
    }

    override fun getContainerName(): Text {
        return Text.translatable("container.brewing")
    }

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory?): ScreenHandler {
        return CookingPotScreenHandler(syncId, playerInventory!!, this.cookingPotInventory, this)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun markDirty() {
        super.markDirty()
        if (this.world != null && !this.world!!.isClient) {
            val currentState = world!!.getBlockState(pos)
            world!!.setBlockState(pos, currentState, 3) // Flags: 2 | 1 = Block update | Render update
            this.world!!.updateNeighborsAlways(this.pos, this.cachedState.block)
        }
    }

    class CookingPotBlockInventory(val cookingPotBlockEntity: CookingPotBlockEntity) : SidedInventory {
        private val BLANK_DISC_SLOT_INDEX = 0
        private val GEM_SLOT_INDEX = 1
        private val MISC_SLOT_INDEX = 2
        private val OUTPUT_SLOT_INDEX = 3
        private val INPUT_SLOTS = intArrayOf(0, 1, 2, 3)
        val entityPos = this.cookingPotBlockEntity.pos
        val entityState = this.cookingPotBlockEntity.cachedState
        val entityWorld = this.cookingPotBlockEntity.world

        var filterTM: ItemStack? = null
        var items: DefaultedList<ItemStack?>? = DefaultedList.ofSize(4, ItemStack.EMPTY)

        fun hasFilterTM(): Boolean {
            return filterTM != null
        }

        override fun clear() {
            cookingPotBlockEntity.markDirty()
            this.items?.clear()
        }

        override fun size(): Int {
            return this.items?.size ?: 0
        }

        fun getInvStack(slot: Int): ItemStack {
            return items?.get(slot) ?: ItemStack.EMPTY
        }

        override fun isEmpty(): Boolean {
            for (i in 0 until size()) {
                val stack: ItemStack = getInvStack(i)
                if (!stack.isEmpty) {
                    return false
                }
            }
            return true
        }

        override fun getStack(slot: Int): ItemStack {
            return items?.get(slot) ?: ItemStack.EMPTY
        }

        override fun getAvailableSlots(side: Direction?): IntArray? {
            return if (side == Direction.DOWN) {
                return listOf(this.OUTPUT_SLOT_INDEX).toIntArray()
            } else this.INPUT_SLOTS
        }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            cookingPotBlockEntity.markDirty()
            return Inventories.splitStack(items, slot, amount)
        }

        override fun removeStack(slot: Int): ItemStack {
            val slotStack = items?.get(slot)
            items?.set(slot, ItemStack.EMPTY)
            cookingPotBlockEntity.markDirty()
            return slotStack ?: ItemStack.EMPTY
        }

        override fun setStack(slot: Int, stack: ItemStack?) {
            items?.set(slot, stack)
            cookingPotBlockEntity.markDirty()
        }

        override fun markDirty() {
            if (cookingPotBlockEntity.world != null)
                cookingPotBlockEntity.markDirty()
        }

        override fun canPlayerUse(player: PlayerEntity?): Boolean {
            return Inventory.canPlayerUse(this.cookingPotBlockEntity, player)
        }

        override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
            return false
        }

        override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
            return dir == Direction.DOWN && slot == this.OUTPUT_SLOT_INDEX
        }
    }
}
