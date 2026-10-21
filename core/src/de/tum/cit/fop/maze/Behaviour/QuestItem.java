package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class QuestItem {
    public enum ItemType { SPARK_PLUG, BATTERY, TIRES, ENGINE, AI_CORE}

    public Vector2 position;
    public Rectangle hitbox;
    private Texture texture;
    private ItemType type;
    private boolean collected = false;

    public QuestItem(float x, float y, int level, Texture sparkPlugTex, Texture batteryTex, Texture tiresTex, Texture engineTex, Texture aiCoreTex) {
        this.position = new Vector2(x, y);
        // 拾取判定区域，32x32 刚好覆盖一个格子
        this.hitbox = new Rectangle(x, y, 32, 32);

        if (level == 1) {
            this.texture = sparkPlugTex;
            this.type = ItemType.SPARK_PLUG;
        } else if (level == 2) {
            this.texture = batteryTex;
            this.type = ItemType.BATTERY;
        } else if (level == 3) {
            this.texture = tiresTex;
            this.type = ItemType.TIRES;
        }else if (level == 4) {
            this.texture = engineTex;
            this.type = ItemType.ENGINE;
        }else if (level == 5) {
            this.texture = aiCoreTex;
            this.type = ItemType.AI_CORE;
        }
    }

    public void render(SpriteBatch batch) {
        if (!collected && texture != null) {
            batch.draw(texture, position.x, position.y, 32, 32);
        }
    }

    public ItemType getType() { return type; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}