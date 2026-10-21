package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;

        var camera = new OrthographicCamera();
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10);
        stage.addActor(table);

        table.add(new Label("Settings", game.getSkin(), "title")).colspan(2).row();

        // Mute textbutton
        final TextButton muteToggle = new TextButton(
                game.isMuted() ? "Unmute" : "Mute",
                game.getSkin()
        );

        muteToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.toggleMute();
                muteToggle.setText(game.isMuted() ? "Unmute" : "Mute");
            }
        });

        table.add(muteToggle).width(300).colspan(2).row();


        // Volume slider (0..1)
        table.add(new Label("Volume", game.getSkin())).left();
        final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        volumeSlider.setValue(game.getVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setVolume(volumeSlider.getValue());
            }
        });
        table.add(volumeSlider).width(350).row();

        // Level selection
        // --- Level selection (no SelectBox needed) ---
        table.add(new Label("Level", game.getSkin())).left();

        final Label levelValueLabel = new Label(String.valueOf(game.getSelectedLevel()), game.getSkin());

        TextButton minusBtn = new TextButton("-", game.getSkin());
        TextButton plusBtn  = new TextButton("+", game.getSkin());

        minusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int lvl = game.getSelectedLevel();
                if (lvl > 1) {
                    game.setSelectedLevel(lvl - 1);
                    levelValueLabel.setText(String.valueOf(game.getSelectedLevel()));
                }
            }
        });

        plusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int lvl = game.getSelectedLevel();
                if (lvl < 5) { // change 5 if you have more levels
                    game.setSelectedLevel(lvl + 1);
                    levelValueLabel.setText(String.valueOf(game.getSelectedLevel()));
                }
            }
        });

// Layout: [-] [level] [+]
        Table levelRow = new Table();
        levelRow.add(minusBtn).width(60);
        levelRow.add(levelValueLabel).padLeft(20).padRight(20);
        levelRow.add(plusBtn).width(60);

        table.add(levelRow).left().row();

        // Back button
        TextButton back = new TextButton("Back", game.getSkin());
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(back).colspan(2).width(300).row();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
