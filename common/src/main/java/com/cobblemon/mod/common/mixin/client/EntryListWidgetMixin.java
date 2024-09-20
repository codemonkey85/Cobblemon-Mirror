/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.gui.ScrollingWidget;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractSelectionList.class)
public abstract class EntryListWidgetMixin {

    @SuppressWarnings("ConstantValue")
    @ModifyConstant(method = "getEntryAtPosition", constant = @Constant(intValue = 4))
    public int cobblemon$adjustOnlyIfNecessary(int input) {
        if(ScrollingWidget.class.isInstance(this)) {
            return 0;
        }

        return input;
    }

}
