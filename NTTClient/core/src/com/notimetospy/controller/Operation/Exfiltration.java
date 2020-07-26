package com.notimetospy.controller.Operation;

import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;

import java.util.UUID;

public class Exfiltration extends Operation {

    private Point from;

    public Exfiltration(OperationEnum type, boolean successful, Point target, Point from, UUID characterID) {
        super(type, successful, target,characterID);
        this.from = from;
    }

    public String toString() {
        return "Exfiltration von Charakter: ";
    }
}
