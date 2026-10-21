package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;


import java.util.HashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LevelJsonExporter {

    public static class TileData {
        public int x;
        public int y;
        public int id;
        public String type;

        public TileData() {}
        public TileData(int x, int y, int id, String type) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.type = type;
        }
    }

    public static class LevelData {
        public int level;
        public int width;
        public int height;
        public ArrayList<TileData> tiles = new ArrayList<>();
    }

    public static class AllLevelsData {
        public int version = 1;
        public Map<String, String> legend = new HashMap<>();
        public ArrayList<LevelData> levels = new ArrayList<>();
    }

    private static final Map<Integer, String> ID_TO_TYPE = new HashMap<>();
    static {
        ID_TO_TYPE.put(1, "Wall");
        ID_TO_TYPE.put(3, "Trap");
        ID_TO_TYPE.put(4, "Wall");
        ID_TO_TYPE.put(5, "Enemy");
    }

    public static String buildAllLevelsJson(int levelCount, String inputDirInAssets) {
        AllLevelsData all = new AllLevelsData();

        all.legend.put("0", "Empty");
        all.legend.put("1", "Wall");
        all.legend.put("2", "Other");
        all.legend.put("3", "Trap");
        all.legend.put("4", "Wall");
        all.legend.put("5", "Enemy");

        for (int i = 1; i <= levelCount; i++) {
            String path = inputDirInAssets + "/level" + i + ".txt";
            LevelData level = parseOneLevel(i, Gdx.files.internal(path));
            all.levels.add(level);
        }

        Json json = new Json(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        return json.prettyPrint(all);
    }

    private static LevelData parseOneLevel(int levelNumber, FileHandle file) {
        if (!file.exists()) {
            throw new RuntimeException("Missing level file: " + file.path());
        }

        LevelData level = new LevelData();
        level.level = levelNumber;

        String text = file.readString("UTF-8");
        String[] lines = text.split("\\R");

        int maxX = -1;
        int maxY = -1;
        Integer width = null;
        Integer height = null;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Width=")) {
                width = Integer.parseInt(line.substring("Width=".length()).trim());
                continue;
            }
            if (line.startsWith("Height=")) {
                height = Integer.parseInt(line.substring("Height=".length()).trim());
                continue;
            }

            int eq = line.indexOf('=');
            if (eq < 0) continue;

            String left = line.substring(0, eq).trim();
            String right = line.substring(eq + 1).trim();

            int comma = left.indexOf(',');
            if (comma < 0) continue;

            int x = Integer.parseInt(left.substring(0, comma).trim());
            int y = Integer.parseInt(left.substring(comma + 1).trim());
            int id = Integer.parseInt(right);

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);

            if (id == 0) continue;

            String type = ID_TO_TYPE.get(id);
            if (type != null) {
                level.tiles.add(new TileData(x, y, id, type));
            }
        }

        level.width = (width != null) ? width : Math.max(0, maxX + 1);
        level.height = (height != null) ? height : Math.max(0, maxY + 1);

        return level;
    }

    public static void buildAllLevelsJson(int levelCount, String inputDirInAssets, String outputLocalPath) {
        String jsonText = buildAllLevelsJson(levelCount, inputDirInAssets);
        Gdx.files.local(outputLocalPath).writeString(jsonText, false, "UTF-8");
    }
    public static HashMap<String, Integer> loadLevelMapFromAllLevelsJson(int levelNumber) {
        // You currently write all_levels.json to LOCAL at runtime
        String jsonText = Gdx.files.local("levels/all_levels.json").readString("UTF-8");

        JsonValue root = new JsonReader().parse(jsonText);
        JsonValue levels = root.get("levels");

        JsonValue chosen = null;
        for (int i = 0; i < levels.size; i++) {
            JsonValue lvl = levels.get(i);
            if (lvl.getInt("level") == levelNumber) {
                chosen = lvl;
                break;
            }
        }

        if (chosen == null) {
            throw new RuntimeException("Level not found in levels/all_levels.json: " + levelNumber);
        }

        HashMap<String, Integer> map = new HashMap<>();

        JsonValue tiles = chosen.get("tiles");
        for (int i = 0; i < tiles.size; i++) {
            JsonValue t = tiles.get(i);
            int x = t.getInt("x");
            int y = t.getInt("y");
            int id = t.getInt("id"); // numeric tile id
            map.put(x + "," + y, id);
        }

        return map;
    }

}

