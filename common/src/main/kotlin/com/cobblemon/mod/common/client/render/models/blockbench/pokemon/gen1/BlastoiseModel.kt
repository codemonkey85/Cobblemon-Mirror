/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

///*
// * Copyright (C) 2023 Cobblemon Contributors
// *
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at https://mozilla.org/MPL/2.0/.
// */
//
//package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1
//
//import com.cobblemon.mod.common.client.render.models.blockbench.EarJoint
//import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
//import com.cobblemon.mod.common.client.render.models.blockbench.RangeOfMotion
//import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
//import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
//import com.cobblemon.mod.common.client.render.models.blockbench.frame.EaredFrame
//import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
//import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
//import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
////import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
//import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext//import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
//import com.cobblemon.mod.common.util.math.geometry.toRadians
//import net.minecraft.client.model.ModelPart
//import net.minecraft.util.math.Vec3d
//class BlastoiseModel(root: ModelPart) : PosableModel(), HeadedFrame, BipedFrame, BimanualFrame, EaredFrame {
//    override val rootPart = root.registerChildWithAllChildren("blastoise")
//    override val head = getPart("head_ai")
//    override val rightArm = getPart("arm_right")
//    override val leftArm = getPart("arm_left")
//    override val rightLeg = getPart("leg_right")
//    override val leftLeg = getPart("leg_left")
//    private val rightEar = getPart("ear_right")
//    private val leftEar = getPart("ear_left")
//    override val leftEarJoint = EarJoint(leftEar, ModelPartTransformation.Z_AXIS, RangeOfMotion(50F.toRadians(), 0F))
//    override val rightEarJoint = EarJoint(rightEar, ModelPartTransformation.Z_AXIS, RangeOfMotion((-50F).toRadians(), 0F))
//
//    override val portraitScale = 1.7F
//    override val portraitTranslation = Vec3d(-0.5, 1.4, 0.0)
//
//    override val profileScale = 0.63F
//    override val profileTranslation = Vec3d(0.0, 0.8, 0.0)
//
//    override val cryAnimation = CryProvider { bedrockStateful("blastoise", "cry") }
//
//    lateinit var sleep: Pose
//    lateinit var standing: Pose
//    lateinit var swimIdle: Pose
//    lateinit var swimMove: Pose
//    lateinit var walk: Pose
//
//    override fun registerPoses() {
//        val blink = quirk { bedrockStateful("blastoise", "blink")}
//        sleep = registerPose(
//            poseType = PoseType.SLEEP,
//            transformTicks = 10,
//            idleAnimations = arrayOf(bedrock("blastoise", "sleep"))
//        )
//
//        standing = registerPose(
//            poseName = "standing",
//            poseTypes = setOf(PoseType.NONE, PoseType.PROFILE, PoseType.PORTRAIT, PoseType.STAND),
//            transformTicks = 10,
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                singleBoneLook(),
//                bedrock("blastoise", "ground_idle")
//            )
//        )
//
//        swimIdle = registerPose(
//            poseName = "swim_idle",
//            poseTypes = setOf(PoseType.FLOAT),
//            transformTicks = 10,
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                bedrock("blastoise", "water_idle"),
//                singleBoneLook()
//            )
//        )
//
//        swimMove = registerPose(
//            poseName = "swim_move",
//            poseTypes = setOf(PoseType.SWIM),
//            transformTicks = 10,
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                singleBoneLook(),
//                bedrock("blastoise", "water_swim")
//            )
//        )
//
//        walk = registerPose(
//            poseType = PoseType.WALK,
//            transformTicks = 10,
//            quirks = arrayOf(blink),
//            idleAnimations = arrayOf(
//                singleBoneLook(),
//                bedrock("blastoise", "ground_walk")
//            )
//        )
//    }
//
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk, sleep)) bedrockStateful("blastoise", "faint") else null
//}