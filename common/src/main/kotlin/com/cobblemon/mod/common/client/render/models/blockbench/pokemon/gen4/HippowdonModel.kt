/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isStandingOnRedSand
import com.cobblemon.mod.common.util.isStandingOnSand
import com.cobblemon.mod.common.util.isStandingOnSandOrRedSand
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class HippowdonModel (root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("hippowdon")
    override val head = getPart("head")

    private val sand = getPart("sand")
    private val redsand = getPart("redsand")

    override var portraitScale = 0.6F
    override var portraitTranslation = Vec3(-0.63, 0.73, 0.0)

    override var profileScale = 0.4F
    override var profileTranslation = Vec3(-0.1, 1.0, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var standingsand: CobblemonPose
    lateinit var walksand: CobblemonPose
    lateinit var battleidlesand: CobblemonPose
    lateinit var battleidleredsand: CobblemonPose
    lateinit var sleep: CobblemonPose
    lateinit var sleepsand: CobblemonPose
    lateinit var sleepredsand: CobblemonPose
    lateinit var standingredsand: CobblemonPose
    lateinit var walkredsand: CobblemonPose

    override val cryAnimation = CryProvider {
        when {
            it.isPosedIn(standingsand, walksand) -> bedrockStateful("hippowdon", "sand_cry")
            it.isPosedIn(battleidle) -> bedrockStateful("hippowdon", "battle_cry")
            it.isPosedIn(battleidlesand, battleidleredsand) -> bedrockStateful("hippowdon", "sand_battle_cry")
            else -> bedrockStateful("hippowdon", "cry")
        }
    }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("hippowdon", "blink") }
        val idlequirk = quirk { bedrockStateful("hippowdon", "quirk_idle") }

        sleep = registerPose(
            poseName = "sleep",
            poseType = PoseType.SLEEP,
            condition = { it.getEntity()?.isStandingOnSand() != true },
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sleep")
            )
        )

        sleepredsand = registerPose(
            poseName = "sleepsand",
            poseType = PoseType.SLEEP,
            condition = { it.getEntity()?.isStandingOnRedSand() == true },
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sand_sleep")
            )
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.UI_POSES + PoseType.STATIONARY_POSES,
            condition = { !it.isBattling && it.getEntity()?.isStandingOnSandOrRedSand() != true },
            quirks = arrayOf(blink, idlequirk),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            condition = { it.getEntity()?.isStandingOnSandOrRedSand() != true },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "ground_walk")
            )
        )

        standingsand = registerPose(
            poseName = "standingsand",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, idlequirk),
            condition = { !it.isBattling && it.getEntity()?.isStandingOnSand() == true },
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sand_idle")
            )
        )

        standingredsand = registerPose(
            poseName = "standingredsand",
            poseTypes = PoseType.STATIONARY_POSES,
            quirks = arrayOf(blink, idlequirk),
            condition = { !it.isBattling && it.getEntity()?.isStandingOnRedSand() == true },
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sand_idle")
            )
        )

        walksand = registerPose(
            poseName = "walksand",
            poseTypes = PoseType.MOVING_POSES,
            condition = { it.getEntity()?.isStandingOnSand() == true },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                bedrock("hippowdon", "sand_swim")
            )
        )

        walkredsand = registerPose(
            poseName = "walkredsand",
            poseTypes = PoseType.MOVING_POSES,
            condition = { it.getEntity()?.isStandingOnRedSand() == true },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                bedrock("hippowdon", "sand_swim")
            )
        )

        battleidle = registerPose(
            poseName = "battleidle",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.isBattling && it.getEntity()?.isStandingOnSandOrRedSand() == true },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "battle_idle")
            )
        )

        battleidlesand = registerPose(
            poseName = "battleidlesand",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.getEntity()?.isStandingOnSand() == true && it.isBattling },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = true),
                redsand.createTransformation().withVisibility(visibility = false)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sand_battle_idle")
            )
        )

        battleidleredsand = registerPose(
            poseName = "battleidleredsand",
            poseTypes = PoseType.STATIONARY_POSES,
            condition = { it.getEntity()?.isStandingOnRedSand() == true && it.isBattling },
            quirks = arrayOf(blink),
            transformedParts = arrayOf(
                sand.createTransformation().withVisibility(visibility = false),
                redsand.createTransformation().withVisibility(visibility = true)
            ),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("hippowdon", "sand_battle_idle")
            )
        )
    }
//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("hippowdon", "faint") else null
}