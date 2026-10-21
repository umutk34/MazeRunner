package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class Enemy extends Character {
    public Enemy(float x, float y, float width, float height) {
        super();
        this.position = new Vector2(x, y);
        this.nextPosition = new Vector2(0, 0);
        this.hitbox = new Rectangle(x, y, width, height);
        this.speed = 20f;
    }

    float moveTimer = 0;
    boolean movingRight = true;
    @Override
    public void update(float delta, MazeRunnerGame game) {
        moveTimer += delta;
        if (moveTimer > 3f) {
            movingRight = !movingRight;
            moveTimer = 0;
        }
        float moverange = speed * delta;
        if (movingRight) {
            position.x += moverange;
            currentAnimation = animRight;
        }
        else  {
            position.x -= moverange;
            currentAnimation = animLeft;
        }
        hitbox.setPosition(position.x, position.y);
        animationTimer += delta;
    }
}
