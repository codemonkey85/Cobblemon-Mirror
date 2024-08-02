/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.api.pokemon.breeding.Egg
import com.cobblemon.mod.common.api.pokemon.breeding.EggPattern
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonModel
import com.mojang.authlib.minecraft.client.MinecraftClient
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.platform.Lighting
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.SimpleBakedModel
import net.minecraft.client.renderer.FaceInfo
import net.minecraft.client.renderer.block.model.ItemModelGenerator
import net.minecraft.client.renderer.block.model.BlockElement
import net.minecraft.client.renderer.block.model.BlockElementFace
import net.minecraft.client.renderer.block.model.BlockFaceUV
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.ModelResourceLocation
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

// Abandon all hope, ye who enter here
// I'm not joking this is so bad. Most of it is copied from MC classes so don't blame me :(
// "Why don't you just define models for every pattern?" - ModelOverrides aren't complex enough to do what we wanna do here, since we only want one egg item
/**
 * Generates and renders models for eggs in the inventory
 *
 * @author Apion
 * @since February 12, 2024
 **/
class PokemonEggItemRenderer : CobblemonBuiltinItemRenderer {
    val generatedModels = hashMapOf<EggPattern, BakedModel>()
    //DONT FORGET TO CACHE MODELS AFTER THEY ARE GENERATED AT SOME POINT
    override fun render(
        stack: ItemStack,
        mode: ItemDisplayContext,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        //FIXME: Componentify
        /*
        val nbt = BlockItem.getBlockEntityNbt(stack) ?: return
        //I think it could be wise to change this so that the pokemon in the egg is not deserialized
        //Basically only deserialize the minimum amount of info needed, textures
        val egg = Egg.fromBlockNbt(nbt)
        //Since we are delegating back to the item renderer, we need the matrix frame from BEFORE the item renderer first ran
        matrices.popPose()
        val pattern = egg.getPattern() ?: return
        val model = getBakedModel(pattern)
        matrices.pushPose()
        //GREMEDYStringMarker.glStringMarkerGREMEDY("Rendering egg item")
        val oldShaderLights = RenderSystem.shaderLightDirections
        //Might need to handle this differently depending on the mode
        Lighting.setupForFlatItems()
        val isLeftHanded = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND
        MinecraftClient.getInstance().itemRenderer.renderItem(stack, mode, isLeftHanded, matrices, vertexConsumers, light, overlay, model)
        matrices.popPose()
        //Since the item renderer pops the frame off the we popped earlier, we need to put a frame back
        matrices.pushPose()
        RenderSystem.setShaderLights(oldShaderLights[0], oldShaderLights[1])

         */
    }

    fun getBakedModel(eggPattern: EggPattern): BakedModel {
        if (generatedModels.containsKey(eggPattern)) {
            return generatedModels[eggPattern]!!
        }
        val baseTexId = eggPattern.baseInvSpritePath
        val overlayTexId = eggPattern.overlayInvSpritePath
        val atlas = Minecraft.getInstance().getTextureAtlas(ResourceLocation.tryParse("minecraft:textures/atlas/blocks.png"))
        val baseSprite = atlas.apply(ResourceLocation.tryBuild(baseTexId.namespace, "item/${baseTexId.path}"))
        val uvArrOne = FloatArray(4)
        uvArrOne[0] = 0F
        uvArrOne[1] = 0F
        uvArrOne[2] = 16F
        uvArrOne[3] = 16F
        val uvArrTwo = FloatArray(4)
        uvArrTwo[0] = 16F
        uvArrTwo[1] = 0F
        uvArrTwo[2] = 0F
        uvArrTwo[3] = 16F
        //North and south faces are done separately for some reason
        val baseElementList = mutableListOf(
            BlockElement(
                Vector3f(0.0F, 0.0F, 7.5F),
                Vector3f(16.0F, 16.0F, 8.5F),
                buildMap {
                    this[Direction.SOUTH] = BlockElementFace(null, 0, "item/${baseTexId.path}", BlockFaceUV(uvArrOne, 0))
                    this[Direction.NORTH] = BlockElementFace(null, 0, "item/${baseTexId.path}", BlockFaceUV(uvArrTwo, 0))
                },
                null,
                false
            )
        )
        //This generates all of the other faces, but some more work is needed to bake them
        baseElementList.addAll(MODEL_GENERATOR.createSideElements(baseSprite.contents(), "layer0", 0))
        var overlayElementList: MutableList<BlockElement>? = null
        var overlaySprite: TextureAtlasSprite? = null
        overlayTexId?.let {
            overlaySprite = atlas.apply(ResourceLocation.fromNamespaceAndPath(baseTexId.namespace, "item/${overlayTexId.path}"))
            if (overlaySprite == null) return@let
            overlayElementList = mutableListOf(
                BlockElement(
                    Vector3f(0.0F, 0.0F, 7.5F),
                    Vector3f(16.0F, 16.0F, 8.5F),
                    buildMap {
                        this[Direction.SOUTH] = BlockElementFace(null, 1, "item/${overlayTexId.path}", BlockFaceUV(uvArrOne, 0))
                        this[Direction.NORTH] = BlockElementFace(null, 1, "item/${overlayTexId.path}", BlockFaceUV(uvArrTwo, 0))
                    },
                    null,
                    true
                )
            )
            overlayElementList!!.addAll(MODEL_GENERATOR.createSideElements(overlaySprite!!.contents(), "layer1", 1))
        }
        //I do not know how these cull faces work
        val faceQuadMaps = Direction.values().associateWith {
            emptyList<BakedQuad>()
        }
        val quads = generateQuads(baseElementList, 0, baseSprite)
        if (overlayElementList != null) {
            quads.addAll(generateQuads(overlayElementList!!, 1, overlaySprite!!))
        }
        return SimpleBakedModel(
            quads,
            faceQuadMaps,
            false,
            false,
            false,
            //This is the particle sprite, so i assume unused in an item context
            baseSprite,
            DEFAULT_MODEL_TRANSFORMS,
            ItemOverrides.EMPTY
        )
    }

    fun generateBakedQuad(
        cornerOnePos: Vector3f,
        cornerOneUv: Vector2f,
        cornerTwoPos: Vector3f,
        cornerTwoUv: Vector2f,
        cornerThreePos: Vector3f,
        cornerThreeUv: Vector2f,
        cornerFourPos: Vector3f,
        cornerFourUv: Vector2f,
        colorIndex: Int,
        sprite: TextureAtlasSprite,
        face: Direction
    ): BakedQuad {
        val vertBuf = MemoryStack.stackPush()
        vertBuf.use {
            //4 verts per quad * 4 bytes per float * 8 floats per vert (only 6 are put in but MC expects 8 ints/floats per vert)
            val buf = it.malloc(4 * 4 * 8)
            packVertexData(cornerOnePos, cornerOneUv, buf)
            packVertexData(cornerTwoPos, cornerTwoUv, buf)
            packVertexData(cornerThreePos, cornerThreeUv, buf)
            packVertexData(cornerFourPos, cornerFourUv, buf)
            val vertData = IntArray(32)
            buf.rewind()
            var vertDataIndex = 0
            while (buf.hasRemaining()) {
                vertData[vertDataIndex] = buf.getInt()
                vertDataIndex++
            }
            return BakedQuad(vertData, colorIndex, face, sprite, true)
        }

    }

    //Mostly copied from BakedQuadFactory, with some consolidation
    //Creates BakedQuads from model elements, tintable using ColorProviders
    //This could be moved into its own class that deals with baking models someday
    //I would like to clean this up to not use all the cube face stuff, but that requires thought
    fun generateQuads(modelElements: List<BlockElement>, colorIndex: Int, sprite: TextureAtlasSprite): MutableList<BakedQuad> {
        val result = mutableListOf<BakedQuad>()
        modelElements.forEach { element ->
            element.faces.forEach { face ->
                val cornerArray = getPositionMatrix(element.from, element.to)
                //These are the index of the corner coordinate in cornerArray
                //All this cube face stuff is basically some abstractions to get the 4 quad coords
                //when we are only given 2 coords that are connected on a diagonal.
                //Could probably get rid of them if we want to do the maths
                //Numbers are drawing order
                val cornerOne = FaceInfo.fromFacing(face.key).getVertexInfo(0)
                val cornerTwo = FaceInfo.fromFacing(face.key).getVertexInfo(1)
                val cornerThree = FaceInfo.fromFacing(face.key).getVertexInfo(2)
                val cornerFour = FaceInfo.fromFacing(face.key).getVertexInfo(3)
                //HIGH CHANCE THIS IS INCCORECT AND MESSED UP IN PORT TO 1.21
                val uOne = sprite.getU(face.value.uv.getU(0))
                val vOne = sprite.getV(face.value.uv.getV(0))
                val uTwo = sprite.getU(face.value.uv.getU(1))
                val vTwo = sprite.getV(face.value.uv.getV(1))
                val uThree = sprite.getU(face.value.uv.getU(2))
                val vThree = sprite.getV(face.value.uv.getV(2))
                val uFour = sprite.getU(face.value.uv.getU(3))
                val vFour = sprite.getV(face.value.uv.getV(3))
                result.add(generateBakedQuad(
                    Vector3f(cornerArray[cornerOne.xFace], cornerArray[cornerOne.yFace], cornerArray[cornerOne.zFace]),
                    Vector2f(uOne, vOne),
                    Vector3f(cornerArray[cornerTwo.xFace], cornerArray[cornerTwo.yFace], cornerArray[cornerTwo.zFace]),
                    Vector2f(uTwo, vTwo),
                    Vector3f(cornerArray[cornerThree.xFace], cornerArray[cornerThree.yFace], cornerArray[cornerThree.zFace]),
                    Vector2f(uThree, vThree),
                    Vector3f(cornerArray[cornerFour.xFace], cornerArray[cornerFour.yFace], cornerArray[cornerFour.zFace]),
                    Vector2f(uFour, vFour),
                    colorIndex,
                    sprite,
                    face.key
                ))
            }
        }
        return result
    }

    //Takes a vertex and packs it into buf in the format expected by BakedQuads
    fun packVertexData(position: Vector3f, uvs: Vector2f, buf: ByteBuffer) {
        buf.putFloat(position.x)
        buf.putFloat(position.y)
        buf.putFloat(position.z)
        //Idk why mc puts this in, but it does, so dont touch it
        buf.putInt(-1)
        buf.putFloat(uvs.x)
        buf.putFloat(uvs.y)
        //Padding
        buf.position(buf.position() + 8)
    }

    companion object {
        val MODEL_GENERATOR = ItemModelGenerator()
        //These are the transformations applied depending on the ModelTransformationMode.
        //Obtained from minecraft/assets/model/item/generated.json, the parent model for all generated item models
        //The modVector function does the same transformations done in [Transformation.Deserializer] <- >:( (So dumb)
        val DEFAULT_MODEL_TRANSFORMS = ItemTransforms(
            ItemTransform(Vector3f(0F, 0F, 0F), modTranslationVector(Vector3f(0F, 3F, 1F)), modScaleVector(Vector3f(0.68F, 0.68F, 0.68F))),
            ItemTransform(Vector3f(0F, 0F, 0F), modTranslationVector(Vector3f(0F, 3F, 1F)), modScaleVector(Vector3f(0.68F, 0.68F, 0.68F))),
            ItemTransform(Vector3f(0F, -90F, 25F), modTranslationVector(Vector3f(1.13F, 3.2F, 1.13F)), modScaleVector(Vector3f(0.68F, 0.68F, 0.68F))),
            ItemTransform(Vector3f(0F, -90F, 25F), modTranslationVector(Vector3f(1.13F, 3.2F, 1.13F)), modScaleVector(Vector3f(0.68F, 0.68F, 0.68F))),
            ItemTransform(Vector3f(0F, 180F, 0F), modTranslationVector( Vector3f(0F, 13F, 7F)), modScaleVector(Vector3f(1F, 1F, 1F))),
            ItemTransform.NO_TRANSFORM,
            ItemTransform(Vector3f(0F, 0F, 0F), modTranslationVector(Vector3f(0F, 2F, 0F)), modScaleVector(Vector3f(0.5F, 0.5F, 0.5F))),
            ItemTransform(Vector3f(0F, 180F, 0F), modTranslationVector(Vector3f(0F, 0F, 0F)), modScaleVector(Vector3f(1F, 1F, 1F)))
        )

        //TODO: Turn these into VectorUtil methods
        //Random transformations that MC does (WHY???? I HAVE NO IDEA)
        //Mostly gotten from Transformation.Deserializer
        fun modTranslationVector(translationVec: Vector3f): Vector3f {
            translationVec.mul(0.0625F)
            translationVec.set(
                Mth.clamp(translationVec.x, -5.0f, 5.0f),
                Mth.clamp(translationVec.y, -5.0f, 5.0f),
                Mth.clamp(translationVec.z, -5.0f, 5.0f)
            )
            return translationVec
        }

        fun modScaleVector(scaleVec: Vector3f): Vector3f {
            scaleVec.set(
                Mth.clamp(scaleVec.x, -4.0f, 4.0f),
                Mth.clamp(scaleVec.y, -4.0f, 4.0f),
                Mth.clamp(scaleVec.z, -4.0f, 4.0f)
            )
            return scaleVec
        }

        //Copied from BakedQuadFactory
        //TODO: Double check that this is correct after 1.21 port
        private fun getPositionMatrix(from: Vector3f, to: Vector3f): FloatArray {
            val fs = FloatArray(Direction.values().size)
            fs[FaceInfo.WEST.ordinal] = from.x() / 16.0f
            fs[FaceInfo.DOWN.ordinal] = from.y() / 16.0f
            fs[FaceInfo.NORTH.ordinal] = from.z() / 16.0f
            fs[FaceInfo.EAST.ordinal] = to.x() / 16.0f
            fs[FaceInfo.UP.ordinal] = to.y() / 16.0f
            fs[FaceInfo.SOUTH.ordinal] = to.z() / 16.0f
            return fs
        }
    }
}