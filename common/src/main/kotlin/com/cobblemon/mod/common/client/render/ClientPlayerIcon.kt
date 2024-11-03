/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.requests.ClientPlayerActionRequest
import com.cobblemon.mod.common.client.ClientMultiBattleTeamMember
import com.cobblemon.mod.common.platform.events.RenderEvent
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Axis
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityAttachment
import net.minecraft.world.entity.player.Player
import org.joml.Matrix4f
import java.util.*

/**
 * Visual notification rendered above a player's nametag for [ClientMultiBattleTeamMember]s and unanswered [ClientPlayerActionRequest]s.
 *
 * @param expiryTime The amount of time (in seconds) this icon is valid.
 *
 * @author Segfault Guy
 * @since September 1st, 2024
 */
abstract class ClientPlayerIcon(expiryTime: Int? = null) {

    /** The texture to use for the icon. */
    open val texture: ResourceLocation = cobblemonResource("textures/particle/request/icon_exclamation.png")
    /** The offset of the icon from the player's head. */
    open val Y_OFFSET = 1.5F
    open val SCALE = 0.75F
    private val FADE_RATIO = 0.25F

    private val tickCount get() = Minecraft.getInstance().player?.tickCount ?: 0    // icon is only ticked when tracked, so derive lifetime from player tick - startTick
    private val startTick = Minecraft.getInstance().player?.tickCount ?: 0
    private val tickRateManager = Minecraft.getInstance().level!!.tickRateManager()
    /** The tick at which the icon begins fading. */
    private val fadeTickStamp = expiryTime?.let { it * tickRateManager.tickrate() * (1 - FADE_RATIO) + startTick}?.toInt() ?: Int.MAX_VALUE // TODO is server expiration based on epoc going to cause a problem?

    /** Duration (in ticks) of the fade transition. */
    open val fadeDuration = 30
    /** How long (in ticks) the icon has been fading. */
    private var fadeCount = 0
    private val fading get() = tickCount >= fadeTickStamp
    private var fadeOut = true
        set(value) {
            fadeCount = 0
            field = value
        }

    private val MAX_ALPHA = 1F
    private val MIN_ALPHA = 0.15F   // MC fragment shaders won't draw textures with < 0.1 alpha and the sudden cutoff from 0.1 to 0 and vice versa is jarring
    private var alpha = 1F
    private val startAlpha get() = if (fadeOut) MAX_ALPHA else MIN_ALPHA
    private val finalAlpha get() = if (fadeOut) MIN_ALPHA else MAX_ALPHA

    fun onTick() {
        if (!fading) return else fadeCount++
        if (fadeCount == fadeDuration) fadeOut = !fadeOut   // toggle transition

        // alpha changed outside of render deltaticks so fade does not freeze off screen
        alpha = Mth.lerp(fadeCount.toFloat()/fadeDuration, startAlpha, finalAlpha)
    }

    fun render(player: Player) {
        if (CobblemonClient.battle != null) return
        DeferredRenderer.enqueue(RenderEvent.Stage.TRANSLUCENT) { event ->
            val poseStack = event.poseStack
            val partialTicks = event.tickCounter.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(player))
            val camera = event.camera

            poseStack.pushPose()
            positionTag(player, poseStack, camera, partialTicks)
            drawTag(poseStack)
            poseStack.popPose()
        }
    }

    /** Offsets and billboards the quad above the player. */
    protected open fun positionTag(player: Player, poseStack: PoseStack, camera: Camera, partialTicks: Float) {
        // position
        val vec3 = player.attachments.getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(partialTicks))
        poseStack.translate(player.getPosition(partialTicks).x, player.getPosition(partialTicks).y, player.getPosition(partialTicks).z)
        poseStack.translate(vec3!!.x, vec3.y + Y_OFFSET, vec3.z)
        poseStack.translate(-camera.position.x, -camera.position.y, -camera.position.z) // event posestack does not have PlayerRenderer context

        // billboard
        val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        poseStack.mulPose(Axis.YP.rotationDegrees((180f - entityRenderDispatcher.camera.yRot)))
        poseStack.scale(SCALE, -SCALE, SCALE)
    }

    /** Constructs the vertices for the rendered quad. */
    protected open fun buildTag(model: Matrix4f, vertices: VertexConsumer) {
        vertices.addVertex(model, -0.5f, 0f, 0f).setUv(0f, 0f).setColor(1f, 1f, 1f, 1f)
        vertices.addVertex(model, -0.5f, 1f, 0f).setUv(0f, 1f).setColor(1f, 1f, 1f, 1f)
        vertices.addVertex(model, 0.5f, 1f, 0f).setUv(1f, 1f).setColor(1f, 1f, 1f, 1f)
        vertices.addVertex(model, 0.5f, 0f, 0f).setUv(1f, 0f).setColor(1f, 1f, 1f, 1f)
    }

    /** Sets the shading and gl states of the quad then queues for drawing. */
    private fun drawTag(poseStack: PoseStack) {
        // texture
        RenderSystem.setShader { GameRenderer.getPositionTexColorShader() }
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha)

        // gl states
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()

        // TODO worth making a custom rendertype and getting buffer from multiBufferSource?
        val model = poseStack.last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buildTag(model, buffer)
        BufferUploader.drawWithShader(buffer.buildOrThrow())

        // undo
        RenderSystem.disableBlend()
        RenderSystem.disableDepthTest()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    companion object {
        private val trackedIcons = mutableMapOf<UUID, ClientPlayerIcon>()

        fun onTick() = trackedIcons.values.forEach { it.onTick() }

        /** Updates the icon that is rendered above the player. */
        fun update(player: UUID) {
            val teamIcon = CobblemonClient.teamData.multiBattleTeamMembers.find { it.uuid == player }
            val requests = CobblemonClient.requests.getRequestsFrom(player).filterIsInstance<ClientPlayerIcon>()

            if (requests.isNotEmpty()) {
                val icon = requests.minBy { it.fadeTickStamp }  // icon that expires the soonest has priority
                trackedIcons[player] = icon
            }
            else if (teamIcon != null) {
                trackedIcons[player] = teamIcon
            }
            else {
                trackedIcons.remove(player)
            }
        }
        // TODO make some api so this is expandable? i cant be assed right now

        fun clear() = trackedIcons.clear()

        fun onRenderPlayer(player: Player) = trackedIcons[player.uuid]?.render(player)
    }
}