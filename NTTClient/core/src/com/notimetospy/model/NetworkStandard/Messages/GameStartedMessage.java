package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GameStartedMessage extends MessageContainer {

    public UUID playerOneId;
    public UUID playerTwoId;
    public String playerOneName;
    public String playerTwoName;
    public UUID sessionId;

    public GameStartedMessage(MessageTypeEnum type, UUID clientId,
                              String date, String debugMessage,
                              UUID playerOneId, UUID playerTwoId,
                              String playerOneName, String playerTwoName,
                              UUID sessionId) {
        super(type, clientId, date, debugMessage);
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.sessionId = sessionId;
    }
}
