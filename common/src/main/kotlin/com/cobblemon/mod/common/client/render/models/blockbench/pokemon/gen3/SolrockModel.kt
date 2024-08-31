/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SolrockModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("solrock")

    override var portraitScale = 1.6F
    override var portraitTranslation = Vec3(-0.08, 0.31, 0.0)

    override var profileScale = 0.8F
    override var profileTranslation = Vec3(0.0, 0.74, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("solrock", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("solrock", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.ALL_POSES - PoseType.SLEEP,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("solrock", "ground_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(sleep)) bedrockStateful("solrock", "faint") else null
}