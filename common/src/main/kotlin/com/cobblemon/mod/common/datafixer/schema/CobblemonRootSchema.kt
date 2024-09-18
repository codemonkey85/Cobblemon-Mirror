/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.schema

import com.cobblemon.mod.common.datafixer.CobblemonTypeReferences
import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.DSL
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.types.templates.TypeTemplate
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.util.datafix.schemas.NamespacedSchema
import java.util.function.Supplier

//I will document this class heavily hoping to spare others the pain caused to me figuring this out
class CobblemonRootSchema(versionKey: Int, parent: Schema?) : Schema(versionKey, parent) {

    //A schema is basically used to parse an arbitrary nbt tag and turn it into "types" to be manipulated by
    //The actual fixes. So here are all the types!
    //Any entity/block entity types in the registerEntity methods get passed here
    override fun registerTypes(
        schema: Schema,
        entityTypes: MutableMap<String, Supplier<TypeTemplate>>,
        blockEntityTypes: MutableMap<String, Supplier<TypeTemplate>>
    ) {
        schema.registerType(true, CobblemonTypeReferences.POKEMON) {
            DSL.optionalFields(
                DataKeys.HELD_ITEM,
                References.ITEM_STACK.`in`(schema)
            )
        }
        //I'm not 100% sure what a recursive type is, but at least one is required
        //Maybe a type that can contain itself?
        schema.registerType(true, References.ENTITY) {
            DSL.taggedChoiceLazy("id", NamespacedSchema.namespacedString(), entityTypes)
        }
        schema.registerType(true, References.BLOCK_ENTITY) {
            //Tagged choice makes it so that the type is determined by a key of some other type
            //So the "id" of a block entity determines what type the actual block entity is
            DSL.taggedChoiceLazy(
                "id",
                NamespacedSchema.namespacedString(),
                blockEntityTypes
            )
        }

        schema.registerType(false, References.CHUNK) {
            DSL.optionalFields(
                "block_entities",
                DSL.list(
                    //Remainder basically passes through anything not previously matched to a type
                    //I believe this is necessary because the "block_entities" type wont be parsed properly if there is leftover data.
                    //So if we only have a gilded chest and a display case defined as block entities (because those are the only ones we need to fix)
                    //Any other block entity data gets put in this remainder tag.
                    //So basically, this is either a "block_entity" or something else we dont care about
                    DSL.or(
                        References.BLOCK_ENTITY.`in`(schema),
                        DSL.remainder()
                    )
                )
            )
        }
        schema.registerType(false, References.ENTITY_CHUNK) {
            DSL.optionalFields(
                "Entities",
                DSL.or(
                    DSL.list(References.ENTITY.`in`(schema)),
                    DSL.remainder()
                )
            )
        }
        //We steal the definition of the item stack type from the vanilla schema
        //DO NOT CHANGE THE NUMBER IN HERE.
        //This is the version of the schema that was being used when THIS schema was made.
        //Specifically, this schema was pre item components
        //If you need a different number, make a new schema
        val vanillaSchema = DataFixers.getDataFixer().getSchema(38180)
        schema.registerType(true, References.ITEM_STACK) {
            vanillaSchema.getType(References.ITEM_STACK).buildTemplate()
        }
    }

    override fun registerEntities(schema: Schema): MutableMap<String, Supplier<TypeTemplate>> {
        val map = mutableMapOf<String, Supplier<TypeTemplate>>()
        map.put("cobblemon:pokemon") {
            DSL.optionalFields(
                "Pokemon", CobblemonTypeReferences.POKEMON.`in`(schema)
            )
        }
        return map
    }

    // If we ever decide to target something do it here
    override fun registerBlockEntities(schema: Schema): MutableMap<String, Supplier<TypeTemplate>> {
        val map = mutableMapOf<String, Supplier<TypeTemplate>>()
        map.put("cobblemon:chest") {
            DSL.optionalFields(
                "Items",
                DSL.list(References.ITEM_STACK.`in`(schema))
            )
        }
        map.put("cobblemon:display_case") {
            DSL.optionalFields(
                "Items",
                DSL.list(References.ITEM_STACK.`in`(schema))
            )
        }
        map.put("cobblemon:fossil_analyzer") {
            DSL.optionalFields(
                "Items",
                DSL.list(References.ITEM_STACK.`in`(schema))
            )
        }
        this.registerSimple(map, "cobblemon:pasture")
        this.registerSimple(map, "cobblemon:fossil_analyzer")
        this.registerSimple(map, "cobblemon:restoration_tank")
        this.registerSimple(map, "cobblemon:fossil_multiblock")
        return map
    }

}