package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.Data.PlayerProfile;
import de.tum.cit.fop.maze.Data.ProfileManager;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.Screens.IntroScreen;

/**
 * Name input screen
 * Displayed before game starts, used to input player name and create save file
 */
public class NameInputScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final ProfileManager profileManager;
    private TextField nameTextField;

    public NameInputScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.profileManager = new ProfileManager();
        
        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Prompt label
        Label promptLabel = new Label("Enter your name\n" + "         Hero:", game.getSkin(), "label-style");
        promptLabel.setFontScale(1.5f);
        root.add(promptLabel).padBottom(30).row();

        // Name input field
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = game.getSkin().getFont("new-font");
        textFieldStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        textFieldStyle.background = game.getSkin().getDrawable("button-small");
        
        nameTextField = new TextField("", textFieldStyle);
        nameTextField.setMessageText("Type your name here...");
        nameTextField.setMaxLength(20); // Limit maximum length
        
        root.add(nameTextField).width(400).height(50).padBottom(30).row();

        // Start game button
        TextButton startButton = new TextButton("Start Game", game.getSkin(), "button-style");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                handleStartGame();
            }
        });
        
        root.add(startButton).width(300).height(50).row();
    }

    /**
     * Handle start game logic
     */
    private void handleStartGame() {
        String playerName = nameTextField.getText().trim();
        
        // Check if name is empty
        if (playerName.isEmpty()) {
            Gdx.app.log("NameInputScreen", "Player name cannot be empty");
            // Can add error prompt here, e.g., display a Label
            return;
        }

        try {
            // Create new PlayerProfile
            PlayerProfile profile = new PlayerProfile(playerName);
            
            // Call ProfileManager to save initial save file
            profileManager.saveProfile(profile);
            
            // Set this profile to global game instance
            game.setPlayerProfile(profile);
            
            // Jump to IntroScreen
            game.setSelectedLevel(1);
            game.setScreen(new IntroScreen(game));
        } catch (Exception e) {
            Gdx.app.error("NameInputScreen", "Failed to create save file", e);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // Automatically focus on input field
        stage.setKeyboardFocus(nameTextField);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background (if needed)
        game.renderBackground(delta);

        // Check for Enter key press when input has content
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String playerName = nameTextField.getText().trim();
            if (!playerName.isEmpty()) {
                game.playClickSound();
                handleStartGame();
                return;
            }
        }

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
