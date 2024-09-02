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

class LunatoneModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("lunatone")

    override var portraitScale = 1.05F
    override var portraitTranslation = Vec3(0.23, 0.53, 0.0)

    override var profileScale = 0.73F
    override var profileTranslation = Vec3(0.05, 0.68, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("lunatone", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            animations = arrayOf(bedrock("lunatone", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.ALL_POSES - PoseType.SLEEP,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("lunatone", "ground_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(sleep)) bedrockStateful("lunatone", "faint") else null
}