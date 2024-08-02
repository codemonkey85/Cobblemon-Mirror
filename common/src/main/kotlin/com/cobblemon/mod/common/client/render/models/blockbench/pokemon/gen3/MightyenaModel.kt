/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3

import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.CryProvider
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.isBattling
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class MightyenaModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("mightyena")
    override val head = getPart("head")

    override var portraitScale = 2.2F
    override var portraitTranslation = Vec3(-0.8, 0.6, 0.0)

    override var profileScale = 0.6F
    override var profileTranslation = Vec3(0.0, 0.8, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose

    override val cryAnimation = CryProvider { bedrockStateful("mightyena", "cry") }

    override fun registerPoses() {
        val blink = quirk { bedrockStateful("mightyena", "blink") }
        val laugh_out_battle = quirk(secondsBetweenOccurrences = 120F to 240F) { bedrockStateful("mightyena", "quirk") }
        val laugh_in_battle = quirk(secondsBetweenOccurrences = 30F to 60F) { bedrockStateful("mightyena", "quirk") }
        val sleep_quirk = quirk(secondsBetweenOccurrences = 30F to 60F) { bedrockStateful("mightyena", "sleep_quirk") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(sleep_quirk),
            animations = arrayOf(bedrock("mightyena", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = PoseType.STATIONARY_POSES + PoseType.UI_POSES,
            transformTicks = 10,
            condition = { !it.isBattling },
            quirks = arrayOf(blink,laugh_out_battle),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mightyena", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = PoseType.MOVING_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mightyena", "ground_walk")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink,laugh_in_battle),
            condition = { it.isBattling },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("mightyena", "battle_idle")
            )
        )
    }
    //override fun getFaintAnimation(
    //    pokemonEntity: PokemonEntity,
    //    state: PosableState<PokemonEntity>
    //) = bedrockStateful("mightyena", "faint")
}