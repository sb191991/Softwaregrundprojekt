package com.notimetospy.controller.Action;

import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;

import java.util.UUID;

public class GambleAction extends Operation {

    private int stake;

    public GambleAction(OperationEnum type, boolean successful, Point target, UUID characterId, int stake) {
        super(type, successful, target, characterId);
        this.stake = stake;
    }

    /**
     * Diese Methode wird verwendet, um im GameScreen Gamble-Aktionen anzuzeigen. Der spielende Charakter
     * muss noch bestimmt und angezeigt werden
     * @return
     */
    @Override
    public String toString() {
        if (super.isSuccessful()) {
            return "Roulette-Spiel an Tisch " + super.getTarget() + " gewonnen: Einsatz: " + stake + " Chips, Charakter ";
        } else {
            return "Roulette-Spiel an Tisch " + super.getTarget() + " verloren: Einsatz: " + stake + " Chips, Charakter: ";
        }
    }
}
