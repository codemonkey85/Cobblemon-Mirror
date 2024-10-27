/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.compat.lambdynamiclights;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.compat.LambDynamicLightsCompat;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

// Java class due to relying on entrypoint
public class LambDynamicLightsCompatEntrypoint implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
        LambDynamicLightsCompat.hookCompat();
        Cobblemon.LOGGER.info("Lamb Dynamic Lights compatibility enabled");
    }
}
