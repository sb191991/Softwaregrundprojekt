package com.notimetospy.controller.Action;

import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.PropertyEnum;

import java.util.UUID;

public class PropertyAction extends Operation {

    private PropertyEnum usedProperty;
    private boolean isEnemy;

    public PropertyAction(PropertyEnum usedProperty, boolean isEnemy, OperationEnum type, boolean successful, Point target, UUID characterId) {
        super(type, successful, target, characterId);
        this.usedProperty = usedProperty;
        this.isEnemy = isEnemy;
    }

    @Override
    public String toString() {
        if (usedProperty.equals(PropertyEnum.OBSERVATION)) {
            if (super.isSuccessful() || isEnemy) {
                return "Observation erfolgreich: Charakter ";
            } else {
                return "Observation gescheitert: Charakter ";
            }
        } else if (usedProperty.equals(PropertyEnum.BANG_AND_BURN)) {
            return "Roulette-Tisch auf Feld " + super.getTarget() + " mit Bang-and-Burn zerstört: Charakter ";
        }
        return "";
    }

    public PropertyEnum getUsedProperty() {
        return usedProperty;
    }

    public boolean isEnemy() {
        return isEnemy;
    }
}
