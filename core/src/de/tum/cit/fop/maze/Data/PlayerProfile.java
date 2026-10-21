package de.tum.cit.fop.maze.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Player save data model class
 * Used for JSON serialization and deserialization
 */
public class PlayerProfile {
    private String playerName;
    private int score;
    private float totalTime;
    private List<String> unlockedWeapons;
    private String currentWeapon;
    private boolean hasPet = false;

    /**
     * No-argument constructor (required by LibGDX Json class)
     */
    public PlayerProfile() {
        this.playerName = "";
        this.score = 0;
        this.totalTime = 0f;
        this.unlockedWeapons = new ArrayList<>();
        this.currentWeapon = "hand";
        this.unlockedWeapons.add("hand");
    }

    /**
     * Constructor with parameters
     */
    public PlayerProfile(String playerName) {
        this();
        this.playerName = playerName;
    }

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }

    public List<String> getUnlockedWeapons() {
        return unlockedWeapons;
    }

    public void setUnlockedWeapons(List<String> unlockedWeapons) {
        this.unlockedWeapons = unlockedWeapons;
    }

    public String getCurrentWeapon() {
        return currentWeapon;
    }

    public void setCurrentWeapon(String currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    /**
     * Add unlocked weapon
     */
    public void unlockWeapon(String weaponId) {
        if (!unlockedWeapons.contains(weaponId)) {
            unlockedWeapons.add(weaponId);
        }
    }

    /**
     * Check if weapon is unlocked
     */
    public boolean isWeaponUnlocked(String weaponId) {
        return unlockedWeapons.contains(weaponId);
    }

    public boolean hasPet() {
        return hasPet;
    }

    public void setHasPet(boolean hasPet) {
        this.hasPet = hasPet;
    }
}
