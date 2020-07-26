package com.notimetospy.controller;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.google.gson.Gson;
import com.notimetospy.controller.Operation.BaseOperation;
import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.RoleEnum;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;
import com.notimetospy.model.NetworkStandard.Messages.*;
import com.notimetospy.view.GameScreen;
import org.java_websocket.WebSocket;

import java.util.*;

/**
 * Diese Klasse wird verwendet um Nachrichten an den Spielserver zu senden.
 */
public class MessageEmitter {

    Gson gson = new Gson();

    private ConnectionHandler connectionHandler;
    private GameScreen gameScreen;

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public MessageEmitter(){
        connectionHandler = null;
    }

    /**
     * Diese Methode sendet eine bereits als json String formatierte Nachricht an den Spielserver.
     *
     * @param jsonMessage die zu sendende Nachricht im json Format
     */
    private void sendMessage(String jsonMessage){

        WebSocket connection = connectionHandler.getConnection();
        if(connection == null){
            return;
        }
        connection.send(jsonMessage);
    }

    public void sendHelloMessage(String name, RoleEnum role) {
        UUID clientId = connectionHandler.getClientId();
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        HelloMessage hello = new HelloMessage(name,role,clientId, date, debugMessage);
        String jsonMessage = gson.toJson(hello);
        sendMessage(jsonMessage);
    }

    public void sendReconnectMessage(UUID sessionId, UUID clientId)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        ReconnectMessage reconnect = new ReconnectMessage(MessageTypeEnum.RECONNECT,clientId,date,debugMessage,sessionId);
        String jsonMessage = gson.toJson(reconnect);
        sendMessage(jsonMessage);
    }

    public void sendItemChoiceMessage(UUID clientId, UUID chosenCharacterId, GadgetEnum chosenGadget)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        ItemChoiceMessage items = new ItemChoiceMessage(MessageTypeEnum.ITEM_CHOICE, clientId,date,debugMessage,chosenCharacterId,chosenGadget);
        String jsonMessage = gson.toJson(items);
        sendMessage(jsonMessage);
    }

    public void sendEquipmentChoiceMessage(UUID clientId, Map<UUID, Set<GadgetEnum>> equipment)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        EquipmentChoiceMessage message = new EquipmentChoiceMessage(MessageTypeEnum.EQUIPMENT_CHOICE,clientId,date,debugMessage,equipment);
        String jsonMessage = gson.toJson(message);
        sendMessage(jsonMessage);
    }

    public void sendGameOperationMessage(UUID clientId, Operation operation)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        GameOperationMessage operationMessage = new GameOperationMessage(MessageTypeEnum.GAME_OPERATION, clientId,date,debugMessage, operation);
        String jsonMessage = gson.toJson(operationMessage);
        sendMessage(jsonMessage);
    }

    public void sendGameLeaveMessage(UUID clientId)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        GameLeaveMessage gameLeave = new GameLeaveMessage(MessageTypeEnum.GAME_LEAVE, clientId, date,debugMessage);
        String jsonMessage = gson.toJson(gameLeave);
        sendMessage(jsonMessage);
    }

    public void sendRequestPauseMessage(UUID clientId,boolean gamePause)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        RequestGamePauseMessage gamePauseMessage = new RequestGamePauseMessage(MessageTypeEnum.REQUEST_GAME_PAUSE,clientId, date,debugMessage,gamePause);
        String jsonMessage = gson.toJson(gamePauseMessage);
        sendMessage(jsonMessage);
    }

    public void sendRequestMetaInformationMessage(UUID clientId, String[] keys)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        RequestMetaInformationMessage message = new RequestMetaInformationMessage(MessageTypeEnum.REQUEST_META_INFORMATION,clientId, date,debugMessage,keys);
        String jsonMessage = gson.toJson(message);
        sendMessage(jsonMessage);
    }

    public void sendRequestReplayMessage(UUID clientId)  {
        String date = connectionHandler.getDate();
        String debugMessage = connectionHandler.getDebugMessage();
        RequestReplayMessage message = new RequestReplayMessage(MessageTypeEnum.REQUEST_REPLAY,clientId, date,debugMessage);
        String jsonMessage = gson.toJson(message);
        sendMessage(jsonMessage);
    }
}
