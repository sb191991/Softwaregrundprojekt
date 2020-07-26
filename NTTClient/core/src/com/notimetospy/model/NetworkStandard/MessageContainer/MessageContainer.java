package com.notimetospy.model.NetworkStandard.MessageContainer;

import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;
import java.util.UUID;

public class MessageContainer {

    public UUID clientId;
    public MessageTypeEnum type;
    public String creationDate;
    public String debugMessage;

    public MessageContainer(MessageTypeEnum type, UUID clientId, String creationDate, String debugMessage){
        this.type = type;
        this.clientId = clientId;
        this.creationDate = creationDate;
        this.debugMessage = debugMessage;
    }

}
