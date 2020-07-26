package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.NoTimeToSpy;

public class VideoSettingScreen implements Screen {

    private NoTimeToSpy parent;

    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    private Label videoSettingLabel;

    private SelectBox<String> resolution;

    private TextButton back;

    private Table layoutTable;

    public VideoSettingScreen(NoTimeToSpy parent){
        this.parent = parent;
    }
    @Override
    public void show() {

        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        videoSettingLabel = new Label("Video Settings", NoTimeToSpy.skin);
        videoSettingLabel.setFontScale(4,4);
        videoSettingLabel.setAlignment(Align.center);

         resolution = new SelectBox<String>(NoTimeToSpy.skin);
         resolution.setOrigin(Align.center);
         resolution.setItems("Resolution","first", "second", "third");

        back = new TextButton("Back",NoTimeToSpy.skin);
        back.setSize(300,40);
        back.addListener(new InputListener(){
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                parent.showSettingScreen();
            }
        });

        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        layoutTable.add(videoSettingLabel).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row();
        layoutTable.add(resolution).padBottom(15).minSize(200, 70).colspan(2);
        layoutTable.row().right().bottom();
        layoutTable.add(back).padLeft(800).padTop(290).padBottom(30).minSize(200, 70).colspan(2);


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
