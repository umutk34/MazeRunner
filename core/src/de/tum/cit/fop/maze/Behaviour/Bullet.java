package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;
import java.util.List;
import java.util.Map;

/**
 * Simple bullet entity: fired from player's front, automatically flies toward nearest enemy,
 * destroyed when hitting wall or enemy.
 */
public class Bullet {
    // Speed = 130% of original, size = 40% of original
    private static final float BULLET_SPEED = 260f * 1.3f;   // ≈ 338f
    private static final float BULLET_SIZE = 16f * 0.4f;    // ≈ 6.4f

    private final MazeRunnerGame game;
    private final TextureRegion sprite;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Rectangle hitbox = new Rectangle();
    private final boolean homing; // Whether automatically tracks nearest enemy

    private boolean alive = true;

    public Bullet(MazeRunnerGame game, TextureRegion sprite, Vector2 spawnPos, Vector2 initialDir, boolean homing) {
        this.game = game;
        this.sprite = sprite;
        this.position.set(spawnPos);
        this.homing = homing;
        setDirection(initialDir);
        this.hitbox.set(position.x, position.y, BULLET_SIZE, BULLET_SIZE);
    }

    public boolean isAlive() {
        return alive;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    private void setDirection(Vector2 dir) {
        if (dir.isZero(0.001f)) {
            // Prevent zero vector
            dir.set(1, 0);
        }
        this.velocity.set(dir).nor().scl(BULLET_SPEED);
    }

    /**
     * Update per frame: move forward in straight line at current speed, and detect collisions with walls, enemies, and Boss.
     */
    public void update(float delta, List<Enemy> enemies, Boss boss) {
        if (!alive) return;

        // 1. If tracking needed, adjust direction toward nearest enemy / Boss once this frame
        if (homing) {
            Vector2 bestDir = null;
            float bestDist2 = Float.MAX_VALUE;

            // 1.1 First consider Boss (if exists), let bullet prioritize tracking Boss
            if (boss != null && !boss.isDead() && !boss.isRemoved()) {
                Rectangle bossBox = boss.getHitbox();
                if (bossBox != null) {
                    Vector2 bossCenter = new Vector2(
                            bossBox.x + bossBox.width / 2f,
                            bossBox.y + bossBox.height / 2f
                    );
                    bestDir = bossCenter.cpy().sub(position);
                    bestDist2 = bestDir.len2();
                }
            }

            // 1.2 Then consider nearest normal enemy (if closer, override Boss)
            Enemy nearest = findNearestEnemy(enemies);
            if (nearest != null) {
                Vector2 toEnemy = nearest.getPosition().cpy().sub(position);
                float d2 = toEnemy.len2();
                if (d2 < bestDist2) {
                    bestDir = toEnemy;
                    bestDist2 = d2;
                }
            }

            if (bestDir != null && bestDist2 > 0.0001f) {
                setDirection(bestDir);
            }
        }

        // 2. Test movement
        float nextX = position.x + velocity.x * delta;
        float nextY = position.y + velocity.y * delta;
        Rectangle testRect = new Rectangle(nextX, nextY, BULLET_SIZE, BULLET_SIZE);

        // 2.1 Destroy if hits wall
        if (hitsWall(testRect)) {
            alive = false;
            return;
        }

        // 2.2 Detect if hit enemy
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            if (testRect.overlaps(enemy.getHitbox())) {
                enemy.takeDamage(1f);
                alive = false;
                return;
            }
        }

        // 2.3 Detect if hit Boss (ranged attack: each hit deals damage, not affected by Boss melee cooldown)
        if (boss != null && !boss.isDead() && !boss.isRemoved()) {
            Rectangle bossBox = boss.getHitbox();
            if (bossBox != null && testRect.overlaps(bossBox)) {
                boss.takeDamage(1, Boss.DamageType.RANGED);
                alive = false;
                return;
            }
        }

        // 3. Move and update hitbox
        position.set(nextX, nextY);
        hitbox.setPosition(position.x, position.y);
    }

    /**
     * Use very simple wall detection: any tile of type 0 (normal wall) or 10/13 is considered blocking.
     */
    private boolean hitsWall(Rectangle rect) {
        Map<Map.Entry<Integer, Integer>, Integer> map = game.getMazeLayout();
        if (map == null) return false;

        float tileSize = 32f;

        for (Map.Entry<Map.Entry<Integer, Integer>, Integer> entry : map.entrySet()) {
            int type = entry.getValue();
            if (!(type == 0 || type == 10 || type == 13)) continue;

            int tileX = entry.getKey().getKey();
            int tileY = entry.getKey().getValue();
            float wallX = tileX * tileSize;
            float wallY = tileY * tileSize;

            Rectangle wallRect = new Rectangle(wallX, wallY, tileSize, tileSize);
            if (rect.overlaps(wallRect)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find nearest enemy to bullet's current position in given list (ignore dead enemies).
     */
    private Enemy findNearestEnemy(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) return null;

        Enemy nearest = null;
        float bestDist2 = Float.MAX_VALUE;

        for (Enemy enemy : enemies) {
            if (enemy == null || enemy.isDead()) continue;
            float d2 = enemy.getPosition().dst2(position);
            if (d2 < bestDist2) {
                bestDist2 = d2;
                nearest = enemy;
            }
        }
        return nearest;
    }

    public void render(Batch batch) {
        if (!alive) return;
        float drawSize = BULLET_SIZE;
        batch.draw(sprite, position.x, position.y, drawSize, drawSize);
    }
}

