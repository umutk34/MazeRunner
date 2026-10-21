package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Shield pickup: grants player one-time damage immunity
 * Inherits from Pickup, unifies position / collision box / collected state.
 */
public class Shield extends Pickup {
    private Texture texture;

    public Shield(float x, float y, Texture shieldTexture) {
        super(x, y, 32, 32);
        this.texture = shieldTexture;
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!collected && texture != null) {
            float scale = 0.6f;
            float size = 32 * scale;
            float offset = (32 - size) / 2f;

            // Render in gold color
            batch.setColor(1f, 0.84f, 0f, 1f); // Gold color

            batch.draw(texture,
                    position.x + offset,
                    position.y + offset,
                    size,
                    size);
            
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }
}
