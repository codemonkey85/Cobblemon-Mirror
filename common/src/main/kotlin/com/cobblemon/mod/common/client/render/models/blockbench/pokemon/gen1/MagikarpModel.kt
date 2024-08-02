/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MagikarpModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("magikarp")

    override var portraitScale = 2.0F
    override var portraitTranslation = Vec3(-0.1, -0.75, 0.0)
    override var profileScale = 0.95F
    override var profileTranslation = Vec3(0.0, 0.40, 0.0)
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var floating: CobblemonPose
    lateinit var swimming: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var watersleep: CobblemonPose
    lateinit var water_surface_idle: CobblemonPose
    lateinit var water_surface_swim: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    val wateroffset = -10

    override val cryAnimation = CryProvider { bedrockStateful("magikarp", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("magikarp", "blink")}
        val sleepQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("magikarp", "sleep_quirk")}
        val waterSurfaceQuirk = quirk(secondsBetweenOccurrences = 60F to 120F) { bedrockStateful("magikarp", "surfacewater_quirk")}

        sleep = registerPose(
            poseName = "sleeping",
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleepQuirk),
            condition = { !it.isInWater },
            animations = arrayOf(bedrock("magikarp", "sleep"))
        )

        watersleep = registerPose(
            poseName = "water_sleeping",
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleepQuirk),
            condition = { it.isInWater },
            animations = arrayOf(bedrock("magikarp", "water_sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STAND,
            transformTicks = 10,
            condition = {!it.isInWater && !it.isUnderWater},
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magikarp", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            transformTicks = 10,
            poseType = PoseType.WALK,
            condition = { !it.isInWater && !it.isUnderWater},
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magikarp", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            condition = { it.isUnderWater },
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magikarp", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            transformTicks = 10,
            condition = { it.isUnderWater },
            poseType = PoseType.SWIM,
            quirks = arrayOf(blink),
            animations = arrayOf(
                bedrock("magikarp", "water_swim"),
            )
        )

//        battleidle = registerPose(
//            poseName = "battle_idle",
//            poseTypes = PoseType.STATIONARY_POSES,
//            transformTicks = 10,
//            quirks = arrayOf(blink),
//            condition = { it.isBattling && !it.isInWater },
//            idleAnimations = arrayOf(
//                bedrock("magikarp", "battle_idle")
//            )
//        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, waterSurfaceQuirk),
            condition = { !it.isUnderWater && it.isInWater },
            animations = arrayOf(
                bedrock("magikarp", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { !it.isUnderWater && it.isInWater },
            animations = arrayOf(
                bedrock("magikarp", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )
    }
    override fun getFaintAnimation(state: PosableState) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("magikarp", "ground_faint") else if (state.isPosedIn(floating, swimming, water_surface_idle, water_surface_swim, watersleep )) bedrockStateful("magikarp", "water_faint") else null
}