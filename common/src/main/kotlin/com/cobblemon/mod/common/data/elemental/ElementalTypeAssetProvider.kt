/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.data.elemental

import com.cobblemon.mod.common.api.types.ElementalTypeDisplay
import com.cobblemon.mod.common.data.DataExport
import com.cobblemon.mod.common.data.OutputtingDataProvider
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ColorRGBA
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class ElementalTypeAssetProvider(
    packOutput: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>
) : OutputtingDataProvider<ElementalTypeDisplay, ElementalTypeAssetProvider.Export>(packOutput, lookupProvider) {

    override fun buildEntries(lookupProvider: HolderLookup.Provider, consumer: Consumer<Export>) {
        consumer.accept(export("bug", 0xA2C831))
        consumer.accept(export("dark", 0x5C6CB2))
        consumer.accept(export("dragon", 0x535DE8))
        consumer.accept(export("electric", 0xEFD128))
        consumer.accept(export("fairy", 0xEA727E))
        consumer.accept(export("fighting", 0xC44C5C))
        consumer.accept(export("fire", 0xE55C32))
        consumer.accept(export("flying", 0xBCC1FF))
        consumer.accept(export("ghost", 0x9572E5))
        consumer.accept(export("grass", 0x4DBC3C))
        consumer.accept(export("ground", 0xD89950))
        consumer.accept(export("ice", 0x6BC3EF))
        consumer.accept(export("normal", 0xDDDDCF))
        consumer.accept(export("poison", 0xA24BD8))
        consumer.accept(export("psychic", 0xD86AD6))
        consumer.accept(export("rock", 0xAA9666))
        consumer.accept(export("steel", 0xC3CCE0))
        consumer.accept(export("water", 0x4A9BE8))
        // TODO: We need asset team to tell us what color here
        consumer.accept(export("stellar", 0))
    }

    override fun pathProvider(): PackOutput.PathProvider = this.createPathForCobblemonRegistryAsset(CobblemonRegistries.ELEMENTAL_TYPE_KEY)

    override fun getName(): String = "elemental type display"

    private fun export(name: String, tint: Int): Export {
        return Export(cobblemonResource(name), ElementalTypeDisplay(ColorRGBA(tint)))
    }

    class Export(private val id: ResourceLocation, private val display: ElementalTypeDisplay) : DataExport<ElementalTypeDisplay> {
        override fun codec(): Codec<ElementalTypeDisplay> = ElementalTypeDisplay.CODEC

        override fun value(): ElementalTypeDisplay  = this.display

        override fun id(): ResourceLocation = this.id
    }

}