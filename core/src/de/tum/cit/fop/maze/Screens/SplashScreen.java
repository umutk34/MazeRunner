package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class SplashScreen implements Screen {
    private final MazeRunnerGame game;
    private float timer = 0f;
    private Stage stage;
    private Image splashImage;
    private Texture splashTex;

    private static final float TOTAL_TIME = 3.0f;

    public SplashScreen(MazeRunnerGame game) {
        this.game = game;
        stage     = new Stage(new ScreenViewport());
        splashTex = new Texture(Gdx.files.internal("SplashScreen.png"));

        splashTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        splashImage = new Image(splashTex);
        splashImage.setScaling(Scaling.fit);

        // 3. 布局
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        table.add(splashImage).expand().fill();
    }

    @Override
    public void render(float delta) {
        timer += delta;

        // 清屏
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 计算渐变透明度
        float alpha;
        if (timer < 1.5f) {
            alpha = timer / 1.5f; // 渐显
        } else {
            alpha = Math.max(0, 1f - ((timer - 1.5f) / 1.5f)); // 渐隐
        }

        // 🚩 关键：把算出来的 alpha 应用到图片上
        splashImage.setColor(1, 1, 1, alpha);

        stage.act(delta);
        stage.draw();

        // 3 秒后跳转
        if (timer >= TOTAL_TIME) {
            Gdx.gl.glClearColor(1f, 0.75f, 0.8f, 1f);
            game.setScreen(new MenuScreen(game));

            game.goToMenu();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {}
    @Override
    public void hide() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (splashTex != null) splashTex.dispose();
    }
}