package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GameLeftMessage extends MessageContainer {

    public UUID leftUserId;

    public GameLeftMessage(MessageTypeEnum type, UUID clientId,
                           String date, String debugMessage,
                           UUID leftUserId) {
        super(type, clientId, date, debugMessage);
        this.leftUserId = leftUserId;
    }
}
