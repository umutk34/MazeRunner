package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Star Wars style intro animation screen
 * Implements three phases: intro text, LOGO zoom, crawl text (using real 3D perspective camera)
 */
public class IntroScreen implements Screen {
    
    private final MazeRunnerGame game;
    private final Stage stage;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    
    private PerspectiveCamera camera3D;
    
    private float scrollYStart = -85f;
    private float scrollY = scrollYStart;
    private float scrollYEnd = 200f;
    
    private enum State {
        INTRO_TEXT,
        LOGO_ZOOM,
        CRAWL_TEXT
    }
    
    private State currentState = State.INTRO_TEXT;
    private float stateTimer = 0f;
    
    private Label introLabel;
    private float introFadeInTime = 1.5f;
    private float introStayTime = 2.0f;
    private float introFadeOutTime = 1.5f;
    private float introTotalTime = introFadeInTime + introStayTime + introFadeOutTime;
    
    private Label logoLabel;
    private float logoZoomTime = 2.0f;
    private float logoFadeOutTime = 0.5f;
    private float logoStartScale = 4.5f;
    private float logoEndScale = 0.1f;
    private float logoStartY = 0f;
    
    private String crawlText = "SUBJECT: THE SPACE MARINE\n" + "SURVIVAL TRIAL INITIALIZED\n\n" +
            "A SPACE MARINE THAT'S BORN INTO BLOOD\n" + "WHO HAS FORGOTTEN WAYS TO KILL\n" + "THEN ENTIRE ARMIES WILL EVER LEARN.\n" +
            "HIS SHOULDERS WERE MOLDED BY PAIN\n" + "HIS BACK WITH BETRAYAL\n" + "YET HIS LEGS WOULD CARRY HIS HONOR..\n" +
            "A NOBLE HERO..\n" +
            "CAPTURED BY THE DARK FORCES \nOF THE UNIVERSE\n" + "PUT ON A TEST TO SAVE THOSE HE SERVES.\n" +
            "THROUGH THE DARKNESS OF THE MAZE:\n" + "SPARK, POWER, TRACTION, ENGINE AND CORE.\n" +
            "THE DESTINY OF THE ENTIRE CIVILIZATIONS\n" + "AT THE SWING OF A \nSPACE MARINE'S LIGHTSABER..\n" +
            "WILL HE STAND?\n" +
            "WILL HE FALL?\n";

    private float crawlSpeed = 8f;
    
    public IntroScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        this.stage = new Stage(new ScreenViewport(), batch);
        
        this.font = game.getSkin().getFont("font");
        this.titleFont = game.getSkin().getFont("new-font");
        
        Label.LabelStyle introStyle = new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.CYAN);
        introLabel = new Label("Steel in hand and silence in heart as I walk the maze....", introStyle);
        introLabel.setFontScale(1.5f);
        introLabel.setPosition(
            (Gdx.graphics.getWidth() - introLabel.getPrefWidth()) / 2f,
            Gdx.graphics.getHeight() / 2f
        );
        introLabel.getColor().a = 0f;
        stage.addActor(introLabel);
        
        Label.LabelStyle logoStyle = new Label.LabelStyle(titleFont, com.badlogic.gdx.graphics.Color.YELLOW);
        logoLabel = new Label("AUTOHAUS\nMAZERUNNER", logoStyle);
        logoLabel.setFontScale(6.1f);
        logoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        
        float screenWidth = Gdx.graphics.getWidth();
        float textWidth = screenWidth * 0.6f;
        logoLabel.setWidth(textWidth);
        float labelHeight = logoLabel.getPrefHeight();
        logoLabel.setPosition(
            (screenWidth - textWidth) / 2f,
            (Gdx.graphics.getHeight() - labelHeight) / 2f
        );
        logoLabel.getColor().a = 0f;
        stage.addActor(logoLabel);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        game.playIntroMusic();
        
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera3D = new PerspectiveCamera(67f, w, h);
        
        camera3D.position.set(0, 100f, 100f);
        camera3D.lookAt(0, 0, 0);
        camera3D.up.set(0, 1, 0);
        
        camera3D.near = 0.1f;
        camera3D.far = 1000f;
        camera3D.update();
        
        titleFont.setColor(1f, 0.9f, 0f, 1f);
        
        scrollY = scrollYStart;
    }
    
    
    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || 
            Gdx.input.justTouched()) {
            skipToGame();
            return;
        }
        
        stateTimer += delta;
        
        switch (currentState) {
            case INTRO_TEXT:
                updateIntroText(delta);
                break;
            case LOGO_ZOOM:
                updateLogoZoom(delta);
                break;
            case CRAWL_TEXT:
                updateCrawlText(delta);
                break;
        }
        
        if (currentState == State.CRAWL_TEXT) {
            drawCrawlText3D();
        } else {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.act(delta);
            stage.draw();
        }
    }
    
    /**
     * Phase 1: intro text fade in/stay/fade out
     */
    private void updateIntroText(float delta) {
        float alpha = 0f;
        
        if (stateTimer < introFadeInTime) {
            alpha = stateTimer / introFadeInTime;
        } else if (stateTimer < introFadeInTime + introStayTime) {
            alpha = 1f;
        } else if (stateTimer < introTotalTime) {
            alpha = 1f - (stateTimer - introFadeInTime - introStayTime) / introFadeOutTime;
        } else {
            currentState = State.LOGO_ZOOM;
            stateTimer = 0f;
            introLabel.getColor().a = 0f;
            logoStartY = Gdx.graphics.getHeight() / 2f;
            
            logoLabel.setFontScale(6.0f * logoStartScale);
            logoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            float screenWidth = Gdx.graphics.getWidth();
            float textWidth = screenWidth * 0.6f;
            logoLabel.setWidth(textWidth);
            float labelHeight = logoLabel.getPrefHeight();
            logoLabel.setPosition(
                (screenWidth - textWidth) / 2f,
                (Gdx.graphics.getHeight() - labelHeight) / 2f
            );
            logoLabel.getColor().a = 1f;
            return;
        }
        
        introLabel.getColor().a = alpha;
    }
    
    /**
     * Phase 2: LOGO zoom fade out
     */
    private void updateLogoZoom(float delta) {
        if (stateTimer >= logoZoomTime) {
            currentState = State.CRAWL_TEXT;
            stateTimer = 0f;
            logoLabel.getColor().a = 0f;
            scrollY = scrollYStart;
            return;
        }
        
        float progress = stateTimer / logoZoomTime;
        float easedProgress = 1f - (1f - progress) * (1f - progress);
        float scale = logoStartScale + (logoEndScale - logoStartScale) * easedProgress;
        
        float newFontScale = 6.0f * scale;
        logoLabel.setFontScale(newFontScale);
        
        float currentY = logoStartY + progress * 200f;
        logoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        float screenWidth = Gdx.graphics.getWidth();
        float textWidth = screenWidth * 0.6f;
        logoLabel.setWidth(textWidth);
        float labelHeight = logoLabel.getPrefHeight();
        logoLabel.setPosition(
            (screenWidth - textWidth) / 2f,
            currentY - labelHeight / 2f
        );
        
        if (stateTimer > logoZoomTime - logoFadeOutTime) {
            float fadeProgress = (stateTimer - (logoZoomTime - logoFadeOutTime)) / logoFadeOutTime;
            logoLabel.getColor().a = 1f - fadeProgress;
        } else {
            logoLabel.getColor().a = 1f;
        }
    }
    
    /**
     * Phase 3: crawl text (update Z position)
     */
    private void updateCrawlText(float delta) {
        scrollY += crawlSpeed * delta;
        
        if (scrollY > scrollYEnd) {
            skipToGame();
        }
    }
    
    /**
     * Draw crawl text (using real 3D perspective effect)
     */
    private void drawCrawlText3D() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(camera3D.combined);
        
        batch.begin();
        
        float normalizedZ = (scrollY - scrollYStart) / (scrollYEnd - scrollYStart);
        normalizedZ = Math.max(0f, Math.min(1f, normalizedZ));
        float alpha = 1f;
        if (normalizedZ < 0.1f) {
            alpha = normalizedZ / 0.1f;
        } else if (normalizedZ > 0.9f) {
            alpha = (1f - normalizedZ) / 0.1f;
        }
        alpha = Math.max(0f, Math.min(1f, alpha));
        
        titleFont.setColor(1f, 0.9f, 0f, alpha);
        
        float baseFontScale = 0.3f;
        titleFont.getData().setScale(baseFontScale);
        
        Matrix4 transform = new Matrix4();
        transform.translate(0, 0, -scrollY);
        transform.rotate(Vector3.X, -90f);
        
        batch.setTransformMatrix(transform);
        
        float screenWidth = Gdx.graphics.getWidth();
        float textWidth = screenWidth * 0.6f;
        titleFont.draw(batch, crawlText, -textWidth / 2f, 0, textWidth, 
                      com.badlogic.gdx.utils.Align.center, true);
        
        titleFont.getData().setScale(1f);
        
        batch.setTransformMatrix(new Matrix4());
        
        batch.end();
    }
    
    /**
     * Skip animation: enter HowToPlayScreen, then HowToPlayScreen decides next step (continue/return to menu)
     */
    private void skipToGame() {
        game.setScreen(new HowToPlayScreen(game, true));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (camera3D != null) {
            camera3D.viewportWidth = width;
            camera3D.viewportHeight = height;
            camera3D.update();
        }
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
