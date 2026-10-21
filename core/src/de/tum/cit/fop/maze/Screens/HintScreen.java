package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class HintScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen returnScreen;
    private final Stage stage;

    public HintScreen(MazeRunnerGame game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;

        stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        // ✅ Freeze gameplay logic while HintScreen is open
        if (returnScreen instanceof GameScreen) {
            ((GameScreen) returnScreen).setPaused(true);
        }

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(12);
        stage.addActor(table);

        int currentLevel = game.getSelectedLevel();

        table.add(new Label("HINT - LEVEL " + currentLevel, game.getSkin(), "label-style")).row();
        table.add(new Label("", game.getSkin(), "label-style")).height(20).row();

        String hintText = getHintText(currentLevel);
        Label hintLabel = new Label(hintText, game.getSkin(), "label-style");
        hintLabel.setWrap(true);
        table.add(hintLabel).width(650).row();

        table.add(new Label("", game.getSkin(), "label-style")).height(20).row();

        // ✅ Fix close button texture by using SAME style as your other UI buttons.
        // If your skin uses "button-style" everywhere (Pause/Main), keep it.
        // If you have a better one (e.g., "menu-button"), swap it here.
        TextButton closeButton = new TextButton("CLOSE", game.getSkin(), "button-style");
        closeButton.getLabel().setFontScale(1.0f);

        table.add(closeButton)
                .width(300f)
                .height(80f)
                .padTop(20f)
                .row();

        closeButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                closeHint();
            }
        });
    }

    /** ✅ Close hint and resume exactly where the game stopped (no restart). */
    private void closeHint() {
        if (returnScreen instanceof GameScreen) {
            GameScreen gs = (GameScreen) returnScreen;
            gs.setPaused(false);
            gs.skipIntroOnNextShow(); // prevents intro/black overlay replay on return
        }
        game.setScreen(returnScreen);
    }

    private String getHintText(int level) {
        switch (level) {
            case 1:
                return "GOAL:\n" +
                        "FIND THE SPARK PLUG TO OPEN ENERGY DOORS, THEN REACH THE EXIT.\n\n" +
                        "TIPS:\n" +
                        "ENERGY DOORS BLOCK SHORTCUTS UNTIL YOU COLLECT THE SPARK PLUG.\n" +
                        "TAKE THE DETOUR FIRST, THEN USE THE OPEN PATHS TO FINISH FAST.";

            case 2:
                return "GOAL:\n" +
                        "FIND THE BATTERY, THEN REACH THE EXIT.\n\n" +
                        "TIPS:\n" +
                        "YOU CAN UNLOCK YOUR FIRST WEAPON SOMEWHERE.\n" +
                        "FOLLOW BRIGHT PATHS TO REACH THE BATTERY.\n" +
                        "MOVING BOMBS PATROL THE AREA — TIME YOUR MOVES.\n" +
                        "AFTER THE BATTERY, DARK TILES (D) BECOME PASSABLE SHORTCUTS.";

            case 3:
                return "GOAL:\n" +
                        "FIND THE KEY, OPEN THE PORTAL, THEN ESCAPE.\n\n" +
                        "TIPS:\n" +
                        "YOUR VISION IS LIMITED — MOVE SLOW AND CHECK CORNERS.\n" +
                        "USE THE ARROW TO TRACK THE PORTAL DIRECTION.\n" +
                        "AVOID TRAPS AND ENEMIES IN THE DARK.";

            case 4:
                return "GOAL:\n" +
                        "GET THE ENGINE, UNLOCK THE PORTAL, THEN REACH THE EXIT.\n\n" +
                        "TIPS:\n" +
                        "A PUZZLE WILL GIVE YOU A RANDOM REWARD (PET / WAND / PISTOL).\n" +
                        "AT SOMEWHERE IN THE LEFT YOU WILL FIND THE WALL KEY.\n" +
                        "AFTER THE WALL, USE THE MINI PORTAL TO TELEPORT.\n" +
                        "GRAB THE FINAL KEY THERE TO UNLOCK THE MAIN PORTAL.";

            case 5:
                return "GOAL:\n" +
                        "DEFEAT THE FINAL BOSS. HE DROPS THE ITEM THAT OPENS THE PORTAL.\n\n" +
                        "TIPS:\n" +
                        "DODGE, WAIT FOR OPENINGS, AND DON’T GET GREEDY.\n" +
                        "ONCE THE ITEM DROPS, PICK IT UP TO UNLOCK THE PORTAL AND LEAVE.";

            default:
                return "NO HINT AVAILABLE";
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Render the game behind hint menu (it is frozen logically by setPaused(true))
        if (returnScreen != null) {
            returnScreen.render(delta);
        }

        // Dark overlay
        game.renderDarkOverlay(0.5f);

        // Hint UI
        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();

        // ESC or H closes (same behavior as CLOSE button)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            closeHint();
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}