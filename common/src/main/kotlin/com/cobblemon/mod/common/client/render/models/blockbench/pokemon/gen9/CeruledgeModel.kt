/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

// import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class CeruledgeModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("ceruledge")
    override val head = getPart("head")
    override val rightArm = getPart("arm_right")
    override val leftArm = getPart("arm_left")
    override val rightLeg = getPart("leg_right")
    override val leftLeg = getPart("leg_left")
    val bladeright = getPart("blade_right")
    val bladeleft = getPart("blade_left")
    val passivebladeright = getPart("passive_blade_right")
    val passivebladeleft = getPart("passive_blade_left")

    override var portraitScale = 1.8F
    override var portraitTranslation = Vec3(-0.2, 1.9, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: Pose
    lateinit var walking: Pose
    lateinit var sleep: Pose
    lateinit var battleidle: Pose

    override val cryAnimation = CryProvider { bedrockStateful("ceruledge", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("ceruledge", "blink") }
        sleep = registerPose(
            poseType = PoseType.SLEEP,
            transformedParts = arrayOf(
                bladeright.createTransformation().withVisibility(visibility = false),
                bladeleft.createTransformation().withVisibility(visibility = false),
                passivebladeright.createTransformation().withVisibility(visibility = true),
                passivebladeleft.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(bedrock("ceruledge", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { !it.isBattling },
            transformedParts = arrayOf(
                bladeright.createTransformation().withVisibility(visibility = false),
                bladeleft.createTransformation().withVisibility(visibility = false),
                passivebladeright.createTransformation().withVisibility(visibility = true),
                passivebladeleft.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ceruledge", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                bladeright.createTransformation().withVisibility(visibility = false),
                bladeleft.createTransformation().withVisibility(visibility = false),
                passivebladeright.createTransformation().withVisibility(visibility = true),
                passivebladeleft.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ceruledge", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            transformedParts = arrayOf(
                bladeright.createTransformation().withVisibility(visibility = true),
                bladeleft.createTransformation().withVisibility(visibility = true),
                passivebladeright.createTransformation().withVisibility(visibility = false),
                passivebladeleft.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("ceruledge", "battle_idle")
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("ceruledge", "faint2") else null
}