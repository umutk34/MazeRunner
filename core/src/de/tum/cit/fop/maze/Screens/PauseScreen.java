package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.Data.PlayerProfile;
import de.tum.cit.fop.maze.Data.ProfileManager;
import de.tum.cit.fop.maze.Screens.GameScreen;

public class PauseScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen returnScreen;
    private final Stage stage;
    private final ProfileManager profileManager;

    public PauseScreen(MazeRunnerGame game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.profileManager = new ProfileManager();
        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Panel container (centered)
        Table panel = new Table();
        panel.defaults().pad(8);

        // Background if available in skin
        if (game.getSkin().has("window", Drawable.class)) {
            panel.setBackground(game.getSkin().getDrawable("window"));
        } else if (game.getSkin().has("default-round", Drawable.class)) {
            panel.setBackground(game.getSkin().getDrawable("default-round"));
        }

        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();

        float panelW = Math.min(520f, vw * 0.62f);
        float panelPad = 18f;

        panel.pad(panelPad);
        root.add(panel).width(panelW).center();

        // -------- Header row: OPTIONS + X --------
        Table header = new Table();
        header.defaults().pad(4);

        Label title = new Label("OPTIONS", game.getSkin(), "label-style");
        title.setAlignment(Align.center);

        TextButton close = new TextButton("X", game.getSkin(), "button-style");
        close.getLabel().setAlignment(Align.center);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                resumeToGame();
            }
        });

        header.add(title).expandX().center();
        header.add(close).width(60).height(48).right();

        panel.add(header).growX().padBottom(14).row();

        float btnH = Math.min(70f, vh * 0.08f);
        float btnW = panelW - (panelPad * 2f);

        TextButton resume     = new TextButton("RESUME", game.getSkin(), "button-style");
        TextButton restart    = new TextButton("RESTART", game.getSkin(), "button-style");
        TextButton levels     = new TextButton("LEVELS", game.getSkin(), "button-style");
        TextButton settings   = new TextButton("SETTINGS", game.getSkin(), "button-style");
        TextButton mainMenu   = new TextButton("MAIN MENU", game.getSkin(), "button-style");
        final TextButton fullscreen = new TextButton(
                Gdx.graphics.isFullscreen() ? "FULLSCREEN: ON" : "FULLSCREEN: OFF",
                game.getSkin(), "button-style"
        );
        TextButton quit       = new TextButton("QUIT", game.getSkin(), "button-style");

        float gap = 12f;

        panel.add(resume).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(restart).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(levels).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(settings).width(btnW).height(btnH).padBottom(gap).row();

        // Extra separation so last two don't crowd
        panel.add(mainMenu).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(fullscreen).width(btnW).height(btnH).padBottom(gap + 6).row();
        panel.add(quit).width(btnW).height(btnH).row();

        // -------- Listeners --------
        resume.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                resumeToGame();
            }
        });

        restart.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                int lvl = game.getSelectedLevel();
                game.goToGame(game.levelFileFor(lvl));
            }
        });

        levels.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                game.goToLevelSelector();
            }
        });

        settings.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                game.setReturnScreen(PauseScreen.this);
                game.goToSettings();
            }
        });

        mainMenu.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                saveCurrentProfile();
                game.goToMenu();
            }
        });

        fullscreen.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                game.toggleFullscreen();
                fullscreen.setText(Gdx.graphics.isFullscreen() ? "FULLSCREEN: ON" : "FULLSCREEN: OFF");
            }
        });

        quit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                saveCurrentProfile();
                Gdx.app.exit();
            }
        });
    }

    /**
     * Save current player profile
     * If there is a user with the same name, directly replace the content in the profile (overwrite save)
     */
    private void saveCurrentProfile() {
        if (game.getPlayerProfile() == null) {
            Gdx.app.log("PauseScreen", "No player profile, skip save");
            return;
        }
        
        try {
            PlayerProfile profile = game.getPlayerProfile();
            
            // Save profile (if there is a user with the same name, will directly overwrite)
            profileManager.saveProfile(profile);
            Gdx.app.log("PauseScreen", "Player profile saved: " + profile.getPlayerName() + 
                       ", Score: " + profile.getScore());
        } catch (Exception e) {
            Gdx.app.error("PauseScreen", "Failed to save player profile", e);
        }
    }

    private void resumeToGame() {
        if (game.getCurrentMusic() != null && !game.getCurrentMusic().isPlaying()) {
            game.getCurrentMusic().play();
        }
        if (returnScreen instanceof GameScreen) {
            GameScreen gs = (GameScreen) returnScreen;
            gs.skipIntroOnNextShow();
            gs.setPaused(false);
        }
        game.setScreen(returnScreen);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        if (game.getCurrentMusic() != null && game.getCurrentMusic().isPlaying()) {
            game.getCurrentMusic().pause();
        }
        if (returnScreen instanceof GameScreen) {
            ((GameScreen) returnScreen).setPaused(true);
        }
    }

    @Override
    public void render(float delta) {
        // Draw the game behind (frozen look)
        if (returnScreen != null) returnScreen.render(delta);

        // Dark overlay on top
        game.renderDarkOverlay(0.55f);

        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeToGame();
        }
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        buildUI(); // keeps layout responsive after fullscreen toggle
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
