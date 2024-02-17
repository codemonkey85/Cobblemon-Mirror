package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.interactive.PokerodItem
import net.minecraft.client.item.ClampedModelPredicateProvider
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object CobblemonModelPredicateRegistry {

    fun registerPredicates() {
        ModelPredicateProviderRegistry.register(CobblemonItems.POKEROD, Identifier("cast"), ClampedModelPredicateProvider { stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int ->
            if (entity == null) {
                return@ClampedModelPredicateProvider 0.0f
            } else {
                val bl = entity.mainHandStack == stack
                var bl2 = entity.offHandStack == stack
                if (entity.mainHandStack.item is PokerodItem) {
                    bl2 = false
                }

                return@ClampedModelPredicateProvider if ((bl || bl2) && entity is PlayerEntity && (entity.fishHook != null)) 1.0f else 0.0f
            }
        })
    }
}