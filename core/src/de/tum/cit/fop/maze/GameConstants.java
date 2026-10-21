package de.tum.cit.fop.maze;

/**
 * Game constants to replace magic numbers throughout the codebase.
 * This improves maintainability and makes the code more readable.
 */
public class GameConstants {
    
    // Tile and Map Constants
    public static final float TILE_SIZE = 32f;
    public static final int MAX_LEVEL = 5;
    
    // Tile Type Constants
    public static final int TILE_WALL = 0;
    public static final int TILE_ENTRANCE = 1;
    public static final int TILE_EXIT = 2;
    public static final int TILE_TRAP = 3;
    public static final int TILE_ENEMY_SPAWN = 4;
    public static final int TILE_QUEST_ITEM = 5;
    public static final int TILE_HEALTH_PACK = 6;
    public static final int TILE_COIN_SMALL = 7;
    public static final int TILE_COIN_LARGE = 8;
    public static final int TILE_TOOL = 9;
    public static final int TILE_TRAPDOOR = 10;
    public static final int TILE_SHIELD = 11;
    public static final int TILE_DOOR = 13;
    public static final int TILE_LIGHT = 21;
    public static final int TILE_BOSS_SPAWN = 19;
    public static final int TILE_PORTAL = 100;
    public static final int TILE_PORTAL_101 = 101;
    
    // Animation Constants
    public static final float ANIMATION_FRAME_DURATION_DEFAULT = 0.1f;
    public static final float ANIMATION_FRAME_DURATION_FAST = 0.07f;
    public static final float ANIMATION_FRAME_DURATION_SLOW = 0.15f;
    public static final float ANIMATION_FRAME_DURATION_VERY_SLOW = 0.24f;
    
    // Player Constants
    public static final float PLAYER_MAX_SPEED = 130f;
    public static final float PLAYER_ACCELERATION = 900f;
    public static final float PLAYER_FRICTION = 450f;
    public static final float PLAYER_SPRINT_MULTIPLIER = 1.7f;
    public static final float PLAYER_SPRINT_COST = 30f;
    public static final float PLAYER_ENERGY_REGEN_RATE = 15f;
    public static final float PLAYER_MAX_ENERGY = 100f;
    public static final float PLAYER_ATTACK_DURATION = 0.4f;
    public static final float PLAYER_HIT_COOLDOWN_TIME = 2.0f;
    public static final int PLAYER_DEFAULT_HEALTH = 4;
    public static final int PLAYER_DEFAULT_MAX_HEALTH = 4;
    
    // Enemy Constants
    public static final float ENEMY_DETECTION_RANGE = 175f;
    public static final float ENEMY_DEFAULT_SPEED = 50f;
    public static final int ENEMY_DEFAULT_HEALTH = 3;
    public static final float ENEMY_HIT_COOLDOWN = 0.5f;
    public static final float ENEMY_PATROL_TIMER = 3f;
    
    // Movement Constants
    public static final float MOVING_BOMB_PATROL_DISTANCE = 3f; // in tiles
    public static final float MOVING_BOMB_SPEED = 90f;
    public static final int MOVING_BOMB_MAX_STEPS = 4;
    
    // Level 2 Specific
    public static final int LEVEL2_DARKNESS_BOUNDARY_X = 20;
    public static final int LEVEL2_SPECIAL_WALL_MIN_X = -13;
    public static final int LEVEL2_SPECIAL_WALL_MAX_X = -1;
    public static final int LEVEL2_SPECIAL_WALL_Y = 1;
    
    // Camera Constants
    public static final float CAMERA_NORMAL_ZOOM = 0.25f;
    public static final float CAMERA_NORMAL_ZOOM_SHIFT = 0.4f;
    public static final float CAMERA_INTRO_ZOOM_START = 0.5f;
    public static final float CAMERA_LIGHT_ZOOM_OUT = 0.7f;
    
    // Screen Shake Constants
    public static final float SHAKE_DURATION = 0.2f;
    public static final float SHAKE_INTENSITY = 6f;
    public static final float HIT_STOP_DURATION = 0.02f;
    
    // Portal Constants
    public static final float PORTAL_TELEPORT_COOLDOWN_TIME = 1.0f;
    
    // City Background Constants
    public static final float CITY_SCROLL_SPEED_DEFAULT = 180f;
    public static final float CITY_SCROLL_SPEED_LAYER_2 = 170f;
    public static final float CITY_SCROLL_SPEED_LAYER_3 = 190f;
    public static final float CITY_SCROLL_SPEED_LAYER_4 = 175f;
    public static final float CITY_SCROLL_SPEED_LAYER_5 = 185f;
    public static final float CITY_SCROLL_SPEED_LAYER_6 = 200f;
    
    // Menu Music Constants
    public static final int MAX_MENU_MUSIC_INDEX = 6;
    public static final int MAX_CITY_INDEX = 6;
    
    // Safe Position Update
    public static final float SAFE_POSITION_UPDATE_INTERVAL = 0.5f;
    
    // Unstuck Constants
    public static final float UNSTUCK_MIN_MOVE_DISTANCE = 2.0f;
    
    // Character Size Constants
    public static final float CHARACTER_WIDTH = 12f;
    public static final float CHARACTER_HEIGHT = 18f;
    public static final float ENEMY_WIDTH = 10f;
    public static final float ENEMY_HEIGHT = 16f;
    
    // Boss Constants
    public static final float BOSS_BASE_SIZE = 64f;
    public static final float BOSS_SIZE_MULTIPLIER = 3f;
    public static final float BOSS_HIT_COOLDOWN_TIME = 0.5f;
    
    // Pet Constants
    public static final float PET_WIDTH = 24f;
    public static final float PET_HEIGHT = 24f;
    
    // Attack Constants
    public static final float ATTACK_HITBOX_SIZE = 96f;
    public static final float ATTACK_HITBOX_WIDTH_REDUCTION = 0.8f;
    public static final float ATTACK_HITBOX_HEIGHT_UP_DOWN = 60f;
    public static final float ATTACK_HITBOX_HEIGHT_LEFT_RIGHT = 0.5f;
    public static final float ATTACK_HITBOX_WIDTH_LEFT_RIGHT = 0.9f;
    public static final float ATTACK_ACTIVE_START = 0.15f;
    public static final float ATTACK_ACTIVE_END = 0.35f;
    
    // Animation Frame Counts
    public static final int ANIMATION_FRAME_COUNT_9 = 9;
    public static final int ANIMATION_FRAME_COUNT_8 = 8;
    public static final int ANIMATION_FRAME_COUNT_6 = 6;
    public static final int ANIMATION_FRAME_COUNT_4 = 4;
    public static final int ANIMATION_FRAME_COUNT_5 = 5;
    public static final int ANIMATION_FRAME_COUNT_7 = 7;
    
    // Texture Region Sizes
    public static final int TEXTURE_REGION_SIZE_64 = 64;
    public static final int TEXTURE_REGION_SIZE_32 = 32;
    public static final int TEXTURE_REGION_SIZE_192 = 192;
    
    // Trapdoor Constants
    public static final int TRAPDOOR_FRAME_COUNT = 6;
    public static final int TRAPDOOR_OPEN_FRAME_COUNT = 5;
    
    // Tool Constants
    public static final int TOOL_FRAME_COUNT = 8;
    
    // Light Constants
    public static final int LIGHT_FRAME_COUNT = 4;
    
    // Boss Animation Frame Counts
    public static final int BOSS_FRAME_COUNT_8 = 8;
    public static final int BOSS_SUMMON_WINDUP_FRAMES = 4;
    public static final int BOSS_SUMMON_ATTACK_FRAMES = 3;
    
    // Pet Animation Frame Counts
    public static final int PET_WALK_FRAME_COUNT = 4;
    public static final int PET_ATTACK_FRAME_COUNT = 6;
    public static final int PET_ATTACK3_WINDUP_FRAMES = 2;
    public static final int PET_ATTACK3_CHARGE_FRAMES = 4;
    
    // Death Animation
    public static final int DEATH_ANIMATION_FRAME_COUNT = 6;
    
    // Coin Values
    public static final int COIN_VALUE_SMALL = 10;
    public static final int COIN_VALUE_LARGE = 50;
    
    // Default Positions
    public static final float DEFAULT_PLAYER_X = 256f;
    public static final float DEFAULT_PLAYER_Y = 192f;
    
    // Prevent instantiation
    private GameConstants() {
        throw new AssertionError("Cannot instantiate GameConstants");
    }
}
