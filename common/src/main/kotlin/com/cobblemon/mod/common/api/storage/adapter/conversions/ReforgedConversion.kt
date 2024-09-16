/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.conversions

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import java.nio.file.Path
import java.util.UUID
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceLocation

const val REFORGED_POKEMON_PER_BOX = 30
class ReforgedConversion(val base: Path) : CobblemonConverter<CompoundTag> {

    override fun root(): Path {
        return this.base.resolve("data").resolve("pokemon")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : StorePosition, T : PokemonStore<E>> load(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess): T? {
        val extension = if (storeClass.simpleName.lowercase() == "playerpartystore") "pk" else "comp"
        val target = this.root().resolve("$uuid.$extension")

        if (!this.exists(target)) {
            return null
        }

        val nbt = NbtIo.read(target)
        if(nbt != null) {
            return (if (extension == "pk") party(uuid, nbt) else pc(uuid, nbt)) as T
        }

        return null
    }

    override fun party(user: UUID, nbt: CompoundTag) : PlayerPartyStore {
        val result = PlayerPartyStore(user)
        for (x in 0..5) {
            val key = "party$x"
            if (nbt.contains(key)) {
                result.add(this.translate(nbt.getCompound(key)))
            }
        }

        return result
    }

    override fun pc(user: UUID, nbt: CompoundTag) : PCStore {
        val result = PCStore(user)
        var box = 0
        while (nbt.contains("BoxNumber$box")) {
            val storage = nbt.getCompound("BoxNumber$box")
            for (x in 0 until REFORGED_POKEMON_PER_BOX) {
                if (storage.contains("pc$x")) {
                    val pokemon = this.translate(storage.getCompound("pc$x"))
                    if (!result.add(pokemon)) {
                        result.backupStore.add(pokemon)
                    }
                }
            }
            ++box
        }

        result.tryRestoreBackedUpPokemon()
        return result
    }

    override fun translate(nbt: CompoundTag) : Pokemon {
        val result = Pokemon()
        result.uuid = nbt.getUUID("UUID")
        result.species = PokemonSpecies.getByPokedexNumber(nbt.getInt("ndex"))
            ?: throw IllegalStateException("Failed to read a species with pokedex identifier ${nbt.getInt("ndex")}")
        PokemonProperties.parse((result.species.forms.find { it.name == nbt.getString("Variant") } ?: result.species.standardForm).name).apply(result)

        result.gender = Gender.values()[nbt.getInt("Gender")]
        result.shiny = this.find(nbt, "IsShiny", CompoundTag::getBoolean) ?:
                        this.find(nbt, "palette", CompoundTag::getString)?.equals("shiny") ?: false
        result.level = nbt.getInt("Level")
        result.addExperience(SidemodExperienceSource("Reforged"), nbt.getInt("EXP"))
        result.setFriendship(nbt.getInt("Friendship"))
        Abilities.get(nbt.getString("Ability"))?.let { template ->
            result.updateAbility(template.create(forced = result.form.abilities.none { it.template == template }))
        }
        result.nature = Natures.getNature(ResourceLocation.parse(ReforgedNatures.entries[nbt.getInt("Nature")].name.lowercase())) ?: Natures.getRandomNature()
        result.mintedNature = Natures.getNature(ResourceLocation.parse(ReforgedNatures.entries[nbt.getInt("MintNature")].name.lowercase()))
        result.currentHealth = nbt.getInt("Health")

        // Stats
        val ivs = IVs()
        ivs[Stats.HP] = nbt.getInt("IVHP")
        ivs[Stats.ATTACK] = nbt.getInt("IVAttack")
        ivs[Stats.DEFENCE] = nbt.getInt("IVDefense")
        ivs[Stats.SPECIAL_ATTACK] = nbt.getInt("IVSpAtt")
        ivs[Stats.SPECIAL_DEFENCE] = nbt.getInt("IVSpDef")
        ivs[Stats.SPEED] = nbt.getInt("IVSpeed")

        val evs = EVs()
        evs[Stats.HP] = nbt.getInt("EVHP")
        evs[Stats.ATTACK] = nbt.getInt("EVAttack")
        evs[Stats.DEFENCE] = nbt.getInt("EVDefense")
        evs[Stats.SPECIAL_ATTACK] = nbt.getInt("EVSpecialAttack")
        evs[Stats.SPECIAL_DEFENCE] = nbt.getInt("EVSpecialDefense")
        evs[Stats.SPEED] = nbt.getInt("EVSpeed")

        ivs.forEach { stat ->
            result.setIV(stat.key, stat.value)
        }
        evs.forEach { stat ->
            result.setEV(stat.key, stat.value)
        }

        for (move in nbt.getList("Moveset", 10)) {
            val compound = move as CompoundTag
            val id = compound.getString("MoveID").replace(Regex("[-\\s]", RegexOption.IGNORE_CASE), "")
            val pp = compound.getInt("MovePP")
            val level = compound.getInt("MovePPLevel")

            val template = Moves.getByNameOrDummy(id.lowercase())
            result.moveSet.add(template.create(pp, level))
        }

        // TODO - Nicknames and Original Trainer Data
        // result.nickname = this.find(nbt, "Nickname", NbtCompound::getString)

        val ball = this.find(nbt, "CaughtBall", CompoundTag::getString)
        result.caughtBall = if(ball != null) PokeBalls.getPokeBall(ResourceLocation.parse(ball)) ?: PokeBalls.POKE_BALL else PokeBalls.POKE_BALL

        return result
    }

    fun <T> find(nbt: CompoundTag, key: String, translator: Translator<T?>) : T? {
        if (nbt.contains(key)) {
            return translator.from(nbt, key)
        }

        return null
    }

    fun interface Translator<out R> {
        fun from(nbt: CompoundTag, key: String) : R?
    }

    enum class ReforgedNatures {
        HARDY,
        SERIOUS,
        DOCILE,
        BASHFUL,
        QUIRKY,
        LONELY,
        BRAVE,
        ADAMANT,
        NAUGHTY,
        BOLD,
        RELAXED,
        IMPISH,
        LAX,
        TIMID,
        HASTY,
        JOLLY,
        NAIVE,
        MODEST,
        MILD,
        QUIET,
        RASH,
        CALM,
        GENTLE,
        SASSY,
        CAREFUL,
    }

}