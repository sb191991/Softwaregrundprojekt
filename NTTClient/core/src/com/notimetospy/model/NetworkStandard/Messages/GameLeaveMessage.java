package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GameLeaveMessage extends MessageContainer {


    public GameLeaveMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage) {
        super(type, clientId, date, debugMessage);
    }
}
