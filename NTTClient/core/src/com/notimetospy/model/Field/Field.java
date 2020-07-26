package com.notimetospy.model.Field;

import com.notimetospy.controller.Gadget.Gadget;

public class Field {

    private FieldStateEnum state;
    private Gadget gadget;

    private boolean isDestroyed;
    private boolean isInverted;
    private int chipAmount;
    private int safeIndex;

    private boolean isFoggy;
    private boolean isUpdated;

    public Field(FieldStateEnum state, Gadget gadget, boolean isDestroyed,
                 boolean isInverted, int chipAmount, int safeIndex, boolean isFoggy,
                 boolean isUpdated) {
        this.state = state;
        this.gadget = gadget;
        this.isDestroyed = isDestroyed;
        this.isInverted = isInverted;
        this.chipAmount = chipAmount;
        this.safeIndex = safeIndex;
        this.isFoggy = isFoggy;
        this.isUpdated = isUpdated;
    }

    public FieldStateEnum getState() {
        return state;
    }

    public Gadget getGadget() {
        return gadget;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public int getChipAmount() {
        return chipAmount;
    }

    public int getSafeIndex() {
        return safeIndex;
    }

    public boolean isFoggy() {
        return isFoggy;
    }

    public boolean isUpdated() {
        return isUpdated;
    }
}
