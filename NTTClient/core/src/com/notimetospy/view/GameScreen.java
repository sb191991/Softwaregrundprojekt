package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.*;
import com.notimetospy.controller.Action.GadgetAction;
import com.notimetospy.controller.Action.GambleAction;
import com.notimetospy.controller.Action.PropertyAction;
import com.notimetospy.controller.Gadget.Cocktail;
import com.notimetospy.controller.Operation.BaseOperation;
import com.notimetospy.controller.Operation.Movement;
import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Character.Character;
import com.notimetospy.model.Field.Field;
import com.notimetospy.model.Field.FieldMap;
import com.notimetospy.model.Field.FieldStateEnum;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.Game.Scenario;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.GameScreenTile;
import com.notimetospy.model.GameScreenTileStateEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.PropertyEnum;

import java.util.*;

/**
 * Die Klasse GameScreen implementeirt das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 * <p>
 * Die Klasse dien dazu, den Spielbildschirm anzuzeigen.
 * Über den GameScreen sind folgende Aktionen möglich:
 * <p>
 * 1. Spiel Menu öffnen
 * 2. Spieler bewegen
 * 3. Spielaktionen durchführen (GadgetAktion, PropertyAktion, CpcktailAktion)
 */
public class GameScreen implements Screen {

    private NoTimeToSpy parent;
    private MessageEmitter messageEmitter;
    private ConnectionHandler connectionHandler;
    private GameHandler gameHandler;
    private State state;

    private java.util.List<BaseOperation> operations;

    //Stage und Kamera
    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    private Table layoutTable;
    private Table buttonTable;

    //InventoryListe
    private ScrollPane inventoryScrollPane;
    public List<GadgetEnum> inventoryGdxList;

    //Überschrift und CharacterInformationen
    private Label inventoryLabel;
    private Label characterInfo;

    //Button
    private TextButton pause;
    private TextButton retire;
    private TextButton selectGadget;
    private TextButton selectBangAndBurn;
    private TextButton selectObservation;
    private TextButton selectSpyAction;
    private TextButton selectMovement;
    private TextButton helpButton;

    private Dialog gambleAction;

    public GameScreenTile[][] gameScreenTiles;

    //der aktive Character, wird bei Erhalt einer RequestGameOperation Message gesetzt
    private Character character;
    public UUID activeCharacter;

    //Texturen
    private Texture player;
    private Texture cat;
    private Texture playerActive;

    //soll gesetzt werden, wenn ein Gadget des aktiven Characters gewaehlt wird
    private GadgetEnum activeGadget = null;

    //soll gesetzt werden, wenn eine der Aktionen Bang-and-Burn oder Observation fuer den aktiven Character gewaehlt wird
    private PropertyEnum selectedAction = null;

    //wird true, falls Spionage als Aktion ausgewaehlt wird
    private boolean spyActionSelected = false;

    //wird true, falls ein Roulette-Tisch angeklickt wird, um dort zu spielen. stake gibt dann die Anzahl
    //der gesetzten Chips an
    private boolean gambleActionSelected = false;
    private int stake = 0;

    private boolean gamePaused = false;
    private boolean isGameOver;
    private Boolean selectedGadget = false;
    public boolean updated = false;

    private Dialog howTo;
    private boolean dialogOpened = false;

    public GameScreen(final NoTimeToSpy parent, final MessageEmitter messageEmitter, final ConnectionHandler connectionHandler,
                      final GameHandler gameHandler) {
        this.parent = parent;
        this.messageEmitter = messageEmitter;
        this.connectionHandler = connectionHandler;
        this.gameHandler = gameHandler;

        //Texturen laden
        player = new Texture("player01.png");
        playerActive = new Texture("activeCharacter.png");

        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        //Überschrift
        inventoryLabel = new Label("Inventory", NoTimeToSpy.skin);
        inventoryLabel.setFontScale(2, 2);

        //Button mit InputListener
        pause = new TextButton("Pause", NoTimeToSpy.skin);
        pause.setSize(300, 60);
        pause.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                gamePaused = true;
                messageEmitter.sendRequestPauseMessage(connectionHandler.getClientId(), gamePaused);
                parent.showGameMenuScreen();
            }
        });
        retire = new TextButton("Retire", NoTimeToSpy.skin);
        retire.setSize(50, 30);
        retire.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()) {
                    characterInfo.clear();

                    Operation retireOperation = new Operation(OperationEnum.RETIRE, true, character.coordinates, character.characterId);
                    messageEmitter.sendGameOperationMessage(connectionHandler.getClientId(), retireOperation);
                    resetTiles();
                    inventoryGdxList.clear();
                    selectedAction = null;
                    selectedGadget = null;
                    spyActionSelected = false;
                }
            }
        });
        selectGadget = new TextButton("Gadget", NoTimeToSpy.skin);
        selectGadget.setSize(50, 30);
        selectGadget.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getAp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (activeGadget == null) {
                    if (inventoryGdxList.getSelected() != null
                            && (inventoryGdxList.getSelected().equals(GadgetEnum.HAIRDRYER)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.MOLEDIE)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.TECHNICOLOUR_PRISM)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.BOWLER_BLADE)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.POISON_PILLS)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.LASER_COMPACT)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.ROCKET_PEN)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.GAS_GLOSS)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.MOTHBALL_POUCH)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.FOG_TIN)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.GRAPPLE)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.JETPACK)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.WIRETAP_WITH_EARPLUGS)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.CHICKEN_FEED)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.NUGGET)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.MIRROR_OF_WILDERNESS)
                            || inventoryGdxList.getSelected().equals(GadgetEnum.COCKTAIL))
                            && character.getAp() > 0) {
                        activeGadget = inventoryGdxList.getSelected();
                        selectedAction = null;
                        spyActionSelected = false;
                        setTargetTiles();
                    }
                }
            }
        });

        selectBangAndBurn = new TextButton("Bang-and-Burn", NoTimeToSpy.skin);
        selectBangAndBurn.setSize(50, 30);
        selectBangAndBurn.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()
                        && character.getProperties().contains(PropertyEnum.BANG_AND_BURN)
                        && character.getAp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getProperties().contains(PropertyEnum.BANG_AND_BURN)) {
                    selectedAction = PropertyEnum.BANG_AND_BURN;
                    activeGadget = null;
                    spyActionSelected = false;
                    setTargetTiles();
                }
            }
        });

        selectObservation = new TextButton("Observation", NoTimeToSpy.skin);
        selectObservation.setSize(50, 30);
        selectObservation.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()
                        && character.getProperties().contains(PropertyEnum.OBSERVATION)
                        && character.getAp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getProperties().contains(PropertyEnum.OBSERVATION)) {
                    selectedAction = PropertyEnum.OBSERVATION;
                    activeGadget = null;
                    spyActionSelected = false;
                    setTargetTiles();
                }
            }
        });

        selectSpyAction = new TextButton("Spy", NoTimeToSpy.skin);
        selectSpyAction.setSize(50, 30);
        selectSpyAction.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getAp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getAp() >= 1) {
                    selectedAction = null;
                    activeGadget = null;
                    spyActionSelected = true;
                    setTargetTiles();
                }
            }
        });

        selectMovement = new TextButton("Move", NoTimeToSpy.skin);
        selectMovement.setSize(50,30);
        selectMovement.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getMp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getMp() >= 1) {
                    selectedAction = null;
                    activeGadget = null;
                    spyActionSelected = false;
                    setMovementTiles();
                }
            }
        });

        //Layout und Anordnung
        layoutTable = new Table(NoTimeToSpy.skin);
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.center);
        layoutTable.padBottom(800);

        buttonTable = new Table(NoTimeToSpy.skin);
        buttonTable.setWidth(stage.getWidth());
        buttonTable.align(Align.center);
        buttonTable.padBottom(800);
        buttonTable.add(helpButton).padLeft(500).padTop(40).minSize(100,50).colspan(1);

        inventoryGdxList = new List<GadgetEnum>(NoTimeToSpy.skin);
        inventoryScrollPane = new ScrollPane(inventoryGdxList);

        layoutTable.add(inventoryLabel).padTop(50).padLeft(500).minSize(200, 60).colspan(2);
        layoutTable.row();
        layoutTable.add(inventoryScrollPane).padLeft(500).minSize(200, 100);
        layoutTable.row();
        layoutTable.add(selectGadget).padLeft(500).minSize(100, 40).colspan(1);
        layoutTable.row();
        layoutTable.add(selectSpyAction).padLeft(500).minSize(100, 40).colspan(1);
        layoutTable.row();
        layoutTable.add(selectBangAndBurn).padLeft(500).minSize(100, 40).colspan(1);
        layoutTable.row();
        layoutTable.add(selectObservation).padLeft(500).minSize(100, 40).colspan(1);
        layoutTable.row();
        layoutTable.add(selectMovement).padLeft(500).minSize(100, 40).colspan(2);
        layoutTable.row();
        layoutTable.add(retire).padLeft(500).minSize(100, 40).colspan(2);
        layoutTable.row();
        layoutTable.add(pause).padLeft(500).minSize(100, 40).colspan(2);

        stage.addActor(layoutTable);
        stage.addActor(buttonTable);
        //howTo.show(stage);

        gambleAction = new Dialog("Gamble", NoTimeToSpy.skin);
        final TextField input = new TextField("Stake", NoTimeToSpy.skin);
        gambleAction.add(input);
        TextButton confirmGamble = new TextButton("Gamble", NoTimeToSpy.skin);
        confirmGamble.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getAp() >= 1) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get() && character.getAp() >= 1) {
                    int stake_input = 0;
                    boolean validInput = true;
                    try {
                        stake_input = Integer.parseInt(input.getText());
                    } catch (NumberFormatException e) {
                        validInput = false;
                    }
                    if (validInput && stake_input > 0 && stake_input <= character.getChips()) {
                        stake = stake_input;
                        gambleActionSelected = true;
                    }
                }
            }
        });
        gambleAction.button(confirmGamble);
        TextButton quitGamble = new TextButton("Quit", NoTimeToSpy.skin);
        quitGamble.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()) {
                    return true;
                }
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (gameHandler.moveAllowed.get()) {
                    gambleAction.hide();
                }
            }
        });
        gambleAction.button(quitGamble);

        //stage.addActor(gambleAction);
        //gambleAction.hide();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        NoTimeToSpy.batch.begin();
        NoTimeToSpy.batch.draw(NoTimeToSpy.background, 0, 0, 1024, 576);
        NoTimeToSpy.batch.end();

        //Anzeigen des Spielfeldes
        parent.batch.begin();
        if (gameHandler.boardUpdated.getAndSet(false)) {
            FieldStateEnum[][] level = gameHandler.getLevel();

            FieldMap map = state.getMap();
            Field[][] field = map.getMap();
            //int b = field.length;
            //gameScreenTiles = new GameScreenTile[b][b];
            int posx = 96;
            int posy = 576 - 96;
            for (int i = 0; i < gameScreenTiles.length; i++) {
                for (int k = 0; k < gameScreenTiles[i].length; k++) {
                    final Image image = new Image();
                    final int index_x = i;
                    final int index_y = k;
                    image.setPosition(i * 32 + 96, posy - (k * 32));
                    image.setSize(32, 32);
                    //int posx = 96;
                    //int posy = 224;

                    image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei)));

                    if (gameScreenTiles[i][k] != null) {
                        if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.akt_char)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.akt_char_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.akt_char_cocktail)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.akt_char_cocktail_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char_cocktail)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char_cocktail_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.verb_char_cocktail_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char_cocktail)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char_cocktail_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.feind_char_cocktail_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char_cocktail)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char_cocktail_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.neutral_char_cocktail_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.WALL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.wand)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.wand_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_s)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_s_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_s_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_t)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_t_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ROULETTE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.roulette)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.roulette_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.kamin)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.kamin_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.SAFE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.tresor)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.tresor_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_klingenhut)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_klingenhut_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_klingenhut_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_diamanthalsband)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_diamanthalsband_betretbar)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.frei_diamanthalsband_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_t_cocktail)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.bar_t_cocktail_ziel)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.CAT)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.katze)));
                        } else if (gameScreenTiles[i][k].getTileState().equals(GameScreenTileStateEnum.CAT_ACCESSIBLE)) {
                            image.setDrawable(new SpriteDrawable(new Sprite(NoTimeToSpy.katze_betretbar)));
                        }
                    }
                    stage.addActor(image);

                    //Charactere anzeigen lassen
                    Character[] characters = new Character[state.getCharacters().size()];
                    state.getCharacters().toArray(characters);

                    for (int m = 0; m < characters.length; m++) {
                        /*Image characterImage = new Image();
                        characterImage.setSize(32, 32);
                        characterImage.setPosition(characters[m].coordinates.getX() * 32 + 96, posy - (characters[m].coordinates.getY() * 32));*/
                        //set character in gameScreenTiles
                        if ((i >= characters[m].coordinates.getX()) && (k >= characters[m].coordinates.getY()))
                            gameScreenTiles[characters[m].coordinates.getX()][characters[m].coordinates.getY()].setCharacter(characters[m]);

                        //Aktiven Character setzen
                        if (characters[m].getCharacterId().equals(activeCharacter) && gameHandler.moveAllowed.get()) {
                            character = characters[m];/*new Character(characters[m].getCharacterId(),
                                    characters[m].toString(), characters[m].getCoordinates(), characters[m].getMp(),
                                    characters[m].getAp(), characters[m].getHp(), characters[m].getIp(), characters[m].getChips(),
                                    characters[m].getProperties(), characters[m].getGadgets());*/
                            //characterImage.setDrawable(new SpriteDrawable(new Sprite(playerActive)));
                            if (activeGadget == null && selectedAction == null) {
                                setMovementTiles();
                            }
                        }
                    }
                    //Gadgets des Aktiven Characters laden
                    if (character != null && character.getGadgetsAsEnum() != null && gameHandler.moveAllowed.get()) {
                        inventoryGdxList.setItems(character.getGadgetsAsEnum());
                    }
                    if (activeGadget != null) setTargetTiles();

                    //Character Infos des Aktiven Characters anzeigen lassen
                    if(gameHandler.getActiveCharacter() != null) {
                        if (!updated) {
                            characterInfo = new Label(gameHandler.getActiveCharacter().toString(), NoTimeToSpy.skin);
                            updated = true;
                            characterInfo.setPosition(650, 30);
                            characterInfo.setFontScale(1, 1);
                        } else {
                            characterInfo.setText(gameHandler.getActiveCharacter().toString());
                            characterInfo.setPosition(650, 30);
                            characterInfo.setFontScale(1, 1);
                        }
                    }
                    //characterInfo.setPosition(650, 100);
                    //characterInfo.setFontScale(1, 1);

                    image.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (gameHandler.moveAllowed.get()) {
                                //wenn ein Gadget gewaehlt wurde, kann ggf eine Aktion gegen das Zielfeld ausgefuehrt werden
                                if (activeGadget != null) {
                                    if (activeGadget.equals(GadgetEnum.BOWLER_BLADE)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.BOWLER_BLADE, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.MOLEDIE)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.MOLEDIE, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.MOTHBALL_POUCH)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.MOTHBALL_POUCH, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.FOG_TIN)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.FOG_TIN, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.GRAPPLE)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.GRAPPLE, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }//das waren alle Gadgets mit Beschraenkung der Reichweite
                                    } else if (activeGadget.equals(GadgetEnum.JETPACK)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.JETPACK, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.ROCKET_PEN)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.ROCKET_PEN, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.TECHNICOLOUR_PRISM)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.TECHNICOLOUR_PRISM, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.HAIRDRYER)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.HAIRDRYER, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.POISON_PILLS)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.POISON_PILLS, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.LASER_COMPACT)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.LASER_COMPACT, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.GAS_GLOSS)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.GAS_GLOSS, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.CHICKEN_FEED)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.CHICKEN_FEED, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.MIRROR_OF_WILDERNESS)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.MIRROR_OF_WILDERNESS, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.NUGGET)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.NUGGET, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.WIRETAP_WITH_EARPLUGS)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.WIRETAP_WITH_EARPLUGS, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (activeGadget.equals(GadgetEnum.COCKTAIL)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            GadgetAction operation = new GadgetAction(GadgetEnum.COCKTAIL, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    }
                                    //das waren alle benutzbaren Gadgets
                                } else if (selectedAction != null) {//Aktionen Bang-and-Burn, Observation
                                    if (selectedAction.equals(PropertyEnum.BANG_AND_BURN)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            PropertyAction operation = new PropertyAction(PropertyEnum.BANG_AND_BURN, true, OperationEnum.PROPERTY_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    } else if (selectedAction.equals(PropertyEnum.OBSERVATION)) {
                                        if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                                || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                                            Point target = new Point(index_x, index_y);
                                            PropertyAction operation = new PropertyAction(PropertyEnum.OBSERVATION, true, OperationEnum.PROPERTY_ACTION, true, target, activeCharacter);
                                            messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                            resetChoice();
                                            gameHandler.moveAllowed.set(false);
                                        }
                                    }
                                } else {//Spionieren/Tresor spicken, Bewegung, Cocktail aufnehmen und Roulette spielen
                                    if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)) {
                                        Point target = new Point(index_x, index_y);
                                        Operation operation = new Operation(OperationEnum.SPY_ACTION, true, target, activeCharacter);
                                        messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                        resetChoice();
                                        gameHandler.moveAllowed.set(false);
                                    } else if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_ACCESSIBLE)
                                            || gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_ACCESSIBLE)) {
                                        Point from = new Point(character.coordinates.getX(), character.coordinates.getY());
                                        Point target = new Point(index_x, index_y);
                                        Movement operation = new Movement(OperationEnum.MOVEMENT, true, target, activeCharacter, from);
                                        messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                        resetChoice();
                                        gameHandler.moveAllowed.set(false);
                                    } else if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)) {
                                        Point target = new Point(index_x, index_y);
                                        GadgetAction operation = new GadgetAction(GadgetEnum.COCKTAIL, OperationEnum.GADGET_ACTION, true, target, activeCharacter);
                                        messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                        resetChoice();
                                        gameHandler.moveAllowed.set(false);
                                    } else if (gameScreenTiles[index_x][index_y].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
                                        if (!gambleActionSelected) {
                                            gambleAction.show(stage);
                                        } else {
                                            Point target = new Point(index_x, index_y);
                                            if (stake > 0 && stake <= gameScreenTiles[index_x][index_y].getChipAmount()) {
                                                GambleAction operation = new GambleAction(OperationEnum.GAMBLE_ACTION, true, target, activeCharacter, stake);
                                                messageEmitter.sendGameOperationMessage(gameHandler.clientId, operation);
                                                resetChoice();
                                                gameHandler.moveAllowed.set(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });

                    //Katze anzeigen lassen
                    /*cat = new Texture(Gdx.files.internal("cat.png"));
                    Image catImage = new Image();
                    catImage.setSize(32, 32);
                    catImage.setPosition(state.getCatCoordinates().getX() * 32 + posx, posy - (state.getCatCoordinates().getY() * 32));
                    catImage.setDrawable(new SpriteDrawable(new Sprite(cat)));

                    stage.addActor(catImage);*/
                }

            }

        }
        if (characterInfo != null) {
            stage.addActor(characterInfo);
        }

        parent.batch.end();

        helpButton = new TextButton("Help", NoTimeToSpy.skin);
        helpButton.setSize(100,50);
        helpButton.setPosition(850,20);
        helpButton.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(!dialogOpened) {
                    TextButton next = new TextButton("Finish", NoTimeToSpy.skin);
                    String instructions = "You are now in the GameScreen.\n" +
                            "Your active character is highlighted in yellow. It is shown with a green border on which fields you can go.\n" +
                            "The fields with a red border show an action field which you can use.\n" +
                            "Next to the field is the list with the gadgets for your Character, you can use this if you click on Gadget.\n" +
                            "As soon as you are confronted with an enemy character, this is displayed with a red tie.\n" +
                            "Your own characters are shown with a blue suit.\n" +
                            "If you want to take a break you can click on the break Button";
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
                    howTo.show(stage);
                }
            }
        });

        stage.addActor(helpButton);
        stage.act();
        stage.draw();
    }


    public void setCharacter(Character character) {
        this.character = character;
    }

    public int distanceOfPoints(Point a, Point b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
    }

    /**
     * Diese Methode gibt an, ob zwischen den gegebenen Punkten eine Sichtlinie gemaess den Anforderungen im Lastenheft besteht.
     *
     * @param a            der erste Punkt
     * @param b            der zweite Punkt
     * @param bowler_blade gibt an, ob die Berechnung fuer den Klingenhut durchgefuehrt wird, da hier auch Felder mit Charakteren
     *                     die Sichtlinie blockieren
     * @return true, falls eine Sichtlinie besteht, sonst false
     */
    public boolean isInSight(Point a, Point b, boolean bowler_blade) {
        int x0 = a.getX();
        int y0 = a.getY();
        int x1 = b.getX();
        int y1 = b.getY();
        int inc_x = (int) Math.signum(x1 - x0);
        int inc_y = (int) Math.signum(y1 - y0);
        if (x1 == x0) {
            for (int j = y0 + inc_y; j != y1; j += inc_y) {
                if (gameScreenTiles[x0][j].getTileState().equals(GameScreenTileStateEnum.WALL)
                        || gameScreenTiles[x0][j].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)
                        || gameScreenTiles[x0][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)
                        || gameScreenTiles[x0][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                        || gameScreenTiles[x0][j].isFoggy()
                        || (gameScreenTiles[x0][j].hasCharacter() && bowler_blade)) {
                    return false;
                }
            }
        } else {
            double slope = (y1 - y0) / (x1 - x0);
            int cnt = 0;
            if (Math.abs(slope) > 1) {
                double inv_slope = (x1 - x0) / (y1 - y0);
                for (int j = y0 + inc_y; j != y1; j += inc_y) {
                    cnt++;
                    int i = (int) Math.floor(x0 + cnt * inv_slope * inc_y);
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                            || (gameScreenTiles[i][j].hasCharacter() && bowler_blade)) {
                        return false;
                    }
                }
            } else {
                cnt = 0;
                for (int i = x0 + inc_x; i != x1; i += inc_x) {
                    cnt++;
                    int j = (int) Math.floor(y0 + cnt * slope * inc_x);
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)
                            || (gameScreenTiles[i][j].hasCharacter() && bowler_blade)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Diese Methode entfernt alle Markierungen von den Tiles und wird immer dann aufgerufen, wenn ein Gadget oder eine
     * Aktion ausgewaehlt oder eine solche Auswahl aufgehoben wird. Das ist notwendig, damit Felder, die nun keine
     * gueltigen Ziele mehr sind, auch nicht mehr als solche markiert sind.
     */
    public void resetTiles() {
        for (int i = 0; i < gameScreenTiles.length; i++) {
            for (int j = 0; j < gameScreenTiles[i].length; j++) {
                if (gameScreenTiles[i][j] != null) {
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_SEAT);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FIREPLACE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.SAFE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.SAFE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.WALL);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_ACCESSIBLE)
                            || gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_TARGET)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER);
                    }
                }
            }
        }
    }

    /**
     * Diese Methode wird aufgerufen, wenn keine Aktion und kein Gadget fuer den aktiven Character ausgewaehlt ist.
     * Alle Felder, auf die der Character sich durch Ausgeben von BP bewegen kann, werden entsprechend markiert,
     * ebenso Tresore, die er oeffnen kann und Cocktails, die er aufnehmen kann sowie benachbarte Roulette-Tische,
     * an denen gespielt werden kann (d.h. Character hat Spielchips und der Tisch ist nicht zerstoert worden)
     */
    public void setMovementTiles() {
        resetTiles();
        int mp = character.getMp();
        int ap = character.getAp();
        int x = character.coordinates.getX();
        int y = character.coordinates.getY();
        for (int i = 0; i < gameScreenTiles.length; i++) {
            for (int j = 0; j < gameScreenTiles[i].length; j++) {
                Point target = new Point(i, j);
                int dist = distanceOfPoints(character.coordinates, target);
                if (mp >= 1) {
                    if (dist <= 1 && !(i == character.coordinates.getX() && j == character.coordinates.getY())) {
                        if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_SEAT_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_ACCESSIBLE);
                        } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.CAT)
                                && character.hasDiamondCollar()) {
                            gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.CAT_ACCESSIBLE);
                        }
                    }
                }
                if (ap >= 1 && (dist == 1 || (dist == 2 && character.getProperties().contains(PropertyEnum.FLAPS_AND_SEALS)))) {
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.SAFE)
                            && state.getMySafeCombinations() != null
                            && state.getMySafeCombinations().contains(gameScreenTiles[i][j].getSafeIndex())
                            && !(gameScreenTiles[x][y].isFoggy())) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.SAFE_TARGET);
                    }
                }
                if (ap >= 1 && dist == 1 && !((character.getGadgets()).contains(Cocktail.class))) {
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)
                            && gameScreenTiles[i][j].hasGadget()
                            && gameScreenTiles[i][j].getGadget().gadget.equals(GadgetEnum.COCKTAIL)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                    }
                }
                if (ap >= 1 && dist == 1 && character.getChips() > 0) {
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)
                            && gameScreenTiles[i][j].getChipAmount() > 0
                            && !(gameScreenTiles[i][j].isDestroyed())
                            && !(gameScreenTiles[x][y].isFoggy())) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                    }
                }
            }
        }
        gameHandler.boardUpdated.set(true);
    }

    /**
     * Diese Methode wird aufgerufen, wenn fuer den aktiven Character eine Aktion oder ein Gadget ausgewaehlt wird.
     * Alle gueltigen Zielfelder werden entsprechend markiert.
     */
    public void setTargetTiles() {
        resetTiles();
        int x = character.coordinates.getX();
        int y = character.coordinates.getY();
        Point pos = character.coordinates;
        if (character.getAp() >= 1 && activeGadget != null && !(gameScreenTiles[x][y].isFoggy())) {
            if (activeGadget.equals(GadgetEnum.JETPACK)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                            if (!(gameScreenTiles[i][j].hasCharacter()) && !(gameScreenTiles[i][j].hasGadget())) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.WIRETAP_WITH_EARPLUGS)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.HAIRDRYER)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= 1) {//darf auch auf sich selbst angewendet werden
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)
                                    && gameScreenTiles[i][j].hasCharacter()
                                    && gameScreenTiles[i][j].getCharacter().getProperties().contains(PropertyEnum.CLAMMY_CLOTHES)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.TECHNICOLOUR_PRISM)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)
                                    && !gameScreenTiles[i][j].isDestroyed()) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.POISON_PILLS)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.GAS_GLOSS)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.CHICKEN_FEED)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.NUGGET)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.MIRROR_OF_WILDERNESS)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.MOLEDIE)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= gameHandler.settings.getRange(GadgetEnum.MOLEDIE)
                                && isInSight(pos, target, false)
                                && !(i == x && j == y)) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_SEAT_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.SAFE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.SAFE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FIREPLACE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.BOWLER_BLADE)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        if (!(i == x && j == y)) {
                            Point target = new Point(i, j);
                            if (isInSight(pos, target, true)
                                    && distanceOfPoints(pos, target) <= gameHandler.settings.getRange(GadgetEnum.BOWLER_BLADE)) {
                                if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                                }
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.LASER_COMPACT)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        if (!(i == x && j == y)) {
                            Point target = new Point(i, j);
                            if (isInSight(pos, target, false)) {
                                if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                                }
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.ROCKET_PEN)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        if (!(i == x && j == y)) {
                            Point target = new Point(i, j);
                            if (isInSight(pos, target, false)) {
                                if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_SEAT_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.SAFE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.SAFE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FIREPLACE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.WALL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.WALL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                                } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                    gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                                }
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.MOTHBALL_POUCH)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= gameHandler.settings.getRange(GadgetEnum.MOTHBALL_POUCH)
                                && isInSight(pos, target, false)) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FIREPLACE_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.FOG_TIN)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= gameHandler.settings.getRange(GadgetEnum.FOG_TIN)
                                && isInSight(pos, target, false)) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_SEAT)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_SEAT_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.SAFE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.SAFE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FIREPLACE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FIREPLACE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.GRAPPLE)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= gameHandler.settings.getRange(GadgetEnum.GRAPPLE)
                                && isInSight(pos, target, false)) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_BOWLER_BLADE)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            } else if (activeGadget.equals(GadgetEnum.COCKTAIL)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) <= 1) {
                            //Markierung des eigenen Feldes erlaubt das Schluerfen des Cocktails
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            }
        } else if (character.getAp() >= 1 && selectedAction != null && !(gameScreenTiles[x][y].isFoggy())) {
            if (selectedAction.equals(PropertyEnum.BANG_AND_BURN)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (distanceOfPoints(pos, target) == 1) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ROULETTE)
                                    && !gameScreenTiles[i][j].isDestroyed()) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ROULETTE_TARGET);
                            }
                        }
                    }
                }
            } else if (selectedAction.equals(PropertyEnum.OBSERVATION)) {
                for (int i = 0; i < gameScreenTiles.length; i++) {
                    for (int j = 0; j < gameScreenTiles[i].length; j++) {
                        Point target = new Point(i, j);
                        if (isInSight(pos, target, false)) {
                            if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                            } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                                gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                            }
                        }
                    }
                }
            }
        } else if (spyActionSelected && character.getAp() >= 1 && !(gameScreenTiles[x][y].isFoggy())) {
            for (int i = 0; i < gameScreenTiles.length; i++) {
                for (int j = 0; j < gameScreenTiles[i].length; j++) {
                    if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_TARGET);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL_TARGET);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_TARGET);
                    } else if (gameScreenTiles[i][j].getTileState().equals(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL)) {
                        gameScreenTiles[i][j].setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL_TARGET);
                    }
                }
            }
        }
        gameHandler.boardUpdated.set(true);
    }

    /**
     * Diese Methode wird aufgerufen, wenn eine Aktion durch Senden einer Nachricht an den Server
     * durchgefuehrt wurde. Sie setzt alle Auswahlen zurueck.
     */
    public void resetChoice() {
        this.selectedAction = null;
        this.activeGadget = null;
        this.selectedGadget = false;
        this.activeCharacter = null;
        this.spyActionSelected = false;
        this.gambleActionSelected = false;
        this.stake = 0;
        this.character = null;
        this.inventoryGdxList.clearItems();
        resetTiles();
        gameHandler.boardUpdated.set(true);
    }

    /**
     * Diese Methode wird aufgerufen, sobald der Client eine GameStatusMessage empfaengt. Sie aktualisiert alle Daten,
     * die notwendig sind, um die Map zu zeichnen.
     *
     * @param operations die durchgefuehrten Operationen seit der letzten GameStatusMessage
     * @param state      der State des Spiels, siehe Klasse State
     * @param isGameOver gibt an, ob das Spiel beendet wurde
     */
    public void updateGameState(UUID activeCharacter, java.util.List<BaseOperation> operations, State state, boolean isGameOver) {
        this.activeCharacter = activeCharacter;
        this.operations = operations;
        this.state = state;
        this.isGameOver = isGameOver;
        updateMap();
    }

    /**
     * Diese Methode wird von updateGameState() aufgerufen, nachdem die fuer die Map notwendigen Daten aktualisiert wurden.
     * Sie uebertraegt die FieldMap aus dem GameState in gameScreenTiles zusammen mit allen notwendigen Informationen.
     */
    public void updateMap() {
        FieldMap fieldMap = state.getMap();
        Field[][] map = fieldMap.getMap();
        for (int j = 0; j < gameScreenTiles.length; j++) {
            for (int i = 0; i < gameScreenTiles[j].length; i++) {
                GameScreenTile tile = new GameScreenTile(GameScreenTileStateEnum.FREE);
                tile.setFoggy(map[i][j].isFoggy());
                tile.setGadget(map[i][j].getGadget());
                Set<Character> characters = state.getCharacters();
                Iterator itr = characters.iterator();
                while (itr.hasNext()) {
                    Character character = (Character) itr.next();
                    if (character.coordinates.getX() == j && character.coordinates.getY() == i) {
                        tile.setCharacter(character);
                    }
                }
                if (tile.hasCharacter()) {
                    if (tile.getCharacter().characterId.equals(activeCharacter)) {
                        if (tile.getCharacter().hasCocktail()) {
                            tile.setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER_COCKTAIL);
                        } else {
                            tile.setTileState(GameScreenTileStateEnum.ACTIVE_CHARACTER);
                        }
                    }
                    if (gameHandler.allyCharacters != null && gameHandler.allyCharacters.contains(tile.getCharacter().characterId)) {
                        if (tile.getCharacter().hasCocktail()) {
                            tile.setTileState(GameScreenTileStateEnum.ALLY_CHARACTER_COCKTAIL);
                        } else {
                            tile.setTileState(GameScreenTileStateEnum.ALLY_CHARACTER);
                        }
                    } else if (gameHandler.enemyCharacters != null && gameHandler.enemyCharacters.contains(tile.getCharacter().characterId)) {
                        if (tile.getCharacter().hasCocktail()) {
                            tile.setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER_COCKTAIL);
                        } else {
                            tile.setTileState(GameScreenTileStateEnum.ENEMY_CHARACTER);
                        }
                    } else {
                        if (tile.getCharacter().hasCocktail()) {
                            tile.setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER_COCKTAIL);
                        } else {
                            tile.setTileState(GameScreenTileStateEnum.NEUTRAL_CHARACTER);
                        }
                    }
                } else if (i == state.getCatCoordinates().getY() && j == state.getCatCoordinates().getX()) {
                    tile.setTileState(GameScreenTileStateEnum.CAT);
                } else if (map[i][j].getState().equals(FieldStateEnum.BAR_SEAT)) {
                    tile.setTileState(GameScreenTileStateEnum.BAR_SEAT);
                } else if (map[i][j].getState().equals(FieldStateEnum.BAR_TABLE)) {
                    if (tile.hasGadget() && tile.getGadget().gadget.equals(GadgetEnum.COCKTAIL)) {
                        tile.setTileState(GameScreenTileStateEnum.BAR_TABLE_COCKTAIL);
                    } else {
                        tile.setTileState(GameScreenTileStateEnum.BAR_TABLE);
                    }
                } else if (map[i][j].getState().equals(FieldStateEnum.ROULETTE_TABLE)) {
                    tile.setTileState(GameScreenTileStateEnum.ROULETTE);
                    tile.setDestroyed(map[i][j].isDestroyed());
                    tile.setInverted(map[i][j].isInverted());
                    tile.setChipAmount(map[i][j].getChipAmount());
                } else if (map[i][j].getState().equals(FieldStateEnum.SAFE)) {
                    tile.setTileState(GameScreenTileStateEnum.SAFE);
                    tile.setSafeIndex(map[i][j].getSafeIndex());
                } else if (map[i][j].getState().equals(FieldStateEnum.WALL)) {
                    tile.setTileState(GameScreenTileStateEnum.WALL);
                } else if (map[i][j].getState().equals(FieldStateEnum.FIREPLACE)) {
                    tile.setTileState(GameScreenTileStateEnum.FIREPLACE);
                } else if (map[i][j].getState().equals(FieldStateEnum.FREE)) {
                    if (tile.hasGadget()) {
                        if (tile.getGadget().gadget.equals(GadgetEnum.BOWLER_BLADE)) {
                            tile.setTileState(GameScreenTileStateEnum.FREE_BOWLER_BLADE);
                        } else if (tile.getGadget().gadget.equals(GadgetEnum.DIAMOND_COLLAR)) {
                            tile.setTileState(GameScreenTileStateEnum.FREE_DIAMOND_COLLAR);
                        }
                    }
                }
                gameScreenTiles[j][i] = tile;
            }
        }
    }

    /**
     * Diese Methode wird einmalig bei Erhalt der HelloReply-Message aufgerufen und uebertraegt das
     * Scenario, das gespielt wird, in gameScreenTiles
     */
    public void initializeGameScreenTiles() {
        FieldStateEnum[][] map = gameHandler.level;
        gameScreenTiles = new GameScreenTile[map[0].length][map.length];
        for (int j = 0; j < gameScreenTiles.length; j++) {
            for (int i = 0; i < gameScreenTiles[j].length; i++) {
                GameScreenTile tile = new GameScreenTile(GameScreenTileStateEnum.FREE);
                tile.setFoggy(false);
                tile.setGadget(null);
                tile.setCharacter(null);
                if (map[i][j].equals(FieldStateEnum.WALL)) {
                    tile.setTileState(GameScreenTileStateEnum.WALL);
                } else if (map[i][j].equals(FieldStateEnum.BAR_TABLE)) {
                    tile.setTileState(GameScreenTileStateEnum.BAR_TABLE);
                } else if (map[i][j].equals(FieldStateEnum.BAR_SEAT)) {
                    tile.setTileState(GameScreenTileStateEnum.BAR_SEAT);
                } else if (map[i][j].equals(FieldStateEnum.FIREPLACE)) {
                    tile.setTileState(GameScreenTileStateEnum.FIREPLACE);
                } else if (map[i][j].equals(FieldStateEnum.ROULETTE_TABLE)) {
                    tile.setTileState(GameScreenTileStateEnum.ROULETTE);
                    tile.setInverted(false);
                    tile.setDestroyed(false);
                } else if (map[i][j].equals(FieldStateEnum.SAFE)) {
                    tile.setTileState(GameScreenTileStateEnum.SAFE);
                }
                gameScreenTiles[j][i] = tile;
            }
        }
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
