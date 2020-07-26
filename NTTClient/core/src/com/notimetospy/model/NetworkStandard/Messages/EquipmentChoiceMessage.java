package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EquipmentChoiceMessage extends MessageContainer {

    public Map<UUID, Set<GadgetEnum>> equipment;

    public EquipmentChoiceMessage(MessageTypeEnum type, UUID clientId,
                                  String date, String debugMessage,
                                  Map<UUID, Set<GadgetEnum>> equipment) {
        super(type, clientId, date, debugMessage);
        this.equipment = equipment;
    }
}
