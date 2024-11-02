package com.cobblemon.mod.common.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.MenuType

/*
object CobblemonScreenHandlers {
    //val TMM_SCREEN = register(cobblemonResource("tmm_screen"), ::TMMScreenHandler)
    val COOKING_POT_SCREEN = register(cobblemonResource("cooking_pot_screen")) { syncId, playerInventory ->
        CookingPotScreenHandler(syncId, playerInventory, ContainerLevelAccess.NULL)
    }

    fun <T : AbstractContainerMenu> register(identifier: ResourceLocation, factory: MenuSupplier<T>): MenuType<T> {

        val result = MenuType(factory)
        Cobblemon.implementation.registerMenuType(identifier, result)
        return result
    }

}*/
