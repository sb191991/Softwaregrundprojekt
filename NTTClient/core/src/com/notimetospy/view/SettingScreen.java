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
 * Die Klasse SettingScreen implementiert das com.badlogic.gdx.Screen Interface.
 * Somit kann eine Instanz der Klasse als Screen gerendert werden.
 *
 * Die Klasse dient dazu, den SettingsScreen datzustellen.
 * In dieser Klasse sind folgende Aktionen möglich:
 *
 *      1.Wechsel zu den AUdioSettings
 *      2.Wechsel zu den VideoSettings
 *      3.Zurück zum Hauptmenu
 */
public class SettingScreen implements Screen {

    private NoTimeToSpy parent;

    private OrthographicCamera camera;
    private StretchViewport viewport;

    private Stage stage;
    private Table layoutTable;

    private TextButton audio;
    private TextButton video;
    private TextButton back;

    private Label settingLabel;

    public SettingScreen(NoTimeToSpy parent){
        this.parent = parent;

    }

    @Override
    public void show() {

        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        settingLabel = new Label("Settings",NoTimeToSpy.skin);
        settingLabel.setFontScale(4,4);
        settingLabel.setAlignment(Align.center);

        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        audio = new TextButton("Audio",NoTimeToSpy.skin);
        audio.setSize(300,60);
        audio.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showAudioSettingScreen();
            }
        });

        video = new TextButton("Video", NoTimeToSpy.skin);
        video.setSize(300,60);
        video.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showVideoSettingScreen();
            }
        });

        back = new TextButton("Back", NoTimeToSpy.skin);
        back.setSize(300,40);
        back.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showMainMenuScreen();
            }
        });
        layoutTable.add(settingLabel).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(audio).padTop(120).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(video).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row().right().bottom();
        layoutTable.add(back).padLeft(800).padTop(85).padBottom(20).minSize(200, 70).colspan(2);

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
