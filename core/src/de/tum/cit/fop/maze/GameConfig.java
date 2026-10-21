package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class GameConfig {

    private static final String PATH = "config/GameConfig.json";

    public boolean muted = false;
    public float volume = 1.0f;
    public int selectedLevel = 1;

    public static GameConfig load() {
        FileHandle file = Gdx.files.local(PATH);

        // If it doesn't exist yet, create defaults and save
        if (!file.exists()) {
            GameConfig cfg = new GameConfig();
            cfg.save();
            return cfg;

        }

        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file.readString("UTF-8"));

        GameConfig cfg = new GameConfig();
        cfg.muted = root.getBoolean("muted", false);
        cfg.volume = root.getFloat("volume", 1.0f);
        cfg.selectedLevel = root.getInt("selectedLevel", 1);
        return cfg;



    }

    public void save() {
        FileHandle file = Gdx.files.local(PATH);
        file.parent().mkdirs();

        String json =
                "{\"muted\":" + muted +
                        ",\"volume\":" + volume +
                        ",\"selectedLevel\":" + selectedLevel + "}";
        file.writeString(json, false, "UTF-8");

    }
}
