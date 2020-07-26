package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GamePauseMessage extends MessageContainer {

    public boolean gamePaused;
    public boolean serverEnforced;


    public GamePauseMessage(MessageTypeEnum type, UUID clientId,
                            String date, String debugMessage, boolean gamePaused,
                            boolean serverEnforced) {
        super(type, clientId, date, debugMessage);
        this.gamePaused = gamePaused;
        this.serverEnforced = serverEnforced;
    }
}
