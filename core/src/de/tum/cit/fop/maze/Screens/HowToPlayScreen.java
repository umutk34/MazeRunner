package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class HowToPlayScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final Screen returnTo;
    // Flag indicating if automatically entered after IntroScreen
    private final boolean fromIntro;

    public HowToPlayScreen(MazeRunnerGame game, Screen returnTo) {
        this.game = game;
        this.returnTo = returnTo;
        this.fromIntro = false;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        buildUI();
    }

    public HowToPlayScreen(MazeRunnerGame game, boolean fromIntro) {
        this.game = game;
        this.returnTo = null;
        this.fromIntro = fromIntro;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        buildUI();
    }
    
    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(12);
        stage.addActor(root);

        root.add(new Label("How To Play", game.getSkin(), "label-style")).padBottom(10).row();

        String text =
                "Move:\n  • W/A/S/D or Arrow Keys\n\n" +
                        "Attack:\n  • SPACE\n\n" +
                        "Switch Weapons:\n  • Q\n\n" +
                        "Pause:\n  • ESC\n\n" +
                        "Zoom:\n  • SHIFT (hold)\n  • M (for the minimap)\n\n" +
                        "Info:\n  • I\n\n" +
                        "Goal:\n  • Find the key, then reach the exit/portal.\n";

        Label body = new Label(text, game.getSkin(), "label-style");
        body.setWrap(true);

        float vw = stage.getViewport().getWorldWidth();
        float w = Math.min(760f, vw * 0.8f);

        root.add(body).width(w).left().padBottom(18).row();

        TextButton back = new TextButton("Continue", game.getSkin(), "button-style");
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                if (fromIntro) {
                    game.setSelectedLevel(1);
                    game.goToGame("maps/level-1.properties");
                } else {
                    if (returnTo != null) game.setScreen(returnTo);
                    else game.goToMenu();
                }
            }
        });

        root.add(back).width(Math.min(360f, vw * 0.45f)).height(60).row();

        // small fade-in
        root.getColor().a = 0f;
        root.moveBy(0, -12);
        root.addAction(Actions.parallel(
                Actions.fadeIn(0.2f, Interpolation.fade),
                Actions.moveBy(0, 12, 0.2f, Interpolation.swingOut)
        ));
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (returnTo != null) game.setScreen(returnTo);
            else game.goToMenu();
            return;
        }

        // No longer clear screen separately, let city background scroll smoothly between interfaces
        game.renderBackground(delta);
        game.renderDarkOverlay(0.55f);

        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { 
        stage.dispose();
    }
}
