package com.notimetospy.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.notimetospy.controller.NoTimeToSpy;

public class LevelEditorScreen implements Screen {

    private NoTimeToSpy parent;

    private Stage stage;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    private Table layoutTable;
    private Table bottonTable;

    private Label levelEditorLabel;

    private TextButton loadLevel;
    private TextButton saveLevel;
    private TextButton back;

    public LevelEditorScreen(NoTimeToSpy parent){
        this.parent = parent;
    }
    @Override
    public void show() {

        camera = new OrthographicCamera(1024, 576);
        viewport = new StretchViewport(1024, 576, camera);
        stage = new Stage(viewport);

        levelEditorLabel = new Label("Level Editor", NoTimeToSpy.skin);
        levelEditorLabel.setFontScale(4,4);
        levelEditorLabel.setAlignment(Align.center);

        loadLevel = new TextButton("Load Level", NoTimeToSpy.skin);
        loadLevel.setSize(200,60);

        saveLevel = new TextButton("Save Level", NoTimeToSpy.skin);
        saveLevel.setSize(200,60);

        back = new TextButton("Back", NoTimeToSpy.skin);
        back.setSize(200,60);

        layoutTable = new Table();
        layoutTable.setWidth(stage.getWidth());
        layoutTable.align(Align.top | Align.center);
        layoutTable.setPosition(0, Gdx.graphics.getHeight());
        layoutTable.padTop(30);

        layoutTable.add(levelEditorLabel).padBottom(15).minSize(200, 70).colspan(2);

        bottonTable = new Table();

        bottonTable.add(loadLevel).padTop(100).padLeft(500).padBottom(900).minSize(200,60).colspan(2);
        bottonTable.add(saveLevel).padTop(100).padLeft(1300).padBottom(900).minSize(200,60).colspan(2);
        bottonTable.row().right();
        bottonTable.add(back).minSize(200,60).colspan(2);

        stage.addActor(layoutTable);
        stage.addActor(bottonTable);

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
