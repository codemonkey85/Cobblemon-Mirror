/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.schema

import com.cobblemon.mod.common.datafixer.CobblemonTypeReferences
import com.mojang.datafixers.DSL
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.types.templates.TypeTemplate
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import java.util.function.Supplier

class CobblemonSchemaV2(versionKey: Int, parent: Schema?) : Schema(versionKey, parent) {

    override fun registerTypes(
        schema: Schema,
        entityTypes: MutableMap<String, Supplier<TypeTemplate>>,
        blockEntityTypes: MutableMap<String, Supplier<TypeTemplate>>
    ) {
        super.registerTypes(schema, entityTypes, blockEntityTypes)
        val vanillaSchema = DataFixers.getDataFixer().getSchema(38185)
        schema.registerType(true, References.ITEM_STACK) {
            vanillaSchema.getType(References.ITEM_STACK).buildTemplate()
        }
    }
}