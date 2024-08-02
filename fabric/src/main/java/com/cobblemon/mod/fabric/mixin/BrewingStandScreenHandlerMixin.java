/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.brewing.BrewingRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to allow the player to fix the stacking of potion items in the Brewing Stand
 * @author Aethen
 * @since 02/20/2024
 */
@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandScreenHandlerMixin {

    /**
     * Allows quick moving stacked PokePotion bases into Brewing Stands.
     * Injects into the 'quickMove' method of BrewingStandScreenHandler at the beginning of the function.
     * @param player PlayerEntity that is moving the item
     * @param index Index of the slot that the item is being moved from
     * @param cir CallbackInfoReturnable that allows the return value to be modified
     */
    @Inject(method = "quickMoveStack", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        final BrewingStandMenu brewingStandScreenHandler = (BrewingStandMenu) (Object) this;

        Slot screenSlot = brewingStandScreenHandler.slots.get(index);
        if (screenSlot.hasItem())
        {
            ItemStack stackInSlot = screenSlot.getItem();

            // We don't care if:
            // - this item is being taken FROM the stand
            // - is not a base in a Cobblemon recipe
            // as it functions as intended in those cases
            if (index < 5 || BrewingRecipes.INSTANCE.getRecipes().stream().noneMatch(recipe -> recipe.component1().matches(stackInSlot)))
                return;

            if (!BrewingStandMenu.PotionSlot.mayPlaceItem(stackInSlot))
                return;

            if (customInsertItem(stackInSlot, 0, 3))
                cir.setReturnValue(stackInSlot);
            else
                cir.setReturnValue(ItemStack.EMPTY);
        }

    }


    /**
     * A trimmed copy of ScreenHandler.InsertItem.
     * Attempts to move a single item from the given stack to a different position in the Screen Inventory.
     * "Screen Inventory" = A temporary inventory made up of both the interacted storage block and Player's inventory.
     * @param stack The stack being inserted.
     * @param startIndex The first Screen Inventory index to try to move to.
     * @param endIndex The last Screen Inventory index to try to move to.
     * @return True if a single item was moved, False if it failed to.
     */
    @Unique
    protected boolean customInsertItem(ItemStack stack, int startIndex, int endIndex) {
        final BrewingStandMenu brewingStandScreenHandler = (BrewingStandMenu) (Object) this;

        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (!stack.isEmpty()) {
            while (i < endIndex) {
                slot = brewingStandScreenHandler.slots.get(i);
                itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(stack.split(stack.getCount()));
                    }
                    slot.setChanged();
                    bl = true;
                    break;
                }
                ++i;
            }
        }
        return bl;
    }
}
