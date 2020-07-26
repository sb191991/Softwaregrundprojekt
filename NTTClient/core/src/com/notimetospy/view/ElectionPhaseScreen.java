package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.*;
import com.notimetospy.model.Character.CharacterInformation;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;

import java.util.UUID;

/**
 * Die Klasse ElectionPhaseScreen implementeirt das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 *
 * Die Klasse dient dazu, die Wahlphase darzustellen.
 * Hier werden folgende Aktionen vom Spieler getätigt:
 *
 *      1. Auswahl der Charactere
 *      2. Auswahl der Gadgets
 *
 * Sobald die RequestEquipmnetChoiceMessage beim Client angekommen ist wird zum EquipmentPhaseScreen gewechselt.
 */
public class ElectionPhaseScreen implements Screen {

    private NoTimeToSpy parent;
    private MessageEmitter messageEmitter;
    private ConnectionHandler connectionHandler;
    private GameHandler gameHandler;
    private MessageReceiver messageReceiver;

    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    //Anordnungs Tabellen
    private Table layoutTable;
    private Table headLinesTable;
    private Table listTable;
    private Table buttonTable;

    //Listen
    private ScrollPane characterScrollPane;
    private List<String> characterGdxList;

    private ScrollPane gadgetScrollPane;
    private List<GadgetEnum> gadgetGdxList;

    private ScrollPane electedScrollPane;
    private List<String> electedGdxList;

    //Überschriften
    private Label electionPhaseLabel;
    private Label characterLabel;
    private Label gadgetLabel;
    private Label electedLabel;
    private Label characterDescription;

    //Buttons
    private TextButton continueButton;
    private TextButton backButton;

    private Dialog howTo;
    private boolean dialogOpened = false;

    public ElectionPhaseScreen(final NoTimeToSpy parent, MessageEmitter messageEmitter,
                               ConnectionHandler connectionHandler, GameHandler gameHandler, MessageReceiver messageReceiver) {
        this.parent = parent;
        this.messageEmitter = messageEmitter;
        this.connectionHandler = connectionHandler;
        this.gameHandler = gameHandler;
        this.messageReceiver = messageReceiver;

        //Kamera und Stage Einstellungen
        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        //Instructions Dialog Fenster
        if(!dialogOpened) {
            TextButton next = new TextButton("Finish", NoTimeToSpy.skin);
            String instructions = "You are now in the election phase.\n" +
                    "You choose gadgets and characters by dragging and dropping them into the respective lists.\n" +
                    "There are exactly 8 slots in your inventory, which means you can select a maximum of 4 characters and 4 gadgets.\n" +
                    "But you can also choose fewer characters for this then several gadgets.";
            Label how = new Label(instructions, NoTimeToSpy.skin);
            howTo = new Dialog("How to No Time To Spy", NoTimeToSpy.skin) {
                protected void result(Object object) {
                    boolean result = (boolean) object;
                    if (result) {
                        dialogOpened = false;
                    } else {
                        dialogOpened = false;
                        return;
                    }
                }
            };

            howTo.getContentTable();
            howTo.getContentTable().add(how);
            howTo.button(next, true);
            howTo.background(new SpriteDrawable(new Sprite(NoTimeToSpy.dialogBackground)));

        }

        //Tabellen für das Layout/Anordnungen
        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        listTable = new Table();
        listTable.setWidth(stage.getWidth());
        listTable.align(Align.center);
        listTable.padBottom(600);

        headLinesTable = new Table();
        headLinesTable.setWidth(stage.getWidth());
        headLinesTable.padBottom(850);

        buttonTable = new Table();
        buttonTable.setWidth(stage.getWidth());
        buttonTable.padBottom(100);

        //Listen für Character/Gadget und ausewählten Items
        characterGdxList = new List<String>(NoTimeToSpy.skin);
        gadgetGdxList = new List<GadgetEnum>(NoTimeToSpy.skin);
        electedGdxList = new List<String>(NoTimeToSpy.skin);

        characterScrollPane = new ScrollPane(characterGdxList);

        gadgetScrollPane = new ScrollPane(gadgetGdxList);

        electedScrollPane = new ScrollPane(electedGdxList);

        //Labels
        electionPhaseLabel = new Label("Election Phase", NoTimeToSpy.skin);
        electionPhaseLabel.setFontScale(4, 4);
        electionPhaseLabel.setAlignment(Align.center);

        characterLabel = new Label("Character", NoTimeToSpy.skin);
        characterLabel.setFontScale(2, 2);

        gadgetLabel = new Label("Gadget", NoTimeToSpy.skin);
        gadgetLabel.setFontScale(2, 2);

        electedLabel = new Label("Elected", NoTimeToSpy.skin);
        electedLabel.setFontScale(2, 2);

        String character ="";
        if(gameHandler.clientId != null) {
            character = gameHandler.getCharacterInfo(characterGdxList.getSelected()).description;
        }
        characterDescription = new Label(character, NoTimeToSpy.skin);
        characterDescription.setFontScale(1,1);

        //Items werden per Drag and Drop ausgewählt
        dragAndDrop();

        //Buttons mit InputListener
        backButton = new TextButton("Back", NoTimeToSpy.skin);
        backButton.setSize(300, 60);
        backButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showMainMenuScreen();
            }
        });

        //layout
        layoutTable.add(electionPhaseLabel).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(characterDescription);

        headLinesTable.add(characterLabel).padRight(30).minSize(200, 60).colspan(2);
        headLinesTable.add(gadgetLabel).padLeft(50).minSize(200, 60).colspan(2);
        headLinesTable.add(electedLabel).padLeft(130).minSize(200, 60).colspan(2);

        listTable.add(characterScrollPane).spaceRight(50).minSize(200, 200);
        listTable.add(gadgetScrollPane).spaceRight(50).minSize(200, 200);
        listTable.add(electedScrollPane).minSize(200, 200);

        buttonTable.add(backButton).padRight(500).minSize(200, 70).colspan(2);

        stage.addActor(headLinesTable);
        stage.addActor(layoutTable);
        stage.addActor(listTable);
        stage.addActor(buttonTable);
        howTo.show(stage);
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * In dieser Methode werden die Items der Character Liste aktualisiert und eingefügt.
     *
     *  @param offeredCharacters Charactere die vom Server bereitgestellt werden
     */
    public void updateCharacters(CharacterInformation[] offeredCharacters) {
        characterGdxList.clearItems();
        String[] names = new String[offeredCharacters.length];
        for(int i = 0; i < offeredCharacters.length; i++){
            names[i] = offeredCharacters[i].name;
        }

        characterGdxList.setItems(names);
    }

    /*Vom Server ausgewählte Gadgets aus der Message lesen */

    /**
     * In dieser Methode werden die Items der Gadget Liste aktualisiert und eingefügt.
     *
     * @param offeredGadgtes Gadgets die vom Server bereitgestellt werden
     */
    public void updateGadgets(GadgetEnum[] offeredGadgtes) {

        gadgetGdxList.setItems(offeredGadgtes);
    }

    /**
     * Diese Methode implementiert DragAndDrop zum Auswählen der Character und Gadgets.
     *
     * Source = CharacterListe und GadgetListe
     * Target = ElectedListe
     */
    public void dragAndDrop() {
        DragAndDrop dragAndDrop = new DragAndDrop();
        //Source = CharacterListe
        dragAndDrop.addSource(new DragAndDrop.Source(characterGdxList) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                String item = characterGdxList.getSelected(); //ausgewählter Character speichern
                payload.setObject(item);
                characterGdxList.getItems().removeIndex(characterGdxList.getSelectedIndex()); // ausgewähltes Objekt aus Liste entfernen
                payload.setDragActor(new Label(item, NoTimeToSpy.skin)); //Name des Characters anzeigen der entnommen wurde
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null){
                    CharacterInformation character = gameHandler.getCharacterInfo(payload.toString());
                    characterGdxList.getItems().add(character.name); //Wenn das Objekt nicht in einer korrekten Position abgesetzt wurde wird es zuück in die ursprüngliche Liste gelegt
                }
            }
        });

        //Source = Gadget Liste
        dragAndDrop.addSource(new DragAndDrop.Source(gadgetGdxList) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                GadgetEnum item = gadgetGdxList.getSelected();
                payload.setObject(item);
                gadgetGdxList.getItems().removeIndex(gadgetGdxList.getSelectedIndex());
                payload.setDragActor(new Label(item.toString(), NoTimeToSpy.skin));
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null){
                    GadgetEnum gadget = gameHandler.getGadget(payload);
                    gadgetGdxList.getItems().add(gadget);
                }

            }
        });

        //Target spezifizieren wo wird das Item abgelegen
        dragAndDrop.addTarget(new DragAndDrop.Target(electedGdxList) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                electedGdxList.getItems().add(payload.getObject().toString());

                //Message mit dem ausgewählten Item senden
                if (payload.getObject().getClass().equals(GadgetEnum.class)) {
                    messageEmitter.sendItemChoiceMessage(gameHandler.getClientId(), null, (GadgetEnum) payload.getObject());
                    updateGadgets(gameHandler.getOfferedGadgets());
                } else if (payload.getObject().getClass().equals(String.class)) {
                    //Nächsten 3 Charactere anzeigen
                    UUID id = gameHandler.getCharacterId(payload);
                    messageEmitter.sendItemChoiceMessage(gameHandler.getClientId(), id, null);
                    updateCharacters(gameHandler.getOfferedCharacters());
                }
            }
        });

    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        NoTimeToSpy.batch.begin();
        NoTimeToSpy.batch.draw(NoTimeToSpy.background, 0, 0, 1024, 576);
        NoTimeToSpy.batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
