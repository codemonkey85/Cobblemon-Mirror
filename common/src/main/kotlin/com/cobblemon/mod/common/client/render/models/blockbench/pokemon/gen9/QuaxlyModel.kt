/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class QuaxlyModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame {
    override val rootPart = root.registerChildWithAllChildren("quaxly")
    override val head = getPart("head")

    private val messyHair = getPart("hair_messy")

    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.1, -0.5, 0.0)

    override var profileScale = 0.7F
    override var profileTranslation = Vec3(0.0, 0.71, 0.0)

    lateinit var standing: Pose
    lateinit var walk: Pose
    lateinit var floating: Pose
    lateinit var swimming: Pose
    lateinit var sleep: Pose
    lateinit var watersleep: Pose
    lateinit var battleidle: Pose
    lateinit var waterbattleidle: Pose
    lateinit var water_surface_idle: Pose
    lateinit var water_surface_swim: Pose

    val wateroffset = -10

    override val cryAnimation = CryProvider { bedrockStateful("quaxly", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("quaxly", "blink")}

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { !it.isInWater },
            animations = arrayOf(bedrock("quaxly", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            poseType = PoseType.SLEEP,
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { it.isInWater },
            animations = arrayOf(bedrock("quaxly", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { !it.isBattling && !it.isInWater && !it.isUnderWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 10,
            poseType = PoseType.WALK,
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { !it.isInWater && !it.isUnderWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { it.isUnderWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { it.isUnderWater },
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "water_swim"),
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { it.isBattling && !it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "battle_idle")
            )
        )

        waterbattleidle = registerPose(
            poseName = "water_battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        messyHair.createTransformation().withVisibility(visibility = false)
                ),
            condition = { it.isBattling && it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "surfacewater_battle_idle")
            )
        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isUnderWater && it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset),
                messyHair.createTransformation().withVisibility(visibility = false)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isUnderWater && it.isInWater},
            animations = arrayOf(
                singleBoneLook(),
                bedrock("quaxly", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset),
                messyHair.createTransformation().withVisibility(visibility = false)
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, battleidle, sleep)) bedrockStateful("quaxly", "faint") else if (state.isPosedIn(water_surface_idle, water_surface_swim, waterbattleidle, watersleep )) bedrockStateful("quaxly", "faint") else null
}