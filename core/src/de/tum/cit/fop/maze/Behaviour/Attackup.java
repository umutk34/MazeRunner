package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Attack power boost pickup: after pickup, attack power increases to 1.5 for this level
 * Inherits from Pickup, unifies position / collision box / collected state.
 */
public class Attackup extends Pickup {
    private Texture texture;

    public Attackup(float x, float y, Texture attackupTexture) {
        super(x, y, 32, 32);
        this.texture = attackupTexture;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!collected && texture != null) {
            // Reduce display size to match other items
            float scale = 0.6f; // Scale to 60% size (approximately 19x19)
            float size = 32 * scale;
            float offset = (32 - size) / 2f; // Calculate offset to center it

            // Render as orange
            batch.setColor(1f, 0.5f, 0f, 1f); // Orange

            batch.draw(texture,
                    position.x + offset,
                    position.y + offset,
                    size,
                    size);
            
            // Reset color
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }
}
