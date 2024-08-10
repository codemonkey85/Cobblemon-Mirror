package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetPredicate.class)
public class TargetPredicateMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    public void test(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> ci) {
        if (targetEntity instanceof ServerPlayerEntity player) {
            boolean canTargetInBattle = player.getWorld().getGameRules().getBoolean(CobblemonGameRules.MOB_TARGET_IN_BATTLE);
            boolean targetInBattle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null;
            if (!canTargetInBattle && targetInBattle) ci.setReturnValue(false);
        }
    }
}
