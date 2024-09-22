package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.platform.events.RenderEvent
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityAttachment
import net.minecraft.world.entity.player.Player
import org.joml.Matrix4f
import org.joml.Quaternionf
import java.util.*

/**
 * Visual notification rendered above a player's nametag.
 *
 * @author Segfault Guy
 * @since September 1st, 2024
 */
abstract class ClientPlayerIcon(expiryTime: Int? = null) {

    /** The texture to render above the player. */
    open val texture: ResourceLocation = cobblemonResource("textures/particle/icon_exclamation.png")
    open val Y_OFFSET = 1.5F

    // lifetime
    open val fadeTime = 0 //100
    private var tickCount = 0
    private val expiryTicks = expiryTime?.let { it % 20 }
    private val tickRateManager by lazy { Minecraft.getInstance().level!!.tickRateManager() }
    private val startTick = Minecraft.getInstance().player!!.tickCount

    // fade
    open val fadeDuration = 30   // ticks
    private var fadeCount = 0
    private var fadeOut = true
        set(value) {
            fadeCount = 0
            field = value
        }

    // alpha
    val MAX_ALPHA = 1F
    val MIN_ALPHA = 0.15F
    private var alpha = 1F
    private val startAlpha get() = if (fadeOut) MAX_ALPHA else MIN_ALPHA
    private val finalAlpha get() = if (fadeOut) MIN_ALPHA else MAX_ALPHA

    fun onTick() {
        tickCount++
        if (tickCount >= fadeTime) fadeCount++
        if (fadeCount == fadeDuration) fadeOut = !fadeOut
        alpha = if (tickCount >= fadeTime) Mth.lerp(fadeCount.toFloat()/fadeDuration, startAlpha, finalAlpha) else 1F
    }

    fun render(player: Player) {
        if (expiryTicks != null && tickCount >= expiryTicks) return
        //if (Minecraft.getInstance().player?.let { BattleRegistry.getBattleByParticipatingPlayerId(it.uuid) } == null) return

        DeferredRenderer.enqueue(RenderEvent.Stage.TRANSLUCENT) { event ->
            val poseStack = event.poseStack
            val partialTicks = event.tickCounter.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(player)) //LevelRenderer for Entities does this
            val camera = event.camera

            // changing alpha here?
            // keep in mind tick rate can change now... thanks Mojang. see TickRateManager
            // keep in mind server timing is based on system time. change this?

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
        poseStack.pushPose()
        poseStack.translate(player.getPosition(partialTicks).x, player.getPosition(partialTicks).y, player.getPosition(partialTicks).z)
        poseStack.translate(vec3!!.x, vec3.y + Y_OFFSET, vec3.z)
        poseStack.translate(-camera.position.x, -camera.position.y, -camera.position.z) // event posestack does not have PlayerRenderer context

        // billboard
        poseStack.mulPose(Quaternionf(0.0F, camera.rotation().y, 0.0F, camera.rotation().w))
        poseStack.scale(1.0f, -1.0f, 1.0f)
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
        RenderSystem.setShader { GameRenderer.getPositionTexColorShader() }
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()

        // TODO worth making a custom rendertype and getting buffer from multiBufferSource?
        val model = poseStack.last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buildTag(model, buffer)
        BufferUploader.drawWithShader(buffer.buildOrThrow())

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
            if (teamIcon != null) {
                trackedIcons[player] = teamIcon
            }
            else if (requests.isNotEmpty()) {
                val icon = if (requests.size > 1) CompoundClientPlayerIcon(*requests.toTypedArray()) else requests.first()
                trackedIcons[player] = icon
            }
            else {
                trackedIcons.remove(player)
            }
        }
        // TODO make some api so this is expandable? i cant be assed right now

        fun onRenderPlayer(player: Player) = trackedIcons[player.uuid]?.render(player)
    }
}