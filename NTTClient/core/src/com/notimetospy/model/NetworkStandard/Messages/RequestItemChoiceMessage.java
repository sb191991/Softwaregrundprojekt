package com.notimetospy.model.NetworkStandard.Messages;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

public class RequestItemChoiceMessage extends MessageContainer {

    public List<UUID> offeredCharacterIds;
    public List<GadgetEnum> offeredGadgets;

    public RequestItemChoiceMessage(MessageTypeEnum type, UUID clientId,
                                    String date, String debugMessage,
                                    List<UUID> offeredCharacterIds,
                                    List<GadgetEnum> offeredGadgets)  {
        super(type, clientId, date, debugMessage);
        this.offeredCharacterIds = offeredCharacterIds;
        this.offeredGadgets = offeredGadgets;
    }
}
