/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.*
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.scheduling.ClientTaskTracker
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.client.gui.PartyOverlay
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.render.block.*
import com.cobblemon.mod.common.client.render.boat.CobblemonBoatRenderer
import com.cobblemon.mod.common.client.render.entity.PokeBobberEntityRenderer
import com.cobblemon.mod.common.client.render.generic.GenericBedrockRenderer
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry
import com.cobblemon.mod.common.client.render.item.PokemonItemRenderer
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.*
import com.cobblemon.mod.common.client.render.npc.NPCRenderer
import com.cobblemon.mod.common.client.render.pokeball.PokeBallRenderer
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer
import com.cobblemon.mod.common.client.sound.battle.BattleMusicController
import com.cobblemon.mod.common.client.starter.ClientPlayerData
import com.cobblemon.mod.common.client.storage.ClientStorageManager
import com.cobblemon.mod.common.client.tooltips.CobblemonTooltipGenerator
import com.cobblemon.mod.common.client.tooltips.FishingBaitTooltipGenerator
import com.cobblemon.mod.common.client.tooltips.FishingRodTooltipGenerator
import com.cobblemon.mod.common.client.tooltips.TooltipManager
import com.cobblemon.mod.common.client.trade.ClientTrade
import com.cobblemon.mod.common.data.CobblemonDataProvider
import com.cobblemon.mod.common.entity.boat.CobblemonBoatType
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.model.BoatModel
import net.minecraft.client.model.ChestBoatModel
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.HangingSignRenderer
import net.minecraft.client.renderer.blockentity.SignRenderer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.entity.player.Player

object CobblemonClient {

    lateinit var implementation: CobblemonClientImplementation
    val storage = ClientStorageManager()
    var trade: ClientTrade? = null
    var battle: ClientBattle? = null
    var clientPlayerData = ClientPlayerData()
    /** If true then we won't bother them anymore about choosing a starter even if it's a thing they can do. */
    var checkedStarterScreen = false
    var requests = ClientPlayerActionRequests()
    var posableModelRepositories = listOf(
        PokemonModelRepository,
        PokeBallModelRepository,
        NPCModelRepository,
        FossilModelRepository,
        BlockEntityModelRepository,
        GenericBedrockEntityModelRepository
    )


    val overlay: PartyOverlay by lazy { PartyOverlay() }
    val battleOverlay: BattleOverlay by lazy { BattleOverlay() }

    fun onLogin() {
        clientPlayerData = ClientPlayerData()
        requests = ClientPlayerActionRequests()
        storage.onLogin()
        CobblemonDataProvider.canReload = false
    }

    fun onLogout() {
        storage.onLogout()
        battle = null
        battleOverlay.onLogout()
        ClientTaskTracker.clear()
        checkedStarterScreen = false
        CobblemonDataProvider.canReload = true
    }

    fun initialize(implementation: CobblemonClientImplementation) {
        LOGGER.info("Initializing Cobblemon client")
        this.implementation = implementation

        PlatformEvents.CLIENT_PLAYER_LOGIN.subscribe { onLogin() }
        PlatformEvents.CLIENT_PLAYER_LOGOUT.subscribe { onLogout() }

        this.registerBlockEntityRenderers()
        registerBlockRenderTypes()
        //registerColors()
        registerFlywheelRenderers()
        this.registerEntityRenderers()
        Berries.observable.subscribe {
            BerryModelRepository.patchModels()
        }
        this.registerTooltipManagers()

        LOGGER.info("Registering custom BuiltinItemRenderers")
        CobblemonBuiltinItemRendererRegistry.register(CobblemonItems.POKEMON_MODEL, PokemonItemRenderer())

        PlatformEvents.CLIENT_ITEM_TOOLTIP.subscribe { event ->
            val stack = event.stack
            val lines = event.lines
            TooltipManager.generateTooltips(stack, lines, Screen.hasShiftDown())
        }
    }

    private fun registerTooltipManagers() {
        TooltipManager.registerTooltipGenerator(CobblemonTooltipGenerator)
        TooltipManager.registerTooltipGenerator(FishingBaitTooltipGenerator)
        TooltipManager.registerTooltipGenerator(FishingRodTooltipGenerator)
    }

    fun registerFlywheelRenderers() {
//        InstancedRenderRegistry
//            .configure(CobblemonBlockEntities.BERRY)
//            .alwaysSkipRender()
//            .factory(::BerryEntityInstance)
//            .apply()
    }

    /*
    fun registerColors() {
        this.implementation.registerBlockColors(BlockColorProvider { _, _, _, _ ->
            return@BlockColorProvider 0xE0A33A
        }, CobblemonBlocks.APRICORN_LEAVES)
        this.implementation.registerItemColors(ItemColorProvider { _, _ ->
            return@ItemColorProvider 0xE0A33A
        }, CobblemonItems.APRICORN_LEAVES)
    }
    */

    private fun registerBlockRenderTypes() {

        this.implementation.registerBlockRenderType(RenderType.cutoutMipped(), CobblemonBlocks.APRICORN_LEAVES)

        this.implementation.registerBlockRenderType(
            RenderType.cutout(),
            CobblemonBlocks.GILDED_CHEST,
            CobblemonBlocks.FOSSIL_ANALYZER,
            CobblemonBlocks.APRICORN_DOOR,
            CobblemonBlocks.APRICORN_TRAPDOOR,
            CobblemonBlocks.APRICORN_SIGN,
            CobblemonBlocks.APRICORN_WALL_SIGN,
            CobblemonBlocks.APRICORN_HANGING_SIGN,
            CobblemonBlocks.APRICORN_WALL_HANGING_SIGN,
            CobblemonBlocks.BLACK_APRICORN_SAPLING,
            CobblemonBlocks.BLUE_APRICORN_SAPLING,
            CobblemonBlocks.GREEN_APRICORN_SAPLING,
            CobblemonBlocks.PINK_APRICORN_SAPLING,
            CobblemonBlocks.RED_APRICORN_SAPLING,
            CobblemonBlocks.WHITE_APRICORN_SAPLING,
            CobblemonBlocks.YELLOW_APRICORN_SAPLING,
            CobblemonBlocks.BLACK_APRICORN,
            CobblemonBlocks.BLUE_APRICORN,
            CobblemonBlocks.GREEN_APRICORN,
            CobblemonBlocks.PINK_APRICORN,
            CobblemonBlocks.RED_APRICORN,
            CobblemonBlocks.WHITE_APRICORN,
            CobblemonBlocks.YELLOW_APRICORN,
            CobblemonBlocks.HEALING_MACHINE,
            CobblemonBlocks.MEDICINAL_LEEK,
            CobblemonBlocks.HEALING_MACHINE,
            CobblemonBlocks.RED_MINT,
            CobblemonBlocks.BLUE_MINT,
            CobblemonBlocks.CYAN_MINT,
            CobblemonBlocks.PINK_MINT,
            CobblemonBlocks.GREEN_MINT,
            CobblemonBlocks.WHITE_MINT,
            CobblemonBlocks.PASTURE,
            CobblemonBlocks.ENERGY_ROOT,
            CobblemonBlocks.BIG_ROOT,
            CobblemonBlocks.REVIVAL_HERB,
            CobblemonBlocks.VIVICHOKE_SEEDS,
            CobblemonBlocks.PEP_UP_FLOWER,
            CobblemonBlocks.POTTED_PEP_UP_FLOWER,
            CobblemonBlocks.REVIVAL_HERB,
            *CobblemonBlocks.berries().values.toTypedArray(),
            CobblemonBlocks.POTTED_PEP_UP_FLOWER,
            CobblemonBlocks.RESTORATION_TANK,
            CobblemonBlocks.SMALL_BUDDING_TUMBLESTONE,
            CobblemonBlocks.MEDIUM_BUDDING_TUMBLESTONE,
            CobblemonBlocks.LARGE_BUDDING_TUMBLESTONE,
            CobblemonBlocks.TUMBLESTONE_CLUSTER,
            CobblemonBlocks.SMALL_BUDDING_BLACK_TUMBLESTONE,
            CobblemonBlocks.MEDIUM_BUDDING_BLACK_TUMBLESTONE,
            CobblemonBlocks.LARGE_BUDDING_BLACK_TUMBLESTONE,
            CobblemonBlocks.BLACK_TUMBLESTONE_CLUSTER,
            CobblemonBlocks.SMALL_BUDDING_SKY_TUMBLESTONE,
            CobblemonBlocks.MEDIUM_BUDDING_SKY_TUMBLESTONE,
            CobblemonBlocks.LARGE_BUDDING_SKY_TUMBLESTONE,
            CobblemonBlocks.SKY_TUMBLESTONE_CLUSTER,
            CobblemonBlocks.GIMMIGHOUL_CHEST,
            CobblemonBlocks.DISPLAY_CASE
        )

        this.createBoatModelLayers()
    }

    fun beforeChatRender(context: GuiGraphics, partialDeltaTicks: Float) {
        val partialDeltaTicks = Minecraft.getInstance().timer // Checking that this even works
//        ClientTaskTracker.update(partialDeltaTicks / 20f)
        if (battle == null) {
            overlay.render(context, partialDeltaTicks)
        } else {
            battleOverlay.render(context, partialDeltaTicks)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun onAddLayer(skinMap: Map<PlayerSkin.Model, EntityRenderer<out Player>>?) {
        var renderer: LivingEntityRenderer<Player, PlayerModel<Player>>? = skinMap?.get(PlayerSkin.Model.WIDE) as LivingEntityRenderer<Player, PlayerModel<Player>>
        renderer?.addLayer(PokemonOnShoulderRenderer(renderer))
        renderer = skinMap[PlayerSkin.Model.SLIM] as LivingEntityRenderer<Player, PlayerModel<Player>>?
        renderer?.addLayer(PokemonOnShoulderRenderer(renderer))
    }

    private fun registerBlockEntityRenderers() {
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.HEALING_MACHINE, ::HealingMachineRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.BERRY, ::BerryBlockRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.SIGN, ::SignRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.HANGING_SIGN, ::HangingSignRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.FOSSIL_ANALYZER, ::FossilAnalyzerRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.RESTORATION_TANK, ::RestorationTankRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.GILDED_CHEST, ::GildedChestBlockRenderer)
        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.DISPLAY_CASE, ::DisplayCaseRenderer)
    }

    private fun registerEntityRenderers() {
        LOGGER.info("Registering Pokémon renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.POKEMON, ::PokemonRenderer)
        LOGGER.info("Registering PokéBall renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.EMPTY_POKEBALL, ::PokeBallRenderer)
        LOGGER.info("Registering Boat renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.BOAT) { ctx -> CobblemonBoatRenderer(ctx, false) }
        LOGGER.info("Registering Boat with Chest renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.CHEST_BOAT) { ctx -> CobblemonBoatRenderer(ctx, true) }
        LOGGER.info("Registering Generic Bedrock renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.GENERIC_BEDROCK_ENTITY, ::GenericBedrockRenderer)
        LOGGER.info("Registering Generic Bedrock Entity renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.GENERIC_BEDROCK_ENTITY, ::GenericBedrockRenderer)
        LOGGER.info("Registering PokeRod Bobber renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.POKE_BOBBER) { ctx -> PokeBobberEntityRenderer(ctx) }
        LOGGER.info("Registering NPC renderer")
        this.implementation.registerEntityRenderer(CobblemonEntities.NPC, ::NPCRenderer)
    }

    fun reloadCodedAssets(resourceManager: ResourceManager) {
        LOGGER.info("Loading assets...")
        // Particles come first because animations need them.
        BedrockParticleOptionsRepository.loadEffects(resourceManager)
        // Animations come next because models need them.
        BedrockAnimationRepository.loadAnimations(
            resourceManager = resourceManager,
            directories = posableModelRepositories.flatMap { it.animationDirectories }
        )
        posableModelRepositories.forEach { it.reload(resourceManager) }

        BerryModelRepository.reload(resourceManager)
        MiscModelRepository.reload(resourceManager)
        LOGGER.info("Loaded assets")
    }

    fun endBattle() {
        battle = null
        battleOverlay.lastKnownBattle = null
        BattleMusicController.endMusic()
    }

    private fun createBoatModelLayers() {
        CobblemonBoatType.entries.forEach { type ->
            this.implementation.registerLayer(CobblemonBoatRenderer.createBoatModelLayer(type, false), BoatModel::createBodyModel)
            this.implementation.registerLayer(CobblemonBoatRenderer.createBoatModelLayer(type, true), ChestBoatModel::createBodyModel)
        }
    }

}