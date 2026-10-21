package de.tum.cit.fop.maze.Data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Player save manager
 * Responsible for saving and loading player saves
 */
public class ProfileManager {
    private static final String SAVES_DIR = "saves";
    private final Json json;

    public ProfileManager() {
        this.json = new Json();
        ensureSavesDirectory();
    }

    /**
     * Ensure saves directory exists
     */
    private void ensureSavesDirectory() {
        FileHandle savesDir = Gdx.files.local(SAVES_DIR);
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
    }

    /**
     * Save player profile
     * 
     * @param profile Player profile to save
     */
    public void saveProfile(PlayerProfile profile) {
        if (profile == null || profile.getPlayerName() == null || profile.getPlayerName().trim().isEmpty()) {
            Gdx.app.error("ProfileManager", "Cannot save profile: player name is empty");
            return;
        }

        try {
            String jsonString = json.prettyPrint(profile);
            String fileName = profile.getPlayerName() + ".json";
            FileHandle file = Gdx.files.local(SAVES_DIR + "/" + fileName);
            file.writeString(jsonString, false);
        } catch (Exception e) {
            Gdx.app.error("ProfileManager", "Failed to save profile", e);
        }
    }

    /**
     * Load player profile
     * 
     * @param playerName Player name
     * @return PlayerProfile object, or null if file doesn't exist or loading fails
     */
    public PlayerProfile loadProfile(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            Gdx.app.error("ProfileManager", "Cannot load profile: player name is empty");
            return null;
        }

        try {
            String fileName = playerName + ".json";
            FileHandle file = Gdx.files.local(SAVES_DIR + "/" + fileName);
            
            if (!file.exists()) {
                return null;
            }
            
            String jsonString = file.readString();
            PlayerProfile profile = json.fromJson(PlayerProfile.class, jsonString);
            
            return profile;
        } catch (Exception e) {
            Gdx.app.error("ProfileManager", "Failed to load profile", e);
            return null;
        }
    }

    /**
     * Check if profile exists
     * 
     * @param playerName Player name
     * @return true if profile exists, false otherwise
     */
    public boolean profileExists(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        
        String fileName = playerName + ".json";
        FileHandle file = Gdx.files.local(SAVES_DIR + "/" + fileName);
        return file.exists();
    }

    /**
     * Load all player profiles
     * Reads from multiple locations: local data directory and project root directory
     * 
     * @return List of all successfully loaded PlayerProfile objects
     */
    public List<PlayerProfile> loadAllProfiles() {
        List<PlayerProfile> profiles = new ArrayList<>();
        Set<String> loadedPlayerNames = new HashSet<>();
        
        try {
            List<FileHandle> savesDirs = new ArrayList<>();
            
            FileHandle localSavesDir = Gdx.files.local(SAVES_DIR);
            if (localSavesDir.exists()) {
                savesDirs.add(localSavesDir);
            }
            
            try {
                FileHandle externalSavesDir = Gdx.files.external(SAVES_DIR);
                if (externalSavesDir.exists() && !savesDirs.contains(externalSavesDir)) {
                    savesDirs.add(externalSavesDir);
                }
            } catch (Exception e) {
                // Ignore error, continue trying other locations
            }
            
            try {
                String workingDir = System.getProperty("user.dir");
                if (workingDir != null) {
                    File projectSavesDir = new File(workingDir, SAVES_DIR);
                    if (projectSavesDir.exists() && projectSavesDir.isDirectory()) {
                        FileHandle projectSavesHandle = Gdx.files.absolute(projectSavesDir.getAbsolutePath());
                        if (!savesDirs.contains(projectSavesHandle)) {
                            savesDirs.add(projectSavesHandle);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore error, continue trying other locations
            }
            
            if (savesDirs.isEmpty()) {
                profiles.add(createDefaultProfile());
                return profiles;
            }
            
            for (FileHandle savesDir : savesDirs) {
                FileHandle[] files = savesDir.list();
                
                if (files == null || files.length == 0) {
                    continue;
                }
                
                for (FileHandle file : files) {
                    String fileName = file.name();
                    String lowerFileName = fileName.toLowerCase();
                    
                    if (!lowerFileName.endsWith(".json")) {
                        continue;
                    }
                    
                    try {
                        String jsonString = file.readString();
                        PlayerProfile profile = json.fromJson(PlayerProfile.class, jsonString);
                        
                        if (profile != null && profile.getPlayerName() != null && !profile.getPlayerName().trim().isEmpty()) {
                            // Deduplicate: skip if same player name already loaded (prefer first found)
                            String playerName = profile.getPlayerName();
                            if (!loadedPlayerNames.contains(playerName)) {
                                profiles.add(profile);
                                loadedPlayerNames.add(playerName);
                            }
                        }
                    } catch (Exception e) {
                        // Catch exception and skip corrupted files to prevent game crash
                        Gdx.app.error("ProfileManager", "Failed to load profile, skipping file: " + fileName, e);
                    }
                }
            }
            
            if (profiles.isEmpty()) {
                profiles.add(createDefaultProfile());
            }
        } catch (Exception e) {
            Gdx.app.error("ProfileManager", "Error loading all profiles, creating default record", e);
            if (profiles.isEmpty()) {
                profiles.add(createDefaultProfile());
            }
        }
        
        return profiles;
    }
    
    /**
     * Create default player profile record
     * rank: 1, name: "Default", score: 0, time: 0
     * 
     * @return Default PlayerProfile object
     */
    private PlayerProfile createDefaultProfile() {
        PlayerProfile defaultProfile = new PlayerProfile("Default");
        defaultProfile.setScore(0);
        defaultProfile.setTotalTime(0f);
        return defaultProfile;
    }
}
