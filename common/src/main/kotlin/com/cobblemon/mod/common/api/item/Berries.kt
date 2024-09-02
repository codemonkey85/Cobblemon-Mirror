/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.item

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * Registry for all Berry types
 * Get or register berry types
 *
 * @author Deltric
 * @since January 13th, 2022
 */
object Berries {
    private val allBerries = mutableListOf<Berry>()

    val CHERI = registerBerry(Berry(cobblemonResource("cheri"), 10, 0, 0, 0, 0))
    val CHESTO = registerBerry(Berry(cobblemonResource("chesto"), 0, 10, 0, 0, 0))
    val PECHA = registerBerry(Berry(cobblemonResource("pecha"), 0, 0, 10, 0, 0))
    val RAWST = registerBerry(Berry(cobblemonResource("rawst"), 0, 0, 0, 10, 0))
    val ASPEAR = registerBerry(Berry(cobblemonResource("aspear"), 0, 0, 0, 0, 10))
    val LEPPA = registerBerry(Berry(cobblemonResource("leppa"), 10, 0, 10, 10, 10))
    val ORAN = registerBerry(Berry(cobblemonResource("oran"), 10, 10, 0, 10, 10))
    val PERSIM = registerBerry(Berry(cobblemonResource("persim"), 10, 10, 10, 0, 10))
    val LUM = registerBerry(Berry(cobblemonResource("lum"), 10, 10, 10, 10, 0))
    val SITRUS = registerBerry(Berry(cobblemonResource("sitrus"), 0, 10, 10, 10, 10))
    val FIGY = registerBerry(Berry(cobblemonResource("figy"), 15, 0, 0, 0, 0))
    val WIKI = registerBerry(Berry(cobblemonResource("wiki"), 0, 15, 0, 0, 0))
    val MAGO = registerBerry(Berry(cobblemonResource("mago"), 0, 0, 15, 0, 0))
    val AGUAV = registerBerry(Berry(cobblemonResource("aguav"), 0, 0, 0, 15, 0))
    val IAPAPA = registerBerry(Berry(cobblemonResource("iapapa"), 0, 0, 0, 0, 15))
    val RAZZ = registerBerry(Berry(cobblemonResource("razz"), 10, 10, 0, 0, 0))
    val BLUK = registerBerry(Berry(cobblemonResource("bluk"), 0, 10, 10, 0, 0))
    val NANAB = registerBerry(Berry(cobblemonResource("nanab"), 0, 0, 10, 10, 0))
    val WEPEAR = registerBerry(Berry(cobblemonResource("wepear"), 0, 0, 0, 10, 10))
    val PINAP = registerBerry(Berry(cobblemonResource("pinap"), 10, 0, 0, 0, 10))
    val POMEG = registerBerry(Berry(cobblemonResource("pomeg"), 10, 0, 10, 10, 0))
    val KELPSY = registerBerry(Berry(cobblemonResource("kelpsy"), 0, 10, 0, 10, 10))
    val QUALOT = registerBerry(Berry(cobblemonResource("qualot"), 10, 0, 10, 0, 10))
    val HONDEW = registerBerry(Berry(cobblemonResource("hondew"), 10, 10, 0, 10, 0))
    val GREPA = registerBerry(Berry(cobblemonResource("grepa"), 0, 10, 10, 0, 10))
    val TAMATO = registerBerry(Berry(cobblemonResource("tamato"), 20, 10, 0, 0, 0))
    val CORNN = registerBerry(Berry(cobblemonResource("cornn"), 0, 20, 10, 0, 0))
    val MAGOST = registerBerry(Berry(cobblemonResource("magost"), 0, 0, 20, 10, 0))
    val RABUTA = registerBerry(Berry(cobblemonResource("rabuta"), 0, 0, 0, 20, 10))
    val NOMEL = registerBerry(Berry(cobblemonResource("nomel"), 10, 0, 0, 0, 20))
    val SPELON = registerBerry(Berry(cobblemonResource("spelon"), 30, 10, 0, 0, 0))
    val PAMTRE = registerBerry(Berry(cobblemonResource("pamtre"), 0, 30, 10, 0, 0))
    val WATMEL = registerBerry(Berry(cobblemonResource("watmel"), 0, 0, 30, 10, 0))
    val DURIN = registerBerry(Berry(cobblemonResource("durin"), 0, 0, 0, 30, 10))
    val BELUE = registerBerry(Berry(cobblemonResource("belue"), 10, 0, 0, 0, 30))
    val OCCA = registerBerry(Berry(cobblemonResource("occa"), 15, 0, 10, 0, 0))
    val PASSHO = registerBerry(Berry(cobblemonResource("passho"), 0, 15, 0, 10, 0))
    val WACAN = registerBerry(Berry(cobblemonResource("wacan"), 0, 0, 15, 0, 10))
    val RINDO = registerBerry(Berry(cobblemonResource("rindo"), 10, 0, 0, 15, 0))
    val YACHE = registerBerry(Berry(cobblemonResource("yache"), 0, 10, 0, 0, 15))
    val CHOPLE = registerBerry(Berry(cobblemonResource("chople"), 15, 0, 0, 10, 0))
    val KEBIA = registerBerry(Berry(cobblemonResource("kebia"), 0, 15, 0, 0, 10))
    val SHUCA = registerBerry(Berry(cobblemonResource("shuca"), 10, 0, 15, 0, 0))
    val COBA = registerBerry(Berry(cobblemonResource("coba"), 0, 10, 0, 15, 0))
    val PAYAPA = registerBerry(Berry(cobblemonResource("payapa"), 0, 0, 10, 0, 15))
    val TANGA = registerBerry(Berry(cobblemonResource("tanga"), 20, 0, 0, 0, 10))
    val CHARTI = registerBerry(Berry(cobblemonResource("charti"), 10, 20, 0, 0, 0))
    val KASIB = registerBerry(Berry(cobblemonResource("kasib"), 0, 10, 20, 0, 0))
    val HABAN = registerBerry(Berry(cobblemonResource("haban"), 0, 0, 10, 20, 0))
    val COLBUR = registerBerry(Berry(cobblemonResource("colbur"), 0, 0, 0, 10, 20))
    val BABIRI = registerBerry(Berry(cobblemonResource("babiri"), 25, 10, 0, 0, 0))
    val CHILAN = registerBerry(Berry(cobblemonResource("chilan"), 0, 25, 10, 0, 0))
    val LIECHI = registerBerry(Berry(cobblemonResource("liechi"), 30, 10, 30, 0, 0))
    val GANLON = registerBerry(Berry(cobblemonResource("ganlon"), 0, 30, 10, 30, 0))
    val SALAC = registerBerry(Berry(cobblemonResource("salac"), 0, 0, 30, 10, 30))
    val PETAYA = registerBerry(Berry(cobblemonResource("petaya"), 30, 0, 0, 30, 10))
    val APICOT = registerBerry(Berry(cobblemonResource("apicot"), 10, 30, 0, 0, 30))
    val LANSAT = registerBerry(Berry(cobblemonResource("lansat"), 30, 10, 30, 10, 30))
    val STARF = registerBerry(Berry(cobblemonResource("starf"), 30, 10, 30, 10, 30))
    val ENIGMA = registerBerry(Berry(cobblemonResource("enigma"), 40, 10, 0, 0, 0))
    val MICLE = registerBerry(Berry(cobblemonResource("micle"), 0, 40, 10, 0, 0))
    val CUSTAP = registerBerry(Berry(cobblemonResource("custap"), 0, 0, 40, 10, 0))
    val JABOCA = registerBerry(Berry(cobblemonResource("jaboca"), 0, 0, 0, 40, 10))
    val ROWAP = registerBerry(Berry(cobblemonResource("rowap"), 10, 0, 0, 0, 40))
    val ROSELI = registerBerry(Berry(cobblemonResource("roseli"), 0, 0, 25, 10, 0))
    val KEE = registerBerry(Berry(cobblemonResource("kee"), 30, 30, 10, 10, 10))
    val MARANGA = registerBerry(Berry(cobblemonResource("maranga"), 10, 10, 30, 30, 10))

    /**
     * Registers a new berry type
     */
    fun registerBerry(berry: Berry): Berry {
        allBerries.add(berry)
        return berry
    }

    /**
     * Gets a berry by registry name
     * @return a berry type or null
     */
    fun getBerry(name: ResourceLocation): Berry? {
        return allBerries.find { berry -> berry.name == name }
    }
}