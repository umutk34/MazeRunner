package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * The Player class represents the hero controlled by the user.
 * It inherits movement logic from Character and adds input handling and sound effects.
 */
public class Player extends Character{
    private final MazeRunnerGame game;
    private int health = 5;
    private boolean hasKey = false;
    private Sound pickupSound;
    private boolean up, down, left, right;
    //漂移移动逻辑
    private Vector2 velocity = new Vector2(0,0);
    private final float maxSpeed = 260f;
    private final float acceleration = 1200f;
    private final float friction = 480f;
    //脱离卡死
    private Vector2 lastSafePosition = new Vector2();
    private float safePointTimer = 0;

    /**
     * @param startX Initial X position.
     * @param startY Initial Y position.
     * @param width  Character width.
     * @param height Character height.
     * @param speed  Movement speed. [cite: 501-502]
     */
    public Player(float startX, float startY, float width, float height, float speed, MazeRunnerGame game) {
        this.game = game;
        this.position = new Vector2(startX, startY);
        this.nextPosition = new Vector2(0, 0);
        this.speed = speed;
        this.hitbox = new Rectangle(startX, startY, width, height);
        this.lastSafePosition.set(startX, startY);
    }
    @Override
    public void update(float delta, MazeRunnerGame game) {
        updateSafePosition(delta);
        Vector2 input = new Vector2(0,0);
        //加速系统
        boolean sprint = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        float speedMultiplier = sprint ? 1.8f : 1f;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) input.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) input.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) input.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) input.x += 1;

        if (input.len() > 0) {
            input.nor();
            velocity.add(input.scl(acceleration * delta * speedMultiplier));
        } else {
            // 没输入时慢慢减速
            float decel = friction * delta;
            if (velocity.len() > decel)
                velocity.sub(velocity.cpy().nor().scl(decel));
            else
                velocity.setZero();
        }

        float currentMax = maxSpeed * speedMultiplier;

        if (velocity.len() > currentMax) {
            velocity.nor().scl(currentMax);
        }

        if (getVelocity().len() > 10f) { // 只有在动的时候才切动画
            float vx = getVelocity().x;
            float vy = getVelocity().y;
            // 比较 X 和 Y 轴哪个分量大，决定播哪个方向
            if (Math.abs(vx) > Math.abs(vy)) {
                currentAnimation = (vx > 0) ? animRight : animLeft;
            } else {
                currentAnimation = (vy > 0) ? animUp : animDown;
            }
        } else {
            animationTimer = 0;
        }

        Rectangle testX = new Rectangle(position.x + delta * velocity.x, position.y, hitbox.width, hitbox.height);
        if (!hasWallAt(testX, game)) {
            position.x += delta * velocity.x;
        }
        else {velocity.x = 0;}
        Rectangle testY = new Rectangle(position.x, position.y + delta * velocity.y, hitbox.width, hitbox.height);
        if (!hasWallAt(testY, game)) {
            position.y += delta * velocity.y;
        }
        else {velocity.y = 0;}
        hitbox.setPosition(position.x,  position.y);

        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            performUnstuck(game);
        }
        nextPosition.set(velocity.x * delta, velocity.y * delta);
    }

    /**
     * Callback method after picking up quest items
     * Here, we define the "special rules" for picking up items in each level.
     */
    public void onPickUp(QuestItem item) {
        switch (item.getType()) {
            case SPARK_PLUG:
                //TODO.......
                break;

            case BATTERY:
                //TODO.......
                break;

            case TIRES:
                //TODO.......
                break;

            case ENGINE:
                //TODO.......
                break;

            case AI_CORE:
                //TODO.......
                break;
        }
    }

    private void updateSafePosition(float delta) {
        safePointTimer += delta;
        if (safePointTimer > 0.5f) {
            lastSafePosition.set(position);
            safePointTimer = 0;
        }
    }

    private void performUnstuck(MazeRunnerGame game) {
        System.out.println("Rewind! Returning to: " + lastSafePosition);
        game.playUnstuckSound();
        // 强制位移到最后一个确定的安全点
        this.position.set(lastSafePosition.x, lastSafePosition.y);
        this.hitbox.setPosition(lastSafePosition.x, lastSafePosition.y);
        this.velocity.setZero();
    }

    // Getters and Setters
    public int getHealth() { return health; }
    public boolean hasKey() { return hasKey; }
    public void collectKey() { this.hasKey = true; }
    public Rectangle getHitbox() { return hitbox; }
    public Vector2 getVelocity() {return velocity;}
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.hitbox.setPosition(x, y);
        this.lastSafePosition.set(x, y);
    }
}
