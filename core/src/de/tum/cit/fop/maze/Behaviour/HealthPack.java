package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Buff pickup: health pack + speed boost, uses type to distinguish specific effects.
 * Inherits from Pickup, unifies position / collision box / collected state.
 */
public class HealthPack extends Pickup {
    public enum BuffType {
        HEALTH, // Add health
        SPEED   // Speed boost
    }

    private Texture texture;
    private BuffType type;

    public HealthPack(float x, float y, Texture hpTexture) {
        this(x, y, hpTexture, BuffType.HEALTH);
    }

    public HealthPack(float x, float y, Texture tex, BuffType type) {
        super(x, y, 32, 32);
        this.texture = tex;
        this.type = type;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!collected && texture != null) {
            float scale = 0.6f;
            float size = 32 * scale;
            float offset = (32 - size) / 2f;

            if (type == BuffType.SPEED) {
                batch.setColor(0.5f, 0f, 1f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }

            batch.draw(texture,
                    position.x + offset,
                    position.y + offset,
                    size,
                    size);
            
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    public BuffType getType() { return type; }
}
