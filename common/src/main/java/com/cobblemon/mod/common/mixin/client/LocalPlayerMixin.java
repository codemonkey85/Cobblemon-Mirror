package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.Rollable;
import com.cobblemon.mod.common.net.messages.server.orientation.C2SUpdateOrientationPacket;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Unique Matrix3f cobblemon$lastOrientation;

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void cobblemon_updateRotationMatrix(CallbackInfo ci) {
        if (!(this instanceof Rollable rollable)) return;
        if (rollable.getOrientation() == cobblemon$lastOrientation) return;
        cobblemon$lastOrientation = rollable.getOrientation() != null ? new Matrix3f(rollable.getOrientation()) : null;
        CobblemonNetwork.INSTANCE.sendToServer(new C2SUpdateOrientationPacket(rollable.getOrientation()));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void cobblemon_updateRotationMatrixPassenger(CallbackInfo ci) {
        if (!(this instanceof Rollable rollable)) return;
        if (rollable.getOrientation() == cobblemon$lastOrientation) return;
        cobblemon$lastOrientation = rollable.getOrientation() != null ? new Matrix3f(rollable.getOrientation()) : null;
        CobblemonNetwork.INSTANCE.sendToServer(new C2SUpdateOrientationPacket(rollable.getOrientation()));
    }

}
