/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.minecraft.network.RegistryFriendlyByteBuf
import java.lang.Integer.max
import java.lang.reflect.Type
import java.util.*

class ShowdownActionRequest(
    var wait: Boolean = false,
    var active: MutableList<ShowdownMoveset>? = null,
    var forceSwitch: List<Boolean> = emptyList(),
    var noCancel: Boolean = false,
    var side: ShowdownSide? = null
) {
    fun <T, E : Targetable> iterate(activePokemon: List<E>, iterator: (E, ShowdownMoveset?, forceSwitch: Boolean) -> T): List<T> {
        val size = max(active?.size ?: 0, forceSwitch.size)
        val responses = mutableListOf<T>()
        repeat(times = size) { index ->
            val activeBattlePokemon = activePokemon.let { if (it.size > index) it[index] else throw IllegalStateException("No active Pokémon for slot $index but needed to choose action for it?") }
            val moveset = active?.let { if (it.size > index) it[index] else null }
            val forceSwitch = forceSwitch.let { if (it.size > index) it[index] else false }
            responses.add(iterator(activeBattlePokemon, moveset, forceSwitch))
        }
        return responses
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(wait)
        buffer.writeSizedInt(IntSize.U_BYTE, active?.size ?: 0)
        active?.forEach { it.saveToBuffer(buffer) }
        buffer.writeSizedInt(IntSize.U_BYTE, forceSwitch.size)
        forceSwitch.forEach(buffer::writeBoolean)
        buffer.writeBoolean(noCancel)
        buffer.writeBoolean(side != null)
        side?.saveToBuffer(buffer)
    }

    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionRequest {
        wait = buffer.readBoolean()
        val activeSize = buffer.readSizedInt(IntSize.U_BYTE)
        if (activeSize > 0) {
            val active = mutableListOf<ShowdownMoveset>()
            repeat(times = activeSize) { active.add(ShowdownMoveset().loadFromBuffer(buffer)) }
            this.active = active
        }
        val forceSwitch = mutableListOf<Boolean>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) { forceSwitch.add(buffer.readBoolean()) }
        this.forceSwitch = forceSwitch
        noCancel = buffer.readBoolean()
        if (buffer.readBoolean()) {
            side = ShowdownSide().loadFromBuffer(buffer)
        }
        return this
    }

    fun sanitize(battle: PokemonBattle, battleActor: BattleActor) {
        val player = battle.players.find { it.uuid == battleActor.uuid } ?: return
        this.active?.forEach { moveset ->
            moveset.getGimmicks().forEach { gimmick ->
                // TODO: use the Identifiers of actual items
                val triggerItem = when(gimmick) {
                    ShowdownMoveset.Gimmick.MEGA_EVOLUTION -> cobblemonResource("key_stone")
                    ShowdownMoveset.Gimmick.DYNAMAX -> cobblemonResource("dynamax_band")
                    ShowdownMoveset.Gimmick.TERASTALLIZATION -> cobblemonResource("tera_orb")
                    else -> cobblemonResource("z_ring")
                }
                if (!player.hasKeyItem(triggerItem)) moveset.blockGimmick(gimmick)
            }
        }
    }
}

enum class ShowdownActionResponseType(val loader: (RegistryFriendlyByteBuf) -> ShowdownActionResponse) {
    SWITCH({ SwitchActionResponse(UUID.randomUUID()) }),
    MOVE({ MoveActionResponse("", null) }),
    DEFAULT({ DefaultActionResponse() }),
    FORCE_PASS({ ForcePassActionResponse() }),
    PASS({ PassActionResponse }),
    HEAL_ITEM({ HealItemActionResponse("potion") }),
    FORFEIT({ ForfeitActionResponse() });
}

abstract class ShowdownActionResponse(val type: ShowdownActionResponseType) {
    companion object {
        fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionResponse {
            val type = ShowdownActionResponseType.values()[buffer.readSizedInt(IntSize.U_BYTE)]
            return type.loader(buffer).loadFromBuffer(buffer)
        }
    }

    open fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, type.ordinal)
    }

    open fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionResponse = this
    abstract fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean
    abstract fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String
}

data class MoveActionResponse(var moveName: String, var targetPnx: String? = null, var gimmickID: String? = null): ShowdownActionResponse(ShowdownActionResponseType.MOVE) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        if (forceSwitch || showdownMoveSet == null) {
            return false
        }

        val move = showdownMoveSet.moves.find { it.id == moveName } ?: return false
        val gimmickMove = move.gimmickMove
        val validGimmickMove = gimmickMove != null && !gimmickMove.disabled
        if (!validGimmickMove && !move.canBeUsed()) {
            // No PP or disabled or something
            return false
        }
        val availableTargets = (gimmickMove?.target ?: move.target).targetList(activeBattlePokemon)?.takeIf { it.isNotEmpty() } ?: return true

        val pnx = targetPnx ?: return false // If the targets list is non-null then they need to have specified a target
        val (_, targetPokemon) = activeBattlePokemon.actor.battle.getActorAndActiveSlotFromPNX(pnx)
        if (targetPokemon !in availableTargets || targetPokemon.battlePokemon == null || targetPokemon.battlePokemon!!.health <= 0) {
            return false // It's not a possible target.
        }

        return true
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        val pnx = targetPnx
        showdownMoveSet!!
        val moveIndex = showdownMoveSet.moves.indexOfFirst { it.id == moveName } + 1

        return if (pnx != null) {
            val (_, targetPokemon) = activeBattlePokemon.actor.battle.getActorAndActiveSlotFromPNX(pnx)
            val digit = targetPokemon.getSignedDigitRelativeTo(activeBattlePokemon)
            "move $moveIndex $digit"
        } else {
            "move $moveIndex"
        }.plus(gimmickID?.let { " $gimmickID" } ?: "")
    }

    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        super.saveToBuffer(buffer)
        buffer.writeString(moveName)
        buffer.writeNullable(targetPnx) { _, targetPnx -> buffer.writeString(targetPnx) }
        buffer.writeNullable(gimmickID) { _, gimmickID -> buffer.writeString(gimmickID) }
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionResponse {
        super.loadFromBuffer(buffer)
        moveName = buffer.readString()
        targetPnx = buffer.readNullable { buffer.readString() }
        gimmickID = buffer.readNullable { buffer.readString() }
        return this
    }
}

data class HealItemActionResponse(var item: String) : ShowdownActionResponse(ShowdownActionResponseType.FORCE_PASS) {
    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        super.saveToBuffer(buffer)
        buffer.writeString(item)
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionResponse {
        super.loadFromBuffer(buffer)
        item = buffer.readString()
        return this
    }

    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        return !forceSwitch
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        return "healitem ${activeBattlePokemon.getPNX()} $item"
    }
}

data class SwitchActionResponse(var newPokemonId: UUID) : ShowdownActionResponse(ShowdownActionResponseType.SWITCH) {
    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        super.saveToBuffer(buffer)
        buffer.writeUUID(newPokemonId)
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownActionResponse {
        super.loadFromBuffer(buffer)
        newPokemonId = buffer.readUUID()
        return this
    }

    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        val pokemon = activeBattlePokemon.actor.pokemonList.find { it.uuid == newPokemonId }
        return when {
            pokemon == null -> false // No such Pokémon
            (!activeBattlePokemon.actor.request?.side?.pokemon?.get(0)?.reviving!! && pokemon.health <= 0) -> false // Checks if the active Pokémon is reviving, if so ignore this check. If not, return false if dead
            showdownMoveSet != null && showdownMoveSet.trapped -> false // You're not allowed to switch
            activeBattlePokemon.actor.getSide().activePokemon.any { it.battlePokemon?.uuid == newPokemonId } -> false // Pokémon is already sent out
            else -> true
        }
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        return "switch ${activeBattlePokemon.actor.pokemonList.indexOfFirst { it.uuid == newPokemonId } + 1}"
    }
}

class DefaultActionResponse: ShowdownActionResponse(ShowdownActionResponseType.DEFAULT) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean) = true
    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "default"
}

object PassActionResponse : ShowdownActionResponse(ShowdownActionResponseType.PASS) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean) = true
    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "pass"
}

/**
 * Only meant to be used when the player is capturing or using an item - they are forced to pass
 */
class ForcePassActionResponse : ShowdownActionResponse(ShowdownActionResponseType.FORCE_PASS) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean): Boolean {
        if (forceSwitch) {
            return false
        } else if (showdownMoveSet == null) {
            return false
        }

        return activeBattlePokemon.actor.expectingPassActions.size > 0
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "pass"
}

class BagItemActionResponse(val bagItem: BagItem, val target: BattlePokemon, val data: String? = null): ShowdownActionResponse(ShowdownActionResponseType.FORCE_PASS) {
    override fun isValid(
        activeBattlePokemon: ActiveBattlePokemon,
        showdownMoveSet: ShowdownMoveset?,
        forceSwitch: Boolean
    ): Boolean {
        if (forceSwitch) {
            return false
        } else if (showdownMoveSet == null) {
            return false
        }

        return activeBattlePokemon.actor.expectingPassActions.size > 0
    }

    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?): String {
        return "useitem ${target.uuid} ${bagItem.itemName} ${bagItem.getShowdownInput(target.actor, target, data)}"
    }
}

class ForfeitActionResponse : ShowdownActionResponse(ShowdownActionResponseType.FORFEIT) {
    override fun isValid(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?, forceSwitch: Boolean) = true
    override fun toShowdownString(activeBattlePokemon: ActiveBattlePokemon, showdownMoveSet: ShowdownMoveset?) = "forfeit"
}

class ShowdownMoveset {
    lateinit var moves: List<InBattleMove>
    var trapped = false
    var canMegaEvo = false
    var canUltraBurst = false
    var canZMove: List<InBattleGimmickMove?>? = null
    var canDynamax = false
    var maxMoves: List<InBattleGimmickMove?>? = null
    var canTerastallize: String? = null

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, moves.size)
        moves.forEach { it.saveToBuffer(buffer) }
        buffer.writeBoolean(trapped)
        buffer.writeBoolean(canMegaEvo)
        buffer.writeBoolean(canUltraBurst)
        buffer.writeNullable(canZMove) { _, canZMove ->
            canZMove.forEach {
                buffer.writeNullable(it) { _, zmove -> zmove.saveToBuffer(buffer)}
            }
        }
        buffer.writeBoolean(canDynamax)
        buffer.writeNullable(maxMoves) { _, maxMoves ->
            maxMoves.forEach {
                buffer.writeNullable(it) { _, maxMove -> maxMove.saveToBuffer(buffer)}
            }
        }
        buffer.writeNullable(canTerastallize) { _, teraType -> buffer.writeString(teraType) }
    }

    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownMoveset {
        val moves = mutableListOf<InBattleMove>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            moves.add(InBattleMove.loadFromBuffer(buffer))
        }
        this.moves = moves
        this.trapped = buffer.readBoolean()
        this.canMegaEvo = buffer.readBoolean()
        this.canUltraBurst = buffer.readBoolean()
        this.canZMove = buffer.readNullable {
            val zMoves = mutableListOf<InBattleGimmickMove?>()
            repeat(moves.size) { zMoves.add(buffer.readNullable { InBattleGimmickMove.loadFromBuffer(buffer) }) }
            return@readNullable zMoves
        }
        this.canDynamax = buffer.readBoolean()
        this.maxMoves = buffer.readNullable {
            val maxMoves = mutableListOf<InBattleGimmickMove?>()
            repeat(moves.size) { maxMoves.add(buffer.readNullable { InBattleGimmickMove.loadFromBuffer(buffer) }) }
            return@readNullable maxMoves
        }
        this.canTerastallize = buffer.readNullable{ buffer.readString() }
        this.setGimmickMapping()
        return this
    }

    /** Showdown IDs of battle gimmicks. */
    enum class Gimmick(val id: String) {
        MEGA_EVOLUTION("mega"),
        ULTRA_BURST("ultra"),
        Z_POWER("zmove"),
        DYNAMAX("max"),
        TERASTALLIZATION("terastal")
    }

    /** Check whether Dynamax/Gigantamax is already active. */
    fun hasActiveGimmick() = !this.canDynamax && this.maxMoves != null

    /** [Gimmick]s that can be activated with a [MoveActionResponse]. */
    fun getGimmicks() = if (!hasActiveGimmick()) buildList{
        if (canMegaEvo) add(Gimmick.MEGA_EVOLUTION)
        if (canUltraBurst) add(Gimmick.ULTRA_BURST)
        if (canZMove != null) add(Gimmick.Z_POWER)
        if (canDynamax) add(Gimmick.DYNAMAX)
        if (canTerastallize != null) add(Gimmick.TERASTALLIZATION)
    }.toList() else listOf() // keep this immutable

    /** Map each [InBattleMove] to their respective [InBattleGimmickMove] variant. */
    fun setGimmickMapping() = (canZMove ?: maxMoves)?.let { gimmickMoves ->
        moves.forEachIndexed { index, move -> move.gimmickMove = gimmickMoves[index] }
    }

    /** Sanitize Moveset by disabling gimmick. */
    fun blockGimmick(gimmick: Gimmick) {
        when (gimmick) {
            Gimmick.MEGA_EVOLUTION -> this.canMegaEvo = false
            Gimmick.DYNAMAX -> this.canDynamax = false
            Gimmick.ULTRA_BURST -> this.canUltraBurst = false
            Gimmick.Z_POWER -> this.canZMove = null
            else -> this.canTerastallize = null
        }
    }
}
class ShowdownSide {
    lateinit var name: UUID
    lateinit var id: String
    lateinit var pokemon: List<ShowdownPokemon>
    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(name)
        buffer.writeString(id)
        buffer.writeSizedInt(IntSize.U_BYTE, pokemon.size)
        pokemon.forEach { it.saveToBuffer(buffer) }
    }
    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownSide {
        name = buffer.readUUID()
        id = buffer.readString()
        val pokemon = mutableListOf<ShowdownPokemon>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            pokemon.add(ShowdownPokemon().loadFromBuffer(buffer))
        }
        this.pokemon = pokemon
        return this
    }
}
class ShowdownPokemon {
    lateinit var ident: String
    lateinit var details: String
    lateinit var condition: String
    var active: Boolean = false
    val moves = mutableListOf<String>()
    lateinit var baseAbility: String
    lateinit var pokeball: String
    lateinit var ability: String
    var reviving: Boolean = false

    val uuid: UUID by lazy { UUID.fromString(details.split(",")[1].trim()) }
    fun saveToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(ident)
        buffer.writeString(details)
        buffer.writeString(condition)
        buffer.writeBoolean(active)
        buffer.writeBoolean(reviving)
        buffer.writeSizedInt(IntSize.U_BYTE, moves.size)
        moves.forEach(buffer::writeString)
        buffer.writeString(baseAbility)
        buffer.writeString(pokeball)
        buffer.writeString(ability)

    }
    fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): ShowdownPokemon {
        ident = buffer.readString()
        details = buffer.readString()
        condition = buffer.readString()
        active = buffer.readBoolean()
        reviving = buffer.readBoolean()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            moves.add(buffer.readString())
        }
        baseAbility = buffer.readString()
        pokeball = buffer.readString()
        ability = buffer.readString()
        return this
    }
}
/** Unwraps useless maxMoves object and initializes [ShowdownMoveset.gimmickMapping] */
object ShowdownMovesetAdapter : JsonDeserializer<ShowdownMoveset> {

    val gson = GsonBuilder().addDeserializationExclusionStrategy(MovesetExclusionStrategy).create()

    object MovesetExclusionStrategy : ExclusionStrategy {
        override fun shouldSkipField(field: FieldAttributes?): Boolean {
            return field?.name == "maxMoves"
        }
        override fun shouldSkipClass(p0: Class<*>?): Boolean {
            return false;
        }
    }

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): ShowdownMoveset {
        val json = jsonElement.asJsonObject
        val moveset = gson.fromJson(json, ShowdownMoveset::class.java)

        json.get("maxMoves")?.asJsonObject?.let { dynamaxOptions ->
            val maxMovesToken = object: TypeToken<List<InBattleGimmickMove?>?>() {}.type
            moveset.maxMoves = Gson().fromJson(dynamaxOptions.get("maxMoves"), maxMovesToken)
        }
        moveset.setGimmickMapping()
        return moveset
    }
}