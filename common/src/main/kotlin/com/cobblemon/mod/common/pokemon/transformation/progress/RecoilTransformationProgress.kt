/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.transformation.progress

import com.cobblemon.mod.common.api.pokemon.transformation.progress.TransformationProgress
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.transformation.form.PermanentForm
import com.cobblemon.mod.common.pokemon.transformation.requirements.RecoilRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * A [TransformationProgress] meant to keep track of recoil taken in battle without fainting.
 *
 * @author Licious
 * @since January 27th, 2022
 */
class RecoilTransformationProgress : TransformationProgress<RecoilTransformationProgress.Progress> {

    private var progress = Progress(0)

    override fun id(): Identifier = ID

    override fun currentProgress(): Progress = this.progress

    override fun updateProgress(progress: Progress) {
        this.progress = progress
    }

    override fun reset() {
        this.updateProgress(Progress(0))
    }

    override fun shouldKeep(pokemon: Pokemon): Boolean = supports(pokemon)

    override fun loadFromNBT(nbt: NbtCompound) {
        val recoil = nbt.getInt(RECOIL)
        this.updateProgress(Progress(recoil))
    }

    override fun saveToNBT(): NbtCompound = NbtCompound().apply { putInt(RECOIL, currentProgress().recoil) }

    override fun loadFromJson(json: JsonObject) {
        val recoil = json.get(RECOIL).asInt
        this.updateProgress(Progress(recoil))
    }

    override fun saveToJson(): JsonObject = JsonObject().apply { addProperty(RECOIL, currentProgress().recoil) }

    data class Progress(val recoil: Int)

    companion object {

        val ID = cobblemonResource(RecoilRequirement.ADAPTER_VARIANT)
        private const val RECOIL = "recoil"

        fun supports(pokemon: Pokemon): Boolean {
            val form = pokemon.form
            if (form !is PermanentForm) return false
            return form.transformations.any { transformation ->
                transformation.requirements.any { requirement ->
                    requirement is RecoilRequirement
                }
            }
        }

    }

}