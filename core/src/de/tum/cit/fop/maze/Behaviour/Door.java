package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Level 4: Door
 * Opens when corresponding pressure plate is activated
 */
public class Door {
    private int tileX;
    private int tileY;
    private Vector2 position;
    private Rectangle hitbox;
    private boolean isOpen = false;
    private int doorId;
    private Texture closedTexture;
    private Texture openTexture;
    
    public Door(int tileX, int tileY, int doorId, Texture closedTexture, Texture openTexture) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.position = new Vector2(tileX * 32f, tileY * 32f);
        this.hitbox = new Rectangle(position.x, position.y, 32, 32);
        this.doorId = doorId;
        this.closedTexture = closedTexture;
        this.openTexture = openTexture;
    }
    
    public void update(boolean activated) {
        if (activated && !isOpen) {
            isOpen = true;
            // Can play door opening sound effect
        } else if (!activated && isOpen) {
            isOpen = false;
        }
    }
    
    public void render(SpriteBatch batch) {
        if (isOpen && openTexture != null) {
            batch.draw(openTexture, position.x, position.y, 32, 32);
        } else if (!isOpen && closedTexture != null) {
            batch.draw(closedTexture, position.x, position.y, 32, 32);
        }
    }
    
    // Getters and Setters
    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }
    public Vector2 getPosition() { return position; }
    public Rectangle getHitbox() { return hitbox; }
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { this.isOpen = open; }
    public int getDoorId() { return doorId; }
}
