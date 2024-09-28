/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9

import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BimanualFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class GimmighoulChestModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame, BipedFrame, BimanualFrame {
    override val rootPart = root.registerChildWithAllChildren("gimmighoul_chest")
    override val head = getPart("head")

    override val leftArm = getPart("arm_left")
    override val rightArm = getPart("arm_right")
    override val leftLeg = getPart("leg_left")
    override val rightLeg = getPart("leg_right")

    override var portraitScale = 2.54F
    override var portraitTranslation = Vec3(-0.01, -1.6, 0.0)

    override var profileScale = 0.65F
    override var profileTranslation = Vec3(0.0, 0.76, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var closed: CobblemonPose
    lateinit var battle: CobblemonPose

    override val cryAnimation = CryProvider { if (it.isPosedIn(battle)) bedrockStateful("gimmighoul_chest", "battle_cry") else bedrockStateful("gimmighoul_chest", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("gimmighoul_chest", "blink") }
        val quirk = quirk(secondsBetweenOccurrences = 30F to 120F) { PrimaryAnimation(bedrockStateful("gimmighoul_chest", "idle_quirk")) }

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            quirks = arrayOf(blink,quirk),
            condition = { (it.getEntity() as? PokemonEntity)?.ownerUUID != null && !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_chest", "ground_idle")
            )
        )

        closed = registerPose(
            poseName = "closed",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { (it.getEntity() as? PokemonEntity)?.ownerUUID == null && !it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_chest", "mimic")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_chest", "ground_walk")
                //bedrock("gimmighoul_chest", "ground_walk")
            )
        )

        battle = registerPose(
            poseName = "battle",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("gimmighoul_chest", "battle_idle"),
            )
        )

        closed.transitions[battle.poseName] = { _, _ ->
            bedrockStateful("gimmighoul_chest", "surprise")
        }
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("gimmighoul_chest", "faint") else null
}