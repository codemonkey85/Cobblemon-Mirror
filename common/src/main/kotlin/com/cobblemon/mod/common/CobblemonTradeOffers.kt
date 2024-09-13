/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerTrades
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import java.util.Optional

/**
 * A generator for various trade offers in the mod.
 */
object CobblemonTradeOffers {

    /**
     * Creates a list of all Cobblemon trade offers for every villager profession.
     *
     * @return The resulting list.
     */
    fun tradeOffersForAll(): List<VillagerTradeOffer> = BuiltInRegistries.VILLAGER_PROFESSION.map(this::tradeOffersFor).flatten()

    /**
     * Creates a list of all Cobblemon trade offers for the given [VillagerProfession].
     *
     * @param profession The [VillagerProfession] being queried.
     * @return The resulting list.
     */
    fun tradeOffersFor(profession: VillagerProfession): List<VillagerTradeOffer> = when (profession) {
        CobblemonVillagerProfessions.NURSE -> listOf(
            // requiredLevel = the level of the villager for the trade to appear for
            // Novice = 1, Apprentice = 2, Journeyman = 3, Expert = 4, Master = 5
            VillagerTradeOffer(CobblemonVillagerProfessions.NURSE, 1, listOf(
                // i = emeraldCost, j = numberOfItems, k = maxUses, l = villagerXp, f = priceMultiplier
                VillagerTrades.ItemsForEmeralds(CobblemonItems.ORAN_BERRY, 4, 1, 16, 1),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.PERSIM_BERRY, 4, 1, 16, 1),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.ENERGY_ROOT, 4, 1, 12, 1),
                TradeOffer(ItemCost(CobblemonItems.MEDICINAL_LEEK, 24), ItemStack(Items.EMERALD, 1), 12, 2)
            )),
            VillagerTradeOffer(CobblemonVillagerProfessions.NURSE, 2, listOf(
                TradeOffer(ItemCost(Items.EMERALD, 8), ItemStack(CobblemonItems.MEDICINAL_BREW), 8, 5, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.BLUE_MINT_LEAF, 2, 1, 8, 5),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.CYAN_MINT_LEAF, 2, 1, 8, 5),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.GREEN_MINT_LEAF, 2, 1, 8, 5),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.PINK_MINT_LEAF, 2, 1, 8, 5),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.RED_MINT_LEAF, 2, 1, 8, 5),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.WHITE_MINT_LEAF, 2, 1, 8, 5)
            )),
            VillagerTradeOffer(CobblemonVillagerProfessions.NURSE, 3, listOf(
                TradeOffer(ItemCost(CobblemonItems.MULCH_BASE, 4), ItemStack(Items.EMERALD, 2), 16, 20),
                VillagerTrades.ItemsForEmeralds(Items.GLASS_BOTTLE, 1, 1, 16, 10)
            )),
            VillagerTradeOffer(CobblemonVillagerProfessions.NURSE, 4, listOf(
                VillagerTrades.ItemsForEmeralds(CobblemonItems.REVIVAL_HERB, 8, 1, 16, 15),
                VillagerTrades.ItemsForEmeralds(CobblemonItems.VIVICHOKE_SEEDS, 24, 1, 4, 15)
            )),
            VillagerTradeOffer(CobblemonVillagerProfessions.NURSE, 5, listOf(
                TradeOffer(ItemCost(Items.EMERALD, 10), ItemStack(CobblemonItems.ANTIDOTE, 1), 12, 30, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                TradeOffer(ItemCost(Items.EMERALD, 10), ItemStack(CobblemonItems.AWAKENING, 1), 12, 30, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                TradeOffer(ItemCost(Items.EMERALD, 10), ItemStack(CobblemonItems.BURN_HEAL, 1), 12, 30, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                TradeOffer(ItemCost(Items.EMERALD, 10), ItemStack(CobblemonItems.ICE_HEAL, 1), 12, 30, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                TradeOffer(ItemCost(Items.EMERALD, 10), ItemStack(CobblemonItems.PARALYZE_HEAL, 1), 12, 30, Optional.of<ItemCost>(ItemCost(Items.GLASS_BOTTLE))),
                TradeOffer(ItemCost(Items.EMERALD, 22), ItemStack(CobblemonItems.VIVICHOKE_DIP, 1), 4, 30, Optional.of<ItemCost>(ItemCost(Items.BOWL)))
            ))
        )
        VillagerProfession.FISHERMAN -> listOf(
            VillagerTradeOffer(VillagerProfession.FISHERMAN, 5, listOf(
                VillagerTrades.ItemsForEmeralds(CobblemonItems.POKEROD_SMITHING_TEMPLATE, 12, 3, 30)
            ))
        )
        else -> emptyList()
    }

    /**
     * Creates a list of all Cobblemon trade offers for the Wandering trader.
     *
     * @return The resulting list.
     */
    fun resolveWanderingTradeOffers(): List<WandererTradeOffer> = listOf(
        WandererTradeOffer(false, listOf(VillagerTrades.ItemsForEmeralds(CobblemonItems.VIVICHOKE_SEEDS, 24, 1, 1, 6)))
    )

    /**
     * Represents trade offers that can be attached to a villager or a wandering trader.
     */
    interface TradeOfferHolder {
        /**
         * The list of the possible [TradeOffers.Factory].
         */
        val tradeOffers: List<VillagerTrades.ItemListing>
    }

    /**
     * Represents trade offers from a villager.
     *
     * @property profession The target profession for this trade.
     * @property requiredLevel The level required must be a possible level.
     * @property tradeOffers The list of the possible [TradeOffers.Factory].
     *
     * @throws IllegalArgumentException If the [requiredLevel] is not within the bounds of [VillagerData.MIN_LEVEL] & [VillagerData.MAX_LEVEL].
     */
    data class VillagerTradeOffer(
        val profession: VillagerProfession,
        val requiredLevel: Int,
        override val tradeOffers: List<VillagerTrades.ItemListing>
    ): TradeOfferHolder {

        init {
            if (this.requiredLevel < VillagerData.MIN_VILLAGER_LEVEL || this.requiredLevel > VillagerData.MAX_VILLAGER_LEVEL) {
                throw IllegalArgumentException("${this.requiredLevel} is not a valid level for a villager trade accepted range is ${VillagerData.MIN_VILLAGER_LEVEL}-${VillagerData.MAX_VILLAGER_LEVEL}")
            }
        }

    }

    /**
     * Represents trade offers from a wandering trader.
     *
     * @property isRareTrade If this is rare or common trade.
     * @property tradeOffers The list of the possible [TradeOffers.Factory].
     */
    data class WandererTradeOffer(
        val isRareTrade: Boolean,
        override val tradeOffers: List<VillagerTrades.ItemListing>
    ): TradeOfferHolder

    /**
     * Represents a trade offer from a villager.
     *
     * @property offeredItem The item cost offered to the villager.
     * @property receivedItem The item received from the villager.
     * @property maxUses How many times the trade can be used before the villager resupplies.
     * @property villagerXp The experience the villager gains from the trade.
     * @property optionalOfferedItem The optional, secondary item cost offered to the villager.
     * @property priceMultiplier The amount multiplier of how much the item cost can rise and fall by depending on discounts and penalties
     */
    class TradeOffer(
        private val offeredItem: ItemCost,
        private val receivedItem: ItemStack,
        private val maxUses: Int,
        private val villagerXp: Int = 1,
        private val optionalOfferedItem: Optional<ItemCost> = Optional.empty(),
        private val priceMultiplier: Float = 0.05F
    ): VillagerTrades.ItemListing {
        override fun getOffer(entity: Entity?, randomSource: RandomSource?): MerchantOffer = MerchantOffer(offeredItem, optionalOfferedItem, receivedItem, maxUses, villagerXp, priceMultiplier)
    }
}