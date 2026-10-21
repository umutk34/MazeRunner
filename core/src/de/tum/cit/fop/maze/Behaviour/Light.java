package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Light pickup (type 21, only appears in Level 2): clears fog in right half of Level 2 after pickup
 * Uses Items/Light.png with 4 frames animation
 */
public class Light extends Pickup {
    private Animation<TextureRegion> animation;
    private float animationTime = 0f;

    public Light(float x, float y, Animation<TextureRegion> lightAnim) {
        super(x, y, 32, 32);
        this.animation = lightAnim;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!collected && animation != null) {
            animationTime += delta;
            TextureRegion currentFrame = animation.getKeyFrame(animationTime, true);
            
            if (currentFrame != null) {
                // Reduce display size to match other items
                float scale = 0.6f; // Scale to 60% size (approximately 19x19)
                float size = 32 * scale;
                float offset = (32 - size) / 2f; // Calculate offset to center it

                batch.draw(currentFrame,
                        position.x + offset,
                        position.y + offset,
                        size,
                        size);
            }
        }
    }
}
