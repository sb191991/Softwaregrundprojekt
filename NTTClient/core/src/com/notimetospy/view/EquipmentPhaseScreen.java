package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.*;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;

import java.util.*;

/**
 * Die Klasse EquipmentPhaseScreen implementeirt das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 *
 * Die Klasse dient dazu, die Ausrüstungsphase darzustellen.
 * In dieser Klasse teilt der Spieler seine zuvor ausgewählten Gadgets auf seine Charaktere auf.
 *
 * Das aufteilen der Gadgtes funktioniert wie folgt:
 *
 *       1. Character auswählen
 *       2. Gadgets per Drag and Drop von der InventoryListe in die GadgetListe legen
 *       3. Wenn alle Gadgtes für diesen Character ausgewählt wurden auf den "Next" Button klicken
 *       4. Schritt 1-3 werden so lange wiederholt bis keine Charactere mehr in der Liste sind.
 *       [Falls ein Character kein Gadget bekommen soll dann muss der Character ausgewählt werden und mit einer leeren Gadget Liste auf "Next" klicken.]
 *       5. Als letzten Schritt muss auf den Button "Send Message" geklickt werden.
 *
 * Nach Erhalt der ersten GameStatusMessage wird zum GameScreen gewechselt.
 */
public class EquipmentPhaseScreen implements Screen {

    private NoTimeToSpy parent;
    private MessageReceiver messageReceiver;
    private MessageEmitter messageEmitter;
    private ConnectionHandler connectionHandler;
    private GameHandler gameHandler;

    //Kamera und Stage
    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    //Layout Tabellen
    private Table layoutTable;
    private Table headLinesTable;
    private Table listTable;
    private Table buttonTable;

    //Überschriften
    private Label equipmentLabel;
    private Label characterLabel;
    private Label gadgetLabel;
    private Label inventoryLabel;

    //Buttons
    private TextButton back;
    private TextButton playButton;
    private TextButton finish;

    //Listen
    private ScrollPane characterScrollPane;
    private List<String> characterGdxList;

    private ScrollPane gadgetScrollPane;
    private List<GadgetEnum> gadgetGdxList;

    private ScrollPane inventoryScrollPane;
    private List<GadgetEnum> inventoryGdxList;

    //AUfgeteilten Charactere und Gadgtes
    private Set<GadgetEnum> gadgets = new HashSet<GadgetEnum>();
    Map<UUID, Set<GadgetEnum>> equipment = new HashMap<>();

    private String activeCharacter;

    private Dialog howTo;
    private boolean dialogOpened = false;

    public EquipmentPhaseScreen(final NoTimeToSpy parent, final MessageEmitter messageEmitter, MessageReceiver messageReceiver,
                                final ConnectionHandler connectionHandler, final GameHandler gameHandler) {
        this.parent = parent;
        this.messageEmitter = messageEmitter;
        this.messageReceiver = messageReceiver;
        this.connectionHandler = connectionHandler;
        this.gameHandler = gameHandler;

        //einstellungen für Kamera und Stage
        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        //Instructions Dialog Fenster
        if(!dialogOpened) {
            TextButton next = new TextButton("Finish", NoTimeToSpy.skin);
            String instructions = "You are now in the equipment phase.\n" +
                    "Here you assign the gadgets to your characters.\n" +
                    "For this you choose a charcater from whom you want to assign a gadget. \n" +
                    "Then use drag and drop to move the selected gadget from the inventory list to the gadget list. \n" +
                    "When you have selected all gadgets for this character, click on Next. \n" +
                    "You do this for every character even if you don't want to assign him a gadget." +
                    "When you're done with everything, click send Messsage.";
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

        //Layout Tabellen /Anordnun der UI Elemente
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

        //Listen
        characterGdxList = new List<String>(NoTimeToSpy.skin);
        gadgetGdxList = new List<GadgetEnum>(NoTimeToSpy.skin);
        inventoryGdxList = new List<GadgetEnum>(NoTimeToSpy.skin);

        characterScrollPane = new ScrollPane(characterGdxList);

        gadgetScrollPane = new ScrollPane(gadgetGdxList);

        inventoryScrollPane = new ScrollPane(inventoryGdxList);

        //Labels
        equipmentLabel = new Label("Equipment Phase", NoTimeToSpy.skin);
        equipmentLabel.setFontScale(4, 4);
        equipmentLabel.setAlignment(Align.center);

        characterLabel = new Label("Character", NoTimeToSpy.skin);
        characterLabel.setFontScale(2, 2);

        gadgetLabel = new Label("Gadget", NoTimeToSpy.skin);
        gadgetLabel.setFontScale(2, 2);

        inventoryLabel = new Label("Inventory", NoTimeToSpy.skin);
        inventoryLabel.setFontScale(2, 2);

        activeCharacter = characterGdxList.getSelected();
        dragAndDrop();

        //Buttons mit InputListener
        back = new TextButton("Back", NoTimeToSpy.skin);
        back.setSize(300, 60);
        back.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showElectionPhaseScreen();
            }
        });

        playButton = new TextButton("Send Message", NoTimeToSpy.skin);
        playButton.setSize(300, 60);
        playButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                messageEmitter.sendEquipmentChoiceMessage(gameHandler.getClientId(), equipment);
            }
        });

        finish = new TextButton("Next", NoTimeToSpy.skin);
        finish.setSize(150,60);
        finish.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                equipment.put(getCharacter(), gadgets);
                gadgetGdxList.clearItems();
                characterGdxList.getItems().removeValue(activeCharacter,true);
                gadgets = new HashSet<>();
            }
        });

        layoutTable.add(equipmentLabel).minSize(200, 70).colspan(2);

        headLinesTable.add(characterLabel).padRight(30).minSize(200, 60).colspan(2);
        headLinesTable.add(gadgetLabel).padLeft(50).minSize(200, 60).colspan(2);
        headLinesTable.add(inventoryLabel).padLeft(130).minSize(200, 60).colspan(2);

        listTable.add(characterScrollPane).space(75).minSize(200, 200);
        listTable.add(gadgetScrollPane).space(75).minSize(200, 200);
        listTable.add(inventoryScrollPane).minSize(200, 200);

        buttonTable.add(back).padRight(150).minSize(200, 70).colspan(2);
        buttonTable.add(finish).minSize(100,40).colspan(2);
        buttonTable.add(playButton).padLeft(200).minSize(200, 70).colspan(2);


        stage.addActor(layoutTable);
        stage.addActor(listTable);
        stage.addActor(headLinesTable);
        stage.addActor(buttonTable);
        howTo.show(stage);
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(stage);
    }

    /* Laden der Charactere und Gadgets */

    /**
     * In dieser Methode werden die Listen befüllt.
     * CharacterListe besitzt die ausgewählten Charactere.
     * InventoryList besitzt die ausgewählten Gadgets.
     *
     * @param chosenCharacters ausgewählten Charactere aus der Wahlphase
     * @param chosenGadgets ausgewählte Gadgets aus der Wahlphase
     */
    public void updateList(UUID[] chosenCharacters, GadgetEnum[] chosenGadgets) {
        //inventoryGdxList.clearItems();
        String [] characterName = new String[chosenCharacters.length];
        for(int i = 0; i < chosenCharacters.length; i++){
            String name = gameHandler.getCharacterName(chosenCharacters[i]);
            characterName[i] = name;
        }
        characterGdxList.setItems(characterName);
        inventoryGdxList.setItems(chosenGadgets);
    }

    /**
     * In dieser Methode wird der ausgewählte Character ermittelt dem die Gadgets zugeteilt werden.
     *
     * @return ausgewählter Character
     */
    public UUID getCharacter() {
        activeCharacter = characterGdxList.getSelected();
        UUID chosenCharacter = gameHandler.getCharacterId(characterGdxList.getSelected());
        return chosenCharacter;
    }

    /**
     * Diese Methode implementiert DragANdDrop zum auswählen der Gadgets.
     *
     * Source = InventoryListe
     * Target = GadgetListe
     */
    public void dragAndDrop() {
        //Drag and Drop -> Source :Inventar | Target: Gadget
        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.addSource(new DragAndDrop.Source(inventoryGdxList) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                GadgetEnum item = inventoryGdxList.getSelected();
                payload.setObject(item);
                try {
                    inventoryGdxList.getItems().removeIndex(inventoryGdxList.getSelectedIndex());
                } catch (ArrayIndexOutOfBoundsException e) {
                  e.printStackTrace();
                }
                payload.setDragActor(new Label(item.toString(), NoTimeToSpy.skin));
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null) {
                    GadgetEnum gadget = gameHandler.getGadget(payload);
                    inventoryGdxList.getItems().add(gadget);
                }
            }
        });

        dragAndDrop.addSource(new DragAndDrop.Source(gadgetGdxList) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                GadgetEnum item = inventoryGdxList.getSelected();
                payload.setObject(item);
                gadgetGdxList.getItems().removeIndex(gadgetGdxList.getSelectedIndex());
                payload.setDragActor(new Label(item.toString(), NoTimeToSpy.skin));
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null)
                    gadgetGdxList.getItems().add((GadgetEnum) payload.getObject());
            }
        });

        //Target: GadgetList
        dragAndDrop.addTarget(new DragAndDrop.Target(gadgetGdxList) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                GadgetEnum gadget = gameHandler.getGadget(payload);
                gadgetGdxList.getItems().add(gadget);
                gadgets.add(gadget); //Gadget einfügen in das Gadget Set
                equipment.put(getCharacter(),gadgets); //Character mit ausgewählten Gadgets speichern


            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(inventoryGdxList) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                //Wenn Gadget wieder zurück gelegt wird in die InventoryListe
                GadgetEnum gadget = gameHandler.getGadget(payload);
                inventoryGdxList.getItems().add(gadget);

                gadgets.remove(gadget);

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

    }
}
