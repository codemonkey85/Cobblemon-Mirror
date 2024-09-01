/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.loot

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.NestedLootTable
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import org.jetbrains.annotations.ApiStatus

/**
 * Used to append our loot table injections to existing ones.
 * This is not meant for API use and is only public due to visibility requirement in the platform implementations.
 *
 * @since June 8th, 2023
 */
@ApiStatus.Internal
object LootInjector {

    private const val PREFIX = "injection/"

    private val VILLAGE_HOUSE = cobblemonResource("injection/chests/village_house")

    private val villageHouseBuiltInLootTables = hashSetOf(
        BuiltInLootTables.VILLAGE_DESERT_HOUSE, 
        BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
        BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
        BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
        BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
    )

    private val injections = hashSetOf(
        BuiltInLootTables.ABANDONED_MINESHAFT,
        BuiltInLootTables.ANCIENT_CITY,
        BuiltInLootTables.BASTION_BRIDGE,
        BuiltInLootTables.BASTION_HOGLIN_STABLE,
        BuiltInLootTables.BASTION_OTHER,
        BuiltInLootTables.BASTION_TREASURE,
        BuiltInLootTables.END_CITY_TREASURE,
        BuiltInLootTables.IGLOO_CHEST,
        BuiltInLootTables.JUNGLE_TEMPLE,
        BuiltInLootTables.NETHER_BRIDGE,
        BuiltInLootTables.PILLAGER_OUTPOST,
        BuiltInLootTables.SHIPWRECK_SUPPLY,
        BuiltInLootTables.SIMPLE_DUNGEON,
        BuiltInLootTables.SPAWN_BONUS_CHEST,
        BuiltInLootTables.STRONGHOLD_CORRIDOR,
        BuiltInLootTables.WOODLAND_MANSION,
        BuiltInLootTables.FISHING_TREASURE
    ).apply { addAll(villageHouseBuiltInLootTables) }

    private val injectionIds = injections.map {it.location()}.toSet()

    private val villageInjectionIds = villageHouseBuiltInLootTables.map { it.location() }.toSet()

    /**
     * Attempts to inject a Cobblemon injection loot table to a loot table being loaded.
     * This will automatically query the existence of an injection.
     *
     * @param id The [ResourceLocation] of the loot table being loaded.
     * @param provider The job invoked if the injection is possible, this is what the platform needs to do to append the loot table.
     * @return If the injection was made.
     */
    fun attemptInjection(id: ResourceLocation, provider: (LootPool.Builder) -> Unit): Boolean {
        if (!this.injectionIds.contains(id)) {
            return false
        }
        val resulting = this.convertToPotentialInjected(id)
        Cobblemon.LOGGER.debug("{}: Injected {} to {}", this::class.simpleName, resulting, id)
        provider(this.injectLootPool(resulting))
        return true
    }

    /**
     * Takes a source ID and converts it into the target injection.
     *
     * @param source The [ResourceLocation] of the base loot table.
     * @return The [ResourceLocation] for the expected Cobblemon injection.
     */
    private fun convertToPotentialInjected(source: ResourceLocation): ResourceLocation {
        if (this.villageInjectionIds.contains(source)) {
            return VILLAGE_HOUSE
        }
        return cobblemonResource("$PREFIX${source.path}")
    }

    /**
     * Creates a loot pool builder with our injection.
     *
     * @param resulting The [ResourceLocation] for our injection table.
     * @return A [LootPool.Builder] with the [resulting] table.
     */
    private fun injectLootPool(resulting: ResourceLocation): LootPool.Builder {
        return LootPool.lootPool()
            .add(
                NestedLootTable
                    .lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, resulting))
                    .setWeight(1)
            )
            .setBonusRolls(UniformGenerator.between(0F, 1F))
    }

}