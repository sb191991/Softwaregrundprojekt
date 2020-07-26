package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.Character.CharacterDescription;
import com.notimetospy.model.Game.Matchconfig;
import com.notimetospy.model.Game.Scenario;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class ReplayMessage extends MessageContainer {

    public UUID sessionId;
    public String gameStart;
    public String gameEnd;
    public UUID playerOneId;
    public UUID playerTwoId;
    public int rounds;
    public Scenario level;
    public Matchconfig settings;
    public CharacterDescription[] characterSettings;
    public MessageContainer messages;

    public ReplayMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                         UUID sessionId, String gameStart, String gameEnd,
                         UUID playerOneId, UUID playerTwoId,
                         int rounds, Scenario level, Matchconfig settings,
                         CharacterDescription[] characterSettings, MessageContainer messages) {
        super(type, clientId, date, debugMessage);
        this.sessionId = sessionId;
        this.gameEnd = gameEnd;
        this.gameStart = gameStart;
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.rounds = rounds;
        this.level = level;
        this.settings = settings;
        this.characterSettings = characterSettings;
        this.messages = messages;
    }
}
