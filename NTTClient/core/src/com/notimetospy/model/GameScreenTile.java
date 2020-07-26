package com.notimetospy.model;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.notimetospy.controller.Gadget.Gadget;
import com.notimetospy.model.Character.Character;

public class GameScreenTile {
    private GameScreenTileStateEnum tileState;

    private Gadget gadget;
    private boolean isDestroyed;
    private boolean isInverted;
    private int chipAmount;
    private int safeIndex;
    private boolean isFoggy;
    private Character character;

    public GameScreenTileStateEnum getTileState(){
        return this.tileState;
    }

    public void setTileState (GameScreenTileStateEnum tileState) {
        this.tileState = tileState;
    }

    public GameScreenTile(GameScreenTileStateEnum tileState) {
        this.tileState = tileState;
    }

    public boolean isFoggy() {
        return this.isFoggy;
    }

    public boolean isDestroyed() {
        if (this.tileState.equals(GameScreenTileStateEnum.ROULETTE) || this.tileState.equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
            return this.isDestroyed;
        }
        return false;
    }

    public boolean isInverted() {
        if (this.tileState.equals(GameScreenTileStateEnum.ROULETTE) || this.tileState.equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
            return this.isInverted;
        }
        return false;
    }

    public int getSafeIndex() {
        return this.safeIndex;
    }

    public boolean hasGadget() {
        return (this.gadget != null);
    }

    public Gadget getGadget() {
        if (this.gadget != null) {
            return this.gadget;
        }
        return null;
    }

    public boolean hasCharacter() {
        return (this.character != null);
    }

    public Character getCharacter() {
        if (this.character != null) {
            return this.character;
        }
        return null;
    }

    public int getChipAmount() {
        return this.chipAmount;
    }

    public void setGadget(Gadget gadget) {
        this.gadget = gadget;
    }

    public void setDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    public void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    public void setFoggy(boolean isFoggy) {
        this.isFoggy = isFoggy;
    }

    public void setChipAmount(int chipAmount) {
        this.chipAmount = chipAmount;
    }

    public void setSafeIndex(int safeIndex) {
        this.safeIndex = safeIndex;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }
}
