package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class StrikeMessage extends MessageContainer {

    public int strikeNr;
    public int strikeMax;
    public String reason;


    public StrikeMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                         int strikeNr, int strikeMax, String reason) {
        super(type, clientId, date, debugMessage);
        this.strikeNr = strikeNr;
        this.strikeMax = strikeMax;
        this.reason = reason;
    }
}
