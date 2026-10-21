package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class GameConfigManager {

    private static final String CONFIG_PATH = "config/GameConfig.json";

    public static GameConfig load() {
        FileHandle file = Gdx.files.local(CONFIG_PATH);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        if (!file.exists()) {
            GameConfig defaults = new GameConfig();
            save(defaults);
            return defaults;
        }

        try {
            return json.fromJson(GameConfig.class, file);
        } catch (Exception e) {
            // If the file is corrupted, reset to defaults
            GameConfig defaults = new GameConfig();
            save(defaults);
            return defaults;
        }
    }

    public static void save(GameConfig config) {
        FileHandle file = Gdx.files.local(CONFIG_PATH);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        file.parent().mkdirs(); // ensure "config/" exists
        file.writeString(json.prettyPrint(config), false, "UTF-8");
    }
}
