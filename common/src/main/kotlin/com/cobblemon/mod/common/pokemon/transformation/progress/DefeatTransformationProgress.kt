/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.transformation.progress

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.transformation.progress.TransformationProgress
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.transformation.form.PermanentForm
import com.cobblemon.mod.common.pokemon.transformation.requirements.DefeatRequirement
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * A [TransformationProgress] meant to keep track of the amount of times a specific Pokémon was defeated in battle.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class DefeatTransformationProgress : TransformationProgress<DefeatTransformationProgress.Progress> {

    private var progress = Progress(PokemonProperties(), 0)

    override fun id(): Identifier = ID

    override fun currentProgress(): Progress = this.progress

    override fun updateProgress(progress: Progress) {
        this.progress = progress
    }

    override fun reset() {
        this.progress = Progress(PokemonProperties(), 0)
    }

    override fun shouldKeep(pokemon: Pokemon): Boolean {
        val form = pokemon.form
        if (form !is PermanentForm) return false
        return form.transformations.any { transformation ->
            transformation.requirements.any { requirement ->
                requirement is DefeatRequirement && requirement.target.originalString.equals(this.progress.target.originalString, true)
            }
        }
    }

    override fun loadFromNBT(nbt: NbtCompound) {
        val target = PokemonProperties.parse(nbt.getString(TARGET))
        val amount = nbt.getInt(AMOUNT)
        this.updateProgress(Progress(target, amount))
    }

    override fun saveToNBT(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString(TARGET, this.currentProgress().target.originalString)
        nbt.putInt(AMOUNT, this.currentProgress().amount)
        return nbt
    }

    override fun loadFromJson(json: JsonObject) {
        val target = PokemonProperties.parse(json.get(TARGET).asString)
        val amount = json.get(AMOUNT).asInt
        this.updateProgress(Progress(target, amount))
    }

    override fun saveToJson(): JsonObject {
        val jObject = JsonObject()
        jObject.addProperty(TARGET, this.currentProgress().target.originalString)
        jObject.addProperty(AMOUNT, this.currentProgress().amount)
        return jObject
    }

    data class Progress(
        val target: PokemonProperties,
        val amount: Int
    )

    companion object {
        val ID = cobblemonResource("defeat")
        private const val TARGET = "target"
        private const val AMOUNT = "amount"
    }

}