/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import net.minecraft.core.particles.ParticleOptions

class SnowstormParticleOptions(val effect: BedrockParticleOptions) : ParticleOptions {
    override fun getType() = CobblemonParticles.SNOWSTORM_PARTICLE_TYPE
    companion object {

    }
}