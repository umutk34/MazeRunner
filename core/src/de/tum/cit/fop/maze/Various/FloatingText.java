package de.tum.cit.fop.maze.Various;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Floating text effect class
 * Used to display hint text when picking up items, killing enemies, etc.
 * Text slowly rises and gradually fades away
 */
public class FloatingText {
    private String text;
    private Vector2 position;
    private float timer;
    private float duration;
    private Color color;
    private BitmapFont font;
    private float scale = 0.3f;
    
    private static final float RISE_SPEED = 30f;
    private static final float DEFAULT_DURATION = 1.5f;
    
    public FloatingText(String text, float x, float y, Color color, BitmapFont font) {
        this.text = text;
        this.position = new Vector2(x, y);
        this.timer = 0f;
        this.duration = DEFAULT_DURATION;
        this.color = color;
        this.font = font;
    }
    
    /**
     * Update floating text (called every frame)
     * @param delta Time interval
     * @return true means still displaying, false means can be removed
     */
    public boolean update(float delta) {
        timer += delta;
        position.y += RISE_SPEED * delta;
        return timer < duration;
    }
    
    /**
     * Draw floating text
     */
    public void render(SpriteBatch batch) {
        if (font == null || !batch.isDrawing()) return;
        
        float alpha = 1f - (timer / duration);
        alpha = Math.max(0f, Math.min(1f, alpha));
        
        Color oldColor = font.getColor();
        float oldScale = font.getData().scaleX;
        Color renderColor = color.cpy();
        renderColor.a = alpha;
        font.setColor(renderColor);
        
        font.getData().setScale(scale);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, text);
        float textWidth = layout.width;
        
        font.draw(batch, text, position.x - textWidth / 2f, position.y);
        
        font.setColor(oldColor);
        font.getData().setScale(oldScale);
    }
    
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Set current floating text scale (multiplier relative to original font size)
     * For example: 0.3f means 30%, 0.5f means 50%
     */
    public void setScale(float scale) {
        this.scale = scale;
    }
}
