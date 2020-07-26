package com.notimetospy.model.NetworkStandard.Messages;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class RequestEquipmentChoiceMessage extends MessageContainer {

    public List<UUID> chosenCharacterIds;
    public List<GadgetEnum> choseGadgets;

    public RequestEquipmentChoiceMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                                         List<UUID> chosenCharacterIds,
                                         List<GadgetEnum> choseGadgets) {
        super(type, clientId, date, debugMessage);
        this.chosenCharacterIds = chosenCharacterIds;
        this.choseGadgets = choseGadgets;
    }
}
