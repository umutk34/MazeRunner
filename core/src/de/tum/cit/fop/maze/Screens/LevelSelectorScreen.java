package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class LevelSelectorScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    private final int MIN_LEVEL = 1;
    private final int MAX_LEVEL = 5; // change if needed

    public LevelSelectorScreen(MazeRunnerGame game) {
        this.game = game;

        OrthographicCamera camera = new OrthographicCamera();
        Viewport viewport         = new ScreenViewport(camera);
        stage                     = new Stage(viewport, game.getSpriteBatch());
        Table root                = new Table();
        // Level controls row
        final Label levelLabel    = new Label("Level: " + game.getSelectedLevel(), game.getSkin());
        TextButton minus          = new TextButton("-", game.getSkin());
        TextButton plus           = new TextButton("+", game.getSkin());
        root.setFillParent(true);
        root.defaults().pad(12);
        stage.addActor(root);

        // Title
        root.add(new Label("Select Level", game.getSkin(), "title")).colspan(3).padBottom(30).row();

        minus.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                int lvl = game.getSelectedLevel();
                if (lvl > MIN_LEVEL) {
                    game.setSelectedLevel(lvl - 1);
                    levelLabel.setText("Level: " + game.getSelectedLevel());
                }
            }
        });

        plus.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                int lvl = game.getSelectedLevel();
                if (lvl < MAX_LEVEL) {
                    game.setSelectedLevel(lvl + 1);
                    levelLabel.setText("Level: " + game.getSelectedLevel());
                }
            }
        });

        root.add(minus).width(80).height(60);
        root.add(levelLabel).padLeft(20).padRight(20);
        root.add(plus).width(80).height(60).row();

        // Buttons row
        TextButton play = new TextButton("Play", game.getSkin());
        TextButton back = new TextButton("Back", game.getSkin());

        // LevelSelectorScreen.java 里的 play 按钮监听器
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 🚩 核心修改：动态拼接地图路径，比如选了 1，就加载 maps/level-1.properties
                int currentLevel = game.getSelectedLevel();
                String path = "maps/level-" + currentLevel + ".properties";

                game.goToGame(path);
            }
        });

        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        root.row().padTop(30);
        root.add(play).width(300).row();
        root.add(back).width(300).row();
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