package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.GameEnums.RoleEnum;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class HelloMessage extends MessageContainer {

    public String name;
    public RoleEnum role;

    public HelloMessage(String name, RoleEnum role, UUID clientId, String date, String debugMessage) {
        super(MessageTypeEnum.HELLO, clientId, date, debugMessage);
        this.name = name;
        this.role = role;
    }


}
