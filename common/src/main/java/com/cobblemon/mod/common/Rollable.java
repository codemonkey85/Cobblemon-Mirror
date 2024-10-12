package com.cobblemon.mod.common;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.function.Function;

public interface Rollable {

    Vector3f FORWARDS = new Vector3f(0, 0, 1);
    Vector3f LEFT = new Vector3f(1, 0, 0);
    Vector3f UP = new Vector3f(0, 1, 0);

    @Nullable Matrix3f getOrientation();

    Rollable updateOrientation(Function<Matrix3f, Matrix3f> update);

    /**
     * @return The forward vector (0, 0, 1) after the rotation matrix is applied.
     */
    default Vector3f getForwardVector() {
        if (getOrientation() == null) return new Vector3f(FORWARDS);
        return getOrientation().transform(FORWARDS, new Vector3f());
    }

    /**
     * @return The left vector (1, 0, 0) after the rotation matrix is applied.
     */
    default Vector3f getLeftVector() {
        if (getOrientation() == null) return new Vector3f(LEFT);
        return getOrientation().transform(LEFT, new Vector3f());
    }

    /**
     * @return The up vector (0, 1, 0) after the rotation matrix is applied.
     */
    default Vector3f getUpVector() {
        if (getOrientation() == null) return new Vector3f(UP);
        return getOrientation().transform(UP, new Vector3f());
    }

    /**
     *
     * @param yaw change in degrees
     * @return The Rollable object this method was called on
     */

    default Rollable rotateYaw(float yaw) {
        return updateOrientation(prev -> prev.rotate((float) Math.toRadians(-yaw), UP));
    }

    /**
     *
     * @param pitch change in degrees
     * @return The Rollable object this method was called on
     */

    default Rollable rotatePitch(float pitch) {
        return updateOrientation(prev -> prev.rotate((float) Math.toRadians(-pitch), LEFT));
    }

    /**
     *
     * @param roll change in degrees
     * @return The Rollable object this method was called on
     */
    default Rollable rotateRoll(float roll) {
        return updateOrientation(prev -> prev.rotate((float) Math.toRadians(-roll), FORWARDS));
    }


    /**
     * Order matters when rotating an object. If you need a different order, individually call rotateYaw, rotatePitch, and rotateRoll
     *
     * @param yaw change in degrees
     * @param pitch change in degrees
     * @param roll change in degrees
     * @return The Rollable object this method was called on
     */
    default Rollable rotate(float yaw, float pitch, float roll) {
        return rotateYaw(yaw).rotatePitch(pitch).rotateRoll(roll);
    }


    /**
    * @return The yaw angle in degrees as a float.
    * Value is between [-90, 90] with 0 being straight forward, 90 being straight down, and -90 being straight up
     */
    default float getYaw() {
        return (float) Math.toDegrees(Math.PI - FORWARDS.angleSigned(getForwardVector(), UP));
    }

    /**
     * Get the pitch angle in degrees as a float.
     * Value is between [-180, 180] with 0 being South, 90 being West, 180/-180 being North, and -90 being East
     */
    default float getPitch() {
        return (float) Math.toDegrees(Math.PI/2 - Math.acos(getForwardVector().y));
    }

    /**
     * Get the roll angle in degrees as a float.
     * Value is between [-180, 180] with 0 being no roll (normal), 90 being 90 degrees CW, 180/-180 being 180 degrees CW/CCW, and -90 being 90 degrees CCW
     */
    default float getRoll() {
        return (float) Math.toDegrees(- LEFT.angleSigned(getLeftVector(), FORWARDS));
    }

    boolean shouldRoll();

    void clearRotation();

}
