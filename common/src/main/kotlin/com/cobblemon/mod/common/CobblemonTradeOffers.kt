/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerTrades

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
        VillagerProfession.FARMER -> listOf(
            VillagerTradeOffer(VillagerProfession.FARMER, 3, listOf(VillagerTrades.ItemsForEmeralds(CobblemonItems.VIVICHOKE_SEEDS, 24, 1, 1, 6)))
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
     * Represents a trade offer that can be attached to a villager or a wandering trader.
     */
    interface TradeOfferHolder {
        /**
         * The list of the possible [TradeOffers.Factory].
         */
        val tradeOffers: List<VillagerTrades.ItemListing>
    }

    /**
     * Represents a trade offer from a villager.
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
    ) : TradeOfferHolder {

        init {
            if (this.requiredLevel < VillagerData.MIN_VILLAGER_LEVEL || this.requiredLevel > VillagerData.MAX_VILLAGER_LEVEL) {
                throw IllegalArgumentException("${this.requiredLevel} is not a valid level for a villager trade accepted range is ${VillagerData.MIN_VILLAGER_LEVEL}-${VillagerData.MAX_VILLAGER_LEVEL}")
            }
        }

    }

    /**
     * Represents a trade offer from a wandering trader.
     *
     * @property isRareTrade If this is rare or common trade.
     * @property tradeOffers The list of the possible [TradeOffers.Factory].
     */
    data class WandererTradeOffer(
        val isRareTrade: Boolean,
        override val tradeOffers: List<VillagerTrades.ItemListing>
    ) : TradeOfferHolder
    
}