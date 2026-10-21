package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * 拾取物父类：封装位置、碰撞箱和是否已拾取等公共逻辑。
 * 所有具体拾取物（金币 / Money / 血包 / 加速）都应该继承自它。
 */
public abstract class Pickup {
    public Vector2 position;
    public Rectangle hitbox;
    protected boolean collected = false;

    public Pickup(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }

    /**
     * 渲染接口，由子类实现具体绘制逻辑。
     */
    public abstract void render(SpriteBatch batch, float delta);
}

