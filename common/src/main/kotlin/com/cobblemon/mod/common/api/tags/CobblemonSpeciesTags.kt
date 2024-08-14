/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.tags.TagKey

/**
 * The default Cobblemon [TagKey]s for [Species].
 */
object CobblemonSpeciesTags {

    /**
     * Represents a legendary Pokémon.
     */
    @JvmStatic
    val LEGENDARY = this.create("legendary")

    /**
     * Represents a mythical Pokémon.
     * In Cobblemon terms they do not exist since we do not share the concept of timed event only Pokémon but the official ones are still tagged.
     */
    @JvmStatic
    val MYTHICAL = this.create("mythical")

    /**
     * Represents Pokémon that originate from Ultra Space.
     */
    @JvmStatic
    val ULTRA_BEAST = this.create("ultra_beast")

    /**
     * Represents the pseudo legendary Pokémon.
     */
    @JvmStatic
    val PSEUDO_LEGENDARY = this.create("pseudo_legendary")

    /**
     * Represents a baby Pokémon, this is not just a first stage Pokémon species, it is also unable to breed.
     * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Baby_Pok%C3%A9mon) page.
     */
    @JvmStatic
    val BABY = this.create("baby")

    /**
     * Represents a Pokémon that has multiple forms depending on the region they're from.
     * In Cobblemon/Minecraft terms there are no regions, but we follow the official concept.
     */
    @JvmStatic
    val REGIONAL = this.create("regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_KANTO = this.create("kanto_regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_JOHTO = this.create("johto_regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_HOENN = this.create("hoenn_regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_SINNOH = this.create("sinnoh_regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_UNOVA = this.create("unova_regional")

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    @JvmStatic
    val REGIONAL_OF_KALOS = this.create("kalos_regional")

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Alolan_Form) page.
     */
    @JvmStatic
    val REGIONAL_OF_ALOLA = this.create("alola_regional")

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Galarian_Form) page.
     */
    @JvmStatic
    val REGIONAL_OF_GALAR = this.create("galar_regional")

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Hisuian_Form) page.
     */
    @JvmStatic
    val REGIONAL_OF_HISUI = this.create("hisui_regional")

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Paldean_Form) page.
     */
    @JvmStatic
    val REGIONAL_OF_PALDEA = this.create("paldea_regional")

    /**
     * Represents a mega evolution.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Mega_Evolution) page.
     */
    @JvmStatic
    val MEGA = this.create("mega")

    /**
     * Represents a primal reversion.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Primal_Reversion) page.
     */
    @JvmStatic
    val PRIMAL = this.create("primal")

    /**
     * Represents a gmax form.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Gigantamax) page.
     */
    @JvmStatic
    val GMAX = this.create("gmax")

    /**
     * Represents a totem form.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Totem_Pok%C3%A9mon) page.
     */
    @JvmStatic
    val TOTEM = this.create("totem")

    /**
     * Represents a Paradox species.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Paradox_Pok%C3%A9mon) page.
     */
    @JvmStatic
    val PARADOX = this.create("paradox")

    /**
     * Pokémon from the national Pokédex of 1 to 151
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_1 = "gen1"

    /**
     * Pokémon from the national Pokédex of 152 to 251
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_2 = "gen2"

    /**
     * Pokémon from the national Pokédex of 252 to 386
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_3 = "gen3"

    /**
     * Pokémon from the national Pokédex of 387 to 493
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_4 = "gen4"

    /**
     * Pokémon from the national Pokédex of 494 to 649
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_5 = "gen5"

    /**
     * Pokémon from the national Pokédex of 650 to 721
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_6 = "gen6"

    /**
     * Pokémon from the national Pokédex of 722 to 809
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_7 = "gen7"

    /**
     * Pokémon from the national Pokédex of 810 to 905
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_8 = "gen8"

    /**
     * Pokémon from the national Pokédex from 906 to 1008
     * This may also include forms of Pokémon that had been previously introduced
     */
    const val GENERATION_9 = "gen9"

    /**
     * Unofficial Pokémon created by a data pack, there is no guarantee authors will adhere to this principle
     */
    @JvmStatic
    val FAKEMON = this.create("fakemon")

    private fun create(path: String) = TagKey.create(CobblemonRegistries.SPECIES_KEY, cobblemonResource(path))

}