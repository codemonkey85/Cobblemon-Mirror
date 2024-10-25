/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonBuildDetails;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public final class CrashReportMixin {

    @Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/SystemReport;appendToCrashReportString(Ljava/lang/StringBuilder;)V"))
    public void cobblemon$printCobblemonDetails(StringBuilder builder, CallbackInfo callback) {
        CrashReportCategory cobblemon = new CrashReportCategory("Cobblemon");
        cobblemon.setDetail("Version", CobblemonBuildDetails.VERSION);
        cobblemon.setDetail("Is Snapshot", CobblemonBuildDetails.SNAPSHOT);
        cobblemon.setDetail("Git Commit", CobblemonBuildDetails.INSTANCE.smallCommitHash() + " (" + "https://gitlab.com/cable-mc/cobblemon/-/commit/" + CobblemonBuildDetails.GIT_COMMIT + ")");
        cobblemon.setDetail("Branch", CobblemonBuildDetails.BRANCH);

        cobblemon.getDetails(builder);
        builder.append("\n\n");
    }

}
