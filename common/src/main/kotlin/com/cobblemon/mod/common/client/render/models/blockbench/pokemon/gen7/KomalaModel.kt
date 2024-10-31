/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class KomalaModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("komala")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(0.0, -0.4, 0.0)
    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.5, 0.0)

    lateinit var standing: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("komala", "cry") }

    override fun registerPoses() {
        val doze = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("komala", "quirk_doze_off")}

        standing = registerPose(
            poseName = "standing",
            quirks = arrayOf(doze),
            poseTypes = PoseType.ALL_POSES,
            animations = arrayOf(bedrock("komala", "ground_idle"))
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing)) bedrockStateful("komala", "faint") else null
}