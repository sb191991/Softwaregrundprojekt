package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class ItemChoiceMessage extends MessageContainer {

    public UUID chosenCharacterId;
    public GadgetEnum chosenGadget;

    public ItemChoiceMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                             UUID chosenCharacterId,
                             GadgetEnum chosenGadget) {
        super(type, clientId, date, debugMessage);
        this.chosenCharacterId = chosenCharacterId;
        this.chosenGadget = chosenGadget;
    }
}
