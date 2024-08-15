/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.data

import com.cobblemon.mod.common.api.resistance.ResistanceMap
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.data.elemental.ElementalTypeAssetProvider
import com.cobblemon.mod.common.data.elemental.ElementalTypeProvider
import com.cobblemon.mod.common.data.elemental.ElementalTypeTagProvider
import com.cobblemon.mod.common.data.species.SpeciesProvider
import com.cobblemon.mod.common.registry.CobblemonRegistries
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey

class CobblemonFabricDataGeneratorEntrypoint : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        this.commonDataGen(fabricDataGenerator.createPack())
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(CobblemonRegistries.ELEMENTAL_TYPE_KEY) { context -> buildDummyElementalTypes(context::register) }
    }

    private fun commonDataGen(pack: FabricDataGenerator.Pack) {
        pack.addProvider(::ElementalTypeProvider)
        pack.addProvider(::ElementalTypeTagProvider)
        pack.addProvider(::ElementalTypeAssetProvider)
        pack.addProvider(::SpeciesProvider)
    }

    private fun buildDummyElementalTypes(consumer: (ResourceKey<ElementalType>, ElementalType) -> Unit) {
        ElementalTypes.keys().forEach { key ->
            val type = ElementalType(Component.empty(), ResistanceMap(emptyMap()))
            consumer(key, type)
        }
    }

}