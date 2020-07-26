package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class RequestGamePauseMessage extends MessageContainer {

    public boolean gamePause;

    public RequestGamePauseMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage, boolean gamePause) {
        super(type, clientId, date, debugMessage);
        this.gamePause = gamePause;
    }
}
