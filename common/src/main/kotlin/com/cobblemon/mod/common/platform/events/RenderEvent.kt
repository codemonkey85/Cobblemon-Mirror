package com.cobblemon.mod.common.platform.events

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.culling.Frustum
import org.joml.Matrix4f

/**
 * Event fired during the various [Stage]s of the [LevelRenderer].
 *
 * @author Segfault Guy
 * @since September 11th, 2024
 */
data class RenderEvent(
    val stage: Stage,
    val levelRenderer: LevelRenderer,
    val poseStack: PoseStack,
    val modelViewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val tickCounter: DeltaTracker,
    val camera: Camera
) {

    /** Represents when a layer is rendered by [LevelRenderer.renderLevel]. Ordinal corresponds with its render order. */
    enum class Stage {
        TRANSLUCENT
    }
}