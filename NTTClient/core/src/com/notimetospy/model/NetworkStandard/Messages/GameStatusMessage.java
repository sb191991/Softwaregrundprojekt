package com.notimetospy.model.NetworkStandard.Messages;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.notimetospy.controller.Operation.BaseOperation;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class GameStatusMessage extends MessageContainer {

    public UUID activeCharacterId;
    public List<BaseOperation> operations;
    public State state;
    public boolean isGameOver;


    public GameStatusMessage(MessageTypeEnum type, UUID clientId,
                             String date, String debugMessage,
                             UUID activeCharacterId, List<BaseOperation> operations,
                             State state, boolean isGameOver) {
        super(type, clientId, date, debugMessage);
        this.activeCharacterId = activeCharacterId;
        this.operations = operations;
        this.state = state;
        this.isGameOver = isGameOver;
    }
}
