package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.Screens.GameScreen;
import de.tum.cit.fop.maze.Behaviour.Pet;
import de.tum.cit.fop.maze.GameConstants;
import de.tum.cit.fop.maze.Data.ProfileManager;

/**
 * GetScreen - Interface displaying acquisition of new weapon
 * Similar to PauseScreen, pauses game and displays gray overlay
 */
public class GetScreen implements Screen {
    private final MazeRunnerGame game;
    private final Screen returnScreen;
    private final Stage stage;
    private final SpriteBatch batch;
    private Texture lightsaberTexture;
    private TextureRegion lightsaberRegion;
    private Animation<TextureRegion> petWalkAnim;
    private float petAnimTimer = 0f;

    public GetScreen(MazeRunnerGame game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.batch = game.getSpriteBatch();
        this.stage = new Stage(new ScreenViewport(), batch);
        
        int currentLevel = game.getSelectedLevel();
        
        // 根据关卡加载不同的资源
        if (currentLevel == 4) {
            // 第4关：加载宠物动画
            try {
                Texture petWalkTex = new Texture(Gdx.files.internal("game_assets/Characters/Pet/Walk.png"));
                int frameWidth = petWalkTex.getWidth() / 4;
                int frameHeight = petWalkTex.getHeight();
                TextureRegion[][] tmp = TextureRegion.split(petWalkTex, frameWidth, frameHeight);
                TextureRegion[] frames = new TextureRegion[4];
                for (int i = 0; i < 4 && i < tmp[0].length; i++) {
                    frames[i] = tmp[0][i];
                }
                petWalkAnim = new Animation<>(0.15f, frames);
                petWalkAnim.setPlayMode(Animation.PlayMode.LOOP);
            } catch (Exception e) {
                Gdx.app.error("GET_SCREEN", "Failed to load Pet Walk animation", e);
            }
        } else if (currentLevel == 2) {
            // 第2关：加载武器图标
            try {
                Texture skillTexture = new Texture(Gdx.files.internal("game_assets/UI/Items/Skill1.png"));
                lightsaberRegion = new TextureRegion(skillTexture);
            } catch (Exception e) {
                Gdx.app.error("GET_SCREEN", "Failed to load weapon texture", e);
            }
        }
        
        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // 根据关卡区分：第2关=武器，第3关=staff，第4关=宠物
        int currentLevel = game.getSelectedLevel();
        boolean isWeaponScreen = (currentLevel == 2);
        boolean isStaffScreen = (currentLevel == 3);
        boolean isPetScreen = (currentLevel == 4);

        // First row: Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(
                game.getSkin().getFont("new-font"),
                com.badlogic.gdx.graphics.Color.WHITE
        );
        Label titleLabel;
        if (isWeaponScreen) {
            titleLabel = new Label("Got a new weapon!", titleStyle);
        } else if (isStaffScreen) {
            titleLabel = new Label("Got a new staff!", titleStyle);
        } else if (isPetScreen) {
            titleLabel = new Label("Got a new pet!", titleStyle);
        } else {
            titleLabel = new Label("Got a new item!", titleStyle);
        }
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);
        root.add(titleLabel).padBottom(40f).row();

        // Second row: Animation (will be rendered separately)
        root.add().height(64f).padBottom(40f).row();

        // Third row: Hint text (only for staff in level 3)
        if (isStaffScreen) {
            Label.LabelStyle hintStyle = new Label.LabelStyle(
                    game.getSkin().getFont("new-font"),
                    com.badlogic.gdx.graphics.Color.WHITE
            );
            Label hintLabel = new Label("(IT'S BETTER TO AIM WITH MOUSE!)", hintStyle);
            hintLabel.setFontScale(0.8f);
            hintLabel.setAlignment(Align.center);
            root.add(hintLabel).padBottom(20f).row();
        }

        // Fourth row: Press any key to exit
        Label.LabelStyle continueStyle = new Label.LabelStyle(
                game.getSkin().getFont("new-font"),
                com.badlogic.gdx.graphics.Color.WHITE
        );
        Label continueLabel = new Label("Press any key to exit", continueStyle);
        continueLabel.setFontScale(1.0f);
        continueLabel.setAlignment(Align.center);
        root.add(continueLabel).row();
    }

    private void resumeToGame() {
        if (game.getCurrentMusic() != null && !game.getCurrentMusic().isPlaying()) {
            game.getCurrentMusic().play();
        }
        if (returnScreen instanceof GameScreen) {
            GameScreen gs = (GameScreen) returnScreen;
            gs.skipIntroOnNextShow();
            gs.setPaused(false);
            
            // 只在第4关处理宠物引用（第四关的解锁宠物逻辑）
            // 注意：宠物已在GameScreen中通过type 96创建，这里只是确保引用正确
            int currentLevel = game.getSelectedLevel();
            if (currentLevel == 4 && game.getPet() != null && returnScreen instanceof GameScreen) {
                // 确保GameScreen的pet引用已更新
                ((GameScreen) returnScreen).setPet(game.getPet());
            }
            // 第2关和第3关不处理宠物，只处理武器解锁
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
        // Draw game screen (frozen state)
        if (returnScreen != null) {
            returnScreen.render(delta);
        }

        // Gray overlay
        game.renderDarkOverlay(0.55f);

        petAnimTimer += delta;

        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();

        // 根据关卡渲染不同的动画：第2关=武器，第3关=staff，第4关=宠物
        int currentLevel = game.getSelectedLevel();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float animX = (screenWidth - 64f) / 2f;
        float animY = screenHeight / 2f - 20f; // Position between title and continue text
        
        if (currentLevel == 4 && petWalkAnim != null) {
            // 第4关：显示宠物动画
            TextureRegion currentFrame = petWalkAnim.getKeyFrame(petAnimTimer, true);
            if (currentFrame != null) {
                batch.begin();
                batch.draw(currentFrame, animX, animY, 64f, 64f);
                batch.end();
            }
        } else if (currentLevel == 2 && lightsaberRegion != null) {
            // 第2关：显示武器图标
            batch.begin();
            batch.draw(lightsaberRegion, animX, animY, 64f, 64f);
            batch.end();
        } else if (currentLevel == 3) {
            // 第3关：显示staff图标（使用Skill2.png或类似资源）
            try {
                Texture staffTexture = new Texture(Gdx.files.internal("game_assets/UI/Items/Skill2.png"));
                TextureRegion staffRegion = new TextureRegion(staffTexture);
                batch.begin();
                batch.draw(staffRegion, animX, animY, 64f, 64f);
                batch.end();
            } catch (Exception e) {
                // 如果Skill2.png不存在，尝试使用其他资源或跳过
                Gdx.app.error("GET_SCREEN", "Failed to load staff texture", e);
            }
        }

        // Press any key to continue (check all keys)
        boolean anyKeyPressed = false;
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                anyKeyPressed = true;
                break;
            }
        }
        if (anyKeyPressed || Gdx.input.justTouched()) {
            resumeToGame();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        buildUI(); // Keep layout responsive
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
        // Note: don't dispose lightsaberTexture, as it may be a shared resource
    }
}
