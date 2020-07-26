package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.Character.CharacterInformation;
import com.notimetospy.model.Game.Matchconfig;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;
import com.notimetospy.model.Game.Scenario;

import java.util.UUID;

public class HelloReplyMessage extends MessageContainer {

    public UUID sessionId;
    public Scenario level;
    public Matchconfig settings;
    public CharacterInformation[] characterSettings;

    public HelloReplyMessage(MessageTypeEnum type, UUID clientId,
                             String date, String debugMessage,
                             UUID sessionId, Scenario level,
                             Matchconfig settings, CharacterInformation[] characterSettings) {
        super(type, clientId,date, debugMessage);
        this.sessionId = sessionId;
        this.level = level;
        this.settings = settings;
        this.characterSettings = new CharacterInformation[characterSettings.length];
    }
}
