package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class MetaInformationMessage extends MessageContainer {

    public Map<String,Object> information;

    public MetaInformationMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                                  Map<String,Object> information) {
        super(type, clientId, date, debugMessage);
        this.information = information;
    }
}
