package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameConstants;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Moving bomb that reuses Trap-like animation + hitbox logic,
 * but patrols horizontally or vertically between min/max bounds.
 */
public class MovingBomb extends MazeObjects {

    private Animation<TextureRegion> animation;

    private final Vector2 position;
    private final Rectangle hitbox;

    private boolean triggered = false;
    private float animationTimer = 0f;

    // Movement
    private final boolean horizontal;
    private final float minPos;
    private final float maxPos;
    private float speed; // pixels/sec

    // Match Trap's drawing scale/offset style
    private final float scale = 0.65f;
    private final float size = 32f * scale;
    private final float offset = (32f - size) / 2f;
    
    // Reusable Rectangle for collision detection
    private static final Rectangle tempWallRect = new Rectangle();
    private static final Rectangle tempCheckRect = new Rectangle();

    public MovingBomb(float x, float y, boolean horizontal, float minPos, float maxPos, float speed) {
        this.position = new Vector2(x, y);
        this.horizontal = horizontal;

        // ensure correct ordering
        this.minPos = Math.min(minPos, maxPos);
        this.maxPos = Math.max(minPos, maxPos);

        this.speed = speed;

        // Match Trap hitbox style: (position + offset, 13x8)
        this.hitbox = new Rectangle(position.x + offset, position.y + offset, 13, 8);
    }

    public void update(float delta, MazeRunnerGame game) {
        animationTimer += delta;

        if (triggered) {
            return;
        }

        if (horizontal) {
            float newX = position.x + speed * delta;
            tempCheckRect.set(newX + offset, position.y + offset, hitbox.width, hitbox.height);
            
            if (newX <= minPos) {
                position.x = minPos;
                speed = Math.abs(speed);
            } else if (newX >= maxPos) {
                position.x = maxPos;
                speed = -Math.abs(speed);
            } else if (!hasWallAt(tempCheckRect, game)) {
                position.x = newX;
            } else {
                speed = -speed;
            }
        } else {
            float newY = position.y + speed * delta;
            tempCheckRect.set(position.x + offset, newY + offset, hitbox.width, hitbox.height);
            
            if (newY <= minPos) {
                position.y = minPos;
                speed = Math.abs(speed);
            } else if (newY >= maxPos) {
                position.y = maxPos;
                speed = -Math.abs(speed);
            } else if (!hasWallAt(tempCheckRect, game)) {
                position.y = newY;
            } else {
                speed = -speed;
            }
        }

        hitbox.setPosition(position.x + offset, position.y + offset);
    }

    public void trigger() {
        if (!triggered) {
            triggered = true;
            animationTimer = 0f;
        }
    }

    public boolean isFinished() {
        return triggered && animation != null && animation.isAnimationFinished(animationTimer);
    }

    public void render(SpriteBatch batch, float delta) {
        if (animation == null) return;
        if (isFinished()) return;

        TextureRegion currentFrame;
        if (!triggered) {
            float frameDuration = animation.getFrameDuration();
            float loopTime = frameDuration * 4f;
            float loopTimer = animationTimer % loopTime;
            currentFrame = animation.getKeyFrame(loopTimer);
        } else {
            currentFrame = animation.getKeyFrame(animationTimer);
        }

        if (currentFrame != null) {
            batch.draw(currentFrame,
                    position.x + offset - 3,
                    position.y + offset,
                    size, size);
        }
    }

    // Getters / setters (same style as Trap)
    public Animation<TextureRegion> getAnimation() { return animation; }
    public void setAnimation(Animation<TextureRegion> animation) { this.animation = animation; }

    public boolean isTriggered() { return triggered; }

    public Vector2 getPosition() { return position; }

    public Rectangle getHitbox() { return hitbox; }
    
    private boolean hasWallAt(Rectangle checkRect, MazeRunnerGame game) {
        float tileSize = GameConstants.TILE_SIZE;
        var map = game.getMazeLayout();
        int currentLevel = game.getSelectedLevel();
        
        if (currentLevel == 2 && !game.hasLight()) {
            int checkTileX = (int)(checkRect.x / tileSize);
            if (checkTileX >= GameConstants.LEVEL2_DARKNESS_BOUNDARY_X) {
                return true;
            }
        }

        for (var entry : map.entrySet()) {
            int tileType = entry.getValue();
            if (tileType == GameConstants.TILE_WALL || tileType == 8 || 
                tileType == GameConstants.TILE_TRAPDOOR || 
                tileType == GameConstants.TILE_DOOR) {
                int tileX = entry.getKey().getKey();
                int tileY = entry.getKey().getValue();
                float wallX = tileX * tileSize;
                float wallY = tileY * tileSize;

                tempWallRect.set(wallX, wallY, tileSize, tileSize);

                if (checkRect.overlaps(tempWallRect)) {
                    
                    if (currentLevel == 2 && tileType == GameConstants.TILE_WALL && 
                        tileX >= GameConstants.LEVEL2_SPECIAL_WALL_MIN_X && 
                        tileX <= GameConstants.LEVEL2_SPECIAL_WALL_MAX_X && 
                        tileY == GameConstants.LEVEL2_SPECIAL_WALL_Y) {
                        continue;
                    }
                    
                    if (currentLevel == 4 && tileType == GameConstants.TILE_DOOR) {
                        for (Door door : game.getDoors()) {
                            if (door.getTileX() == tileX && door.getTileY() == tileY) {
                                return !door.isOpen();
                            }
                        }
                        return true;
                    }
                    
                    if (!isSpecialTilePassable(tileX, tileY, tileType, currentLevel, game)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if special tile is passable
     * @param tileX Tile X coordinate
     * @param tileY Tile Y coordinate
     * @param tileType Tile type number
     * @param level Current level
     * @param game Game object
     * @return true means passable, false means it's a wall
     */
    private boolean isSpecialTilePassable(int tileX, int tileY, int tileType, int level, MazeRunnerGame game) {
        switch (tileType) {
            case 8: // Track/rail tile
                return true;
                
            case GameConstants.TILE_TRAPDOOR:
                return game.hasSparkPlug();
        }
        
        return false;
    }
}
