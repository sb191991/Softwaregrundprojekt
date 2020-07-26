package com.notimetospy.controller;

import com.google.gson.*;
import com.notimetospy.controller.Gadget.Cocktail;
import com.notimetospy.controller.Operation.BaseOperation;
import com.notimetospy.model.Character.Character;
import com.notimetospy.model.Character.CharacterDescription;
import com.notimetospy.model.Character.CharacterInformation;
import com.notimetospy.model.Field.FieldStateEnum;
import com.notimetospy.model.Game.Matchconfig;
import com.notimetospy.model.Game.Scenario;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.GameScreenTileStateEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.*;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;
import com.notimetospy.model.NetworkStandard.Messages.*;
import com.notimetospy.model.NetworkStandard.Statistics.StatisticsEntry;
import com.notimetospy.view.ElectionPhaseScreen;
import com.notimetospy.view.EquipmentPhaseScreen;
import com.notimetospy.view.GameMenuScreen;
import com.notimetospy.view.GameScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * In dieser Klasse werden die Messages die der Client vom Server bekommt verwaltet und geparsed.
 */
public class MessageReceiver {

    final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    final ExecutorService executorService = Executors.newFixedThreadPool(1);
    Gson gson = new Gson();
    GsonBuilder builder = new GsonBuilder();

    private NoTimeToSpy parent;
    private ConnectionHandler connectionHandler;
    private GameMenuScreen gameMenuScreen;
    private GameScreen gameScreen;

    private Matchconfig matchconfig;

    private ElectionPhaseScreen electionPhaseScreen;
    private EquipmentPhaseScreen equipmentPhaseScreen;
    private GameHandler gameHandler;

    private boolean first = true;

    public void setParent(NoTimeToSpy parent) {
        this.parent = parent;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void setGameHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void setElectionPhaseScreen(ElectionPhaseScreen electionPhaseScreen) {
        this.electionPhaseScreen = electionPhaseScreen;
    }

    public void setEquipmentPhaseScreen(EquipmentPhaseScreen equipmentPhaseScreen) {
        this.equipmentPhaseScreen = equipmentPhaseScreen;
    }

    public MessageReceiver() {

    }

    public void handleMessage(final String json) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                logger.info("Received message: " + json);
                gson = builder.create();

                //try {

                    MessageContainer message = gson.fromJson(json, MessageContainer.class);
                    MessageTypeEnum messageType = message.type;

                    JsonParser jsonParser = new JsonParser();
                    JsonObject jo = (JsonObject) jsonParser.parse(json);

                    //HelloReply bekommt der Client sobald die Verbindung zum Spielserver steht
                    if (messageType.equals(MessageTypeEnum.HELLO_REPLY)) {

                       JsonArray jsonArray = jo.getAsJsonArray("level");
                       FieldStateEnum[][] level = gson.fromJson(jsonArray, FieldStateEnum[][].class);

                        JsonElement sessionId = jo.getAsJsonObject().getAsJsonPrimitive("sessionId");
                        UUID sessionIds = gson.fromJson(sessionId, UUID.class);

                        JsonElement matchConfig = jo.getAsJsonObject("settings");
                        Matchconfig settings = gson.fromJson(matchConfig, Matchconfig.class);

                        JsonArray characterInfos = jo.getAsJsonArray("characterSettings");
                        CharacterInformation[] characterSettings = gson.fromJson(characterInfos, CharacterInformation[].class);

                        JsonElement clientId = jo.getAsJsonObject().getAsJsonPrimitive("clientId");
                        UUID client = gson.fromJson(clientId, UUID.class);

                        gameHandler.setValues(client, level, sessionIds, settings, characterSettings);
                        gameScreen.initializeGameScreenTiles();
                        //RequestItemChoice bekommt der Client in der Wahlphase. Es sind immer jeweils 3 Gadgets und Charactere enthalten
                        //Sobald einer ausgewählt wurde erhält der Client erneut 3 weitere Charactere oder Gadgets
                    } else if (messageType.equals(MessageTypeEnum.REQUEST_ITEM_CHOICE)) {

                        JsonArray characters = jo.getAsJsonArray("offeredCharacterIds");
                        UUID[] offeredCharacterIds = gson.fromJson(characters, UUID[].class);

                        JsonArray gadgets = jo.getAsJsonArray("offeredGadgets");
                        GadgetEnum[] offeredGadgets = gson.fromJson(gadgets, GadgetEnum[].class);

                        gameHandler.setCharacterList(offeredCharacterIds);
                        gameHandler.setGadgetList(offeredGadgets);

                        //ReqeustEquipmentChoice in dieser Message sind die Charactere und Gadgets enthalten die man in der Whalphase ausgewählt hat
                    } else if (messageType.equals(MessageTypeEnum.REQUEST_EQUIPMENT_CHOICE)) {
                        JsonArray characters = jo.getAsJsonArray("chosenCharacterIds");
                        UUID[] chosenCharacters = gson.fromJson(characters, UUID[].class);

                        JsonArray gadgtes = jo.getAsJsonArray("chosenGadgets");
                        GadgetEnum[] chosenGadgets = gson.fromJson(gadgtes, GadgetEnum[].class);

                        gameHandler.setEquipmentChoice(chosenCharacters,chosenGadgets);
                        parent.showEquipmentPhaseScreen();

                        //GameStarted wird versendet wenn eine Verbindung zu 2 Clients steht
                    } else if (messageType.equals(MessageTypeEnum.GAME_STARTED)) {
                        receiveGameStartedMessage(gson.fromJson(json, GameStartedMessage.class));

                    }else if (messageType.equals(MessageTypeEnum.REQUEST_GAME_OPERATION)){

                        receiveRequestGameOperationMessage(gson.fromJson(json, RequestGameOperationMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.GAME_STATUS)) {

                        JsonElement clientId = jo.get("activeCharacterId");
                        UUID activeCharacterId = null;
                        if(!(clientId instanceof JsonNull)){
                            JsonPrimitive propertyToBeCopied = (JsonPrimitive) clientId;
                            activeCharacterId = gson.fromJson(propertyToBeCopied, UUID.class);
                        }

                        JsonArray baseop = jo.getAsJsonArray("operation");
                        BaseOperation[] operations = gson.fromJson(baseop, BaseOperation[].class);

                        JsonElement stat = jo.getAsJsonObject("state");
                        State state = gson.fromJson(stat, State.class);

                        JsonElement isgameover = jo.getAsJsonObject().getAsJsonPrimitive("isGameOver");
                        Boolean isGameOver = gson.fromJson(isgameover, Boolean.class);
                        if (first) {
                            gameHandler.initializeAllyCharacters();
                            gameHandler.updateGameStatus(activeCharacterId,null, state, isGameOver);
                            first = false;
                        } else
                            gameHandler.updateGameStatus(activeCharacterId,operations,state,isGameOver);


                    } else if (messageType.equals(MessageTypeEnum.STATISTICS)) {

                        JsonArray stat = jo.getAsJsonArray("statistics");
                        StatisticsEntry[] statistics = gson.fromJson(stat, StatisticsEntry[].class);

                        JsonElement win = jo.getAsJsonPrimitive("winner");
                        UUID winner = gson.fromJson(win, UUID.class);

                        JsonElement reasonWinner = jo.getAsJsonPrimitive("reason");
                        VictoryEnum reason = gson.fromJson(reasonWinner, VictoryEnum.class);

                        JsonElement hasReplay = jo.getAsJsonPrimitive("hasReplay");
                        Boolean hasReplayBoolean = gson.fromJson(hasReplay, Boolean.class);

                        gameHandler.setStatistics(statistics,winner, reason,hasReplayBoolean);

                    } else if (messageType.equals(MessageTypeEnum.GAME_LEFT)) {
                        receiveGameLeftMessage(gson.fromJson(json, GameLeftMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.GAME_PAUSE)) {
                        receiveGamePauseMessage(gson.fromJson(json, GamePauseMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.META_INFORMATION)) {
                        receiveMetaInformationMessage(gson.fromJson(json, MetaInformationMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.STRIKE)) {
                        receiveStrikeMessage(gson.fromJson(json, StrikeMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.ERROR)) {
                        receiveErrorMessage(gson.fromJson(json, ErrorMessage.class));

                    } else if (messageType.equals(MessageTypeEnum.REPLAY)) {
                        receiveReplayMessage(gson.fromJson(json, ReplayMessage.class));
                    }
                /*}catch(JsonSyntaxException jse) {
                    //logger.error("JsonSyntaxException: " + jse.getMessage());
                    System.out.println("Message nicht verarbeitet!");
                }*/
            }
        });
    }


    private void receiveGameStartedMessage(GameStartedMessage message) {

        gameHandler.gameStarted(message.playerOneId, message.playerTwoId, message.playerOneName, message.playerTwoName, message.sessionId);
    }


    private void receiveRequestGameOperationMessage(RequestGameOperationMessage message) {
       UUID characterId = message.characterId;
       gameHandler.requestGameOperation(characterId);
       gameScreen.activeCharacter = characterId;
       Iterator itr = gameHandler.state.getCharacters().iterator();
       while(itr.hasNext()) {
           Character character = (Character) itr.next();
           if (character.characterId.equals(characterId)) {
               int x = character.coordinates.getX(), y = character.coordinates.getY();
               if (character.hasCocktail()) {
                   gameScreen.gameScreenTiles[x][y].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL);
               } else {
                   gameScreen.gameScreenTiles[x][y].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER);
               }
               gameScreen.setCharacter(character);
           }
       }
       System.out.println(message.characterId);
       gameHandler.moveAllowed.set(true);
    }

    private void receiveGameLeftMessage(GameLeftMessage message) {
        UUID leftUserID = message.leftUserId;
    }

    private void receiveGamePauseMessage(GamePauseMessage message) {
        boolean gamePaused = message.gamePaused;
        boolean serverForced = message.serverEnforced;

    }

    private void receiveMetaInformationMessage(MetaInformationMessage message) {
        Map<String, Object> information = message.information;
        matchconfig.setMatchConfig(information);
    }

    private void receiveStrikeMessage(StrikeMessage message) {
        int strikeNr = message.strikeNr;
        int strikeMax = message.strikeMax;
        String reason = message.reason;
    }

    private void receiveErrorMessage(ErrorMessage message) {
        ErrorTypeEnum reason = message.reason;

        gameHandler.setErrorReason(reason);
    }

    private void receiveReplayMessage(ReplayMessage message) {
        UUID sessionId = message.sessionId;
        String gameStart = message.gameStart;
        String gameEnd = message.gameEnd;
        UUID playerOneId = message.playerOneId;
        UUID playerTwoId = message.playerTwoId;
        int rounds = message.rounds;
        Scenario level = message.level;
        Matchconfig settings = message.settings;
        CharacterDescription[] characterSettings = message.characterSettings;
        MessageContainer messages = message.messages;

    }
}