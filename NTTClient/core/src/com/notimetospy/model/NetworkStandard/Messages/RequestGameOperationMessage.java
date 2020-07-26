package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class RequestGameOperationMessage extends MessageContainer {

    public UUID characterId;

    public RequestGameOperationMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage, UUID characterId) {
        super(type, clientId, date, debugMessage);
        this.characterId = characterId;
    }
}
