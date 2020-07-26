package com.notimetospy.controller.Operation;

import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;

import java.util.UUID;

public class Movement extends Operation {

    private Point from;

    public Movement(OperationEnum type, boolean successful, Point target, UUID characterId, Point from) {
        super(type, successful, target, characterId);
        this.from = from;
    }

    public String toString() {
        return "Bewegung von Feld " + from.toString() + " auf Feld " + super.getTarget().toString() + " : Charakter ";
    }
}
