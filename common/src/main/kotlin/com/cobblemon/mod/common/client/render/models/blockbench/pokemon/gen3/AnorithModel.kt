/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class AnorithModel (root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("anorith")

    override var portraitTranslation = Vec3(-0.14, -1.56, 0.0)
    override var portraitScale = 1.8F

    override var profileTranslation = Vec3(-0.05, -0.19, 0.0)
    override var profileScale = 1.03F

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var ui_poses: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var water_surface_sleep: CobblemonPose
    lateinit var water_sleep: CobblemonPose
    lateinit var water_surface_idle: CobblemonPose
    lateinit var water_idle: CobblemonPose
    lateinit var water_surface_battleidle: CobblemonPose
    lateinit var water_battleidle: CobblemonPose
    lateinit var water_surface_swim: CobblemonPose
    lateinit var water_swim: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose

    val shoulderOffset = 5.5
    val wateroffset = -4.5
    val watersurfaceoffset = 1

    override val cryAnimation = CryProvider { bedrockStateful("anorith", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("anorith", "blink") }

        ui_poses = registerPose (
                poseName = "ui_poses",
                poseTypes = PoseType.UI_POSES,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "summary_idle")
                )
        )

        sleep = registerPose(
                poseName = "sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isInWater },
                animations = arrayOf(bedrock("anorith", "sleep")
                )
        )

        water_surface_sleep = registerPose(
                poseName = "water_surface_sleep",
                poseType = PoseType.SLEEP,
                condition = { !it.isUnderWater && it.isInWater },
                animations = arrayOf(bedrock("anorith", "water_sleep")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, watersurfaceoffset)
                )
        )

        water_sleep = registerPose(
                poseName = "water_sleep",
                poseType = PoseType.SLEEP,
                condition = { it.isUnderWater && it.isInWater },
                animations = arrayOf(bedrock("anorith", "water_sleep")),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
                )
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                condition = { !it.isBattling && !it.isInWater},
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "ground_idle")
            )
        )

        water_surface_idle = registerPose(
                poseName = "water_surface_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                condition = { !it.isUnderWater && it.isInWater && !it.isBattling},
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "water_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, watersurfaceoffset)
                )
        )

        water_idle = registerPose(
                poseName = "water_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                condition = { it.isUnderWater && it.isInWater && !it.isBattling},
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "water_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
                )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                condition = { it.isBattling && !it.isInWater},
                animations = arrayOf(
                        bedrock("anorith", "battle_idle")
                )
        )

        water_surface_battleidle = registerPose(
                poseName = "surface_battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                condition = { !it.isUnderWater && it.isInWater && it.isBattling},
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "water_battle_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, watersurfaceoffset)
                )
        )

        water_battleidle = registerPose(
                poseName = "water_battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                condition = { it.isUnderWater && it.isInWater && it.isBattling},
                transformTicks = 10,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "water_battle_idle")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
                )
        )

        walking = registerPose(
                poseName = "walking",
                poseTypes = PoseType.MOVING_POSES,
                quirks = arrayOf(blink),
                condition = { !it.isUnderWater && !it.isInWater },
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("anorith", "ground_walk")
            )
        )

        water_surface_swim = registerPose(
                poseName = "surface_swim",
                poseTypes = PoseType.MOVING_POSES,
                quirks = arrayOf(blink),
                condition = { !it.isUnderWater && it.isInWater},
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("anorith", "water_swim")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, watersurfaceoffset)
                )
        )

        water_swim = registerPose(
                poseName = "water_swim",
                poseTypes = PoseType.MOVING_POSES,
                quirks = arrayOf(blink),
                condition = { it.isUnderWater && it.isInWater },
                transformTicks = 10,
                animations = arrayOf(
                        bedrock("anorith", "water_swim")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
                )
        )

        shoulderLeft = registerPose(
                poseType = PoseType.SHOULDER_LEFT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "shoulder_left")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, shoulderOffset),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, 2),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Z_AXIS, 2)
                )
        )

        shoulderRight = registerPose(
                poseType = PoseType.SHOULDER_RIGHT,
                quirks = arrayOf(blink),
                animations = arrayOf(
                        bedrock("anorith", "shoulder_right")
                ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.X_AXIS, -shoulderOffset),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, 2),
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Z_AXIS, 2)
                )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking)) bedrockStateful("anorith", "faint") else null
}