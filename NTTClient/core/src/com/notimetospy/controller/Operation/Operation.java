package com.notimetospy.controller.Operation;

import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;

import java.util.UUID;

public class Operation extends BaseOperation {

    private UUID characterId;

    public Operation(OperationEnum type, boolean successful, Point target, UUID characterId) {
        super(type, successful, target);
        this.characterId = characterId;
    }

    public UUID getCharacterId() {
        return characterId;
    }
}
