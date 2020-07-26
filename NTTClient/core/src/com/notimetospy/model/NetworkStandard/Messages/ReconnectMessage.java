package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class ReconnectMessage extends MessageContainer {

    public UUID sessionId;

    public ReconnectMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage, UUID sessionId) {
        super(type, clientId, date, debugMessage);
        this.sessionId = sessionId;
    }
}
