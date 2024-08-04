/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.item.RodBaitComponent
import com.cobblemon.mod.common.item.berry.BerryItem
import com.cobblemon.mod.common.util.enchantmentRegistry
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.toEquipmentSlot
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent

class PokerodItem(val pokeRodId: ResourceLocation, settings: Properties) : FishingRodItem(settings) {

    companion object {
        fun getBaitOnRod(stack: ItemStack): FishingBait? {
            return stack.components.get(CobblemonItemComponents.BAIT)?.bait
        }

        fun getBaitStackOnRod(stack: ItemStack): ItemStack {
            return stack.components.get(CobblemonItemComponents.BAIT)?.stack ?: ItemStack.EMPTY
        }

        fun setBait(stack: ItemStack, bait: ItemStack) {
            if (bait.isEmpty) {
                stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                return
            }
            val fishingBait = FishingBaits.getFromBaitItemStack(bait) ?: return
            stack.set(CobblemonItemComponents.BAIT, RodBaitComponent(fishingBait, bait))
            // add a new component that stores the itemStack as a component? Yes!
        }

        fun consumeBait(stack: ItemStack) {
            val baitStack = getBaitStackOnRod(stack)
            val baitCount = baitStack.count
            if (baitCount == 1) {
                stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                return
            }
            if (baitCount > 1) {
                val fishingBait = FishingBaits.getFromBaitItemStack(baitStack) ?: return
                stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, RodBaitComponent(fishingBait, ItemStack(baitStack.item, baitCount - 1)))
            }
        }

        fun getBaitEffects(stack: ItemStack): List<FishingBait.Effect> {
            return getBaitOnRod(stack)?.effects ?: return emptyList()
        }
    }

    //var bait: ItemStack = ItemStack.EMPTY
    //var baitEffects: List<FishingBait.Effect>? = mutableListOf()

    // Fishing Rod: Bundle edition
    override fun overrideOtherStackedOnMe(
        itemStack: ItemStack,
        itemStack2: ItemStack,
        slot: Slot,
        clickAction: ClickAction,
        player: Player,
        slotAccess: SlotAccess
    ): Boolean {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player))
            return false

        val baitStack = getBaitStackOnRod(itemStack)

        // If not holding an item on cursor
        if (itemStack2.isEmpty) {
            // Retrieve bait onto cursor
            if(baitStack != ItemStack.EMPTY) {
                playDetachSound(player)
                setBait(itemStack, ItemStack.EMPTY)
                slotAccess.set(baitStack.copy())
                return true
            }
        }
        // If holding item on cursor
        else {

            // If item on cursor is a valid bait
            if (FishingBaits.getFromBaitItemStack(itemStack2) != null) {

                // Add as much as possible
                if (baitStack != ItemStack.EMPTY) {
                    if (baitStack.item == itemStack2.item) {

                        playAttachSound(player)
                        // Calculate how much bait to add
                        val diff = (baitStack.maxStackSize - baitStack.count).coerceIn(0, itemStack2.count)
                        itemStack2.shrink(diff)
                        baitStack.grow(diff)
                        setBait(itemStack, baitStack)
                        return true
                    }

                    // If Item on rod is different from cursor item, swap them
                    playAttachSound(player)
                    setBait(itemStack, itemStack2.copy())
                    slotAccess.set(baitStack.copy())
                    return true
                }

                // If no bait currently on rod, add all
                playAttachSound(player)
                setBait(itemStack, itemStack2.copy())
                itemStack2.shrink(itemStack2.count)
                return true
            }
        }
        return false
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        // if item in mainhand is berry item then don't do anything
//        if (user.getItemInHand(InteractionHand.MAIN_HAND).item is BerryItem)
//            return InteractionResultHolder(
//                InteractionResult.FAIL,
//                user.getItemInHand(hand)
//            )

        val itemStack = user.getItemInHand(hand)
        val offHandItem = user.getItemInHand(InteractionHand.OFF_HAND)
        val offHandBait = FishingBaits.getFromBaitItemStack(offHandItem)

        // if there already is bait on the bobber then drop it on the ground
        var baitOnRod = getBaitOnRod(itemStack)

        // if the item in the offhand is a bait item and the mainhand item is a pokerod then apply the bait
//        if (!world.isClientSide && user.fishing == null && offHandBait != null && offHandBait != baitOnRod && !user.isShiftKeyDown) {
//
//            if (baitOnRod != null) {
//                val item = world.itemRegistry.get(baitOnRod.item)
//                if (item != null) {
//                    user.spawnAtLocation(ItemStack(item))
//                }
//            }
//
//            // set the bait and bait effects on the bobber
//            setBait(itemStack, offHandItem.copyWithCount(1))
//
//            // remove 1 bait from the offhand
//            offHandItem.shrink(1)
//
//            // remove old bait tooltip from rod
////            removeBaitTooltip(itemStack, world)
//
//            // set new bait tooltip to rod
////            setBaitTooltips(itemStack, world)
//        }

        // if the user is sneaking when casting then remove the bait from the bobber
//        if (!world.isClientSide && user.fishing == null && user.isShiftKeyDown) {
//            // If there is a bait on the bobber
//            if (baitOnRod != null) {
//                // drop the stack of bait
//                val item = world.itemRegistry.get(baitOnRod.item)
//                if (item != null) {
//                    user.spawnAtLocation(ItemStack(item))
//                }
//                //set the bait and bait effects on the rod to be empty
//                setBait(itemStack, ItemStack.EMPTY)
//
//                // remove old bait tooltip from rod
//                removeBaitTooltip(itemStack, world)
//            }
//        }

        // If rod is empty and offhand has bait, add bait from offhand
        if (!world.isClientSide && user.fishing == null && offHandBait != null && baitOnRod == null) {
            playAttachSound(user)
            setBait(itemStack, offHandItem.copy())
            offHandItem.shrink(offHandItem.count)
        }

        val i: Int
        if (user.fishing != null) { // if the bobber is out yet
            if (!world.isClientSide) {
                i = user.fishing!!.retrieve(itemStack)
                itemStack.hurtAndBreak(i, user, hand.toEquipmentSlot())
            }

            // stop sound of casting when reeling in
            //(MinecraftClient.getInstance().getSoundManager() as SoundManagerDuck).stopSounds(CobblemonSounds.FISHING_ROD_CAST.id, SoundCategory.PLAYERS)



            //(MinecraftClient.getInstance().getSoundManager()).stop

            world.playSound(null as Player?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_REEL_IN, SoundSource.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))
            user.gameEvent(GameEvent.ITEM_INTERACT_FINISH)
        } else { // if the bobber is not out yet
            // play the Rod casting sound and set it
            world.playSound(null as Player?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_CAST, SoundSource.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))

            // create a SoundInstance for the casting sound to be also sent to the bobber
            val castingSoundInstance = SimpleSoundInstance(
                    CobblemonSounds.FISHING_ROD_CAST,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f),
                    world.random,
                    user.x,
                    user.y,
                    user.z
            )

            if (!world.isClientSide) {
                val lureLevel = EnchantmentHelper.getItemEnchantmentLevel(world.enchantmentRegistry.getHolder(Enchantments.LURE).get(), itemStack)
                val luckLevel = EnchantmentHelper.getItemEnchantmentLevel(world.enchantmentRegistry.getHolder(Enchantments.LUCK_OF_THE_SEA).get(), itemStack)

                /*// play the Rod casting sound and set it
                world.playSound(null as PlayerEntity?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f))

                // create a SoundInstance for the casting sound to be also sent to the bobber
                val castingSoundInstance = CancellableSoundInstance(
                        CobblemonSounds.FISHING_ROD_CAST,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f),
                        world.random,
                        user.x,
                        user.y,
                        user.z
                )*/


                val bobberEntity = PokeRodFishingBobberEntity(user, pokeRodId, getBaitOnRod(itemStack)?.toItemStack(world.itemRegistry) ?: ItemStack.EMPTY, world, luckLevel, lureLevel, castingSoundInstance, itemStack)

                // Set the casting sound to the bobber entity
                //bobberEntity.castingSound = castingSoundInstance

                world.addFreshEntity(bobberEntity)
                CobblemonCriteria.CAST_POKE_ROD.trigger(user as ServerPlayer, baitOnRod != null)
            }

            user.awardStat(Stats.ITEM_USED.get(this))
            user.gameEvent(GameEvent.ITEM_INTERACT_START)
        }
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }

    override fun getEnchantmentValue(): Int {
        return 1
    }

    override fun getDescriptionId(): String {
        return "item.cobblemon.poke_rod"
    }

    private fun playAttachSound(entity: Entity) {
        entity.playSound(CobblemonSounds.FISHING_BAIT_ATTACH, 1F, 1F)
    }

    private fun playDetachSound(entity: Entity) {
        entity.playSound(CobblemonSounds.FISHING_BAIT_DETACH, 1F, 1F)
    }

}
