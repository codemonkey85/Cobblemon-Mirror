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
public abstract class CameraMixin {

    @Shadow private Entity entity;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Shadow public abstract float getXRot();

    @Shadow public abstract float getYRot();

    @Unique private float returnTimer = 0;
    @Unique private float rollAngleStart = 0;
    @Unique Minecraft minecraft = Minecraft.getInstance();

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void open_camera$setRotation(float f, float g, CallbackInfo ci) {
        if (!(this.entity instanceof Rollable rollable)) return;
        if (!rollable.shouldRoll() && rollable.getOrientation() != null) {
            if(this.returnTimer < 1) {
                //Rotation is taken from entity since we no longer handle mouse ourselves
                //Stops a period of time when you can't input anything.
                rollable.getOrientation().set(
                        new Matrix3f()
                                .rotateY((float) Math.toRadians(180 - this.entity.getYRot()))
                                .rotateX((float) Math.toRadians(-this.entity.getXRot()))
                );
                if(rollAngleStart == 0){
                    this.returnTimer = 1;
                    rollable.clearRotation();
                    return;
                }
                rollable.getOrientation().rotateZ((float) Math.toRadians(-rollAngleStart*(1-returnTimer)));
                applyRotation();
                this.returnTimer += .05F;
                ci.cancel();
            } else {
                this.returnTimer = 1;
                rollable.clearRotation();
            }
            return;
        }
        if (rollable.getOrientation() == null) return;
        applyRotation();

        this.returnTimer = 0;
        this.rollAngleStart = rollable.getRoll();
        ci.cancel();
    }

    @Unique
    private void applyRotation(){
        if (!(this.entity instanceof Rollable rollable) || rollable.getOrientation() == null) return;
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
    }
}
