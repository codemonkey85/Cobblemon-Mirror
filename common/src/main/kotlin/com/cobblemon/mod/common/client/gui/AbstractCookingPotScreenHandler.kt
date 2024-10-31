package com.cobblemon.mod.common.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.recipe.book.RecipeBookCategory
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.FurnaceOutputSlot
import net.minecraft.screen.slot.Slot
import net.minecraft.world.World

abstract class AbstractCookingPotScreenHandler(
    type: ScreenHandlerType<*>?,
    private val recipeType: RecipeType<out AbstractCookingRecipe?>,
    private val category: RecipeBookCategory,
    syncId: Int,
    playerInventory: PlayerInventory?,
    inventory: Inventory? = SimpleInventory(3),
    propertyDelegate: PropertyDelegate? = ArrayPropertyDelegate(
        4
    )
) :
    AbstractRecipeScreenHandler<Inventory?>(type, syncId) {
    lateinit var inventory: Inventory
    lateinit var propertyDelegate: PropertyDelegate
    lateinit var world: World

    init {
        checkSize(inventory, 3)
        checkDataCount(propertyDelegate, 4)
        if (inventory != null) {
            this.inventory = inventory
        }
        if (propertyDelegate != null) {
            this.propertyDelegate = propertyDelegate
        }
        if (playerInventory != null) {
            this.world = playerInventory.player.world
        }
        this.addSlot(Slot(inventory, 0, 56, 17))
        //this.addSlot(FurnaceFuelSlot(this, inventory, 1, 56, 53))
        if (playerInventory != null) {
            this.addSlot(FurnaceOutputSlot(playerInventory.player, inventory, 2, 116, 35))
        }
        var i = 0
        while (i < 3) {
            for (j in 0..8) {
                this.addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
            ++i
        }

        i = 0
        while (i < 9) {
            this.addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
            ++i
        }

        this.addProperties(propertyDelegate)
    }

    override fun populateRecipeFinder(finder: RecipeMatcher) {
        if (inventory is RecipeInputProvider) {
            (inventory as RecipeInputProvider).provideRecipeInputs(finder)
        }
    }

    override fun clearCraftingSlots() {
        getSlot(0).setStackNoCallbacks(ItemStack.EMPTY)
        getSlot(2).setStackNoCallbacks(ItemStack.EMPTY)
    }

    override fun matches(recipe: Recipe<in Inventory?>): Boolean {
        return recipe.matches(this.inventory, this.world)
    }

    override fun getCraftingResultSlotIndex(): Int {
        return 2
    }

    override fun getCraftingWidth(): Int {
        return 1
    }

    override fun getCraftingHeight(): Int {
        return 1
    }

    override fun getCraftingSlotCount(): Int {
        return 3
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return inventory.canPlayerUse(player)
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot2 = slots[slot]
        if (slot2 != null && slot2.hasStack()) {
            val itemStack2 = slot2.stack
            itemStack = itemStack2.copy()
            if (slot == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY
                }

                slot2.onQuickTransfer(itemStack2, itemStack)
            } else if (slot != 1 && slot != 0) {
                /*if (this.isSmeltable(itemStack2)) {
                    if (!this.insertItem(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                }*/ /*else if (this.isFuel(itemStack2)) {
                    if (!this.insertItem(itemStack2, 1, 2, false)) {
                        return ItemStack.EMPTY
                    }
                }*/ if (slot >= 3 && slot < 30) {
                    if (!this.insertItem(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (slot >= 30 && slot < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.insertItem(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY
            }

            if (itemStack2.isEmpty) {
                slot2.stack = ItemStack.EMPTY
            } else {
                slot2.markDirty()
            }

            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }

            slot2.onTakeItem(player, itemStack2)
        }

        return itemStack
    }

    /*fun isSmeltable(itemStack: ItemStack): Boolean {
        return world.recipeManager.getFirstMatch(
            this.recipeType, SimpleInventory(*arrayOf(itemStack)),
            this.world
        ).isPresent
    }*/

    /*protected fun isFuel(itemStack: ItemStack?): Boolean {
        return AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)
    }*/

    val cookProgress: Int
        get() {
            val i = propertyDelegate[2]
            val j = propertyDelegate[3]
            return if (j != 0 && i != 0) i * 24 / j else 0
        }

    val fuelProgress: Int
        get() {
            var i = propertyDelegate[1]
            if (i == 0) {
                i = 200
            }

            return propertyDelegate[0] * 13 / i
        }

    val isBurning: Boolean
        get() = propertyDelegate[0] > 0

    override fun getCategory(): RecipeBookCategory {
        return this.category
    }

    override fun canInsertIntoSlot(index: Int): Boolean {
        return index != 1
    }

    companion object {
        const val field_30738: Int = 0
        const val field_30739: Int = 1
        const val field_30740: Int = 2
        const val field_30741: Int = 3
        const val field_30742: Int = 4
        private const val field_30743 = 3
        private const val field_30744 = 30
        private const val field_30745 = 30
        private const val field_30746 = 39
    }
}