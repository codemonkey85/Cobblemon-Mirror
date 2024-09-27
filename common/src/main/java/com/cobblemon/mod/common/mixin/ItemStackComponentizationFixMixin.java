/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.util.DataKeys;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin
{
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixItemStack(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        cobblemon$attemptPokemonModelDataFix(itemStackData, dynamic);
    }

    @Unique
    private static void cobblemon$attemptPokemonModelDataFix(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic)
    {
        var species = itemStackData.removeTag(DataKeys.POKEMON_ITEM_SPECIES).asString();
        var aspects = itemStackData.removeTag(DataKeys.POKEMON_ITEM_ASPECTS).asList(dynamic1 -> dynamic1);
        var redTint = itemStackData.removeTag(DataKeys.POKEMON_ITEM_TINT_RED).asNumber();
        var greenTint = itemStackData.removeTag(DataKeys.POKEMON_ITEM_TINT_GREEN).asNumber();
        var blueTint = itemStackData.removeTag(DataKeys.POKEMON_ITEM_TINT_BLUE).asNumber();
        var alphaTint = itemStackData.removeTag(DataKeys.POKEMON_ITEM_TINT_ALPHA).asNumber();
        if(species.hasResultOrPartial() && aspects != null) {
            var pokeComponent = dynamic.emptyMap().set("species", dynamic.createString(species.getOrThrow()));
            pokeComponent = pokeComponent.set("aspects", dynamic.createList(aspects.stream()));
            if(redTint.hasResultOrPartial() || blueTint.hasResultOrPartial() || greenTint.hasResultOrPartial() || alphaTint.hasResultOrPartial()) {
                pokeComponent = pokeComponent.set("tint", dynamic.createList(Stream.of(redTint, greenTint, blueTint, alphaTint)
                                                                                     .map(x -> x.mapOrElse(Number::floatValue, error -> 0F))
                                                                                     .map(dynamic::createFloat)));
            }
            itemStackData.setComponent("cobblemon:pokemon_item", pokeComponent);
        }
    }
}
