package com.notimetospy.controller.Operation;

import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;
import com.notimetospy.model.Game.Point;

public class BaseOperation {

    private OperationEnum type;
    private boolean successful;
    private Point target;

    public BaseOperation(OperationEnum type, boolean successful, Point target) {
        this.type = type;
        this.successful = successful;
        this.target = target;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Point getTarget() {
        return target;
    }

    public OperationEnum getType() {
        return type;
    }
}
