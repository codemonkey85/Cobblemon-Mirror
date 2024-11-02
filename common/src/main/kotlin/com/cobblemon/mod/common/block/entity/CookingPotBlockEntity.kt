package com.cobblemon.mod.common.block.entity
/*
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.CookingPotBlock
import com.cobblemon.mod.common.gui.CookingPotScreenHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.*
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.minecraft.world.level.block.state.BlockState


class CookingPotBlockEntity(
        blockPos: BlockPos,
        blockState: BlockState
) : BaseContainerBlockEntity(CobblemonBlockEntities.COOKING_POT, blockPos, blockState) {

    var cookingPotInventory = CookingPotBlockInventory(this)
    var automationDelay: Int = AUTOMATION_DELAY
    var partialTicks = 0.0f

    val stateManager: ContainerOpenersCounter = object : ContainerOpenersCounter() {
        override fun onOpen(level: Level, pos: BlockPos, state: BlockState) {
            this@CookingPotBlockEntity.setOpen(state, true)
        }

        override fun onClose(level: Level, pos: BlockPos, state: BlockState) {
            this@CookingPotBlockEntity.setOpen(state, false)
        }

        override fun openerCountChanged(level: Level, pos: BlockPos, state: BlockState, oldCount: Int, newCount: Int) {
            // Implement if needed
        }

        override fun isOwnContainer(player: Player): Boolean {
            if (player.containerMenu is CookingPotScreenHandler) {
                val inventory = (player.containerMenu as CookingPotScreenHandler).inventory
                return inventory === this@CookingPotBlockEntity
            }
            return false
        }
    }

    companion object {
        const val AUTOMATION_DELAY = 4
        const val FILTER_TM_NBT = "FilterTM"
    }

    fun setOpen(state: BlockState, open: Boolean) {
        level!!.setBlockAndUpdate(worldPosition, state.setValue(CookingPotBlock.COOKING, open))
    }

    fun tick() {
        if (!this.isRemoved) {
            stateManager.recheckOpeners(level!!, worldPosition, blockState)
        }
    }

    override fun clearContent() {
        cookingPotInventory.clearContent()
    }

    override fun getContainerSize(): Int {
        return this.cookingPotInventory.containerSize
    }

    override fun isEmpty(): Boolean {
        return this.cookingPotInventory.isEmpty
    }

    override fun getItem(slot: Int): ItemStack {
        return this.cookingPotInventory.getItem(slot)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        return this.cookingPotInventory.removeItem(slot, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return this.cookingPotInventory.removeItemNoUpdate(slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        this.cookingPotInventory.setItem(slot, stack)
    }

    override fun stillValid(player: Player): Boolean {
        return this.cookingPotInventory.stillValid(player)
    }

    override fun getDisplayName(): Component {
        return Component.translatable("container.cooking_pot")
    }

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return CookingPotScreenHandler(syncId, playerInventory, this.cookingPotInventory, this)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun setChanged() {
        super.setChanged()
        if (this.level != null && !this.level!!.isClientSide) {
            val currentState = level!!.getBlockState(worldPosition)
            level!!.sendBlockUpdated(worldPosition, currentState, currentState, Block.UPDATE_ALL)
            this.level!!.neighborChanged(this.worldPosition, this.blockState.block, this.worldPosition)
        }
    }

    class CookingPotBlockInventory(val cookingPotBlockEntity: CookingPotBlockEntity) : WorldlyContainer {
        private val BLANK_DISC_SLOT_INDEX = 0
        private val GEM_SLOT_INDEX = 1
        private val MISC_SLOT_INDEX = 2
        private val OUTPUT_SLOT_INDEX = 3
        private val INPUT_SLOTS = intArrayOf(0, 1, 2, 3)
        val entityPos = this.cookingPotBlockEntity.blockPos
        val entityState = this.cookingPotBlockEntity.blockState
        val entityWorld = this.cookingPotBlockEntity.level

        var filterTM: ItemStack? = null
        var items: NonNullList<ItemStack> = NonNullList.withSize(4, ItemStack.EMPTY)

        fun hasFilterTM(): Boolean {
            return filterTM != null
        }

        override fun clearContent() {
            cookingPotBlockEntity.setChanged()
            this.items.clear()
        }

        override fun getContainerSize(): Int {
            return items.size
        }

        fun getInvStack(slot: Int): ItemStack {
            return items.getOrNull(slot) ?: ItemStack.EMPTY
        }

        override fun isEmpty(): Boolean {
            return items.all { it.isEmpty }
        }

        override fun getItem(slot: Int): ItemStack {
            return items.getOrNull(slot) ?: ItemStack.EMPTY
        }

        override fun getSlotsForFace(side: Direction): IntArray {
            return if (side == Direction.DOWN) {
                intArrayOf(this.OUTPUT_SLOT_INDEX)
            } else INPUT_SLOTS
        }

        override fun removeItem(slot: Int, amount: Int): ItemStack {
            cookingPotBlockEntity.setChanged()
            return ContainerHelper.removeItem(items, slot, amount)
        }

        override fun removeItemNoUpdate(slot: Int): ItemStack {
            val slotStack = items.getOrNull(slot)
            items[slot] = ItemStack.EMPTY
            cookingPotBlockEntity.setChanged()
            return slotStack ?: ItemStack.EMPTY
        }

        override fun setItem(slot: Int, stack: ItemStack) {
            items[slot] = stack
            cookingPotBlockEntity.setChanged()
        }

        override fun setChanged() {
            cookingPotBlockEntity.setChanged()
        }

        override fun stillValid(player: Player): Boolean {
            return Container.stillValidBlockEntity(cookingPotBlockEntity, player)
        }


        override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
            return false
        }

        override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction): Boolean {
            return dir == Direction.DOWN && slot == this.OUTPUT_SLOT_INDEX
        }
    }
}
*/
