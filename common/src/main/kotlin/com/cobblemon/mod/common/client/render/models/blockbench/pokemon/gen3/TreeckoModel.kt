/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TreeckoModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("treecko")
    override val head = getPart("head")

    override var portraitScale = 2.25F
    override var portraitTranslation = Vec3(0.02, 0.12, 0.0)

    override var profileScale = 0.74F
    override var profileTranslation = Vec3(0.0, 0.6, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("treecko", "cry") }

    override fun registerPoses() {
        //sleep = registerPose(
        //    poseType = PoseType.SLEEP,
        //    animations = arrayOf(bedrock("treecko", "sleep"))
        //)

        val blink = quirk { bedrockStateful("treecko", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES + PoseType.SLEEP,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("treecko", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("treecko", "ground_walk"),
            )
        )
    }
    //override fun getFaintAnimation(
    //    pokemonEntity: PokemonEntity,
    //    state: PosableState<PokemonEntity>
    //) = if (state.isNotPosedIn(sleep)) bedrockStateful("treecko", "faint") else null
}
