/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.scheduling.ScheduledTaskTracker
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.client.gui.PartyOverlay
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay
import com.cobblemon.mod.common.client.net.ClientPacketRegistrar
import com.cobblemon.mod.common.client.render.block.BerryBlockRenderer
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.client.render.block.HealingMachineRenderer
import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry
import com.cobblemon.mod.common.client.render.item.PokemonItemRenderer
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokeBallModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.PokemonModelRepository
import com.cobblemon.mod.common.client.render.pokeball.PokeBallRenderer
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer
import com.cobblemon.mod.common.client.starter.ClientPlayerData
import com.cobblemon.mod.common.client.storage.ClientStorageManager
import com.cobblemon.mod.common.client.trade.ClientTrade
import com.cobblemon.mod.common.data.CobblemonDataProvider
import com.cobblemon.mod.common.world.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.client.color.block.BlockColorProvider
import net.minecraft.client.color.item.ItemColorProvider
import net.minecraft.client.color.world.BiomeColors
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager

object CobblemonClient {
    lateinit var implementation: CobblemonClientImplementation
    val storage = ClientStorageManager()
    var trade: ClientTrade? = null
    var battle: ClientBattle? = null
    var clientPlayerData = ClientPlayerData()
    /** If true then we won't bother them anymore about choosing a starter even if it's a thing they can do. */
    var checkedStarterScreen = false
    var requests = ClientPlayerActionRequests()


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
        ScheduledTaskTracker.clear()
        checkedStarterScreen = false
        CobblemonDataProvider.canReload = true
    }

    fun initialize(implementation: CobblemonClientImplementation) {
        LOGGER.info("Initializing Cobblemon client")
        this.implementation = implementation

        PlatformEvents.CLIENT_PLAYER_LOGIN.subscribe { onLogin() }
        PlatformEvents.CLIENT_PLAYER_LOGOUT.subscribe { onLogout() }

        this.implementation.registerBlockEntityRenderer(CobblemonBlockEntities.HEALING_MACHINE, ::HealingMachineRenderer)
        BlockEntityRendererRegistry.register(CobblemonBlockEntities.BERRY.get(), ::BerryBlockRenderer)

        registerBlockRenderTypes()
        registerColors()
        PlatformEvents
        LOGGER.info("Registering custom BuiltinItemRenderers")
        CobblemonBuiltinItemRendererRegistry.register(CobblemonItems.POKEMON_MODEL, PokemonItemRenderer())
    }

    fun registerColors() {
        this.implementation.registerBlockColors(BlockColorProvider { _, _, _, _ ->
            return@BlockColorProvider 0x71c219
        }, CobblemonBlocks.APRICORN_LEAVES)
        this.implementation.registerItemColors(ItemColorProvider { _, _ ->
            return@ItemColorProvider 0x71c219
        }, CobblemonItems.APRICORN_LEAVES)

        // Berry trees don't have an item representation
        CobblemonBlocks.berries().values.forEach { berry ->
            ColorHandlerRegistry.registerBlockColors({ _, blockAndTintGetter, blockPos, tintIndex ->
                (blockAndTintGetter?.getBlockEntity(blockPos) as? BerryBlockEntity)?.berry()?.tintIndexes?.get(tintIndex)?.rgb ?: -1
            }, berry.get())
        }
    }

    private fun registerBlockRenderTypes() {
        this.implementation.registerBlockRenderType(RenderLayer.getCutout(),
            CobblemonBlocks.APRICORN_DOOR,
            CobblemonBlocks.APRICORN_TRAPDOOR,
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
            CobblemonBlocks.RED_MINT,
            CobblemonBlocks.BLUE_MINT,
            CobblemonBlocks.CYAN_MINT,
            CobblemonBlocks.PINK_MINT,
            CobblemonBlocks.GREEN_MINT,
            CobblemonBlocks.WHITE_MINT,
            *CobblemonBlocks.berries().values.map { it.get() }.toTypedArray()
        )
    }

    fun beforeChatRender(matrixStack: MatrixStack, partialDeltaTicks: Float) {
        if (battle == null) {
            overlay.render(matrixStack, partialDeltaTicks)
        } else {
            battleOverlay.render(matrixStack, partialDeltaTicks)
        }
    }

    fun onAddLayer(skinMap: Map<String, EntityRenderer<out PlayerEntity>>?) {
        var renderer: LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>? = skinMap?.get("default") as LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>
        renderer?.addFeature(PokemonOnShoulderRenderer(renderer))
        renderer = skinMap.get("slim") as LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>>?
        renderer?.addFeature(PokemonOnShoulderRenderer(renderer))
    }

    fun registerPokemonRenderer(context: EntityRendererFactory.Context): PokemonRenderer {
        LOGGER.info("Registering Pokémon renderer")
        return PokemonRenderer(context)
    }

    fun registerPokeBallRenderer(context: EntityRendererFactory.Context): PokeBallRenderer {
        LOGGER.info("Registering PokéBall renderer")
        return PokeBallRenderer(context)
    }

    fun reloadCodedAssets(resourceManager: ResourceManager) {
        LOGGER.info("Loading assets...")
        BedrockParticleEffectRepository.loadEffects(resourceManager)
        BedrockAnimationRepository.loadAnimations(
            resourceManager = resourceManager,
            directories = PokemonModelRepository.animationDirectories + PokeBallModelRepository.animationDirectories
        )
        PokemonModelRepository.reload(resourceManager)
        PokeBallModelRepository.reload(resourceManager)
        LOGGER.info("Loaded assets")
//        PokeBallModelRepository.reload(resourceManager)
    }

    fun endBattle() {
        battle = null
    }
}