package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;

    // 城市动画
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> cityAnim;
    private float cityTime = 0f;
    private com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.Texture> cityTextures;
    private com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.Texture> cityLayers;
    private float scroll = 0f;

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        Viewport viewport = new ScreenViewport(); // 让 UI 自然铺满
        this.game = game;
        // 🚩 统一视口：和 SplashScreen 保持一致，比例再也不会怪
        stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        // 1. 加载背景 (保持原有逻辑) [cite: 322-325]
        loadCityAnimation();

        // 2. 统一 UI 布局
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 添加标题 [cite: 319-320]
        Label title = new Label("MazeRunner-AutoHaus", game.getSkin(), "title");
        table.add(title).padBottom(50).row();

        // 🚩 统一接口：把所有功能按钮都加进来
        TextButton playBtn = new TextButton("Start Game", game.getSkin());
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { game.goToLevelSelect(); }
        });
        table.add(playBtn).width(250).padBottom(10).row();

        TextButton settingsBtn = new TextButton("Settings", game.getSkin());
        settingsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { game.goToSettings(); }
        });
        table.add(settingsBtn).width(250).padBottom(10).row();

        TextButton exitBtn = new TextButton("Exit", game.getSkin());
        exitBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { Gdx.app.exit(); }
        });
        table.add(exitBtn).width(250).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // 让背景慢慢动
        scroll += delta * 180f;
        // 🚩 获取当前视口真实的物理宽高
        float screenW = stage.getViewport().getWorldWidth();
        float screenH = stage.getViewport().getWorldHeight();

        var batch = stage.getBatch();
        // 关键：用 stage 的投影，否则你 camera.zoom=1.5 时尺寸会“看起来怪”
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        for (int i = 0; i < cityLayers.size; i++) {
            var tex = cityLayers.get(i);
            float speedFactor = 0.05f + i * 0.03f;
            float x = -(scroll * speedFactor) % screenW;
            batch.draw(tex, x, 0, screenW, screenH);
            batch.draw(tex, x + screenW, 0, screenW, screenH);
        }
        batch.end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    private void loadCityAnimation() {
        final String BASE = "menu/city 5/";
        final int LAYER_COUNT = 7;

        cityLayers = new com.badlogic.gdx.utils.Array<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            String path = BASE + i + ".png";
            System.out.println("Loading layer: " + path + " exists=" + Gdx.files.internal(path).exists());
            cityLayers.add(new com.badlogic.gdx.graphics.Texture(Gdx.files.internal(path)));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (cityTextures != null) {
            for (var t : cityTextures) t.dispose();
        }
    }

    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
    }

    // The following methods are part of the Screen interface but are not used in this screen.
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
}