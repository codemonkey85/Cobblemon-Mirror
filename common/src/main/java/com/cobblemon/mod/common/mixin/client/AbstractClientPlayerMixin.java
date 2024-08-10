package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(
        method = "getFieldOfViewModifier()F",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true
    )
    public void cobblemon$getPokedexFovMultiplier(CallbackInfoReturnable<Float> cir) {
        PokedexUsageContext usageContext = CobblemonClient.INSTANCE.getPokedexUsageContext();
        if (usageContext.getScanningGuiOpen()) {
            cir.setReturnValue(usageContext.getFovMultiplier());
        }
    }
}
