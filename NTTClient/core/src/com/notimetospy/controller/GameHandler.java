package com.notimetospy.controller;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.notimetospy.controller.Action.GadgetAction;
import com.notimetospy.controller.Action.PropertyAction;
import com.notimetospy.controller.Operation.BaseOperation;
import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Character.Character;
import com.notimetospy.model.Character.CharacterInformation;
import com.notimetospy.model.Field.FieldStateEnum;
import com.notimetospy.model.Game.Matchconfig;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.NetworkStandard.GameEnums.*;
import com.notimetospy.model.NetworkStandard.Statistics.StatisticsEntry;
import com.notimetospy.view.ElectionPhaseScreen;
import com.notimetospy.view.EquipmentPhaseScreen;
import com.notimetospy.view.GameScreen;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Diese Klasse verwaltet das Spiel.
 * Hier werden die Werte gespeichert die man von Server bekommt. Die Werte werden hier auch während dem Spelen aktualisiert.
 * Es werden in dieser Klasse auch die Default-Werte gesetzt.
 */
public class GameHandler {
    private NoTimeToSpy parent;

    private ElectionPhaseScreen electionPhaseScreen;
    private EquipmentPhaseScreen equipmentPhaseScreen;
    private GameScreen gameScreen;

    private MessageEmitter messageEmitter;

    //HelloReply Werte
    public UUID clientId;
    public UUID sessionId;
    public FieldStateEnum[][] level;
    public Matchconfig settings;
    public CharacterInformation[] characterInfo;

    public ErrorTypeEnum reason;

    //StatisticsMessage Werte
    public StatisticsEntry[] statistics;
    public UUID winner;
    public VictoryEnum victoryReason;
    public boolean hasReplay;

    //RequestItemChoice
    public List<UUID> offeredCharacters;
    public GadgetEnum[] offeredGadgets;

    //RequestEquipmentChoice
    public UUID[] chosenCharacterIds;
    public GadgetEnum[] chosenGadgets;

    //GameStarted
    public UUID playerOneId;
    public UUID playerTwoId;
    public String playerOneName;
    public String playerTwoName;

    public AtomicBoolean moveAllowed = new AtomicBoolean(false);

    //GameStatus
    public UUID activeCharacter;
    public List<BaseOperation> operations;
    public State state;
    public boolean isGameOver;

    public Set<UUID> allyCharacters = new HashSet<UUID>();
    public Set<UUID> enemyCharacters = new HashSet<UUID>();

    public AtomicBoolean boardUpdated=new AtomicBoolean();

    //Setter für private Attribute: MessageEmitter, NoTimeToSpy, ElectionPhaseScreen, EquipmentPhaseScreen, GameScreen
    public void setMessageEmitter(MessageEmitter messageEmitter) {
        this.messageEmitter = messageEmitter;
    }

    public void setParent(NoTimeToSpy parent) {
        this.parent = parent;
    }

    public void setElectionPhaseScreen(ElectionPhaseScreen electionPhaseScreen) {

        this.electionPhaseScreen = electionPhaseScreen;
    }

    public void setEquipmentPhaseScreen(EquipmentPhaseScreen equipmentPhaseScreen){
        this.equipmentPhaseScreen = equipmentPhaseScreen;
    }

    public void setGameScreen(GameScreen gameScreen){
        this.gameScreen = gameScreen;
    }

    public GameHandler() {
        setDefaultValues();
    }

    public void setDefaultValues() {
        clientId = null;
        sessionId = null;
        level = null;
        settings = null;
        characterInfo = null;

        offeredGadgets = null;
        //offeredCharacters = null;
        chosenCharacterIds = null;
        chosenGadgets = null;

        playerOneId = null;
        playerTwoId = null;
        playerOneName = null;
        playerTwoName = null;
    }

    /**
     * Diese Methode wird aufgerufen wenn die erste Message des Servers angekommen ist. (Hello-Reply Message)
     * Hier werden die Werte abgespeichert die gesendet wurden.
     *
     * @param clientId
     * @param level Spielfeld
     * @param sessionId
     * @param settings
     * @param characterInfo
     */
    public void setValues(UUID clientId, FieldStateEnum[][] level, UUID sessionId,
                          Matchconfig settings, CharacterInformation[] characterInfo) {

        this.clientId = clientId;
        this.level = level;
        this.sessionId = sessionId;
        this.settings = settings;

        this.characterInfo = characterInfo;
    }

    public FieldStateEnum[][] getLevel() {
        return level;
    }

    /**
     * Diese Methode wird aufgerufen wenn der Client die GameStarted Message bekommt.
     * Es werden die Werte die gesendet wurden abgespeichert.
     *
     * Nachdem die Werte verarbeitet wurden wird die Wahlphase angezeigt.
     * @param playerOneId
     * @param playerTwoId
     * @param playerOneName
     * @param playerTwoName
     * @param sessionId
     */
    public void gameStarted(UUID playerOneId, UUID playerTwoId, String playerOneName, String playerTwoName, UUID sessionId) {

        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.sessionId = sessionId;

        parent.showElectionPhaseScreen();
    }
    public void requestGameOperation(UUID activeCharacter){
        this.activeCharacter = activeCharacter;
    }

    /**
     * Diese Methode wird aufgerufen wenn die GameStatus Message beim Client angekommen ist.
     * Hier werden die Werte abgespeichert die vom Server gesendet wurden.
     *
     * Nachdem die Werte verarbeitet wurden wird der GameScreen angezeigt.
     * @param activeCharacter
     * @param operations
     * @param state
     * @param isGameOver
     */
    public void updateGameStatus( UUID activeCharacter, BaseOperation[] operations,
                                 State state, boolean isGameOver){

        if(activeCharacter == null){
            this.activeCharacter = null;
        }else
            this.activeCharacter = activeCharacter;

        if(operations == null) {
            this.operations = null;
        } else {
            this.operations.addAll(Arrays.asList(operations));
            //determineAlliesAndEnemies();
        }
        this.state = state;
        this.isGameOver = isGameOver;
        boardUpdated.set(true);
        gameScreen.updateGameState(this.activeCharacter,this.operations, this.state, this.isGameOver);
        parent.showGameScreen();
    }

    /**
     * Diese Methode sucht den Aktiven Character im State damit man die kompletten Character Informationen hat.
     *
     * @return Aktiver Character
     */
    public Character getActiveCharacter(){

        Character active = null;
            if (activeCharacter != null) {
                Character[] characters = new Character[state.characters.size()];
                state.characters.toArray(characters);
                for (int i = 0; i < characters.length; i++) {
                    if (activeCharacter.equals(characters[i].characterId))
                        active = characters[i];
                }
            }
        return active;
    }

    /**
     * Diese Methode gibt die Coordinaten des übergebenen Characters zurück.
     *
     * @param characterId
     * @return
     */
    public Point getCoordinates(UUID characterId){

        Character[] characters = new Character[state.getCharacters().size()];
        state.getCharacters().toArray(characters);

        Point coordinates = null;

        for(int k = 0; k < characters.length; k++){
            if(characterId.equals(characters[k].characterId)){
                coordinates = characters[k].coordinates;
            }
        }

        return coordinates;
    }

    /**
     * In dieser Methode werden die Charactere der Wahlphase aktualisiert nachdem einer ausgewählt wurde
     *
     * @param offeredCharactersS
     * @throws NullPointerException
     */
    public void setCharacterList(UUID[] offeredCharactersS) throws NullPointerException {
        this.offeredCharacters = Arrays.asList(offeredCharactersS); //UUIDs als Liste

        //CharacterInformation durchgehen um Namen zu den UUIDs rauszufinden
        List<CharacterInformation> offeredCharacterName = new ArrayList<CharacterInformation>();
        for(int i = 0; i < characterInfo.length; i++){
            if(offeredCharacters.indexOf(characterInfo[i].characterId) >= 0){
                offeredCharacterName.add(characterInfo[i]);
            }
        }

        CharacterInformation[] array = new CharacterInformation[offeredCharacterName.size()];
        offeredCharacterName.toArray(array);

        electionPhaseScreen.updateCharacters(array);
    }

    /**
     * Diese Methode gibt die CharacterInformation zurück vom übergebenen Namen.
     *
     * @param name
     * @return
     */
    public CharacterInformation getCharacterInfo(String name){

        List<CharacterInformation> offeredCharacterName = new ArrayList<CharacterInformation>();
        for(int i = 0; i < characterInfo.length; i++){
            if(offeredCharacters.indexOf(characterInfo[i].characterId) >= 0){
                offeredCharacterName.add(characterInfo[i]);
            }
        }

        CharacterInformation[] array = new CharacterInformation[offeredCharacterName.size()];
        offeredCharacterName.toArray(array);

        CharacterInformation character = null;

        for(int k = 0; k < array.length; k++){
            if(array[k].name.equals(name)){
                character = array[k];
            }
        }
        return character;

    }

    /**
     * Diese Methode gibt den Namen der übergeben id zurück
     *
     * @param id
     * @return
     */
    public String getCharacterName(UUID id){

        String name = "";
        List<CharacterInformation> offeredCharacterName = new ArrayList<CharacterInformation>();
        for(int i = 0; i < characterInfo.length; i++){
            if(offeredCharacters.indexOf(characterInfo[i].characterId) >= 0){
                offeredCharacterName.add(characterInfo[i]);
            }
            if(id.equals(characterInfo[i].characterId)){
                name = characterInfo[i].name;
            }
        }

        return name;
    }

    /**
     * Diese Methode gibt den Name der übergebenen id zurück.
     *
     * @param name
     * @return
     */
    public UUID getCharacterId(String name){

        UUID characterId = null;
        for(int i = 0; i < characterInfo.length; i++){
            if(name.equals(characterInfo[i].name)){
                characterId = characterInfo[i].characterId;
            }
        }
        return characterId;
    }

    /**
     * Diese Methode gibt die Character Id zurück vom Übergeben DragAndDrop payload.
     *
     * @param payload
     * @return
     */
    public UUID getCharacterId(DragAndDrop.Payload payload){

        UUID characterId = null;
        for(int i = 0; i < characterInfo.length; i++){
            if(payload.getObject().toString().equals(characterInfo[i].name)){
                characterId = characterInfo[i].characterId;
            }
        }
        return characterId;
    }

    /**
     * Diese Methode gibt das GadgetEnum zurück vom übergebenen Payload.
     *
     * @param payload
     * @return
     */
    public GadgetEnum getGadget(DragAndDrop.Payload payload){

        GadgetEnum gadget = null;
        for(int i = 0; i < GadgetEnum.values().length; i++){
            if(payload.getObject().toString().equals(GadgetEnum.valueOf(payload.getObject().toString()).toString())){
                gadget = GadgetEnum.valueOf(payload.getObject().toString());
            }
        }
        return gadget;
    }

    /**
     * Diese Methode aktualisiert die Werte der Gadgets in der Wahlphase nachdem eins ausgewählt wurde.
     *
     * @param offeredGadgets
     */
    public void setGadgetList(GadgetEnum[] offeredGadgets) {

        this.offeredGadgets = offeredGadgets;

        electionPhaseScreen.updateGadgets(this.offeredGadgets);

    }

    /**
     * In dieser Methode werden die Werte der StatisticsMessage gesetzt.
     *
     * @param stat
     * @param winner
     * @param reason
     * @param hasReplay
     */
    public void setStatistics(StatisticsEntry[] stat, UUID winner, VictoryEnum reason, boolean hasReplay){
        this.statistics = stat;
        this.winner = winner;
        this.victoryReason = reason;
        this.hasReplay = hasReplay;
    }

    /**
     * In dieser Methode werden die Charactere und Gadgets gesetzt die man aus der EquipmentCHoiceMessage bekommt. Um sie in der Aurüstungsphase anzuzeigen.
     *
     * @param chosenCharacters
     * @param chosenGadgets
     */
    public void setEquipmentChoice(UUID [] chosenCharacters, GadgetEnum[] chosenGadgets) {
        this.chosenCharacterIds = chosenCharacters;
        this.chosenGadgets = chosenGadgets;


        equipmentPhaseScreen.updateList(this.chosenCharacterIds, this.chosenGadgets);
    }

    public void setErrorReason(ErrorTypeEnum reason) {
        this.reason = reason;
    }

    public UUID getClientId(){
        return clientId;
    }

    public CharacterInformation[] getOfferedCharacters() {

        List<CharacterInformation> offeredCharacterName = new ArrayList<CharacterInformation>();
        for(int i = 0; i < characterInfo.length; i++){
            if(offeredCharacters.indexOf(characterInfo[i].characterId) >= 0){
                offeredCharacterName.add(characterInfo[i]);
            }
        }

        CharacterInformation[] array = new CharacterInformation[offeredCharacterName.size()];
        offeredCharacterName.toArray(array);

        return array;
    }

    public GadgetEnum[] getOfferedGadgets() {
        return offeredGadgets;
    }

    public void matchConfig() {
        String[] keys = {"Configuration.Matchconfig"};
        messageEmitter.sendRequestMetaInformationMessage(clientId, keys);
    }

    public void initializeAllyCharacters() {
        allyCharacters.addAll(Arrays.asList(chosenCharacterIds));
    }

    /**
     * Diese Methode bestimmt die UUIDs von Charakteren, die der gegnerischen Fraktion angehoeren
     * und fuegt ggt einen NPC, der durch ein Nugget bestochen wurde, den eigenen Charakteren hinzu.
     * Die Basis dafuer sind die gesendeten Operationen aus der GameStatus Message: Wird eine Aktion,
     * die nicht Bewegung oder Maulwuerfelwurf ist, von einem Charakter ausgefuehrt, so muss dieser
     * ein PC sein. Folglich handelt es sich um einen Gegner, falls der Charakter nicht zur eigenen
     * Fraktion gehoert.
     */
    public void determineAlliesAndEnemies () {
        if (operations != null) {
            Iterator itr = operations.iterator();
            while (itr.hasNext()) {
                BaseOperation baseOperation = (BaseOperation) itr.next();
                if (baseOperation.getType().equals(OperationEnum.GADGET_ACTION)) {
                    GadgetAction gadgetAction = (GadgetAction) baseOperation;
                    if (gadgetAction.getGadget().equals(GadgetEnum.NUGGET)
                            && allyCharacters.contains(gadgetAction.getCharacterId())
                            && gadgetAction.isSuccessful()) {
                        allyCharacters.add(gadgetAction.getCharacterId());
                    }
                    if (!gadgetAction.getGadget().equals(GadgetEnum.MOLEDIE)) {
                        if (!(allyCharacters.contains(gadgetAction.getCharacterId()))) {
                            enemyCharacters.add(gadgetAction.getCharacterId());
                        }
                    }
                } else if (baseOperation.getType().equals(OperationEnum.PROPERTY_ACTION)) {
                    PropertyAction propertyAction = (PropertyAction) baseOperation;
                    if (propertyAction.getUsedProperty().equals(PropertyEnum.OBSERVATION)
                            && allyCharacters.contains(propertyAction.getCharacterId())
                            && propertyAction.isEnemy()) {
                        enemyCharacters.add(propertyAction.getCharacterId());
                    }
                    if (!(allyCharacters.contains(propertyAction.getCharacterId()))) {
                        enemyCharacters.add(propertyAction.getCharacterId());
                    }
                } else if (baseOperation.getType().equals(OperationEnum.SPY_ACTION)
                        || baseOperation.getType().equals(OperationEnum.GAMBLE_ACTION)) {
                    Operation operation = (Operation) baseOperation;
                    if (!(allyCharacters.contains(operation.getCharacterId()))) {
                        enemyCharacters.add(operation.getCharacterId());
                    }
                }
            }
        }
    }
}