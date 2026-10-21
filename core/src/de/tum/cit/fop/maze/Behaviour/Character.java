package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.Map;

public abstract class Character {
    public Vector2 position;        // Current position [cite: 2664]
    protected Vector2 nextPosition; //  [cite: 2665]
    protected Rectangle hitbox;     //  [cite: 2665, 2706]
    protected float speed;          // Move speed [cite: 2665, 2678]
    protected int lives;            // HP [cite: 2665, 2679]
    // Animation
    public Animation<TextureRegion> animUp, animDown, animLeft, animRight; // 四向走 [cite: 2667]
    protected Animation<TextureRegion> currentAnimation;  // 当前正在播的动画 [cite: 2668]
    protected float animationTimer; // 动画计时器 [cite: 2668, 2683]

    protected boolean hasWallAt(Rectangle checkRect, MazeRunnerGame game) {
        float tileSize = 32f;
        var map=game.getMazeLayout();
        for (var entry : map.entrySet()) {
            if (entry.getValue() == 0) {
                float wallX = entry.getKey().getKey() * tileSize;
                float wallY = entry.getKey().getValue() * tileSize;

                Rectangle wallRect = new Rectangle(wallX, wallY, tileSize, tileSize);

                if (checkRect.overlaps(wallRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    public abstract void update(float delta, MazeRunnerGame game);

    public void render(SpriteBatch spriteBatch, float delta, int width, int height, float offsetX, float offsetY) {
        animationTimer += delta; // 更新动画时间 [cite: 2683, 2724]
        // 获取当前应该播放的那一帧 [cite: 2724]
        TextureRegion currentFrame = currentAnimation.getKeyFrame(animationTimer, true);
        // Draw！ [cite: 2725]
        spriteBatch.draw(currentFrame, position.x + offsetX, position.y + offsetY, width, height);
    }

    //Getters and Setters
    public Vector2 getNextPosition() {return nextPosition;}
    public Animation<TextureRegion> getAnimUp() {return animUp;}
    public Animation<TextureRegion> getAnimDown() {return animDown;}
    public Animation<TextureRegion> getAnimLeft() {return animLeft;}
    public Animation<TextureRegion> getAnimRight() {return animRight;}
    public Rectangle getHitbox() { return hitbox; }
    public float getSpeed() { return speed; }
    public Vector2 getPosition() { return position; }
    public void setCurrentAnimation(Animation<TextureRegion> animation) {this.currentAnimation = animation;}
    public void setAnimations(Animation<TextureRegion> up, Animation<TextureRegion> down,
                              Animation<TextureRegion> left, Animation<TextureRegion> right) {
        this.animUp = up;
        this.animDown = down;
        this.animLeft = left;
        this.animRight = right;
    }
}
