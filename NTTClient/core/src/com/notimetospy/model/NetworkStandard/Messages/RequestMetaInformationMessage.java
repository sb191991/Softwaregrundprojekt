package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class RequestMetaInformationMessage extends MessageContainer {

    public String[] keys;

    public RequestMetaInformationMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                                         String[] keys) {
        super(type, clientId, date, debugMessage);
        this.keys = keys;
    }
}
