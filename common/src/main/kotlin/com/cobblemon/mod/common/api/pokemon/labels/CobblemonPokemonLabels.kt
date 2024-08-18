/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.labels

/**
 * A collection of commonly used labels in the mod.
 *
 * @author Licious
 * @since August 8th, 2022
 */
@Deprecated("This is no longer used anywhere in the Cobblemon codebase", ReplaceWith("CobblemonSpeciesTags", "com.cobblemon.mod.common.api.tags.CobblemonSpeciesTags"))
object CobblemonPokemonLabels {

    /**
     * Represents a legendary Pokémon.
     */
    const val LEGENDARY = "legendary"

    /**
     * Represents a mythical Pokémon.
     * In Cobblemon terms they do not exist since we do not share the concept of timed event only Pokémon but the official ones are still tagged.
     */
    const val MYTHICAL = "mythical"

    /**
     * Represents Pokémon that originate from Ultra Space.
     */
    const val ULTRA_BEAST = "ultra_beast"

    /**
     * Represents the pseudo legendary Pokémon.
     */
    const val PSEUDO_LEGENDARY = "pseudo_legendary"

    /**
     * Represents a baby Pokémon, this is not just a first stage Pokémon species, it is also unable to breed.
     * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Baby_Pok%C3%A9mon) page.
     */
    const val BABY = "baby"

    /**
     * Represents a Pokémon that has multiple forms depending on the region they're from.
     * In Cobblemon/Minecraft terms there are no regions, but we follow the official concept.
     */
    const val REGIONAL = "regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_KANTO = "kanto_regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_JOHTO = "johto_regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_HOENN = "hoenn_regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_SINNOH = "sinnoh_regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_UNOVA = "unova_regional"

    /**
     * See [REGIONAL], this has no official regionals but it consists of the "base" form that comes from the region.
     */
    const val REGIONAL_OF_KALOS = "kalos_regional"

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Alolan_Form) page.
     */
    const val REGIONAL_OF_ALOLA = "alola_regional"

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Galarian_Form) page.
     */
    const val REGIONAL_OF_GALAR = "galar_regional"

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Hisuian_Form) page.
     */
    const val REGIONAL_OF_HISUI = "hisui_regional"

    /**
     * See [REGIONAL] and this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Regional_form#Paldean_Form) page.
     */
    const val REGIONAL_OF_PALDEA = "paldea_regional"

    /**
     * Represents a mega evolution.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Mega_Evolution) page.
     */
    const val MEGA = "mega"

    /**
     * Represents a primal reversion.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Primal_Reversion) page.
     */
    const val PRIMAL = "primal"

    /**
     * Represents a gmax form.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Gigantamax) page.
     */
    const val GMAX = "gmax"

    @Deprecated("This is no longer used nor has an equivalent", level = DeprecationLevel.ERROR)
    /**
     * Represents a totem form.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Totem_Pok%C3%A9mon) page.
     */
    const val TOTEM = "totem"

    /**
     * Represents a Paradox species.
     * For more information see the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Paradox_Pok%C3%A9mon) page.
     */
    const val PARADOX = "paradox"

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

    @Deprecated("This is no longer used nor has an equivalent", level = DeprecationLevel.ERROR)
    /**
     * Official Pokémon changed by a data pack, there is no guarantee authors will adhere to this principle
     */
    const val CUSTOMIZED_OFFICIAL = "customized_official"

    @Deprecated("This is no longer used anywhere in the Cobblemon codebase", ReplaceWith("CobblemonSpeciesTags.FAKEMON", "com.cobblemon.mod.common.api.tags.CobblemonSpeciesTags"))
    /**
     * Unofficial Pokémon created by a data pack, there is no guarantee authors will adhere to this principle
     */
    const val CUSTOM = "custom"
}
