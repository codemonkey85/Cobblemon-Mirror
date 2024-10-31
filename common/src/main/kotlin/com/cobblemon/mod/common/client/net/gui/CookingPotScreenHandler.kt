package com.cobblemon.mod.common.gui

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.CookingPotBlockEntity
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import com.cobblemon.mod.common.gui.CobblemonScreenHandlers
import net.minecraft.world.Container
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.inventory.CraftingContainer

class CookingPotScreenHandler : RecipeBookMenu<CraftingContainer> {
    val input: CraftingContainer
    val result: ResultContainer
    val context: ContainerLevelAccess
    val player: Player
    var inventory: Inventory? = null


    companion object {
        const val RESULT_ID = 0
        private const val INPUT_START = 1
        private const val INPUT_END = 10
        private const val INVENTORY_START = 10
        private const val INVENTORY_END = 37
        private const val HOTBAR_START = 37
        private const val HOTBAR_END = 46

        fun updateResult(
                handler: AbstractContainerMenu,
                level: Level,
                player: Player,
                craftingInventory: CraftingContainer,
                resultInventory: ResultContainer
        ) {
            if (level.isClientSide) return
            val serverPlayerEntity = player as ServerPlayer
            var itemStack = ItemStack.EMPTY
            val optional = level.server?.recipeManager?.getRecipeFor(
                RecipeType.CRAFTING, craftingInventory, level
            )
            if (optional != null) {
                if (optional.isPresent) {
                    val craftingRecipe = optional.get()
                    val itemStack2 = craftingRecipe.assemble(craftingInventory, level.registryAccess())
                    if (itemStack2.isEnabled(level.enabledFeatures)) {
                        itemStack = itemStack2
                    }
                }
            }
            resultInventory.setItem(0, itemStack)
            handler.setRemoteSlot(0, itemStack)
            serverPlayerEntity.connection.send(
                    ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, itemStack)
            )
        }
    }

    constructor(syncId: Int, playerInventory: Inventory, context: ContainerLevelAccess) :
            super(CobblemonScreenHandlers.COOKING_POT_SCREEN, syncId) {
        this.context = context
        this.player = playerInventory.player
        this.input = CraftingContainer(this, 3, 3)
        this.result = ResultContainer()
        initializeSlots(playerInventory)
    }

    constructor(syncId: Int, playerInventory: Inventory, cookingPotInventory: CookingPotBlockEntity.CookingPotBlockInventory, cookingPotBlockEntity: CookingPotBlockEntity) :
            super(CobblemonScreenHandlers.COOKING_POT_SCREEN, syncId) {
        this.context = ContainerLevelAccess.NULL
        this.player = playerInventory.player
        this.input = CraftingContainer(this, 3, 3)
        this.result = ResultContainer()
        this.inventory = cookingPotInventory
        initializeSlots(playerInventory)
    }


    private fun initializeSlots(playerInventory: Inventory) {
        val craftingGridOffsetX = 14
        val craftingGridOffsetY = 10
        val craftingOutputOffsetX = 16
        val craftingOutputOffsetY = 10
        val playerInventoryOffsetX = 0
        val playerInventoryOffsetY = 16

        addSlot(ResultSlot(playerInventory.player, input, result, 0, 124 + craftingOutputOffsetX, 35 + craftingOutputOffsetY))


        for (i in 0..2) {
            for (j in 0..2) {
                addSlot(Slot(input, j + i * 3, 30 + craftingGridOffsetX + j * 18, 17 + craftingGridOffsetY + i * 18))
            }
        }
        for (i in 0..2) {
            for (j in 0..8) {
                addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + playerInventoryOffsetX + j * 18, 84 + playerInventoryOffsetY+ i * 18))
            }
        }
        for (i in 0..8) {
            addSlot(Slot(playerInventory, i, 8 + playerInventoryOffsetX + i * 18, 142 + playerInventoryOffsetY))
        }
    }ResultSlot


    override fun slotsChanged(container: Container) {
        context.run { level, pos -> updateResult(this@CookingPotScreenHandler, level, player, input, result) }
    }

    override fun fillCraftSlotsStackedContents(stackedContents: StackedContents) {
        input.fillStackedContents(stackedContents)
    }

    override fun clearCraftingContent() {
        input.clearContent()
        result.clearContent()
    }

    override fun recipeMatches(recipe: Recipe<in CraftingContainer>): Boolean {
        return recipe.matches(input, player.level)
    }

    override fun removed(player: Player) {
        super.removed(player)
        access.execute { level, pos -> clearContainer(player, input) }
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, CobblemonBlocks.COOKING_POT)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val slotStack = slot.item
            itemStack = slotStack.copy()
            if (index == 0) {
                access.execute { level, pos -> slotStack.item.onCraftedBy(slotStack, level, player) }
                if (!moveItemStackTo(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickCraft(slotStack, itemStack)
            } else if (index in 10 until 46) {
                if (!moveItemStackTo(slotStack, 1, 10, false) && (index < 37 && !moveItemStackTo(
                                slotStack,
                                37,
                                46,
                                false
                        ) || !moveItemStackTo(slotStack, 10, 37, false))
                ) {
                    return ItemStack.EMPTY
                }
            } else if (!moveItemStackTo(slotStack, 10, 46, false)) {
                return ItemStack.EMPTY
            }
            if (slotStack.isEmpty) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
            if (slotStack.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTake(player, slotStack)
            if (index == 0) {
                player.drop(slotStack, false)
            }
        }
        return itemStack
    }

    override fun canTakeItemForPickAll(stack: ItemStack, slot: Slot): Boolean {
        return slot.container !== result && super.canTakeItemForPickAll(stack, slot)
    }

    override fun getResultSlotIndex(): Int {
        return RESULT_ID
    }

    override fun getGridWidth(): Int {
        return input.width
    }

    override fun getGridHeight(): Int {
        return input.height
    }

    override fun getSize(): Int {
        return 10
    }

    override fun getRecipeBookType(): RecipeBookType {
        return RecipeBookType.CRAFTING
    }

    override fun shouldMoveToInventory(index: Int): Boolean {
        return index != resultSlotIndex
    }
}
