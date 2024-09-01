/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey

object CobblemonParticles : PlatformRegistry<Registry<ParticleType<*>>, ResourceKey<Registry<ParticleType<*>>>, ParticleType<*>>() {

    override val registry: Registry<ParticleType<*>> = BuiltInRegistries.PARTICLE_TYPE
    override val resourceKey: ResourceKey<Registry<ParticleType<*>>> = Registries.PARTICLE_TYPE

    val SNOWSTORM_PARTICLE_TYPE = this.create("snowstorm", SnowstormParticleType())

}