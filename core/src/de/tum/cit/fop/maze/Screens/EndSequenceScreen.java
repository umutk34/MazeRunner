package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * 结束序列屏幕：包含字幕序列和制作人员表
 */
public class EndSequenceScreen implements Screen {

    private static final String[] SUBTITLE_LINES = {
        "You escaped the maze.",
        "But the maze was never the test.",
        "You were."
    };
    
    private static final float[] SUBTITLE_WAIT_TIMES = {
        1.2f,  // 第1行后等待
        1.3f,  // 第2行后等待
        1.6f   // 第3行后等待
    };
    
    private static final float FADE_IN_DURATION = 0.2f;
    private static final float FADE_OUT_DURATION = 0.2f;
    private static final float SUBTITLE_TO_CREDITS_DELAY = 0.6f;
    private static final float OVERLAY_ALPHA = 0.7f; // 70%黑色遮罩
    private static final float SUBTITLE_Y_POSITION = 0.575f; // 屏幕高度57.5%位置（略偏下）
    
    private static final String[] CREDITS = {
            "UMUT KAPLAN",
        "GUANZE SONG",
        "HAOJUN LIANG",
            "RUI WANG",
            "CREATED BY"
    };
    
    private static final float CREDITS_SCROLL_SPEED = 90f; // 每秒滚动像素数
    private static final float CREDITS_LINE_SPACING = 80f; // 人名之间的间距
    private static final float CREDITS_START_Y_OFFSET = -100f; // 从屏幕下方开始
    private static final float CREDITS_FONT_SIZE = 36f;
    private static final float CREDITS_LOGO_SPACING = 120f; // Logo和第一个名字之间的间距
    private static final float CREDITS_LOGO_WIDTH = 600f; // Logo宽度（保持宽高比）
    
    private static final float MUSIC_VOLUME_DURING_SUBTITLES = 0.7f; // 字幕期间音量70%
    private static final float MUSIC_VOLUME_DURING_CREDITS = 1.0f; // credits期间音量100%
    
    // ========== 状态枚举 ==========
    private enum SequenceState {
        SUBTITLE_1,      // 显示第1行字幕
        SUBTITLE_2,      // 显示第2行字幕
        SUBTITLE_3,      // 显示第3行字幕
        SUBTITLE_TO_CREDITS_TRANSITION, // 字幕到credits的过渡
        CREDITS,         // 制作人员表滚动
        FINAL_MENU      // 最终菜单（返回主菜单按钮）
    }
    
    // ========== 成员变量 ==========
    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final Stage stage;
    private BitmapFont subtitleFont;
    private BitmapFont creditsFont;
    private Texture autohausLogo;
    private Music starWarsMusic;
    
    private SequenceState currentState = SequenceState.SUBTITLE_1;
    private float stateTimer = 0f;
    private int currentSubtitleIndex = 0;
    private float subtitleAlpha = 0f;
    private boolean isFadingIn = true;
    private boolean isFadingOut = false;
    
    private float creditsY = CREDITS_START_Y_OFFSET;
    private boolean creditsStarted = false;
    private boolean creditsFinished = false;
    
    private TextButton backToMenuButton;
    private float originalMusicVolume = 1.0f;
    
    public EndSequenceScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        this.stage = new Stage(new ScreenViewport(), batch);
        
        // 保存原始音乐音量
        if (game.getCurrentMusic() != null) {
            originalMusicVolume = game.getCurrentMusic().getVolume();
        }
        
        // 创建字体
        createFonts();
        
        // 加载AutoHaus logo
        loadLogo();
        
        // 构建最终菜单按钮（初始隐藏）
        buildFinalMenu();
    }
    
    private void loadLogo() {
        try {
            autohausLogo = new Texture(Gdx.files.internal("AutoHaus.png"));
        } catch (Exception e) {
            Gdx.app.error("EndSequenceScreen", "Failed to load AutoHaus.png", e);
            autohausLogo = null;
        }
    }
    
    private void playStarWarsMusic() {
        // 如果音乐已经在播放，不重复播放
        if (starWarsMusic != null && starWarsMusic.isPlaying()) {
            return;
        }
        
        try {
            String musicPath = "game_assets/Music/Star wars.mp3";
            if (Gdx.files.internal(musicPath).exists()) {
                // 如果已经加载过但没在播放，先停止并释放
                if (starWarsMusic != null) {
                    starWarsMusic.stop();
                    starWarsMusic.dispose();
                }
                starWarsMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
                if (starWarsMusic != null) {
                    starWarsMusic.setLooping(false);
                    // 应用游戏音量设置
                    if (!game.isMusicMuted()) {
                        starWarsMusic.setVolume(game.getMusicVolume());
                    } else {
                        starWarsMusic.setVolume(0f);
                    }
                    starWarsMusic.play();
                }
            }
        } catch (Exception e) {
            Gdx.app.error("EndSequenceScreen", "Failed to load Star wars.mp3", e);
            starWarsMusic = null;
        }
    }
    
    private void createFonts() {
        // 字幕字体（清晰无衬线，白色）
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("game_assets/UI/Font/CyberpunkCraftpixPixel.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        param.size = 32;
        param.color = Color.WHITE;
        param.borderWidth = 0;
        subtitleFont = generator.generateFont(param);
        
        param.size = (int)CREDITS_FONT_SIZE;
        param.color = Color.WHITE;
        creditsFont = generator.generateFont(param);
        
        generator.dispose();
    }
    
    private void buildFinalMenu() {
        stage.clear();
        
        backToMenuButton = new TextButton("Back to Main Menu", game.getSkin(), "button-style");
        backToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                game.goToMenu();
            }
        });
        
        backToMenuButton.setVisible(false);
        backToMenuButton.setPosition(
            Gdx.graphics.getWidth() / 2f - backToMenuButton.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - backToMenuButton.getHeight() / 2f
        );
        
        stage.addActor(backToMenuButton);
    }
    
    @Override
    public void show() {
        // 使用InputMultiplexer来同时处理Stage和按键检测
        // 注意：按键检测处理器放在Stage之前，这样它会先处理输入
        InputMultiplexer multiplexer = new InputMultiplexer();
        // 先添加按键检测处理器（用于跳过字幕）
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // 只在字幕状态时允许跳过
                if (currentState == SequenceState.SUBTITLE_1 || 
                    currentState == SequenceState.SUBTITLE_2 || 
                    currentState == SequenceState.SUBTITLE_3 ||
                    currentState == SequenceState.SUBTITLE_TO_CREDITS_TRANSITION) {
                    skipToCredits();
                    return true; // 返回true表示已处理，阻止Stage处理
                }
                return false; // 返回false让Stage继续处理
            }
            
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // 鼠标点击也可以跳过字幕（但不在按钮上时）
                if (currentState == SequenceState.SUBTITLE_1 || 
                    currentState == SequenceState.SUBTITLE_2 || 
                    currentState == SequenceState.SUBTITLE_3 ||
                    currentState == SequenceState.SUBTITLE_TO_CREDITS_TRANSITION) {
                    // 检查是否点击在按钮上
                    if (backToMenuButton != null && backToMenuButton.isVisible()) {
                        // 如果按钮可见，让Stage处理点击
                        return false;
                    }
                    skipToCredits();
                    return true;
                }
                return false;
            }
        });
        // 然后添加Stage处理器
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
        
        // 开始时降低BGM音量到70%
        if (game.getCurrentMusic() != null) {
            game.getCurrentMusic().setVolume(MUSIC_VOLUME_DURING_SUBTITLES);
        }
    }
    
    @Override
    public void render(float delta) {
        // 在render中直接检查按键（作为备用方案，确保跳过功能工作）
        // 只在字幕状态时检查
        if (currentState == SequenceState.SUBTITLE_1 || 
            currentState == SequenceState.SUBTITLE_2 || 
            currentState == SequenceState.SUBTITLE_3 ||
            currentState == SequenceState.SUBTITLE_TO_CREDITS_TRANSITION) {
            // 检查常见按键和鼠标点击
            boolean keyPressed = false;
            // 检查空格、回车、ESC
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                keyPressed = true;
            }
            // 检查方向键
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
                Gdx.input.isKeyJustPressed(Input.Keys.DOWN) ||
                Gdx.input.isKeyJustPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                keyPressed = true;
            }
            // 检查字母键（A-Z）
            for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    keyPressed = true;
                    break;
                }
            }
            // 检查鼠标点击
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                keyPressed = true;
            }
            
            if (keyPressed) {
                skipToCredits();
            }
        }
        
        // 更新状态机
        updateSequence(delta);
        
        // 纯黑背景（干净）
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        // 根据状态渲染
        switch (currentState) {
            case SUBTITLE_1:
            case SUBTITLE_2:
            case SUBTITLE_3:
                renderSubtitle();
                break;
            case SUBTITLE_TO_CREDITS_TRANSITION:
                // 过渡期间不显示字幕
                break;
            case CREDITS:
                renderCredits();
                break;
            case FINAL_MENU:
                // 只显示按钮，背景保持黑色
                break;
        }
        
        batch.end();
        
        // 渲染Stage（按钮）
        stage.act(delta);
        stage.draw();
    }
    
    private void updateSequence(float delta) {
        stateTimer += delta;
        
        switch (currentState) {
            case SUBTITLE_1:
            case SUBTITLE_2:
            case SUBTITLE_3:
                updateSubtitle(delta);
                break;
            case SUBTITLE_TO_CREDITS_TRANSITION:
                if (stateTimer >= SUBTITLE_TO_CREDITS_DELAY) {
                    // 恢复BGM音量到100%
                    if (game.getCurrentMusic() != null) {
                        game.getCurrentMusic().setVolume(MUSIC_VOLUME_DURING_CREDITS);
                    }
                    currentState = SequenceState.CREDITS;
                    creditsStarted = true;
                    stateTimer = 0f;
                }
                break;
            case CREDITS:
                updateCredits(delta);
                break;
            case FINAL_MENU:
                // 最终菜单状态，不需要更新
                break;
        }
    }
    
    private void updateSubtitle(float delta) {
        if (isFadingIn) {
            subtitleAlpha += delta / FADE_IN_DURATION;
            if (subtitleAlpha >= 1f) {
                subtitleAlpha = 1f;
                isFadingIn = false;
            }
        } else if (!isFadingOut) {
            // 等待指定时间
            float waitTime = SUBTITLE_WAIT_TIMES[currentSubtitleIndex];
            if (stateTimer >= waitTime) {
                isFadingOut = true;
                stateTimer = 0f;
            }
        } else {
            // 淡出
            subtitleAlpha -= delta / FADE_OUT_DURATION;
            if (subtitleAlpha <= 0f) {
                subtitleAlpha = 0f;
                isFadingOut = false;
                isFadingIn = true;
                
                // 移动到下一行或进入credits
                currentSubtitleIndex++;
                if (currentSubtitleIndex >= SUBTITLE_LINES.length) {
                    // 所有字幕显示完毕，进入credits过渡
                    // 播放Star wars音乐
                    playStarWarsMusic();
                    currentState = SequenceState.SUBTITLE_TO_CREDITS_TRANSITION;
                    stateTimer = 0f;
                } else {
                    // 切换到下一行字幕
                    currentState = SequenceState.values()[currentSubtitleIndex];
                    stateTimer = 0f;
                }
            }
        }
    }
    
    private void renderSubtitle() {
        if (subtitleFont == null || currentSubtitleIndex >= SUBTITLE_LINES.length) {
            return;
        }
        
        String text = SUBTITLE_LINES[currentSubtitleIndex];
        GlyphLayout layout = new GlyphLayout(subtitleFont, text);
        
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float x = screenW / 2f - layout.width / 2f;
        float y = screenH * SUBTITLE_Y_POSITION;
        
        // 应用alpha
        Color originalColor = subtitleFont.getColor();
        subtitleFont.setColor(originalColor.r, originalColor.g, originalColor.b, subtitleAlpha);
        subtitleFont.draw(batch, text, x, y);
        subtitleFont.setColor(originalColor);
    }
    
    private void updateCredits(float delta) {
        if (!creditsStarted) {
            return;
        }
        
        creditsY += CREDITS_SCROLL_SPEED * delta;
        
        // 检查是否所有credits都已滚出屏幕上方
        // 计算总高度：logo高度 + logo间距 + 所有名字高度
        float logoHeight = autohausLogo != null ? 
            (autohausLogo.getHeight() * CREDITS_LOGO_WIDTH / autohausLogo.getWidth()) : 0f;
        float totalCreditsHeight = logoHeight + CREDITS_LOGO_SPACING + (CREDITS.length * CREDITS_LINE_SPACING);
        float creditsEndY = creditsY + totalCreditsHeight;
        float screenH = Gdx.graphics.getHeight();
        
        if (creditsEndY > screenH + 100 && !creditsFinished) {
            creditsFinished = true;
            // 进入最终菜单状态
            currentState = SequenceState.FINAL_MENU;
            if (backToMenuButton != null) {
                backToMenuButton.setVisible(true);
            }
        }
    }
    
    private void renderCredits() {
        if (!creditsStarted) {
            return;
        }
        
        float screenW = Gdx.graphics.getWidth();
        float currentY = creditsY;
        
        // 渲染AutoHaus logo（在最上方）
        if (autohausLogo != null) {
            float logoHeight = autohausLogo.getHeight() * CREDITS_LOGO_WIDTH / autohausLogo.getWidth();
            float logoX = screenW / 2f - CREDITS_LOGO_WIDTH / 2f;
            
            // 只渲染在屏幕范围内的logo
            if (currentY + logoHeight > -50 && currentY < Gdx.graphics.getHeight() + 50) {
                batch.draw(autohausLogo, logoX, currentY, CREDITS_LOGO_WIDTH, logoHeight);
            }
            
            currentY += logoHeight + CREDITS_LOGO_SPACING;
        }
        
        // 渲染制作人员名字
        if (creditsFont != null) {
            for (String name : CREDITS) {
                GlyphLayout layout = new GlyphLayout(creditsFont, name);
                float x = screenW / 2f - layout.width / 2f;
                
                // 只渲染在屏幕范围内的credits
                if (currentY > -50 && currentY < Gdx.graphics.getHeight() + 50) {
                    creditsFont.draw(batch, name, x, currentY);
                }
                
                currentY += CREDITS_LINE_SPACING;
            }
        }
    }
    
    private void skipToCredits() {
        // 跳过字幕序列，直接进入credits
        if (currentState == SequenceState.SUBTITLE_1 || 
            currentState == SequenceState.SUBTITLE_2 || 
            currentState == SequenceState.SUBTITLE_3 ||
            currentState == SequenceState.SUBTITLE_TO_CREDITS_TRANSITION) {
            
            // 播放Star wars音乐（如果还没播放）
            if (starWarsMusic == null || !starWarsMusic.isPlaying()) {
                playStarWarsMusic();
            }
            
            // 恢复BGM音量
            if (game.getCurrentMusic() != null) {
                game.getCurrentMusic().setVolume(MUSIC_VOLUME_DURING_CREDITS);
            }
            
            currentState = SequenceState.CREDITS;
            creditsStarted = true;
            stateTimer = 0f;
            subtitleAlpha = 0f;
        }
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (backToMenuButton != null) {
            backToMenuButton.setPosition(
                width / 2f - backToMenuButton.getWidth() / 2f,
                height / 2f - backToMenuButton.getHeight() / 2f
            );
        }
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {
        // 恢复原始音乐音量
        if (game.getCurrentMusic() != null) {
            game.getCurrentMusic().setVolume(originalMusicVolume);
        }
        // 停止Star wars音乐
        if (starWarsMusic != null) {
            starWarsMusic.stop();
        }
    }
    
    @Override
    public void dispose() {
        if (subtitleFont != null) {
            subtitleFont.dispose();
        }
        if (creditsFont != null) {
            creditsFont.dispose();
        }
        if (autohausLogo != null) {
            autohausLogo.dispose();
        }
        if (starWarsMusic != null) {
            starWarsMusic.stop();
            starWarsMusic.dispose();
        }
        stage.dispose();
    }
}
