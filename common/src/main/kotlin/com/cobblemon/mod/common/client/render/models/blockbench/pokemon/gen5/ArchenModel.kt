/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class ArchenModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("archen")
    override val head = getPart("head")

    override var portraitTranslation = Vec3(-0.32, -0.07, 0.0)
    override var portraitScale = 1.81F

    override var profileTranslation = Vec3(-0.02, 0.87, 0.0)
    override var profileScale = 0.58F

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var swim: CobblemonPose
    lateinit var float: CobblemonPose
    lateinit var falling: CobblemonPose
    lateinit var battleIdle: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    override val cryAnimation = CryProvider { if (it.isPosedIn(battleIdle)) bedrockStateful("archen", "battle_cry") else bedrockStateful("archen", "cry") }

    val shoulderOffset = 0

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("archen", "blink") }
        val idleQuirk = quirk (30f to 60f) { bedrockStateful("archen", "idle_quirk") }
        val quirk = quirk (30f to 60f) { bedrockStateful("archen", "quirk") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            transformTicks = 10,
            animations = arrayOf(
                bedrock("archen", "sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES - PoseType.HOVER,
            quirks = arrayOf(blink, idleQuirk, quirk),
            condition = { (it.getEntity() as? PokemonEntity)?.isFalling() != true && !it.isInWater && !it.isBattling },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "ground_idle")
            )
        )

        walking = registerPose(
            poseName = "walking",
            poseTypes = PoseType.MOVING_POSES - PoseType.SWIM,
            condition = { (it.getEntity() as? PokemonEntity)?.isFalling() != true && !it.isInWater },
            quirks = arrayOf(blink),
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "ground_walk")
            )
        )

        swim = registerPose(
            poseName = "swim",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "surfacewater_swim")
            )
        )

        float = registerPose(
            poseName = "float",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, idleQuirk),
            condition = { it.isInWater },
            transformTicks = 10,
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "surfacewater_idle")
            )
        )

        battleIdle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "battle_idle")
            )
        )

        falling = registerPose(
            poseName = "falling",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { (it.getEntity() as? PokemonEntity)?.isFalling() == true },
            transformTicks = 10,
            animations = arrayOf(
                bedrock("archen", "chicken_fall")
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "shoulder_left")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset)
            )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            quirks = arrayOf(blink, quirk),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("archen", "shoulder_right")
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset)
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking)) bedrockStateful("archen", "faint") else null
}