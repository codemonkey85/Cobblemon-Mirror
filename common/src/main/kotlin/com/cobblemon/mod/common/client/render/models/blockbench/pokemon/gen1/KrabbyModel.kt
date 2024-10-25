/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isDusk
import com.cobblemon.mod.common.util.isInWaterOrRain
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class KrabbyModel(root: ModelPart) : PokemonPosableModel(root) {
    override val rootPart = root.registerChildWithAllChildren("krabby")

    override var portraitScale = 2.5F
    override var portraitTranslation = Vec3(-0.15, -1.8, 0.0)

    override var profileScale = 1.0F
    override var profileTranslation = Vec3(0.0, 0.2, 0.0)

    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var standingBubbles: CobblemonPose
    lateinit var standingRain: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("krabby", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("krabby", "blink")}
        val snipLeft = quirk { bedrockStateful("krabby", "snip_left")}
        val snipRight = quirk { bedrockStateful("krabby", "snip_right")}
        val bubble = quirk(5F to 24F) { bedrockStateful("krabby", "quirk_bubble")}

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES,
            condition = { !it.isBattling && it.getEntity()?.isDusk() != true },
            quirks = arrayOf(blink, snipLeft, snipRight ),
            animations = arrayOf(
                bedrock("krabby", "ground_idle")
            )
        )

        standingBubbles = registerPose(
            poseName = "standing_bubbles",
            poseTypes = STATIONARY_POSES,
            condition = { !it.isBattling && it.getEntity()?.isDusk() == true && !it.isInWaterOrRain },
            quirks = arrayOf(blink, snipLeft, snipRight, bubble),
            animations = arrayOf(
                bedrock("krabby", "ground_idle")
            )
        )

        standingRain = registerPose(
            poseName = "standing_rain",
            poseTypes = STATIONARY_POSES,
            condition = { !it.isBattling && it.getEntity()?.isDusk() == true && it.isInWaterOrRain},
            quirks = arrayOf(blink, snipLeft, snipRight),
            animations = arrayOf(
                bedrock("krabby", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES,
            quirks = arrayOf(blink, snipLeft, snipRight),
            animations = arrayOf(
                bedrock("krabby", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, snipLeft, snipRight),
            condition = { it.isBattling },
            animations = arrayOf(
                bedrock("krabby", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PosableState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("krabby", "faint") else null
}