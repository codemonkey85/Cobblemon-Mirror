/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ZubatModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("zubat")

    val wings_folded = getPart("wings_folded")
    val wings_open = getPart("wings_open")

    override var portraitScale = 1.7F
    override var portraitTranslation = Vec3(0.0, 0.0, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.7, 0.0)

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var hover: Pose
    lateinit var fly: Pose
    lateinit var shoulderLeft: Pose
    lateinit var shoulderRight: Pose

    val shoulderOffset = 0

    override val cryAnimation = CryProvider { bedrockStateful("zubat", "cry") }

    override fun registerPoses() {
        val twitch = quirk { bedrockStateful("zubat", "eartwitch") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformedParts = arrayOf(
                wings_folded.createTransformation().withVisibility(visibility = false),
                wings_open.createTransformation().withVisibility(visibility = true),
            ),
            quirks = arrayOf(twitch),
            animations = arrayOf(bedrock("zubat", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.HOVER,
            transformedParts = arrayOf(
                wings_folded.createTransformation().withVisibility(visibility = false),
                wings_open.createTransformation().withVisibility(visibility = true),
            ),
            transformTicks = 10,
            quirks = arrayOf(twitch),
            animations = arrayOf(
                bedrock("zubat", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES - PoseType.FLY,
            transformedParts = arrayOf(
                wings_folded.createTransformation().withVisibility(visibility = false),
                wings_open.createTransformation().withVisibility(visibility = true),
            ),
            transformTicks = 10,
            quirks = arrayOf(twitch),
            animations = arrayOf(
                bedrock("zubat", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            transformedParts = arrayOf(
                wings_folded.createTransformation().withVisibility(visibility = false),
                wings_open.createTransformation().withVisibility(visibility = true),
            ),
            transformTicks = 10,
            quirks = arrayOf(twitch),
            animations = arrayOf(
                bedrock("zubat", "air_idle")
            )
        )

        fly = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            transformedParts = arrayOf(
                wings_folded.createTransformation().withVisibility(visibility = false),
                wings_open.createTransformation().withVisibility(visibility = true),
            ),
            quirks = arrayOf(twitch),
            transformTicks = 10,
            animations = arrayOf(
                bedrock("zubat", "air_fly")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            animations = arrayOf(
                bedrock("zubat", "shoulder_left")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(X_AXIS, shoulderOffset)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            animations = arrayOf(
                bedrock("zubat", "shoulder_right")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(X_AXIS, -shoulderOffset)
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("zubat", "faint") else null
}