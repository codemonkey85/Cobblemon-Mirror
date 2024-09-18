/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFix
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.Typed
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import net.minecraft.util.datafix.ExtraDataFixUtils
import net.minecraft.util.datafix.fixes.References

class BlockPosUpdateFix(outputSchema: Schema) : DataFix(outputSchema, false) {
    override fun makeRule(): TypeRewriteRule {
        val pokemonType = inputSchema.getChoiceType(References.ENTITY, "cobblemon:pokemon")
        val controllerFinder = DSL.namedChoice("cobblemon:fossil_analyzer", inputSchema.getChoiceType(References.BLOCK_ENTITY, "cobblemon:fossil_multiblock"))

        return TypeRewriteRule.seq(
            this.createBlockEntityFixer(References.BLOCK_ENTITY, "cobblemon:pasture", mapOf(
                "TetherMaxRoamPos" to "TetherMaxRoamPos",
                "TetherMinRoamPos" to "TetherMinRoamPos"
            )),
            this.createBlockEntityFixer(References.BLOCK_ENTITY, "cobblemon:fossil_analyzer", mapOf(
                "ControllerBlock" to "ControllerBlock",
            )),
            this.createBlockEntityFixer(References.BLOCK_ENTITY, "cobblemon:restoration_tank", mapOf(
                "ControllerBlock" to "ControllerBlock"
            )),
            this.createBlockEntityFixer(References.BLOCK_ENTITY, "cobblemon:fossil_multiblock", mapOf(
                "ControllerBlock" to "ControllerBlock"
            )),
            this.fixTypeEverywhereTyped("Fossil multiblock pos fixer", inputSchema.getType(References.BLOCK_ENTITY)) { typed ->
                typed.updateTyped(controllerFinder) { controllerTyped ->
                    controllerTyped.update(DSL.remainderFinder()) {
                        it.update("MultiblockStore", ::fixFossilMultiblock)
                    }
                }
            },
            this.fixTypeEverywhereTyped("Pokemon entity tethering pos fixer", pokemonType) { typed ->
                 typed.update(DSL.remainderFinder(), ::fixPokemonEntity)
            }
        )
    }

    private fun createBlockEntityFixer(
        typeReference: DSL.TypeReference,
        string: String,
        map: Map<String, String>
    ): TypeRewriteRule {
        val fixerName = "Block pos fixer for $string}"
        val opticFinder = DSL.namedChoice(
            string,
            this.inputSchema.getChoiceType(typeReference, string)
        )
        return this.fixTypeEverywhereTyped(fixerName, this.inputSchema.getType(typeReference)) { typed ->
            typed.updateTyped(opticFinder) { typedx ->
                this.fixFields(typedx, map)
            }
        }
    }

    private fun fixFields(typed: Typed<*>, map: Map<String, String>): Typed<*> {
        return typed.update(DSL.remainderFinder()) {
            var result = it
            map.forEach { key, value ->
                result = result.renameAndFixField(key, value) { dynamic ->
                    ExtraDataFixUtils.fixBlockPos(
                        dynamic!!
                    )
                }
            }
            result
        }
    }

    private fun fixPokemonEntity(entityNbt: Dynamic<*>): Dynamic<*> {
        return entityNbt.update("Tethering") {
            return@update it.update("TetherMaxRoamPos", ExtraDataFixUtils::fixBlockPos)
                            .update("TetherMinRoamPos", ExtraDataFixUtils::fixBlockPos)
        }
    }

    private fun fixFossilMultiblock(multiblockNbt: Dynamic<*>): Dynamic<*> {
        return multiblockNbt
            .update("MonitorPos", ExtraDataFixUtils::fixBlockPos)
            .update("TankBasePos", ExtraDataFixUtils::fixBlockPos)
            .update("AnalyzerPos", ExtraDataFixUtils::fixBlockPos)
    }



}