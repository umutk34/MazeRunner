package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.fop.maze.Behaviour.Player;
import de.tum.cit.fop.maze.Behaviour.QuestItem;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.Various.Event;
import de.tum.cit.fop.maze.Various.LevelLoader;

import java.util.Map;

//The GameScreen class is responsible for rendering the gameplay screen.
public class GameScreen implements Screen {
    private Event currentEvent;
    private static final int TILE_SIZE = 32;
    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private SpriteBatch spriteBatch;

    // Telemetry (avoid exporting multiple times)
    private boolean telemetryExported = false;
    private String telemetryResult = "quit";

    private float sinusInput = 0f;
    private Map<Map.Entry<Integer, Integer>, Integer> levelData;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game, String levelFile) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.40f;

        //Fix the camera in the center of the world
        camera.setToOrtho(false, 800, 600);
        camera.position.set(200, 150, 0);
        camera.update();


        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        currentEvent = new Event();
        levelData = LevelLoader.loadMap(levelFile);
        levelData.put(new java.util.AbstractMap.SimpleEntry<>(12, 5), 0);
        System.out.println("levelData size = " + levelData.size());
        System.out.println("types = " + levelData.values().stream().distinct().sorted().toList());

        game.setMazeLayout(levelData);
        game.loadAllMazeElements();

    }

    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 15;
    private void drawMap(SpriteBatch batch) {
        //先都画上地板
        for (int col = 0; col < MAP_WIDTH; col++) {
            for (int row = 0; row < MAP_HEIGHT; row++) {
                float x = col * TILE_SIZE;
                float y = row * TILE_SIZE;
                batch.draw(game.getFloorRegion(), x, y, TILE_SIZE, TILE_SIZE);
            }
        }
        //有墙的地方画上墙
        for (var entry : levelData.entrySet()) {
            int type = entry.getValue();

            if (type == 0) {
                float x = entry.getKey().getKey() * TILE_SIZE;
                float y = entry.getKey().getValue() * TILE_SIZE;

                batch.draw(game.getWallRegion(), x, y, TILE_SIZE, TILE_SIZE);
            }

            // 如果以后有陷阱(3)，也可以在这里画
            // if (type == 3) ...
        }
    }

    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        de.tum.cit.fop.maze.MazeRunnerGame game = this.game;
        var mapData = game.getMazeLayout();
        for (de.tum.cit.fop.maze.Behaviour.Character enemy : game.getEnemies()) {
            enemy.update(delta, game);
        }
        if (game.getPlayer() != null) {
            game.getPlayer().update(delta, game);
        }

        handleItemPickups();

        ScreenUtils.clear(0, 0, 0, 1);

        //Camera
        camera.position.set(game.getPlayer().position.x+16, game.getPlayer().position.y+16, 0);
        camera.update();

        SpriteBatch batch = game.getSpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float totalMapWidth = MAP_WIDTH * TILE_SIZE;
        float totalMapHeight = MAP_HEIGHT * TILE_SIZE;
        batch.disableBlending();
        batch.draw(game.getGameBackground(), -MAP_WIDTH*TILE_SIZE/2, -MAP_HEIGHT*TILE_SIZE/2, totalMapWidth*2, totalMapHeight*2);//绘制大背景
        batch.enableBlending();
        //画地图
        drawMap(batch);
        //画敌人
        for (de.tum.cit.fop.maze.Behaviour.Character enemy : game.getEnemies()) {
            float hitboxW = enemy.getHitbox().width;
            float hitboxH = enemy.getHitbox().height;
            float offsetX = 0;
            float offsetY = 0;
            enemy.render(batch, delta, 32, 32, offsetX, offsetY);
        }
        //画玩家
        game.getPlayer().render(batch, delta, 32, 32, -9, 0);
        //画道具
        for (QuestItem item : game.getQuestItems()) {
            item.render(game.getSpriteBatch());
        }

        batch.end();

        // back to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            telemetryResult = "quit";
            exportTelemetryOnce();
            game.goToMenu();
            return;
        }

    }

    private void exportTelemetryOnce() {
        if (telemetryExported) return;
        telemetryExported = true;

        game.getTelemetry().endSession(telemetryResult);
        String jsonPath = game.getTelemetry().exportJson();
        String csvPath  = game.getTelemetry().exportCsv();
        Gdx.app.log("TELEMETRY", "Exported JSON: " + jsonPath);
        Gdx.app.log("TELEMETRY", "Exported CSV: " + csvPath);
    }

    @Override
    public void show() {
        game.getTelemetry().startSession(game.getSelectedLevel());
    }



    private void handleItemPickups() {
        Player player = game.getPlayer();
        Rectangle playerBox = player.getHitbox();
        for (QuestItem item : game.getQuestItems()) {
            // 如果没被捡走 且 玩家撞到了它
            if (!item.isCollected() && playerBox.overlaps(item.hitbox)) {
                item.setCollected(true);

                // 针对不同道具做逻辑处理
                if (item.getType() == QuestItem.ItemType.SPARK_PLUG) {

                    System.out.println("Picked up: SPARK_PLUG");
                    item.setCollected(true);
                } else if (item.getType() == QuestItem.ItemType.BATTERY) {
                    //TODO......
                    System.out.println("Picked up: BATTERY! Let's fix the car!");
                }else if (item.getType() == QuestItem.ItemType.TIRES) {
                    //TODO......
                    System.out.println("Picked up: TIRES! Let's fix the car!");
                }else if (item.getType() == QuestItem.ItemType.ENGINE) {
                    //TODO......
                    System.out.println("Picked up: ENGINE! Let's fix the car!");
                }else if (item.getType() == QuestItem.ItemType.AI_CORE) {
                    //TODO......
                    System.out.println("Picked up: AI Core! Let's fix the car!");
                }

                // 这里可以顺手调土哥写的音效接口
                // pickupSound.play();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}