/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.cobblemon.mod.common.client.render.shader.CobblemonShaders
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureManager

object ParticleMaterials {
    val ALPHA = object : ParticleRenderType {
        override fun begin(tessellator: Tesselator, textureManager: TextureManager): BufferBuilder {
            RenderSystem.enableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.setShader { CobblemonShaders.PARTICLE_CUTOUT }
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
            return tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
        }

        override fun toString() = "ALPHA"
    }

    val ADD = object : ParticleRenderType {
        override fun begin(tessellator: Tesselator, textureManager: TextureManager): BufferBuilder {
            RenderSystem.enableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.setShader { CobblemonShaders.PARTICLE_BLEND }
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
            return tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
        }

        override fun toString() = "ADD"
    }

    val BLEND = object : ParticleRenderType {
        override fun begin(tessellator: Tesselator, textureManager: TextureManager): BufferBuilder {
            RenderSystem.enableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.setShader { CobblemonShaders.PARTICLE_BLEND }
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
            RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO)
            return tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
        }

        override fun toString() = "BLEND"
    }

    val OPAQUE = object : ParticleRenderType {
        override fun begin(tessellator: Tesselator, textureManager: TextureManager): BufferBuilder {
            RenderSystem.enableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.setShader { CobblemonShaders.PARTICLE_BLEND }
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE)
            return tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
        }

        override fun toString() =  "OPAQUE"
    }
}