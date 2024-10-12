package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Rollable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private Entity entity;
    @Shadow @Final
    private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Unique
    Minecraft minecraft = Minecraft.getInstance();

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void open_camera$setRotation(float f, float g, CallbackInfo ci) {
        if (!(this.entity instanceof Rollable rollable)) return;
        if (!rollable.shouldRoll()) {
            rollable.clearRotation();
            return;
        }
        if (rollable.getOrientation() == null) return;

        var newRotation = rollable.getOrientation().normal(new Matrix3f()).getNormalizedRotation(new Quaternionf());
        if (this.minecraft.options.getCameraType().isMirrored()) {
            newRotation.rotateY((float)Math.toRadians(180));
        }
        this.rotation.set(newRotation);
        this.xRot = rollable.getPitch();
        this.yRot = rollable.getYaw();
        this.forwards.set(rollable.getForwardVector());
        this.up.set(rollable.getUpVector());
        this.left.set(rollable.getLeftVector());

        ci.cancel();
    }

}
