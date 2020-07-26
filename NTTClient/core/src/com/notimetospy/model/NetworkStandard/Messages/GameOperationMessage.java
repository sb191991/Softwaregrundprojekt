package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GameOperationMessage extends MessageContainer {

    public Operation operation;

    public GameOperationMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage, Operation operation) {
        super(type, clientId, date, debugMessage);
        this.operation = operation;
    }
}
