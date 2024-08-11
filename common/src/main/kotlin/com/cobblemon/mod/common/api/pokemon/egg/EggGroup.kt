/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.egg

import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

/**
 * These represent categories which determine which Pokémon are able to interbreed.
 *
 * See the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Egg_Group) article for more information.
 *
 * @property showdownID used for data synchronization with Showdown data format.
 */
enum class EggGroup(private val showdownID: String) : ShowdownIdentifiable, StringRepresentable {

    MONSTER("Monster"),
    WATER_1("Water 1"),
    BUG("Bug"),
    FLYING("Flying"),
    FIELD("Field"),
    FAIRY("Fairy"),
    GRASS("Grass"),
    HUMAN_LIKE("Human-Like"),
    WATER_3("Water 3"),
    MINERAL("Mineral"),
    AMORPHOUS("Amorphous"),
    WATER_2("Water 2"),
    DITTO("Ditto"),
    DRAGON("Dragon"),
    UNDISCOVERED("Undiscovered");

    override fun showdownId(): String = this.showdownID

    override fun getSerializedName(): String = this.name

    companion object {
        @JvmStatic
        val CODEC: Codec<EggGroup> = StringRepresentable.fromEnum(EggGroup::values)
    }
}