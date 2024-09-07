/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.shader

import com.cobblemon.mod.common.util.ShaderRegistryData
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.server.packs.resources.ResourceProvider
import java.util.function.Consumer

object CobblemonShaders {
    val SHADERS_TO_REGISTER = mutableListOf<Pair<(ResourceProvider) -> ShaderRegistryData, Consumer<ShaderInstance>>>()
    lateinit var PARTICLE_BLEND: ShaderInstance
    // This is Material.ALPHA. Weird internal name for "alphatest" shader.
    lateinit var PARTICLE_CUTOUT: ShaderInstance

    private fun registerShader(shader: (ResourceProvider) -> ShaderRegistryData, callback: Consumer<ShaderInstance>){
        SHADERS_TO_REGISTER.add(Pair(shader, callback))
    }
    fun init(){
        registerShader({rm: ResourceProvider -> ShaderRegistryData(rm, cobblemonResource("particle_add"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) }) {
            PARTICLE_BLEND = it
        }
        registerShader({rm: ResourceProvider -> ShaderRegistryData(rm, cobblemonResource("particle_cutout"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) }) {
            PARTICLE_CUTOUT = it
        }
    }
}