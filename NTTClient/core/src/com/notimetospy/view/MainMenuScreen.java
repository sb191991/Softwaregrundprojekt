package com.notimetospy.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.*;
import com.notimetospy.model.NetworkStandard.GameEnums.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import java.util.concurrent.TimeUnit;

/**
 * Die Klasse MainMenuScreen implementeirt das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 *
 * Die KLasse dient dazu, das Hauptmenu darzustellen
 * Über das Hauptmenü kann der Benutzer folgende Aktionen durchführen:
 *
 *      1. Verbindung zum Spielserver aufbauen
 *      2. Zum Leveleditor wechseln
 *      3. Zu den Einstellungen gelangen
 *      4. Die Anwendung beenden
 *
 * Sobald die Verbindung steht und der zweite Spieler sich verbunden hat wird zur Wahlphase gewechselt.
 */
public class MainMenuScreen implements Screen {

    private final Logger logger = LoggerFactory.getLogger(MainMenuScreen.class);

    private NoTimeToSpy parent;

    private MessageEmitter messageEmitter;
    private ConnectionHandler connectionHandler;
    private MessageReceiver messageReceiver;

    private Stage stage;
    private Table layoutTable;

    //Name
    private Label noTimeToSpyLabel;

    private OrthographicCamera camera;
    private StretchViewport viewport;

    //Buttons
    private TextButton play;
    private TextButton levelEditor;
    private TextButton settings;
    private TextButton quit;

    //InputListener
    private InputListener playListener;
    private InputListener levelListener;
    private InputListener settingsListener;
    private InputListener quitListener;

    private Dialog howTo;
    private boolean dialogOpened = false;

    public MainMenuScreen(final NoTimeToSpy parent,
                          final MessageEmitter messageEmitter,
                          final ConnectionHandler connectionHandler,
                          final MessageReceiver messageReceiver) {
        this.parent = parent;
        this.messageEmitter = messageEmitter;
        this.connectionHandler = connectionHandler;
        this.messageReceiver = messageReceiver;



        //InputListener
        playListener = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            //Eingabe des namen und connection zum server aufbauen
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!dialogOpened) {
                    dialogOpened = true;

                    final TextField nameTextField = new TextField("Name", NoTimeToSpy.skin);
                    final SelectBox<RoleEnum> role = new SelectBox<RoleEnum>(NoTimeToSpy.skin);
                    role.setItems(RoleEnum.PLAYER, RoleEnum.SPECTATOR, RoleEnum.AI);

                    TextButton cancelButton = new TextButton("Cancel", NoTimeToSpy.skin);
                    TextButton connectButton = new TextButton("Connect", NoTimeToSpy.skin);

                    Dialog dialog = new Dialog("WELCOME", NoTimeToSpy.skin) {
                        protected void result(Object object) {
                            boolean result = (boolean) object;
                            if (result) {

                                //Verbindungsaufbau
                                boolean successfullyConnected = false;

                                connectClient: try {

                                    SimpleClient simpleClient = new SimpleClient(
                                            new URI("ws://localhost:7007"),
                                            connectionHandler,
                                            messageReceiver);

                                    successfullyConnected = simpleClient.connectBlocking(10, TimeUnit.SECONDS);

                                    if (!successfullyConnected) {
                                        break connectClient;
                                    }
                                    connectionHandler.setSimpleClient(simpleClient);

                                    successfullyConnected = true;
                                } catch (Exception e){
                                    System.out.println("Exception: " + e.getMessage());
                                }


                                messageEmitter.sendHelloMessage(nameTextField.getText(), role.getSelected());

                                dialogOpened = false;

                            } else {
                                dialogOpened = false;
                                return;
                            }
                        }
                    };
                    dialog.getContentTable();
                    dialog.getContentTable().add(nameTextField);
                    dialog.getContentTable().add(role);
                    dialog.button(cancelButton, false);
                    dialog.button(connectButton, true);
                    dialog.background(new SpriteDrawable(new Sprite(NoTimeToSpy.dialogBackground)));
                    dialog.show(stage);
                }
            }
        };

        levelListener = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showLevelEditorScreen();
            }
        };

        settingsListener = new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showSettingScreen();
            }
        };

        quitListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.exit();
            }
        };
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {

        //Kamera Einstellungen + Stage
        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        //Name
        noTimeToSpyLabel = new Label("No Time to Spy!", NoTimeToSpy.skin);
        noTimeToSpyLabel.setFontScale(5, 5);
        noTimeToSpyLabel.setAlignment(Align.center);
        noTimeToSpyLabel.setColor(Color.WHITE);

        //Instructions Dialog Fenster
        if(!dialogOpened) {
            TextButton next = new TextButton("Finish", NoTimeToSpy.skin);
            String instructions = "Welcome! \n To start the Game you need to click on the Play Button. \n If an opponent has been selected, " +
                    "you will automatically be forwarded to the election phase";
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

        //Buttons
        play = new TextButton("Play", NoTimeToSpy.skin);
        play.setSize(300, 60);
        play.addListener(playListener);

        levelEditor = new TextButton("Leveleditor", NoTimeToSpy.skin);
        levelEditor.setSize(300, 60);
        levelEditor.addListener(levelListener);

        settings = new TextButton("Settings", NoTimeToSpy.skin);
        settings.setSize(300, 60);
        settings.addListener(settingsListener);

        quit = new TextButton("Quit", NoTimeToSpy.skin);
        quit.addListener(quitListener);

        //Anordnung auf dem Screen
        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        layoutTable.add(noTimeToSpyLabel).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(play).padTop(15).padBottom(5).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(levelEditor).padTop(15).padBottom(5).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(settings).padTop(15).padBottom(5).minSize(200, 70).colspan(2);
        layoutTable.row().right().bottom();
        layoutTable.add(quit).minSize(100, 50);


        stage.addActor(layoutTable);
        howTo.show(stage);
        Gdx.input.setInputProcessor(stage);
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

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
    }

    /**
     * @see ApplicationListener#pause()
     */
    @Override
    public void pause() {
    }

    /**
     * @see ApplicationListener#resume()
     */
    @Override
    public void resume() {
    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }
}
