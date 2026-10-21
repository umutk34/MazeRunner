package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Score pickup: handles coins and Money, they only differ in texture/animation and score value.
 * Inherits from Pickup, unifies position / collision box / collected state.
 */
public class Coin extends Pickup {
    private Texture texture;
    private TextureRegion[] frames;
    private float animTimer = 0f;
    private float frameDuration = 0.1f;
    private int value;

    public Coin(float x, float y, Texture coinTexture, int value) {
        super(x, y, 32, 32);
        this.texture = coinTexture;
        this.value = value;

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

    @Override
    public void render(SpriteBatch batch, float delta) {
        if (!collected && texture != null) {
            if (frames != null) {
                animTimer += delta;
            }

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
    }

    public int getValue() { return value; }
}
