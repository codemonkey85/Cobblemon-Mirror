/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class TyphlosionHisuianModel  (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("typhlosion_hisui")
    override val head = getPart("head")

    override var portraitScale = 1.4F
    override var portraitTranslation = Vec3(-0.45, 2.37, 0.0)

    override var profileScale = 0.5F
    override var profileTranslation = Vec3(0.0, 1.0, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walking: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var battleidle: CobblemonPose

    val spoopy_flame = getPart("fire_main")
    val spoopy_orb1 = getPart("fire_orb_right1")
    val spoopy_orb2 = getPart("fire_orb_middle1")
    val spoopy_orb3 = getPart("fire_orb_left1")

    override val cryAnimation = CryProvider { if (it.isBattling) bedrockStateful("typhlosion_hisuian", "battle_cry") else bedrockStateful("typhlosion_hisuian", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("typhlosion_hisuian", "blink") }

        sleep = registerPose(
                poseType = PoseType.SLEEP,
                transformedParts = arrayOf(
                        spoopy_flame.createTransformation().withVisibility(visibility = false),
                        spoopy_orb1.createTransformation().withVisibility(visibility = false),
                        spoopy_orb2.createTransformation().withVisibility(visibility = false),
                        spoopy_orb3.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(bedrock("typhlosion_hisuian", "sleep"))
        )

        standing = registerPose(
                poseName = "standing",
                poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
                transformTicks = 10,
                condition = { !it.isBattling },
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        spoopy_flame.createTransformation().withVisibility(visibility = false),
                        spoopy_orb1.createTransformation().withVisibility(visibility = false),
                        spoopy_orb2.createTransformation().withVisibility(visibility = false),
                        spoopy_orb3.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(
                        singleBoneLook(minPitch = 0F),
                        bedrock("typhlosion_hisuian", "ground_idle")
                )
        )

        walking = registerPose(
                poseName = "walking",
                poseTypes = PoseType.MOVING_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        spoopy_flame.createTransformation().withVisibility(visibility = false),
                        spoopy_orb1.createTransformation().withVisibility(visibility = false),
                        spoopy_orb2.createTransformation().withVisibility(visibility = false),
                        spoopy_orb3.createTransformation().withVisibility(visibility = false)
                ),
                animations = arrayOf(
                        singleBoneLook(minPitch = 0F),
                        bedrock("typhlosion_hisuian", "ground_walk")
                )
        )

        battleidle = registerPose(
                poseName = "battle_idle",
                poseTypes = PoseType.STATIONARY_POSES,
                transformTicks = 10,
                quirks = arrayOf(blink),
                transformedParts = arrayOf(
                        spoopy_flame.createTransformation().withVisibility(visibility = true),
                        spoopy_orb1.createTransformation().withVisibility(visibility = true),
                        spoopy_orb2.createTransformation().withVisibility(visibility = true),
                        spoopy_orb3.createTransformation().withVisibility(visibility = true)
                ),
                condition = { it.isBattling },
                animations = arrayOf(
                        singleBoneLook(minPitch = 0F),
                        bedrock("typhlosion_hisuian", "battle_idle")
                )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walking, battleidle, sleep)) bedrockStateful("typhlosion_hisuian", "faint") else null
}