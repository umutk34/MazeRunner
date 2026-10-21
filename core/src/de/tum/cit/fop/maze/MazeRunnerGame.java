package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.Behaviour.*;
import de.tum.cit.fop.maze.Behaviour.Character;
import de.tum.cit.fop.maze.Screens.*;
import de.tum.cit.fop.maze.Various.LevelLoader;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.cit.fop.maze.Screens.SplashScreen;


/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;

    // UI Skin
    private Skin skin;


    // Telemetry Recorder
    private TelemetryRecorder telemetry;

    // Orthographic Camera
    private OrthographicCamera camera;

    // Character animation downwards
    private Animation<TextureRegion> characterDownAnimation;

    // Game configuration
    private GameConfig config;


    //All Characters and Objects
    private List<Character> characters;//存放角色
    private List<MazeObjects> objects;//存放物体，道具等，静态渲染物不在此列
    private Player player;
    private LevelLoader levelLoader;
    //Textures and walls
    private Texture gameBackground;
    private TextureRegion wallRegion;
    private TextureRegion floorRegion;
    private TextureRegion trapRegion;
    private TextureRegion explosionRegion;
    private Texture Tiles;
    private TextureRegion closedGateRegion;
    private Texture mobsTexture;
    private String fileName;
    //存地图生成信息
    private Map<Map.Entry<Integer, Integer>, Integer> mazeLayout;
    //GameConfig
    private int selectedLevel = 1;
    private boolean muted = false;
    private float volume = 1.0f;
    // QuestItems
    private List<QuestItem> questItems = new ArrayList<>();
    private Texture sparkPlugTex;
    private Texture batteryTex;
    private Texture tiresTex;
    private Texture engineTex;
    private Texture aiCoreTex;
    // Musics and Sounds
    private Sound unstuckSound;
    private Music menuMusic;
    private Music gameMusic;
    private Music currentMusic;

    private Animation<TextureRegion> PlayerAnimationDown;

    //Telemetry Interface
    public void logTelemetry(float x, float y) {
        //TODO:对接Umut的保存坐标逻辑--------------------------------------------------------------------------------------
    }

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {//初始化
        config = GameConfig.load();
        sparkPlugTex = new Texture(Gdx.files.internal("cyber/cyber icons/1 Icons/Icon13_05.png"));
        batteryTex = new Texture(Gdx.files.internal("cyber/cyber icons/1 Icons/Icon13_09.png"));
        tiresTex = new Texture(Gdx.files.internal("cyber/cyber icons/1 Icons/Icon13_14.png"));
        engineTex = new Texture(Gdx.files.internal("cyber/cyber icons/1 Icons/Icon13_19.png"));
        aiCoreTex = new Texture(Gdx.files.internal("cyber/cyber icons/1 Icons/Icon13_25.png"));
        fileName = "maps/level-1.properties";
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Menu.mp3"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Background.mp3"));
        unstuckSound = Gdx.audio.newSound(Gdx.files.internal("SoundEffects/Unstuck.mp3"));
        mazeLayout = new HashMap<>();
        levelLoader = new LevelLoader();
        player = new Player(0, 0, 15, 15, 100, this);
        spriteBatch = new SpriteBatch();
        telemetry = new TelemetryRecorder();
        gameBackground = new Texture(Gdx.files.internal("screen/background_Game.png"));
        Tiles = new Texture(Gdx.files.internal("cyber/cyberwall tileset/PSD/Tiles.png"));
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        mobsTexture = new Texture(Gdx.files.internal("mobs.png"));
        characters = new ArrayList<Character>();
        objects = new ArrayList<>();
        muted = config.muted;
        volume = config.volume;
        selectedLevel = config.selectedLevel;

        characters.add(player);

        this.loadCharacterAnimation();
        player.setCurrentAnimation(PlayerAnimationDown);
        player.setPosition(256, 192);

        loadAllMazeElements();

        currentMusic = menuMusic;

        this.setScreen(new SplashScreen(this));

        currentMusic.setLooping(true);
        applyAudioConfig();   // applies muted/volume
        currentMusic.play();


        setScreen(new SplashScreen(this));

        // Load saved audio config
        GameConfig cfg = GameConfig.load();
        this.muted = cfg.muted;
        this.volume = cfg.volume;


    }

    public void goToSettings() {
        this.setScreen(new SettingsScreen(this));
    }

    public void goToLevelSelect() {
        this.setScreen(new LevelSelectorScreen(this));
    }

    public void goToMenu() {

        this.setScreen(new MenuScreen(this));

        // switch music back to menu music


        if (currentMusic != menuMusic) {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = menuMusic;
            currentMusic.setLooping(true);
            applyAudioConfig();
            currentMusic.play();
        }


        goToSplash(); // splash screen only at the startup

        // go directly to MenuScreen (no splash)
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        setScreen(menuScreen);

        // clean up the game screen if needed

        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }


    /**
     * Switches to the game screen.
     */
    public void goToGame(String levelFile) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = gameMusic; // 切换指针到 B
        currentMusic.setLooping(true);
        applyAudioConfig();
        currentMusic.play();

        this.setScreen(new GameScreen(this, levelFile));
    }

    //静态资源切割


    // Splash screen method
    public void goToSplash() {
        setScreen(new SplashScreen(this));
    }


    private void loadWallTextures() {
        floorRegion = new TextureRegion(Tiles, 32, 32, 32, 32);
        wallRegion = new TextureRegion(Tiles, 32, 97, 32, 10);
    }

    //加载主角动画
    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character2.png"));
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, 64, 64);
        int frameCount = 9;
        Animation<TextureRegion> animUp = createAnimation(tmp[8], 0, frameCount);
        Animation<TextureRegion> animLeft = createAnimation(tmp[9], 0, frameCount);
        Animation<TextureRegion> animDown = createAnimation(tmp[10], 0, frameCount);
        Animation<TextureRegion> animRight = createAnimation(tmp[11], 0, frameCount);
        Animation<TextureRegion> characterDownAnimation = animDown;
        player.setAnimations(animUp, animDown, animLeft, animRight);
        player.setCurrentAnimation(animDown);
        setCharacterDownAnimation(animDown);

    }

    //加载敌人动画
    private void loadEnemyAnimations(Enemy enemy) {
        Texture enemySheet = new Texture(Gdx.files.internal("mobs.png"));
        TextureRegion[][] tmp = TextureRegion.split(enemySheet, 16, 16);
        int frameCount = 3;
        Animation<TextureRegion> enemyDown = createAnimation(tmp[0], 9, frameCount);
        Animation<TextureRegion> enemyLeft = createAnimation(tmp[1], 9, frameCount);
        Animation<TextureRegion> enemyRight = createAnimation(tmp[2], 9, frameCount);
        Animation<TextureRegion> enemyUp = createAnimation(tmp[3], 9, frameCount);

        enemy.setAnimations(enemyUp, enemyDown, enemyLeft, enemyRight);
        enemy.setCurrentAnimation(enemyDown);
    }

    //辅助方法
    private Animation<TextureRegion> createAnimation(TextureRegion[] row, int startCol, int count) {
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int i = 0; i < count; i++) {
            frames.add(row[startCol + i]);
        }
        return new Animation<>(0.1f, frames);
    }

    //Audio configurations
    public void applyAudioConfig() {
        float effective = muted ? 0f : volume;

        if (currentMusic != null) currentMusic.setVolume(effective);

        // Optional but good: keep both tracks in sync so switching screens is seamless
        if (menuMusic != null) menuMusic.setVolume(effective);
        if (gameMusic != null) gameMusic.setVolume(effective);
    }

    private void saveAudioConfig() {
        GameConfig cfg = new GameConfig();
        cfg.muted = muted;
        cfg.volume = volume;
        cfg.save();
    }


    public void loadAllMazeElements() {
        loadWallTextures();
        loadCharacterAnimation();
        buildLevelObjects();
    }

    //Cleans up resources when the game is disposed.
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }

    //Processes raw map data and populates the game world with actual objects.
    private void buildLevelObjects() {
        if (mazeLayout == null || mazeLayout.isEmpty()) {
            System.err.println("mazeLayout 为空，无法生成物件");
            return;
        }

        // 清空以前的动态物体列表 (防止重玩时累积)
        questItems.clear();
        characters.clear();
        objects.clear();


        // Clear previous run objects (prevent duplicates when restarting/changing levels)
        questItems.clear();
        characters.clear();
        objects.clear();


        int currentLevel = getSelectedLevel();

        boolean spawned = false;

        for (Map.Entry<Map.Entry<Integer, Integer>, Integer> entry : mazeLayout.entrySet()) {
            int x = entry.getKey().getKey();
            int y = entry.getKey().getValue();
            int type = entry.getValue();

            float worldX = x * 32f;
            float worldY = y * 32f;

            switch (type) {

                case 1: // Player spawn
                    if (!spawned) {
                        player.setPosition(worldX, worldY);

                        // Reset movement so player doesn't carry momentum between levels
                        player.getVelocity().set(0, 0);
                        player.getNextPosition().set(0, 0);

                        // Optional: face down on spawn

                        spawned = true;
                    }

                    break;

                case 2: // Wall (if you later add wall objects)
                    // objects.add(new MazeObjects(worldX, worldY, wallRegion));
                    break;

                case 3: // Trap
                    // objects.add(new Trap(worldX, worldY));
                    break;

                case 4: // 敌人
                    Enemy newEnemy = new Enemy(worldX, worldY, 18, 18);


                    loadEnemyAnimations(newEnemy);
                    characters.add(newEnemy);
                    break;

                case 5: // Key / QuestItem
                    questItems.add(new QuestItem(
                            worldX, worldY, currentLevel,
                            sparkPlugTex, batteryTex, tiresTex, engineTex, aiCoreTex
                    ));
                    break;

                default:
                    break;
            }
        }

        // Fallback spawn if the level has no "1" tile
        if (!spawned) {
            player.setPosition(256, 192);
            player.getVelocity().set(0, 0);
            player.getNextPosition().set(0, 0);
            System.err.println("⚠️ No spawn tile (1) found in level. Using fallback spawn.");
        }
    }

    //Getters and Setters
    public List<Character> getCharacters() {
        return characters;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return PlayerAnimationDown;
    }

    public void setCharacterDownAnimation(Animation<TextureRegion> animation) {
        this.PlayerAnimationDown = animation;
    }

    public Skin getSkin() {
        return skin;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Player getPlayer() {
        return player;
    }


    public String getFileName() {
        return fileName;
    }

    public void setMazeLayout(Map<Map.Entry<Integer, Integer>, Integer> mazeLayout) {
        this.mazeLayout = mazeLayout;
    }

    public Map<Map.Entry<Integer, Integer>, Integer> getMazeLayout() {
        return mazeLayout;
    }

    public Texture getGameBackground() {
        return gameBackground;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public TextureRegion getFloorRegion() {
        return floorRegion;
    }

    public TextureRegion getWallRegion() {
        return wallRegion;
    }

    public TextureRegion getClosedGateRegion() {
        return closedGateRegion;
    }

    public List<Character> getEnemies() {
        return characters;
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(int lvl) {
        selectedLevel = lvl;
        if (config != null) {
            config.selectedLevel = selectedLevel;
            config.save();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        applyAudioConfig();
        if (config != null) {
            config.muted = muted;
            config.volume = volume;
            config.selectedLevel = selectedLevel;
            config.save();
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        applyAudioConfig();
        if (config != null) {
            config.muted = muted;
            config.volume = volume;
            config.selectedLevel = selectedLevel;
            config.save();
        }
    }

    public TelemetryRecorder getTelemetry() {
        return telemetry;
    }

    public List<QuestItem> getQuestItems() {
        return questItems;
    }

    public void playUnstuckSound() {
        if (unstuckSound != null && !muted) unstuckSound.play(volume);
    }
}

