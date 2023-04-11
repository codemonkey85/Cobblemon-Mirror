package com.cobblemon.mod.common.api.battles.interpreter

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * The context of how a condition was created or changed during a battle.
 *
 * @author Segfault Guy
 * @since April 10th, 2023
 */
interface BattleContext {

    /** The effect or condition this [BattleContext] represents. */
    val id: String

    /** The turn this [BattleContext] was created. */
    val turn: Int

    /** The [BattleContext.Type] tied to this [BattleContext]. */
    val type: Type

    /** The [BattlePokemon] that directly or indirectly caused this [BattleContext]. */
    val origin: BattlePokemon?

    /**
     * The type of [BattleContext].
     *
     * @property damaging Whether a context of this type is a source of damage.
     * @property exclusive Whether only one context of this type can exist.
     *
     * @author Segfault Guy
     * @since April 10th, 2023
     */
    enum class Type(val damaging: Boolean, val exclusive: Boolean) {
        ITEM(true, true),
        STATUS(true, false),
        VOLATILE(true, false),
        HAZARD(true, false),
        WEATHER(true, true),
        ROOM(false, true),
        SPORT(false, false),
        TERRAIN(false, true),
        GRAVITY(false, true),
        TAILWIND(false, true),
        SCREEN(false, false),
        FAINT(false, true),
        MISC(false, false);
    }
}

data class BasicContext(
        override val id: String,
        override val turn: Int,
        override val type: BattleContext.Type,
        override val origin: BattlePokemon?
) : BattleContext

data class MissingContext(
        override val id: String = "error",
        override val turn: Int = 0,
        override val type: BattleContext.Type = BattleContext.Type.MISC,
        override val origin: BattlePokemon? = null
) : BattleContext
