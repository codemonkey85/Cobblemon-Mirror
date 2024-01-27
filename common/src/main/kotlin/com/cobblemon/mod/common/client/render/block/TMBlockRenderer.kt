package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.tms.TechnicalMachines
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.block.TMBlock
import com.cobblemon.mod.common.block.entity.TMBlockEntity
import com.cobblemon.mod.common.client.CobblemonBakingOverrides
import com.cobblemon.mod.common.client.render.layer.CobblemonRenderLayers
import com.cobblemon.mod.common.client.render.models.blockbench.repository.MiscModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.setRotation
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.geometry.Axis
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import java.awt.Color
import kotlin.math.sin

class TMBlockRenderer(context: BlockEntityRendererFactory.Context) : BlockEntityRenderer<TMBlockEntity> {
    //TODO Do the render optimizations that we do for berries and fossils
    override fun render(
        entity: TMBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val diskModel = MiscModelRepository.modelOf(MODEL_ID)
        matrices.push()
        val tm = entity.tmmInventory.filterTM?.let {
            TechnicalMachines.getTechnicalMachineFromStack(
                it
            )
        }
        val color = tm?.let { Color(ElementalTypes.get(it.type)!!.hue) } ?: Color.WHITE
        when (entity.cachedState.get(TMBlock.FACING)) {
            Direction.SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F), 8.5F/16F, 0F, 8.5F/16F)
            Direction.WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F), 8.5F/16F, 0F, 8.5F/16F)
            Direction.EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F), 8.5F/16F, 0F, 8.5F/16F)
            else -> {matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.world!!.time.toFloat() + tickDelta) % 360), 8F/16F, 0F, 8F/16F)}
        }
        matrices.translate(15F / 16F, 5.5F / 16F, -1F / 16F)

        val renderLayer = RenderLayer.getEntityCutout(cobblemonResource("textures/block/tm_machine.png"))
        diskModel?.render(
            matrices,
            vertexConsumers.getBuffer(renderLayer),
            light,
            overlay,
            color.red.toFloat() / 255F,
            color.green.toFloat() / 255F,
            color.blue.toFloat() / 255F,
            1.0F
        )
        matrices.pop()
    }

    companion object {
        val MODEL_ID = cobblemonResource("tm_disk.geo")
    }

}