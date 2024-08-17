/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.BiConsumer;

//@Mixin(LootManager.class)
public class LootManagerMixin {

    private static final String LOAD_CONDITIONS = "cobblemon:forge_load_conditions";

    // TODO(Deltric)
    // Forge was going to add support to LootTable for their ICondition in 1.20, then it was 1.20.1, now it looks like it's 1.20.2
    // Who knows when it will actually make it in, keep an eye on the necessity of this
    /*@Redirect(method = "m_278660_", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"), remap = false)
    private static void cobblemon$supportICondition(Map<Identifier, JsonElement> map, BiConsumer<Identifier, JsonElement> consumer) {
        map.forEach((identifier, jsonElement) -> {
            // If the element isn't present the result is true as well,
            // It is not in fact safe to cast to JsonObject, breaks Incendium
            if (jsonElement.isJsonObject()) {
                if (CraftingHelper.processConditions(jsonElement.getAsJsonObject(), LOAD_CONDITIONS, ICondition.IContext.EMPTY)) {
                    consumer.accept(identifier, jsonElement);
                    return;
                }
            }
            //Still need to do stuff if not obj
            consumer.accept(identifier, jsonElement);

        });
    }*/

}