package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.NoTimeToSpy;

/**
 * Die Klasse GameMenuScreen implementeirt das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 *
 * Diese Klasse dient dazu, das SpielMenu darzustellen.
 * Über das Speilmenu sind folgende Aktionen möglich:
 *
 *      1. Das Spiel fortführen
 *      2. Zu den Einstellungen zu wechseln
 *      3. Das Spiel beenden und zum Hauptmenü zurück zukehren
 */
public class GameMenuScreen implements Screen {

    private NoTimeToSpy parent;

    //Stage und Kamera
    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    //Layout und anordnung
    private Table layoutTable;

    //Label
    private Label pauseMenuLabel;

    //Buttons
    private TextButton continueButton;
    private TextButton settingsButton;
    private TextButton quitButton;

    public GameMenuScreen(NoTimeToSpy parent){
        this.parent = parent;
    }

    @Override
    public void show() {

        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        //Layout Tabelle
        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        //Überschrift
        pauseMenuLabel = new Label("Pause",NoTimeToSpy.skin);
        pauseMenuLabel.setFontScale(4,4);
        pauseMenuLabel.setAlignment(Align.center);

        //Buttons mit InputListener
        continueButton = new TextButton("Continue Game", NoTimeToSpy.skin);
        continueButton.setSize(300,60);
        continueButton.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showGameScreen();
            }
        });

        settingsButton = new TextButton("Settings",NoTimeToSpy.skin);
        settingsButton.setSize(300,60);
        settingsButton.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showSettingScreen();
            }
        });

        quitButton = new TextButton("Quit",NoTimeToSpy.skin);
        quitButton.setSize(300,60);
        quitButton.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showMainMenuScreen();
            }
        });

        //layout und Anordnung
        layoutTable.add(pauseMenuLabel).spaceBottom(25).minSize(200,70).colspan(2);
        layoutTable.row();
        layoutTable.add(continueButton).spaceBottom(15).minSize(200,70).colspan(2);
        layoutTable.row();
        layoutTable.add(settingsButton).spaceBottom(15).minSize(200,70).colspan(2);
        layoutTable.row();
        layoutTable.add(quitButton).minSize(200,70).colspan(2);

        stage.addActor(layoutTable);

        Gdx.input.setInputProcessor(stage);
    }


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
