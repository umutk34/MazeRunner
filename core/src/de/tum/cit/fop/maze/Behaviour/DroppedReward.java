package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Dropped reward item that jumps out from boss position with physics
 */
public class DroppedReward {
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private TextureRegion[] frames;
    private float animTimer = 0f;
    private float frameDuration = 0.1f;
    private int value;
    private boolean collected = false;
    private float gravity = 400f;
    private float bounceDamping = 0.6f;
    private float groundY;
    private boolean onGround = false;
    private float size = 32f;
    private WallCollisionChecker wallChecker;
    
    public DroppedReward(float startX, float startY, float horizontalAngle, float verticalAngle, 
                        float horizontalSpeed, float verticalSpeed,
                        Texture coinTexture, int value, float groundY, WallCollisionChecker wallChecker) {
        this.position = new Vector2(startX, startY);
        // Calculate velocity for 3D-like explosion in 2D:
        // horizontalAngle: 0-360 degrees controls direction in X-Y plane
        // verticalAngle: 30-80 degrees controls how much upward velocity (higher = more upward)
        // Combine both to create explosion effect
        
        // Horizontal component in X-Y plane
        float horizontalX = MathUtils.cosDeg(horizontalAngle) * horizontalSpeed;
        float horizontalY = MathUtils.sinDeg(horizontalAngle) * horizontalSpeed;
        
        // Vertical component (upward)
        float upwardY = MathUtils.sinDeg(verticalAngle) * verticalSpeed;
        
        // Combine: X from horizontal, Y from both horizontal and vertical
        this.velocity = new Vector2(
            horizontalX,
            horizontalY + upwardY
        );
        this.texture = coinTexture;
        this.value = value;
        this.groundY = groundY;
        this.wallChecker = wallChecker;
        
        if (coinTexture != null && coinTexture.getWidth() % 6 == 0) {
            int frameWidth = coinTexture.getWidth() / 6;
            int frameHeight = coinTexture.getHeight();
            frames = new TextureRegion[6];
            for (int i = 0; i < 6; i++) {
                frames[i] = new TextureRegion(coinTexture, i * frameWidth, 0, frameWidth, frameHeight);
            }
        } else {
            frames = null;
        }
    }
    
    public void update(float delta) {
        if (collected) return;
        
        animTimer += delta;
        
        // Apply gravity
        if (!onGround) {
            velocity.y -= gravity * delta;
            
            // Calculate new position
            Vector2 newPosition = new Vector2(position);
            newPosition.add(velocity.x * delta, velocity.y * delta);
            
            // Check wall collision
            Rectangle newHitbox = new Rectangle(newPosition.x, newPosition.y, size, size);
            if (wallChecker != null && wallChecker.hasWallAt(newHitbox)) {
                // Bounce off wall
                velocity.x *= -0.5f; // Reverse and dampen horizontal velocity
                velocity.y *= 0.8f; // Dampen vertical velocity
            } else {
                position.set(newPosition);
            }
            
            // Check ground collision
            if (position.y <= groundY) {
                position.y = groundY;
                velocity.y *= -bounceDamping;
                velocity.x *= 0.9f; // Friction
                
                if (Math.abs(velocity.y) < 50f) {
                    onGround = true;
                    velocity.y = 0;
                    velocity.x = 0;
                }
            }
        } else {
            // When on ground, still check wall collision for horizontal movement
            Vector2 newPosition = new Vector2(position);
            newPosition.x += velocity.x * delta;
            
            Rectangle newHitbox = new Rectangle(newPosition.x, newPosition.y, size, size);
            if (wallChecker != null && wallChecker.hasWallAt(newHitbox)) {
                velocity.x = 0;
            } else {
                position.x = newPosition.x;
            }
        }
    }
    
    public interface WallCollisionChecker {
        boolean hasWallAt(Rectangle rect);
    }
    
    public void render(SpriteBatch batch, float delta) {
        if (collected || texture == null) return;
        
        float scale = (frames == null) ? 0.42f : 0.71f;
        float size = 32 * scale;
        float offset = (32 - size) / 2f;
        
        if (frames != null) {
            int frameIndex = (int)(animTimer / frameDuration) % frames.length;
            TextureRegion frame = frames[frameIndex];
            batch.draw(frame,
                    position.x + offset,
                    position.y + offset,
                    size,
                    size);
        } else {
            batch.draw(texture,
                    position.x + offset,
                    position.y + offset,
                    size,
                    size);
        }
    }
    
    public Vector2 getPosition() {
        return position;
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    public void setCollected(boolean collected) {
        this.collected = collected;
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean isOnGround() {
        return onGround;
    }
}
