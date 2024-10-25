/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.client

import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonClient.pokedexUsageContext
import com.cobblemon.mod.common.client.CobblemonClient.reloadCodedAssets
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds
import com.cobblemon.mod.common.client.render.atlas.CobblemonAtlases
import com.cobblemon.mod.common.client.render.item.CobblemonModelPredicateRegistry
import com.cobblemon.mod.common.item.PokedexItem
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.particle.SnowstormParticleType
import com.cobblemon.mod.common.platform.events.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.fabric.CobblemonFabric
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.InteractionHand

class CobblemonFabricClient: ClientModInitializer, CobblemonClientImplementation {
    override fun onInitializeClient() {
        registerParticleFactory(CobblemonParticles.SNOWSTORM_PARTICLE_TYPE, SnowstormParticleType::Factory)
        CobblemonClient.initialize(this)
        ModelLoadingPlugin.register {
            PokeBalls.all().forEach { ball ->
                it.addModels(ball.model3d)
            }
        }

        CobblemonFabric.networkManager.registerClientHandlers()

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(object : IdentifiableResourceReloadListener {
            override fun reload(
                synchronizer: PreparableReloadListener.PreparationBarrier,
                manager: ResourceManager,
                prepareProfiler: ProfilerFiller,
                applyProfiler: ProfilerFiller,
                prepareExecutor: Executor,
                applyExecutor: Executor
            ): CompletableFuture<Void> {
                val atlasFutures = mutableListOf<CompletableFuture<Void>>()
                CobblemonAtlases.atlases.forEach {
                    atlasFutures.add(it.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor))
                }
                val result = CompletableFuture.allOf(*atlasFutures.toTypedArray()).thenRun {
                    reloadCodedAssets(manager)
                }
                return result
            }

            override fun getFabricId() = cobblemonResource("atlases")

        })

        // Register the HUD render callback for Pokédex
        HudRenderCallback.EVENT.register { graphics, tickDelta ->
            val client = Minecraft.getInstance()
            val player = client.player
            if (player != null) {
                val itemStack = player.mainHandItem
                val offhandStack = player.offhandItem
                if (((itemStack.item is PokedexItem && player.usedItemHand == InteractionHand.MAIN_HAND) ||
                    (offhandStack.item is PokedexItem && player.usedItemHand == InteractionHand.OFF_HAND))
                ) {
                    pokedexUsageContext.renderUpdate(graphics, tickDelta)
                } else if (pokedexUsageContext.transitionIntervals > 0) {
                    pokedexUsageContext.resetState()
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            val player = client.player
            if (player != null) {
                val itemStack = player.mainHandItem
                val offhandStack = player.offhandItem
                if (((itemStack.item is PokedexItem && player.usedItemHand == InteractionHand.MAIN_HAND) ||
                    (offhandStack.item is PokedexItem && player.usedItemHand == InteractionHand.OFF_HAND)) &&
                    player.isUsingItem &&
                    pokedexUsageContext.scanningGuiOpen
                ) {
                    val keyAttack = client.options.keyAttack
                    pokedexUsageContext.attackKeyHeld(keyAttack.isDown)
                }
            }
        })

        CobblemonKeyBinds.register(KeyBindingHelper::registerKeyBinding)

        ClientEntityEvents.ENTITY_LOAD.register { entity, level -> PlatformEvents.CLIENT_ENTITY_LOAD.post(ClientEntityEvent.Load(entity, level))}
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, level -> PlatformEvents.CLIENT_ENTITY_UNLOAD.post(ClientEntityEvent.Unload(entity, level))}
        ClientTickEvents.START_CLIENT_TICK.register { client -> PlatformEvents.CLIENT_TICK_PRE.post(ClientTickEvent.Pre(client)) }
        ClientTickEvents.END_CLIENT_TICK.register { client -> PlatformEvents.CLIENT_TICK_POST.post(ClientTickEvent.Post(client)) }
        ClientPlayConnectionEvents.JOIN.register { _, _, client -> client.player?.let { PlatformEvents.CLIENT_PLAYER_LOGIN.post(ClientPlayerEvent.Login(it)) } }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client -> client.player?.let { PlatformEvents.CLIENT_PLAYER_LOGOUT.post(ClientPlayerEvent.Logout(it)) } }
        ItemTooltipCallback.EVENT.register { stack, context, type, lines -> PlatformEvents.CLIENT_ITEM_TOOLTIP.post(ItemTooltipEvent(stack, context, type, lines)) }

        CobblemonModelPredicateRegistry.registerPredicates()
    }

    override fun registerLayer(modelLayer: ModelLayerLocation, supplier: Supplier<LayerDefinition>) {
        EntityModelLayerRegistry.registerModelLayer(modelLayer) { supplier.get() }
    }

    override fun <T : ParticleOptions> registerParticleFactory(type: ParticleType<T>, factory: (SpriteSet) -> ParticleProvider<T>) {
        ParticleFactoryRegistry.getInstance().register(type, ParticleFactoryRegistry.PendingParticleFactory { factory(it) })
    }

    override fun registerBlockRenderType(layer: RenderType, vararg blocks: Block) {
        BlockRenderLayerMap.INSTANCE.putBlocks(layer, *blocks)
    }

    override fun registerItemColors(provider: ItemColor, vararg items: Item) {
        ColorProviderRegistry.ITEM.register(provider, *items)
    }

    override fun registerBlockColors(provider: BlockColor, vararg blocks: Block) {
        ColorProviderRegistry.BLOCK.register(provider, *blocks)
    }

    override fun <T : BlockEntity> registerBlockEntityRenderer(type: BlockEntityType<out T>, factory: BlockEntityRendererProvider<T>) {
        BlockEntityRenderers.register(type, factory)
    }

    override fun <T : Entity> registerEntityRenderer(type: EntityType<out T>, factory: EntityRendererProvider<T>) {
        EntityRendererRegistry.register(type, factory)
    }
}