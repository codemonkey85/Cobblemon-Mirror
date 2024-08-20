/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.platform.PlatformRegistry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

object CobblemonSounds : PlatformRegistry<Registry<SoundEvent>, RegistryKey<Registry<SoundEvent>>, SoundEvent>() {

    override val registry: Registry<SoundEvent> = Registries.SOUND_EVENT
    override val registryKey: RegistryKey<Registry<SoundEvent>> = RegistryKeys.SOUND_EVENT

    @JvmField
    val GUI_CLICK = this.create("gui.click")

    @JvmField
    val PC_ON = this.create("pc.on")
    @JvmField
    val PC_OFF = this.create("pc.off")
    @JvmField
    val PC_GRAB = this.create("pc.grab")
    @JvmField
    val PC_DROP = this.create("pc.drop")
    @JvmField
    val PC_RELEASE = this.create("pc.release")
    @JvmField
    val PC_CLICK = this.create("pc.click")

    @JvmField
    val HEALING_MACHINE_ACTIVE = this.create("healing_machine.active")

    @JvmField
    val POKE_BALL_CAPTURE_STARTED = this.create("poke_ball.capture_started")
    @JvmField
    val POKE_BALL_CAPTURE_SUCCEEDED = this.create("poke_ball.capture_succeeded")
    @JvmField
    val POKE_BALL_SHAKE = this.create("poke_ball.shake")
    @JvmField
    val POKE_BALL_OPEN = this.create("poke_ball.open")
    @JvmField
    val POKE_BALL_HIT = this.create("poke_ball.hit")
    @JvmField
    val POKE_BALL_SEND_OUT = this.create("poke_ball.send_out")
    @JvmField
    val POKE_BALL_RECALL = this.create("poke_ball.recall")
    @JvmField
    val POKE_BALL_THROW = this.create("poke_ball.throw")
    @JvmField
    val POKE_BALL_TRAIL = this.create("poke_ball.trail")

    @JvmField
    val ITEM_USE = this.create("item.use")
    @JvmField
    val CAN_EVOLVE = this.create("pokemon.can_evolve")
    @JvmField
    val EVOLVING = this.create("pokemon.evolving")
    @JvmField
    val EVOLVE = this.create("evolution.evolve")

    @JvmField
    val PVN_BATTLE = this.create("battle.pvn.default")
    @JvmField
    val PVP_BATTLE = this.create("battle.pvp.default")
    @JvmField
    val PVW_BATTLE = this.create("battle.pvw.default")

    @JvmField
    val MEDICINE_HERB_USE = this.create("medicine_herb.use")
    @JvmField
    val MEDICINE_LIQUID_USE = this.create("medicine_liquid.use")
    @JvmField
    val MEDICINE_PILLS_USE = this.create("medicine_pills.use")
    @JvmField
    val MEDICINE_SPRAY_USE = this.create("medicine_spray.use")
    @JvmField
    val MEDICINE_FEATHER_USE = this.create("medicine_feather.use")

    @JvmField
    val BERRY_HARVEST = this.create("berry.harvest")
    @JvmField
    val BERRY_EAT = this.create("berry.eat")

    @JvmField
    val MULCH_PLACE = this.create("mulch.place")
    @JvmField
    val MULCH_REMOVE = this.create("mulch.remove")

    @JvmField
    val FOSSIL_MACHINE_ACTIVATE = this.create("fossil_machine.activate")
    @JvmField
    val FOSSIL_MACHINE_ACTIVE_LOOP = this.create("fossil_machine.active_loop")
    @JvmField
    val FOSSIL_MACHINE_ASSEMBLE = this.create("fossil_machine.assemble")
    @JvmField
    val FOSSIL_MACHINE_DNA_FULL = this.create("fossil_machine.dna_full")
    @JvmField
    val FOSSIL_MACHINE_FINISHED = this.create("fossil_machine.finished")
    @JvmField
    val FOSSIL_MACHINE_INSERT_DNA = this.create("fossil_machine.insert_dna")
    @JvmField
    val FOSSIL_MACHINE_INSERT_DNA_SMALL = this.create("fossil_machine.insert_dna_small")
    @JvmField
    val FOSSIL_MACHINE_INSERT_FOSSIL = this.create("fossil_machine.insert_fossil")
    @JvmField
    val FOSSIL_MACHINE_RETRIEVE_FOSSIL = this.create("fossil_machine.retrieve_fossil")
    @JvmField
    val FOSSIL_MACHINE_RETRIEVE_POKEMON = this.create("fossil_machine.retrieve_pokemon")
    @JvmField
    val FOSSIL_MACHINE_UNPROTECTED = this.create("fossil_machine.unprotected")

    @JvmField
    val RELIC_COIN_SACK_BREAK = this.create("relic_coin_sack.break")
    @JvmField
    val RELIC_COIN_SACK_HIT = this.create("relic_coin_sack.hit")
    @JvmField
    val RELIC_COIN_SACK_STEP = this.create("relic_coin_sack.step")
    @JvmField
    val RELIC_COIN_SACK_PLACE = this.create("relic_coin_sack.place")
    @JvmField
    val RELIC_COIN_POUCH_BREAK = this.create("relic_coin_pouch.break")
    @JvmField
    val RELIC_COIN_POUCH_PLACE = this.create("relic_coin_pouch.place")

    @JvmField
    val TUMBLESTONE_BREAK = this.create("tumblestone.break")
    @JvmField
    val TUMBLESTONE_BLOCK_BREAK = this.create("tumblestone.block_break")
    @JvmField
    val TUMBLESTONE_HIT = this.create("tumblestone.hit")
    @JvmField
    val TUMBLESTONE_PLACE = this.create("tumblestone.place")
    @JvmField
    val TUMBLESTONE_STEP = this.create("tumblestone.step")

    @JvmField
    val EVOLUTION_STONE_BLOCK_BREAK = this.create("evolution_stone_block.break")
    @JvmField
    val EVOLUTION_STONE_BLOCK_HIT = this.create("evolution_stone_block.hit")
    @JvmField
    val EVOLUTION_STONE_BLOCK_PLACE = this.create("evolution_stone_block.place")
    @JvmField
    val EVOLUTION_STONE_BLOCK_STEP = this.create("evolution_stone_block.step")

    @JvmField
    val GIMMIGHOUL_GIVE_ITEM_SMALL = this.create("gimmighoul.give_item_small")
    @JvmField
    val GIMMIGHOUL_REVEAL = this.create("gimmighoul.reveal")

    @JvmField
    val BERRY_BUSH_BREAK = this.create("berry_bush.break")
    @JvmField
    val BERRY_BUSH_PLACE = this.create("berry_bush.place")

    @JvmField
    val BIG_ROOT_BREAK = this.create("big_root.break")
    @JvmField
    val ENERGY_ROOT_PLACE = this.create("energy_root.place")

    @JvmField
    val VIVICHOKE_BREAK = this.create("vivichoke.break")
    @JvmField
    val VIVICHOKE_PLACE = this.create("vivichoke.place")

    @JvmField
    val MINT_BREAK = this.create("mint.break")
    @JvmField
    val MINT_PLACE = this.create("mint.place")

    @JvmField
    val REVIVAL_HERB_BREAK = this.create("revival_herb.break")
    @JvmField
    val REVIVAL_HERB_PLACE = this.create("revival_herb.place")

    @JvmField
    val MEDICINAL_LEEK_BREAK = this.create("medicinal_leek.break")
    @JvmField
    val MEDICINAL_LEEK_PLACE = this.create("medicinal_leek.plant")

    @JvmField
    val GILDED_CHEST_OPEN = this.create("gilded_chest.open")
    @JvmField
    val GILDED_CHEST_CLOSE = this.create("gilded_chest.close")
    @JvmField
    val GILDED_CHEST_STEP = this.create("gilded_chest.step")
    @JvmField
    val GILDED_CHEST_HIT = this.create("gilded_chest.hit")
    @JvmField
    val GILDED_CHEST_BREAK = this.create("gilded_chest.break")
    @JvmField
    val GILDED_CHEST_PLACE = this.create("gilded_chest.place")

    @JvmField
    val RELIC_COIN_SACK_SOUNDS = BlockSoundGroup(1f, 1f,
        RELIC_COIN_SACK_BREAK,
        RELIC_COIN_SACK_STEP,
        RELIC_COIN_SACK_PLACE,
        RELIC_COIN_SACK_HIT,
        RELIC_COIN_SACK_STEP
    )
    @JvmField
    val RELIC_COIN_POUCH_SOUNDS = BlockSoundGroup(1f, 1f,
        RELIC_COIN_POUCH_BREAK,
        RELIC_COIN_SACK_STEP,
        RELIC_COIN_POUCH_PLACE,
        RELIC_COIN_SACK_HIT,
        RELIC_COIN_SACK_STEP
    )

    @JvmField
    val IMPACT_NORMAL = this.create("impact.normal")
    @JvmField
    val IMPACT_BUG = this.create("impact.bug")
    @JvmField
    val IMPACT_DARK = this.create("impact.dark")
    @JvmField
    val IMPACT_DRAGON = this.create("impact.dragon")
    @JvmField
    val IMPACT_ELECTRIC = this.create("impact.electric")
    @JvmField
    val IMPACT_FAIRY = this.create("impact.fairy")
    @JvmField
    val IMPACT_FIGHTING = this.create("impact.fighting")
    @JvmField
    val IMPACT_FIRE = this.create("impact.fire")
    @JvmField
    val IMPACT_FLYING = this.create("impact.flying")
    @JvmField
    val IMPACT_GHOST = this.create("impact.ghost")
    @JvmField
    val IMPACT_GRASS = this.create("impact.grass")
    @JvmField
    val IMPACT_GROUND = this.create("impact.ground")
    @JvmField
    val IMPACT_ICE = this.create("impact.ice")
    @JvmField
    val IMPACT_POISON = this.create("impact.poison")
    @JvmField
    val IMPACT_PSYCHIC = this.create("impact.psychic")
    @JvmField
    val IMPACT_ROCK = this.create("impact.rock")
    @JvmField
    val IMPACT_STEEL = this.create("impact.steel")
    @JvmField
    val IMPACT_WATER = this.create("impact.water")

    @JvmField
    val TUMBLESTONE_SOUNDS = BlockSoundGroup(1f, 1f,
        TUMBLESTONE_BREAK,
        TUMBLESTONE_STEP,
        TUMBLESTONE_PLACE,
        TUMBLESTONE_HIT,
        TUMBLESTONE_STEP
    )

    @JvmField
    val TUMBLESTONE_BLOCK_SOUNDS = BlockSoundGroup(1f, 1f,
        TUMBLESTONE_BLOCK_BREAK,
        TUMBLESTONE_STEP,
        TUMBLESTONE_PLACE,
        TUMBLESTONE_HIT,
        TUMBLESTONE_STEP
    )

    @JvmField
    val EVOLUTION_STONE_BLOCK_SOUNDS = BlockSoundGroup(1f, 1.1f,
        EVOLUTION_STONE_BLOCK_BREAK,
        EVOLUTION_STONE_BLOCK_STEP,
        EVOLUTION_STONE_BLOCK_PLACE,
        EVOLUTION_STONE_BLOCK_HIT,
        EVOLUTION_STONE_BLOCK_STEP
    )

    @JvmField
    val BERRY_BUSH_SOUNDS = BlockSoundGroup(1f, 1f,
        BERRY_BUSH_BREAK,
        SoundEvents.BLOCK_GRASS_STEP,
        BERRY_BUSH_PLACE,
        SoundEvents.BLOCK_GRASS_HIT,
        SoundEvents.BLOCK_GRASS_STEP
    )

    @JvmField
    val BIG_ROOT_SOUNDS = BlockSoundGroup(1f, 1f,
        BIG_ROOT_BREAK,
        SoundEvents.BLOCK_ROOTS_STEP,
        SoundEvents.BLOCK_ROOTS_PLACE,
        SoundEvents.BLOCK_ROOTS_HIT,
        SoundEvents.BLOCK_ROOTS_FALL
    )

    @JvmField
    val ENERGY_ROOT_SOUNDS = BlockSoundGroup(1f, 1f,
        SoundEvents.BLOCK_ROOTS_BREAK,
        SoundEvents.BLOCK_ROOTS_STEP,
        ENERGY_ROOT_PLACE,
        SoundEvents.BLOCK_ROOTS_HIT,
        SoundEvents.BLOCK_ROOTS_FALL
    )

    @JvmField
    val MEDICINAL_LEEK_SOUNDS = BlockSoundGroup(1f, 1f,
        MEDICINAL_LEEK_BREAK,
        SoundEvents.BLOCK_GRASS_STEP,
        MEDICINAL_LEEK_PLACE,
        SoundEvents.BLOCK_GRASS_HIT,
        SoundEvents.BLOCK_GRASS_FALL
    )

    @JvmField
    val VIVICHOKE_SOUNDS = BlockSoundGroup(1f, 1f,
        VIVICHOKE_BREAK,
        SoundEvents.BLOCK_GRASS_STEP,
        VIVICHOKE_PLACE,
        SoundEvents.BLOCK_GRASS_HIT,
        SoundEvents.BLOCK_GRASS_FALL
    )

    @JvmField
    val MINT_SOUNDS = BlockSoundGroup(1f, 1f,
        MINT_BREAK,
        SoundEvents.BLOCK_GRASS_STEP,
        MINT_PLACE,
        SoundEvents.BLOCK_GRASS_HIT,
        SoundEvents.BLOCK_GRASS_FALL
    )

    @JvmField
    val REVIVAL_HERB_SOUNDS = BlockSoundGroup(1f, 1f,
        REVIVAL_HERB_BREAK,
        SoundEvents.BLOCK_GRASS_STEP,
        REVIVAL_HERB_PLACE,
        SoundEvents.BLOCK_GRASS_HIT,
        SoundEvents.BLOCK_GRASS_FALL
    )

    @JvmField
    val GILDED_CHEST_SOUNDS = BlockSoundGroup(1f, 1f,
        GILDED_CHEST_BREAK,
        GILDED_CHEST_STEP,
        GILDED_CHEST_PLACE,
        GILDED_CHEST_HIT,
        GILDED_CHEST_STEP
    )

    @JvmField
    val DISPLAY_CASE_ADD_ITEM = this.create("display_case.add_item")
    @JvmField
    val DISPLAY_CASE_REMOVE_ITEM = this.create("display_case.remove_item")
    @JvmField
    val DISPLAY_CASE_BREAK = this.create("display_case.break")
    @JvmField
    val DISPLAY_CASE_HIT = this.create("display_case.hit")
    @JvmField
    val DISPLAY_CASE_PLACE = this.create("display_case.place")
    @JvmField
    val DISPLAY_CASE_STEP = this.create("display_case.step")

    @JvmField
    val DISPLAY_CASE_SOUNDS = BlockSoundGroup(1f, 1f,
        DISPLAY_CASE_BREAK,
        DISPLAY_CASE_STEP,
        DISPLAY_CASE_PLACE,
        DISPLAY_CASE_HIT,
        DISPLAY_CASE_STEP
    )

    private fun create(name: String): SoundEvent = this.create(name, SoundEvent.of(cobblemonResource(name)))
}