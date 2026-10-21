package de.tum.cit.fop.maze.Various;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * LevelLoader is responsible for parsing raw map data.
 * It focuses solely on data acquisition, leaving object instantiation to the game engine.
 */
public class LevelLoader {
    private HashMap<String, Integer> rawMapData; // Stores "x,y" -> tileID [cite: 188-191]
    private int mapWidth;  // Total width in grid units [cite: 212]
    private int mapHeight; // Total height in grid units [cite: 213]

    public LevelLoader() {
        this.rawMapData = new HashMap<>();
    }

    /**
     * Loads map data from a JSON file.
     * To be implemented by Umut using JSON serialization.
     * @param path Path to the level file.
     */
    public void loadLevel(String path) {
        // Umut will place his JSON parsing logic here [cite: 193-196]
        // Example: this.rawMapData = UmutParser.parse(path);
        // And update mapWidth/mapHeight based on the max x and y found [cite: 197]
    }

    // Getters for MazeRunnerGame to access the data [cite: 237-241]
    public HashMap<String, Integer> getRawMapData() { return rawMapData; }
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }

    public static Map<Map.Entry<Integer,Integer>,Integer> loadMap(String filePath) {
        Map<Map.Entry<Integer,Integer>, Integer> levelData = new HashMap<>();

        try {
            FileHandle file = Gdx.files.internal(filePath);
            Properties properties = new Properties();
            properties.load(file.reader());

            if (!file.exists()) {
                System.err.println("⚠️ 警告：地图文件 " + filePath + " 不存在，将使用空白地图。");
                return levelData;
            }

            for (String key : properties.stringPropertyNames()) {
                try {
                    String[] coords = key.split(",");
                    int tileX = Integer.parseInt(coords[0]);
                    int tileY = Integer.parseInt(coords[1]);
                    int tileType = Integer.parseInt(properties.getProperty(key));
                    levelData.put(new AbstractMap.SimpleEntry<>(tileX, tileY), tileType);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Invalid level data for key: " + key);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return levelData;
    }

}
