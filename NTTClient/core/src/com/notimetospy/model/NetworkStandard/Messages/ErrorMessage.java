package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.GameEnums.ErrorTypeEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class ErrorMessage extends MessageContainer {

    public ErrorTypeEnum reason;


    public ErrorMessage(MessageTypeEnum type, UUID clientId,
                        String date, String debugMessage, ErrorTypeEnum reason) {
        super(type, clientId, date, debugMessage);
        this.reason = reason;
    }
}
