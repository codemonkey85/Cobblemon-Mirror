/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class WalkingwakeModel (root: ModelPart) : PosableModel(), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("walkingwake")
    override val head = getPart("head")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override val portraitScale = 2.2F
    override val portraitTranslation = Vec3d(-2.5, 2.4, 0.0)

    override val profileScale = 0.35F
    override val profileTranslation = Vec3d(0.0, 1.2, 0.0)

    val hair = getPart("hair")

    lateinit var sleep: Pose
    lateinit var standing: Pose
    lateinit var walk: Pose

    lateinit var waterstanding: Pose
    lateinit var waterwalk: Pose

    //lateinit var shearedstanding: Pose
    //lateinit var shearedwalk: Pose
    val wateroffset = -6

    override fun registerPoses() {
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            idleAnimations = arrayOf(bedrock("walkingwake", "sleep"))
        )

        val blink = quirk { bedrockStateful("walkingwake", "blink") }
        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == false },
            /*          condition = { !it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
                        transformedParts = arrayOf(
                            hair.asTransformed().withVisibility(visibility = true)
                        ), */
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_idle")
            )
        )

        waterstanding = registerPose(
            poseName = "waterstanding",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == true },
            /*          condition = { !it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
                        transformedParts = arrayOf(
                            hair.asTransformed().withVisibility(visibility = true)
                        ), */
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_idle")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == false },
            /*          condition = { !it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
                        transformedParts = arrayOf(
                            hair.asTransformed().withVisibility(visibility = true)
                        ), */
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_walk"),
            )
        )

        waterwalk = registerPose(
            poseName = "waterwalk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { it.entity?.isTouchingWater == true },
            /*          condition = { !it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
                        transformedParts = arrayOf(
                            hair.asTransformed().withVisibility(visibility = true)
                        ), */
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_walk"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
/*        shearedstanding = registerPose(
            poseName = "shearedstanding",
            poseTypes = setOf(PoseType.NONE, PoseType.STAND, PoseType.PORTRAIT, PoseType.PROFILE),
            transformTicks = 0,
            quirks = arrayOf(blink),
            condition = { it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
            transformedParts = arrayOf(
                hair.asTransformed().withVisibility(visibility = false)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_idle")
            )
        )
        shearedwalk = registerPose(
            poseName = "shearedwalking",
            poseTypes = setOf(PoseType.SWIM, PoseType.WALK),
            transformTicks = 0,
            quirks = arrayOf(blink),
            condition = { it.containsAspect(DataKeys.HAS_BEEN_SHEARED).get() },
            transformedParts = arrayOf(
                hair.asTransformed().withVisibility(visibility = false)
            ),
            idleAnimations = arrayOf(
                singleBoneLook(),
                bedrock("walkingwake", "ground_walk")
            )
        )
*/
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isNotPosedIn(sleep)) bedrockStateful("walkingwake", "faint") else null
}