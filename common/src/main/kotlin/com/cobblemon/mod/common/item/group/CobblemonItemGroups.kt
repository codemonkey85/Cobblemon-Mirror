/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.group

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemGroup.*
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Suppress("unused", "UNUSED_PARAMETER")
object CobblemonItemGroups {

    // See https://docs.google.com/spreadsheets/d/1QgaIlW-S9A-Blqhc-5G7OO3igaQdEAiYQqVEPnEBFmc/edit#gid=978365418 for what goes what

    private val ALL = arrayListOf<ItemGroupHolder>()
    private val INJECTORS = hashMapOf<RegistryKey<ItemGroup>, (injector: Injector) -> Unit>()

    @JvmStatic val BLOCKS_KEY = this.create("blocks", this::blockEntries) { ItemStack(CobblemonItems.PC) }
    @JvmStatic val POKEBALLS_KEY = this.create("pokeball", this::pokeballentries) { ItemStack(CobblemonItems.POKE_BALL) }
    @JvmStatic val AGRICULTURE_KEY = this.create("agriculture", this::agricultureEntries) { ItemStack(CobblemonItems.MEDICINAL_LEEK) }
    @JvmStatic val ARCHAEOLOGY_KEY = this.create("archaeology", this::archaeologyEntries) { ItemStack(CobblemonItems.HELIX_FOSSIL) }
    @JvmStatic val CONSUMABLES_KEY = this.create("consumables", this::consumableEntries) { ItemStack(CobblemonItems.ROASTED_LEEK) }
    @JvmStatic val HELD_ITEMS_KEY = this.create("held_item", this::heldItemEntries) { ItemStack(CobblemonItems.EXP_SHARE) }
    @JvmStatic val EVOLUTION_ITEMS_KEY = this.create("evolution_item", this::evolutionItemEntries) { ItemStack(CobblemonItems.BLACK_AUGURITE) }

    @JvmStatic val BLOCKS get() = Registries.ITEM_GROUP.get(BLOCKS_KEY)
    @JvmStatic val POKEBALLS get() = Registries.ITEM_GROUP.get(POKEBALLS_KEY)
    @JvmStatic val AGRICULTURE get() = Registries.ITEM_GROUP.get(AGRICULTURE_KEY)
    @JvmStatic val ARCHAEOLOGY get() = Registries.ITEM_GROUP.get(ARCHAEOLOGY_KEY)
    @JvmStatic val CONSUMABLES get() = Registries.ITEM_GROUP.get(CONSUMABLES_KEY)
    @JvmStatic val HELD_ITEMS get() = Registries.ITEM_GROUP.get(HELD_ITEMS_KEY)
    @JvmStatic val EVOLUTION_ITEMS get() = Registries.ITEM_GROUP.get(EVOLUTION_ITEMS_KEY)

    @JvmStatic val FOOD_INJECTIONS = this.inject(RegistryKey.of(Registries.ITEM_GROUP.key, Identifier("food_and_drinks")), this::foodInjections)
    @JvmStatic val TOOLS_AND_UTILITIES_INJECTIONS = this.inject(RegistryKey.of(Registries.ITEM_GROUP.key, Identifier("tools_and_utilities")), this::toolsAndUtilitiesInjections)
    @JvmStatic val INGREDIENTS_INJECTIONS = this.inject(RegistryKey.of(Registries.ITEM_GROUP.key, Identifier("ingredients")), this::ingredientsInjections)

    fun register(consumer: (holder: ItemGroupHolder) -> ItemGroup) {
        ALL.forEach(consumer::invoke)
    }

    fun inject(tabKey: RegistryKey<ItemGroup>, injector: Injector) {
        INJECTORS[tabKey]?.invoke(injector)
    }

    fun injectorKeys(): Collection<RegistryKey<ItemGroup>> = this.INJECTORS.keys

    data class ItemGroupHolder(
        val key: RegistryKey<ItemGroup>,
        val displayIconProvider: () -> ItemStack,
        val entryCollector: EntryCollector,
        val displayName: Text = Text.translatable("itemGroup.${key.value.namespace}.${key.value.path}")
    )

    private fun create(name: String, entryCollector: EntryCollector, displayIconProvider: () -> ItemStack): RegistryKey<ItemGroup> {
        val key = RegistryKey.of(Registries.ITEM_GROUP.key, cobblemonResource(name))
        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
        return key
    }

    private fun inject(key: RegistryKey<ItemGroup>, consumer: (injector: Injector) -> Unit): (injector: Injector) -> Unit {
        this.INJECTORS[key] = consumer
        return consumer
    }

    private fun agricultureEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.MEDICINAL_LEEK)
        entries.add(CobblemonItems.BIG_ROOT)
        entries.add(CobblemonItems.ENERGY_ROOT)
        entries.add(CobblemonItems.REVIVAL_HERB)
        entries.add(CobblemonItems.PEP_UP_FLOWER)
        entries.add(CobblemonItems.MENTAL_HERB)
        entries.add(CobblemonItems.POWER_HERB)
        entries.add(CobblemonItems.WHITE_HERB)
        entries.add(CobblemonItems.MIRROR_HERB)
        entries.add(CobblemonItems.VIVICHOKE)
        entries.add(CobblemonItems.VIVICHOKE_SEEDS)

        entries.add(CobblemonItems.RED_APRICORN)
        entries.add(CobblemonItems.YELLOW_APRICORN)
        entries.add(CobblemonItems.GREEN_APRICORN)
        entries.add(CobblemonItems.BLUE_APRICORN)
        entries.add(CobblemonItems.PINK_APRICORN)
        entries.add(CobblemonItems.BLACK_APRICORN)
        entries.add(CobblemonItems.WHITE_APRICORN)
        entries.add(CobblemonItems.RED_APRICORN_SEED)
        entries.add(CobblemonItems.YELLOW_APRICORN_SEED)
        entries.add(CobblemonItems.GREEN_APRICORN_SEED)
        entries.add(CobblemonItems.BLUE_APRICORN_SEED)
        entries.add(CobblemonItems.PINK_APRICORN_SEED)
        entries.add(CobblemonItems.BLACK_APRICORN_SEED)
        entries.add(CobblemonItems.WHITE_APRICORN_SEED)

        entries.add(CobblemonItems.RED_MINT_SEEDS)
        entries.add(CobblemonItems.RED_MINT_LEAF)
        entries.add(CobblemonItems.BLUE_MINT_SEEDS)
        entries.add(CobblemonItems.BLUE_MINT_LEAF)
        entries.add(CobblemonItems.CYAN_MINT_SEEDS)
        entries.add(CobblemonItems.CYAN_MINT_LEAF)
        entries.add(CobblemonItems.PINK_MINT_SEEDS)
        entries.add(CobblemonItems.PINK_MINT_LEAF)
        entries.add(CobblemonItems.GREEN_MINT_SEEDS)
        entries.add(CobblemonItems.GREEN_MINT_LEAF)
        entries.add(CobblemonItems.WHITE_MINT_SEEDS)
        entries.add(CobblemonItems.WHITE_MINT_LEAF)

        entries.add(CobblemonItems.GROWTH_MULCH)
        entries.add(CobblemonItems.RICH_MULCH)
        entries.add(CobblemonItems.SURPRISE_MULCH)
        entries.add(CobblemonItems.LOAMY_MULCH)
        entries.add(CobblemonItems.COARSE_MULCH)
        entries.add(CobblemonItems.PEAT_MULCH)
        entries.add(CobblemonItems.HUMID_MULCH)
        entries.add(CobblemonItems.SANDY_MULCH)
        entries.add(CobblemonItems.MULCH_BASE)

        CobblemonItems.berries().values.forEach(entries::add)
    }

    private fun archaeologyEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.HELIX_FOSSIL)
        entries.add(CobblemonItems.DOME_FOSSIL)
        entries.add(CobblemonItems.OLD_AMBER_FOSSIL)
        entries.add(CobblemonItems.ROOT_FOSSIL)
        entries.add(CobblemonItems.CLAW_FOSSIL)
        entries.add(CobblemonItems.SKULL_FOSSIL)
        entries.add(CobblemonItems.ARMOR_FOSSIL)
        entries.add(CobblemonItems.COVER_FOSSIL)
        entries.add(CobblemonItems.PLUME_FOSSIL)
        entries.add(CobblemonItems.JAW_FOSSIL)
        entries.add(CobblemonItems.SAIL_FOSSIL)
        entries.add(CobblemonItems.FOSSILIZED_BIRD)
        entries.add(CobblemonItems.FOSSILIZED_FISH)
        entries.add(CobblemonItems.FOSSILIZED_DRAKE)
        entries.add(CobblemonItems.FOSSILIZED_DINO)

        entries.add(CobblemonItems.TUMBLESTONE)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.SKY_TUMBLESTONE)

        entries.add(CobblemonItems.SMALL_BUDDING_TUMBLESTONE)
        entries.add(CobblemonItems.SMALL_BUDDING_BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.SMALL_BUDDING_SKY_TUMBLESTONE)

        entries.add(CobblemonItems.MEDIUM_BUDDING_TUMBLESTONE)
        entries.add(CobblemonItems.MEDIUM_BUDDING_BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.MEDIUM_BUDDING_SKY_TUMBLESTONE)

        entries.add(CobblemonItems.LARGE_BUDDING_TUMBLESTONE)
        entries.add(CobblemonItems.LARGE_BUDDING_BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.LARGE_BUDDING_SKY_TUMBLESTONE)

        entries.add(CobblemonItems.TUMBLESTONE_CLUSTER)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_CLUSTER)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_CLUSTER)

        entries.add(CobblemonItems.TUMBLESTONE_BLOCK)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BLOCK)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BLOCK)

        entries.add(CobblemonItems.BYGONE_SHERD)
        entries.add(CobblemonItems.CAPTURE_SHERD)
        entries.add(CobblemonItems.DOME_SHERD)
        entries.add(CobblemonItems.HELIX_SHERD)
        entries.add(CobblemonItems.NOSTALGIC_SHERD)
        entries.add(CobblemonItems.SUSPICIOUS_SHERD)

        entries.add(CobblemonItems.AUTOMATON_ARMOR_TRIM_SMITHING_TEMPLATE)

        entries.add(CobblemonItems.RELIC_COIN)
        entries.add(CobblemonItems.RELIC_COIN_POUCH)
        entries.add(CobblemonItems.RELIC_COIN_SACK)
        entries.add(CobblemonItems.GILDED_CHEST)
        entries.add(CobblemonItems.YELLOW_GILDED_CHEST)
        entries.add(CobblemonItems.GREEN_GILDED_CHEST)
        entries.add(CobblemonItems.BLUE_GILDED_CHEST)
        entries.add(CobblemonItems.PINK_GILDED_CHEST)
        entries.add(CobblemonItems.BLACK_GILDED_CHEST)
        entries.add(CobblemonItems.WHITE_GILDED_CHEST)
        entries.add(CobblemonItems.GIMMIGHOUL_CHEST)

        entries.add(CobblemonItems.NORMAL_GEM)
        entries.add(CobblemonItems.FIRE_GEM)
        entries.add(CobblemonItems.WATER_GEM)
        entries.add(CobblemonItems.GRASS_GEM)
        entries.add(CobblemonItems.ELECTRIC_GEM)
        entries.add(CobblemonItems.ICE_GEM)
        entries.add(CobblemonItems.FIGHTING_GEM)
        entries.add(CobblemonItems.POISON_GEM)
        entries.add(CobblemonItems.GROUND_GEM)
        entries.add(CobblemonItems.FLYING_GEM)
        entries.add(CobblemonItems.PSYCHIC_GEM)
        entries.add(CobblemonItems.BUG_GEM)
        entries.add(CobblemonItems.ROCK_GEM)
        entries.add(CobblemonItems.GHOST_GEM)
        entries.add(CobblemonItems.DRAGON_GEM)
        entries.add(CobblemonItems.DARK_GEM)
        entries.add(CobblemonItems.STEEL_GEM)
        entries.add(CobblemonItems.FAIRY_GEM)
    }

    private fun blockEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.RESTORATION_TANK)
        entries.add(CobblemonItems.FOSSIL_ANALYZER)
        entries.add(CobblemonItems.MONITOR)
        entries.add(CobblemonItems.PC)
        entries.add(CobblemonItems.HEALING_MACHINE)
        entries.add(CobblemonItems.PASTURE)

        entries.add(CobblemonItems.GILDED_CHEST)
        entries.add(CobblemonItems.YELLOW_GILDED_CHEST)
        entries.add(CobblemonItems.GREEN_GILDED_CHEST)
        entries.add(CobblemonItems.BLUE_GILDED_CHEST)
        entries.add(CobblemonItems.PINK_GILDED_CHEST)
        entries.add(CobblemonItems.BLACK_GILDED_CHEST)
        entries.add(CobblemonItems.WHITE_GILDED_CHEST)
        entries.add(CobblemonItems.GIMMIGHOUL_CHEST)
        entries.add(CobblemonItems.RELIC_COIN_POUCH)
        entries.add(CobblemonItems.RELIC_COIN_SACK)

        entries.add(CobblemonItems.DISPLAY_CASE)
        entries.add(CobblemonItems.APRICORN_LOG)
        entries.add(CobblemonItems.APRICORN_WOOD)
        entries.add(CobblemonItems.STRIPPED_APRICORN_LOG)
        entries.add(CobblemonItems.STRIPPED_APRICORN_WOOD)
        entries.add(CobblemonItems.APRICORN_PLANKS)
        entries.add(CobblemonItems.APRICORN_STAIRS)
        entries.add(CobblemonItems.APRICORN_SLAB)
        entries.add(CobblemonItems.APRICORN_FENCE)
        entries.add(CobblemonItems.APRICORN_FENCE_GATE)
        entries.add(CobblemonItems.APRICORN_DOOR)
        entries.add(CobblemonItems.APRICORN_TRAPDOOR)
        entries.add(CobblemonItems.APRICORN_BUTTON)
        entries.add(CobblemonItems.APRICORN_PRESSURE_PLATE)
        entries.add(CobblemonItems.APRICORN_SIGN)
        entries.add(CobblemonItems.APRICORN_HANGING_SIGN)
        entries.add(CobblemonItems.APRICORN_LEAVES)

        entries.add(CobblemonItems.TUMBLESTONE_BLOCK)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BLOCK)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BLOCK)

        entries.add(CobblemonItems.POLISHED_TUMBLESTONE)
        entries.add(CobblemonItems.POLISHED_TUMBLESTONE_STAIRS)
        entries.add(CobblemonItems.POLISHED_TUMBLESTONE_SLAB)
        entries.add(CobblemonItems.POLISHED_TUMBLESTONE_WALL)
        entries.add(CobblemonItems.CHISELED_POLISHED_TUMBLESTONE)
        entries.add(CobblemonItems.TUMBLESTONE_BRICKS)
        entries.add(CobblemonItems.TUMBLESTONE_BRICK_STAIRS)
        entries.add(CobblemonItems.TUMBLESTONE_BRICK_SLAB)
        entries.add(CobblemonItems.TUMBLESTONE_BRICK_WALL)
        entries.add(CobblemonItems.CHISELED_TUMBLESTONE_BRICKS)
        entries.add(CobblemonItems.POLISHED_BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_STAIRS)
        entries.add(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_SLAB)
        entries.add(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_WALL)
        entries.add(CobblemonItems.CHISELED_POLISHED_BLACK_TUMBLESTONE)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BRICKS)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BRICK_STAIRS)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BRICK_SLAB)
        entries.add(CobblemonItems.BLACK_TUMBLESTONE_BRICK_WALL)
        entries.add(CobblemonItems.CHISELED_BLACK_TUMBLESTONE_BRICKS)
        entries.add(CobblemonItems.POLISHED_SKY_TUMBLESTONE)
        entries.add(CobblemonItems.POLISHED_SKY_TUMBLESTONE_STAIRS)
        entries.add(CobblemonItems.POLISHED_SKY_TUMBLESTONE_SLAB)
        entries.add(CobblemonItems.POLISHED_SKY_TUMBLESTONE_WALL)
        entries.add(CobblemonItems.CHISELED_POLISHED_SKY_TUMBLESTONE)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BRICKS)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BRICK_STAIRS)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BRICK_SLAB)
        entries.add(CobblemonItems.SKY_TUMBLESTONE_BRICK_WALL)
        entries.add(CobblemonItems.CHISELED_SKY_TUMBLESTONE_BRICKS)

        entries.add(CobblemonItems.DAWN_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_DAWN_STONE_ORE)
        entries.add(CobblemonItems.DUSK_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_DUSK_STONE_ORE)
        entries.add(CobblemonItems.FIRE_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_FIRE_STONE_ORE)
        entries.add(CobblemonItems.NETHER_FIRE_STONE_ORE)
        entries.add(CobblemonItems.ICE_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_ICE_STONE_ORE)
        entries.add(CobblemonItems.LEAF_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_LEAF_STONE_ORE)
        entries.add(CobblemonItems.MOON_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_MOON_STONE_ORE)
        entries.add(CobblemonItems.DRIPSTONE_MOON_STONE_ORE)
        entries.add(CobblemonItems.SHINY_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_SHINY_STONE_ORE)
        entries.add(CobblemonItems.SUN_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_SUN_STONE_ORE)
        entries.add(CobblemonItems.TERRACOTTA_SUN_STONE_ORE)
        entries.add(CobblemonItems.THUNDER_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_THUNDER_STONE_ORE)
        entries.add(CobblemonItems.WATER_STONE_ORE)
        entries.add(CobblemonItems.DEEPSLATE_WATER_STONE_ORE)
    }

    private fun consumableEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.ROASTED_LEEK)
        entries.add(CobblemonItems.LEEK_AND_POTATO_STEW)
        entries.add(CobblemonItems.BRAISED_VIVICHOKE)
        entries.add(CobblemonItems.VIVICHOKE_DIP)
        entries.add(CobblemonItems.BERRY_JUICE)
        entries.add(CobblemonItems.REMEDY)
        entries.add(CobblemonItems.FINE_REMEDY)
        entries.add(CobblemonItems.SUPERB_REMEDY)
        entries.add(CobblemonItems.HEAL_POWDER)
        entries.add(CobblemonItems.MEDICINAL_BREW)

        entries.add(CobblemonItems.POTION)
        entries.add(CobblemonItems.SUPER_POTION)
        entries.add(CobblemonItems.HYPER_POTION)
        entries.add(CobblemonItems.MAX_POTION)
        entries.add(CobblemonItems.FULL_RESTORE)

        entries.add(CobblemonItems.ANTIDOTE)
        entries.add(CobblemonItems.AWAKENING)
        entries.add(CobblemonItems.BURN_HEAL)
        entries.add(CobblemonItems.ICE_HEAL)
        entries.add(CobblemonItems.PARALYZE_HEAL)

        entries.add(CobblemonItems.FULL_HEAL)

        entries.add(CobblemonItems.ETHER)
        entries.add(CobblemonItems.MAX_ETHER)
        entries.add(CobblemonItems.ELIXIR)
        entries.add(CobblemonItems.MAX_ELIXIR)

        entries.add(CobblemonItems.REVIVE)
        entries.add(CobblemonItems.MAX_REVIVE)

        entries.add(CobblemonItems.X_ATTACK)
        entries.add(CobblemonItems.X_DEFENSE)
        entries.add(CobblemonItems.X_SP_ATK)
        entries.add(CobblemonItems.X_SP_DEF)
        entries.add(CobblemonItems.X_SPEED)
        entries.add(CobblemonItems.X_ACCURACY)

        entries.add(CobblemonItems.DIRE_HIT)
        entries.add(CobblemonItems.GUARD_SPEC)

        entries.add(CobblemonItems.HEALTH_FEATHER)
        entries.add(CobblemonItems.MUSCLE_FEATHER)
        entries.add(CobblemonItems.RESIST_FEATHER)
        entries.add(CobblemonItems.GENIUS_FEATHER)
        entries.add(CobblemonItems.CLEVER_FEATHER)
        entries.add(CobblemonItems.SWIFT_FEATHER)

        entries.add(CobblemonItems.HP_UP)
        entries.add(CobblemonItems.PROTEIN)
        entries.add(CobblemonItems.IRON)
        entries.add(CobblemonItems.CALCIUM)
        entries.add(CobblemonItems.ZINC)
        entries.add(CobblemonItems.CARBOS)
        entries.add(CobblemonItems.PP_UP)
        entries.add(CobblemonItems.PP_MAX)
        entries.add(CobblemonItems.EXPERIENCE_CANDY_XS)
        entries.add(CobblemonItems.EXPERIENCE_CANDY_S)
        entries.add(CobblemonItems.EXPERIENCE_CANDY_M)
        entries.add(CobblemonItems.EXPERIENCE_CANDY_L)
        entries.add(CobblemonItems.EXPERIENCE_CANDY_XL)
        entries.add(CobblemonItems.RARE_CANDY)

        entries.add(CobblemonItems.LONELY_MINT)
        entries.add(CobblemonItems.ADAMANT_MINT)
        entries.add(CobblemonItems.NAUGHTY_MINT)
        entries.add(CobblemonItems.BRAVE_MINT)
        entries.add(CobblemonItems.BOLD_MINT)
        entries.add(CobblemonItems.IMPISH_MINT)
        entries.add(CobblemonItems.LAX_MINT)
        entries.add(CobblemonItems.RELAXED_MINT)
        entries.add(CobblemonItems.MODEST_MINT)
        entries.add(CobblemonItems.MILD_MINT)
        entries.add(CobblemonItems.RASH_MINT)
        entries.add(CobblemonItems.QUIET_MINT)
        entries.add(CobblemonItems.CALM_MINT)
        entries.add(CobblemonItems.GENTLE_MINT)
        entries.add(CobblemonItems.CAREFUL_MINT)
        entries.add(CobblemonItems.SASSY_MINT)
        entries.add(CobblemonItems.TIMID_MINT)
        entries.add(CobblemonItems.HASTY_MINT)
        entries.add(CobblemonItems.JOLLY_MINT)
        entries.add(CobblemonItems.NAIVE_MINT)
        entries.add(CobblemonItems.SERIOUS_MINT)

        entries.add(CobblemonItems.ABILITY_CAPSULE)
        entries.add(CobblemonItems.ABILITY_PATCH)
    }

    private fun evolutionItemEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.FIRE_STONE)
        entries.add(CobblemonItems.WATER_STONE)
        entries.add(CobblemonItems.THUNDER_STONE)
        entries.add(CobblemonItems.LEAF_STONE)
        entries.add(CobblemonItems.MOON_STONE)
        entries.add(CobblemonItems.SUN_STONE)
        entries.add(CobblemonItems.SHINY_STONE)
        entries.add(CobblemonItems.DUSK_STONE)
        entries.add(CobblemonItems.DAWN_STONE)
        entries.add(CobblemonItems.ICE_STONE)
        entries.add(CobblemonItems.LINK_CABLE)
        entries.add(CobblemonItems.KINGS_ROCK)
        entries.add(CobblemonItems.GALARICA_CUFF)
        entries.add(CobblemonItems.GALARICA_WREATH)
        entries.add(CobblemonItems.METAL_COAT)
        entries.add(CobblemonItems.BLACK_AUGURITE)
        entries.add(CobblemonItems.PROTECTOR)
        entries.add(CobblemonItems.OVAL_STONE)
        entries.add(CobblemonItems.DRAGON_SCALE)
        entries.add(CobblemonItems.ELECTIRIZER)
        entries.add(CobblemonItems.MAGMARIZER)
        entries.add(CobblemonItems.UPGRADE)
        entries.add(CobblemonItems.DUBIOUS_DISC)
        entries.add(CobblemonItems.RAZOR_FANG)
        entries.add(CobblemonItems.RAZOR_CLAW)
        entries.add(CobblemonItems.PEAT_BLOCK)
        entries.add(CobblemonItems.PRISM_SCALE)
        entries.add(CobblemonItems.REAPER_CLOTH)
        entries.add(CobblemonItems.DEEP_SEA_TOOTH)
        entries.add(CobblemonItems.DEEP_SEA_SCALE)
        entries.add(CobblemonItems.SACHET)
        entries.add(CobblemonItems.WHIPPED_DREAM)
        entries.add(CobblemonItems.TART_APPLE)
        entries.add(CobblemonItems.SWEET_APPLE)
        entries.add(CobblemonItems.CRACKED_POT)
        entries.add(CobblemonItems.CHIPPED_POT)
        entries.add(CobblemonItems.MASTERPIECE_TEACUP)
        entries.add(CobblemonItems.UNREMARKABLE_TEACUP)
        entries.add(CobblemonItems.STRAWBERRY_SWEET)
        entries.add(CobblemonItems.LOVE_SWEET)
        entries.add(CobblemonItems.BERRY_SWEET)
        entries.add(CobblemonItems.CLOVER_SWEET)
        entries.add(CobblemonItems.FLOWER_SWEET)
        entries.add(CobblemonItems.STAR_SWEET)
        entries.add(CobblemonItems.RIBBON_SWEET)
        entries.add(CobblemonItems.AUSPICIOUS_ARMOR)
        entries.add(CobblemonItems.MALICIOUS_ARMOR)
    }

    private fun heldItemEntries(displayContext: DisplayContext, entries: Entries) {
        entries.add(CobblemonItems.ABILITY_SHIELD)
        entries.add(CobblemonItems.ABSORB_BULB)
        entries.add(CobblemonItems.AIR_BALLOON)
        entries.add(CobblemonItems.ASSAULT_VEST)
        entries.add(CobblemonItems.BIG_ROOT)
        entries.add(CobblemonItems.BINDING_BAND)
        entries.add(CobblemonItems.BLACK_BELT)
        entries.add(CobblemonItems.BLACK_GLASSES)
        entries.add(CobblemonItems.BLACK_SLUDGE)
        entries.add(CobblemonItems.BLUNDER_POLICY)
        entries.add(CobblemonItems.BRIGHT_POWDER)
        entries.add(CobblemonItems.CELL_BATTERY)
        entries.add(CobblemonItems.CHARCOAL)
        entries.add(CobblemonItems.CHOICE_BAND)
        entries.add(CobblemonItems.CHOICE_SCARF)
        entries.add(CobblemonItems.CHOICE_SPECS)
        entries.add(CobblemonItems.CLEANSE_TAG)
        entries.add(CobblemonItems.COVERT_CLOAK)
        entries.add(CobblemonItems.DAMP_ROCK)
        entries.add(CobblemonItems.DEEP_SEA_SCALE)
        entries.add(CobblemonItems.DEEP_SEA_TOOTH)
        entries.add(CobblemonItems.DESTINY_KNOT)
        entries.add(CobblemonItems.DRAGON_FANG)
        entries.add(CobblemonItems.EJECT_BUTTON)
//        entries.add(CobblemonItems.EJECT_PACK)
        entries.add(CobblemonItems.EVERSTONE)
        entries.add(CobblemonItems.EVIOLITE)
        entries.add(CobblemonItems.EXPERT_BELT)
        entries.add(CobblemonItems.EXP_SHARE)
        entries.add(CobblemonItems.FAIRY_FEATHER)
        entries.add(CobblemonItems.FLAME_ORB)
        entries.add(CobblemonItems.FLOAT_STONE)
        entries.add(CobblemonItems.FOCUS_BAND)
        entries.add(CobblemonItems.FOCUS_SASH)
        entries.add(CobblemonItems.HARD_STONE)
        entries.add(CobblemonItems.HEAT_ROCK)
        entries.add(CobblemonItems.HEAVY_DUTY_BOOTS)
        entries.add(CobblemonItems.ICY_ROCK)
        entries.add(CobblemonItems.IRON_BALL)
        entries.add(CobblemonItems.KINGS_ROCK)
        entries.add(CobblemonItems.LEFTOVERS)
        entries.add(CobblemonItems.LIFE_ORB)
        entries.add(CobblemonItems.LIGHT_BALL)
        entries.add(CobblemonItems.LIGHT_CLAY)
        entries.add(CobblemonItems.LOADED_DICE)
        entries.add(CobblemonItems.LUCKY_EGG)
        entries.add(CobblemonItems.MAGNET)
        entries.add(CobblemonItems.MENTAL_HERB)
        entries.add(CobblemonItems.METAL_COAT)
        entries.add(CobblemonItems.METAL_POWDER)
        entries.add(CobblemonItems.METRONOME)
        entries.add(CobblemonItems.MIRACLE_SEED)
        entries.add(CobblemonItems.MIRROR_HERB)
        entries.add(CobblemonItems.MUSCLE_BAND)
        entries.add(CobblemonItems.MYSTIC_WATER)
        entries.add(CobblemonItems.NEVER_MELT_ICE)
        entries.add(CobblemonItems.POISON_BARB)
        entries.add(CobblemonItems.POWER_ANKLET)
        entries.add(CobblemonItems.POWER_BAND)
        entries.add(CobblemonItems.POWER_BELT)
        entries.add(CobblemonItems.POWER_BRACER)
        entries.add(CobblemonItems.POWER_LENS)
        entries.add(CobblemonItems.POWER_WEIGHT)
        entries.add(CobblemonItems.POWER_HERB)
        entries.add(CobblemonItems.PROTECTIVE_PADS)
        entries.add(CobblemonItems.PUNCHING_GLOVE)
        entries.add(CobblemonItems.QUICK_CLAW)
        entries.add(CobblemonItems.QUICK_POWDER)
        entries.add(CobblemonItems.RAZOR_CLAW)
        entries.add(CobblemonItems.RAZOR_FANG)
        entries.add(CobblemonItems.RED_CARD)
        entries.add(CobblemonItems.RING_TARGET)
        entries.add(CobblemonItems.ROCKY_HELMET)
        entries.add(CobblemonItems.ROOM_SERVICE)
        entries.add(CobblemonItems.SAFETY_GOGGLES)
        entries.add(CobblemonItems.SCOPE_LENS)
        entries.add(CobblemonItems.SHARP_BEAK)
        entries.add(CobblemonItems.SHED_SHELL)
        entries.add(CobblemonItems.SHELL_BELL)
        entries.add(CobblemonItems.SILK_SCARF)
        entries.add(CobblemonItems.SILVER_POWDER)
        entries.add(CobblemonItems.SMOKE_BALL)
        entries.add(CobblemonItems.SMOOTH_ROCK)
        entries.add(CobblemonItems.SOFT_SAND)
        entries.add(CobblemonItems.SOOTHE_BELL)
        entries.add(CobblemonItems.SPELL_TAG)
        entries.add(CobblemonItems.STICKY_BARB)
        entries.add(CobblemonItems.TERRAIN_EXTENDER)
        entries.add(CobblemonItems.THROAT_SPRAY)
        entries.add(CobblemonItems.TOXIC_ORB)
        entries.add(CobblemonItems.TWISTED_SPOON)
        entries.add(CobblemonItems.UTILITY_UMBRELLA)
        entries.add(CobblemonItems.WEAKNESS_POLICY)
        entries.add(CobblemonItems.WHITE_HERB)
        entries.add(CobblemonItems.WIDE_LENS)
        entries.add(CobblemonItems.WISE_GLASSES)
        entries.add(CobblemonItems.ZOOM_LENS)

        entries.add(CobblemonItems.MEDICINAL_LEEK)
        entries.add(Items.BONE)
        entries.add(Items.SNOWBALL)

        entries.add(CobblemonItems.NORMAL_GEM)
        entries.add(CobblemonItems.FIRE_GEM)
        entries.add(CobblemonItems.WATER_GEM)
        entries.add(CobblemonItems.GRASS_GEM)
        entries.add(CobblemonItems.ELECTRIC_GEM)
        entries.add(CobblemonItems.ICE_GEM)
        entries.add(CobblemonItems.FIGHTING_GEM)
        entries.add(CobblemonItems.POISON_GEM)
        entries.add(CobblemonItems.GROUND_GEM)
        entries.add(CobblemonItems.FLYING_GEM)
        entries.add(CobblemonItems.PSYCHIC_GEM)
        entries.add(CobblemonItems.BUG_GEM)
        entries.add(CobblemonItems.ROCK_GEM)
        entries.add(CobblemonItems.GHOST_GEM)
        entries.add(CobblemonItems.DRAGON_GEM)
        entries.add(CobblemonItems.DARK_GEM)
        entries.add(CobblemonItems.STEEL_GEM)
        entries.add(CobblemonItems.FAIRY_GEM)
    }

    private fun pokeballentries(displayContext: DisplayContext, entries: Entries) {
        CobblemonItems.pokeBalls.forEach(entries::add)
    }

    private fun foodInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.MEDICINAL_LEEK, Items.POISONOUS_POTATO)
        injector.putAfter(CobblemonItems.ROASTED_LEEK, CobblemonItems.MEDICINAL_LEEK)
        injector.putAfter(CobblemonItems.BRAISED_VIVICHOKE, CobblemonItems.ROASTED_LEEK)
        injector.putAfter(CobblemonItems.LEEK_AND_POTATO_STEW, Items.RABBIT_STEW)
        injector.putAfter(CobblemonItems.VIVICHOKE_DIP, CobblemonItems.LEEK_AND_POTATO_STEW)
    }

    private fun toolsAndUtilitiesInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.APRICORN_BOAT, Items.BAMBOO_CHEST_RAFT)
        injector.putAfter(CobblemonItems.APRICORN_CHEST_BOAT, CobblemonItems.APRICORN_BOAT)
    }

    private fun ingredientsInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.BYGONE_SHERD, Items.SNORT_POTTERY_SHERD)
        injector.putAfter(CobblemonItems.CAPTURE_SHERD, CobblemonItems.BYGONE_SHERD)
        injector.putAfter(CobblemonItems.DOME_SHERD, CobblemonItems.CAPTURE_SHERD)
        injector.putAfter(CobblemonItems.HELIX_SHERD, CobblemonItems.DOME_SHERD)
        injector.putAfter(CobblemonItems.NOSTALGIC_SHERD, CobblemonItems.HELIX_SHERD)
        injector.putAfter(CobblemonItems.SUSPICIOUS_SHERD, CobblemonItems.NOSTALGIC_SHERD)

        injector.putAfter(CobblemonItems.AUTOMATON_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
    }

    /**
     * The abstract behaviour of injecting into existing [ItemGroup]s in specific positions.
     * Each platform has to implement the behaviour.
     */
    interface Injector {

        /**
         * Places the given [item] at the start of a creative tab.
         *
         * @param item The [ItemConvertible] being added at the start of a tab.
         */
        fun putFirst(item: ItemConvertible)

        /**
         * Places the given [item] before the [target].
         * If the [target] is not present behaves as [putLast].
         *
         * @param item The [ItemConvertible] being added before [target].
         * @param target The [ItemConvertible] being targeted.
         */
        fun putBefore(item: ItemConvertible, target: ItemConvertible)

        /**
         * Places the given [item] after the [target].
         * If the [target] is not present behaves as [putLast].
         *
         * @param item The [ItemConvertible] being added after [target].
         * @param target The [ItemConvertible] being targeted.
         */
        fun putAfter(item: ItemConvertible, target: ItemConvertible)

        /**
         * Places the given [item] at the end of a creative tab.
         *
         * @param item The [ItemConvertible] being added at the end of a tab.
         */
        fun putLast(item: ItemConvertible)

    }

}
