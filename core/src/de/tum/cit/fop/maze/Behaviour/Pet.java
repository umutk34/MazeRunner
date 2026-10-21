package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.List;

/**
 * Pet AI: implements state machine for follow, detect, attack, retreat
 */
public class Pet extends Character {

    /**
     * Pet state enumeration
     */
    public enum PetState {
        FOLLOW,  // Follow state
        DETECT,  // Detect state
        ATTACK,  // Attack state
        RETURN,  // Retreat state
        ROAM     // Roam state
    }

    private PetState currentState = PetState.FOLLOW;
    private float stateTimer = 0f;

    // Key attributes
    private Character owner;           // Player (owner)
    private Enemy targetEnemy;         // Currently locked enemy
    private float moveSpeed = 150f;
    private float attackRange = 200f;  // Attack range
    private float followRadius = 60f; // Follow radius
    private float detectionRadius = 200f; // Detection radius
    private float attackDistance = 20f;    // Attack distance (distance to deal damage)
    private float maxDistanceFromOwner = 300f; // Maximum distance from owner (beyond this force follow)

    private Vector2 velocity = new Vector2(0, 0);

    // Animations
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private TextureRegion[] attack3WindupFrames;
    private Animation<TextureRegion> attack3ChargeAnim;
    
    private Boss targetBoss;
    private Vector2 chargeStart = new Vector2();
    private Vector2 chargeTarget = new Vector2();
    private Vector2 chargeEnd = new Vector2();
    private float chargeSpeed = 400f;
    private float chargeProgress = 0f;
    private float chargeDuration = 0.6f;
    private boolean chargeDamageDone = false;
    private static final float WINDUP_DURATION = 0.24f;
    
    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_TIME = 4.0f;

    private float attackDamageCooldown = 0f;
    private static final float ATTACK_DAMAGE_COOLDOWN_TIME = 0.5f;
    
    private boolean isFlipped = false;
    
    private Vector2 roamDirection = new Vector2();
    private static final float ROAM_DURATION = 2.0f; // Roam duration (2 seconds)

    /**
     * Constructor
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param width Width
     * @param height Height
     * @param owner Owner (player)
     */
    public Pet(float x, float y, float width, float height, Character owner) {
        this.position = new Vector2(x, y);
        this.nextPosition = new Vector2(0, 0);
        this.hitbox = new Rectangle(x, y, width, height);
        this.speed = moveSpeed;
        this.owner = owner;
        this.currentState = PetState.FOLLOW;
    }

    public void setAnimations(Animation<TextureRegion> walk,
                              Animation<TextureRegion> attack,
                              TextureRegion[] attack3Windup,
                              Animation<TextureRegion> attack3Charge) {
        this.walkAnim = walk;
        this.attackAnim = attack;
        this.attack3WindupFrames = attack3Windup;
        this.attack3ChargeAnim = attack3Charge;
    }

    /**
     * Main update method
     * @param delta Time delta
     * @param game Game object
     * @param enemies Enemy list
     * @param boss Boss object (for charge attack)
     */
    public void update(float delta, MazeRunnerGame game, List<Enemy> enemies, Boss boss) {
        if (owner == null) return;

        stateTimer += delta;
        animationTimer += delta;

        if (attackCooldown > 0f) {
            attackCooldown -= delta;
            if (attackCooldown < 0f) attackCooldown = 0f;
        }
        
        if (attackDamageCooldown > 0f) {
            attackDamageCooldown -= delta;
            if (attackDamageCooldown < 0f) attackDamageCooldown = 0f;
        }

        float distanceToOwner = position.dst(owner.getPosition());
        if (distanceToOwner > maxDistanceFromOwner) {
            currentState = PetState.FOLLOW;
            stateTimer = 0f;
            targetEnemy = null;
            targetBoss = null;
        }

        // State machine logic
        switch (currentState) {
            case FOLLOW:
                updateFollow(delta, game);
                if (distanceToOwner < followRadius) {
                    currentState = PetState.DETECT;
                    stateTimer = 0f;
                }
                break;
            case DETECT:
                updateDetect(delta, game, enemies, boss);
                break;
            case ATTACK:
                updateAttack(delta, game, enemies, boss);
                break;
            case RETURN:
                updateReturn(delta, game);
                break;
            case ROAM:
                updateRoam(delta, game);
                break;
        }
        
        if (currentState != PetState.ATTACK || targetBoss == null) {
            // 第四关：宠物无视墙体，方便追踪主角
            boolean ignoreWalls = (game.getSelectedLevel() == 4);
            
            Rectangle testX = new Rectangle(position.x + velocity.x * delta, position.y, hitbox.width, hitbox.height);
            if ((ignoreWalls || !hasWallAt(testX, game)) && isWithinMapBounds(testX, game)) {
                position.x += velocity.x * delta;
            } else {
                velocity.x = 0;
            }
            
            Rectangle testY = new Rectangle(position.x, position.y + velocity.y * delta, hitbox.width, hitbox.height);
            if ((ignoreWalls || !hasWallAt(testY, game)) && isWithinMapBounds(testY, game)) {
                position.y += velocity.y * delta;
            } else {
                velocity.y = 0;
            }
            
            hitbox.setPosition(position.x, position.y);
        }
    }

    /**
     * FOLLOW state: follow owner
     */
    private void updateFollow(float delta, MazeRunnerGame game) {
        Vector2 ownerPos = owner.getPosition();
        Vector2 target = ownerPos.cpy().add(-32f, 32f);

        Vector2 direction = target.cpy().sub(position);
        float distance = direction.len();

        if (distance > 5f) {
            direction.nor();
            velocity.set(direction.scl(moveSpeed));
            isFlipped = (velocity.x < 0);
        } else {
            velocity.setZero();
            float petCenterX = position.x + hitbox.width / 2f;
            isFlipped = (ownerPos.x < petCenterX);
        }
    }

    /**
     * DETECT state: detect enemies
     */
    private void updateDetect(float delta, MazeRunnerGame game, List<Enemy> enemies, Boss boss) {
        if (boss != null && !boss.isDead() && !boss.isRemoved() && attackCooldown <= 0f) {
            float distanceToBoss = position.dst(boss.getPosition());
            if (distanceToBoss <= detectionRadius) {
                // 第四关禁用roam状态，直接攻击Boss
                if (game.getSelectedLevel() == 4 || Math.random() < 0.5f) {
                    targetBoss = boss;
                    currentState = PetState.ATTACK;
                    stateTimer = 0f;
                    chargeProgress = 0f;
                    chargeDamageDone = false;
                    attackCooldown = ATTACK_COOLDOWN_TIME;
                    
                    Rectangle bossHitbox = boss.getHitbox();
                    float bossCenterX = bossHitbox.x + bossHitbox.width / 2f;
                    float bossCenterY = bossHitbox.y + bossHitbox.height / 2f;
                    
                    chargeStart = position.cpy();
                    chargeTarget = new Vector2(bossCenterX, bossCenterY);
                    
                    Vector2 direction = chargeTarget.cpy().sub(chargeStart).nor();
                    float extendDistance = 100f;
                    chargeEnd = chargeTarget.cpy().add(direction.scl(extendDistance));
                    
                    float petCenterX = position.x + hitbox.width / 2f;
                    isFlipped = (bossCenterX < petCenterX);
                    
                    return;
                } else {
                    currentState = PetState.ROAM;
                    stateTimer = 0f;
                    attackCooldown = ATTACK_COOLDOWN_TIME;
                    float angle = (float)(Math.random() * Math.PI * 2);
                    roamDirection.set((float)Math.cos(angle), (float)Math.sin(angle));
                    return;
                }
            }
        }
        
        Enemy nearestEnemy = null;
        float nearestDistance = detectionRadius;

        for (Enemy enemy : enemies) {
            if (enemy == null || enemy.isDead()) continue;

            float distance = position.dst(enemy.getPosition());
            if (distance < nearestDistance) {
                nearestEnemy = enemy;
                nearestDistance = distance;
            }
        }

        if (nearestEnemy != null) {
            // 检测到敌人立即切换到ATTACK状态
            targetEnemy = nearestEnemy;
            currentState = PetState.ATTACK;
            stateTimer = 0f;
            attackCooldown = ATTACK_COOLDOWN_TIME;
        } else {
            float distanceToOwner = position.dst(owner.getPosition());
            if (distanceToOwner > followRadius) {
                currentState = PetState.FOLLOW;
                stateTimer = 0f;
            }
        }
    }

    /**
     * ATTACK state: charge attack (for Boss) or normal attack (for Enemy)
     * 在攻击过程中，只要怪物碰到宠物的碰撞体积就会扣血
     */
    private void updateAttack(float delta, MazeRunnerGame game, List<Enemy> enemies, Boss boss) {
        if (targetBoss != null) {
            if (targetBoss.isDead() || targetBoss.isRemoved()) {
                targetBoss = null;
                currentState = PetState.FOLLOW;
                stateTimer = 0f;
                return;
            }
            
            if (stateTimer < WINDUP_DURATION) {
                velocity.setZero();
                if (targetBoss != null && !targetBoss.isDead() && !targetBoss.isRemoved()) {
                    Rectangle bossHitbox = targetBoss.getHitbox();
                    float bossCenterX = bossHitbox.x + bossHitbox.width / 2f;
                    float bossCenterY = bossHitbox.y + bossHitbox.height / 2f;
                    chargeTarget.set(bossCenterX, bossCenterY);
                    
                    Vector2 direction = chargeTarget.cpy().sub(chargeStart).nor();
                    float extendDistance = 100f;
                    chargeEnd.set(chargeTarget.cpy().add(direction.scl(extendDistance)));
                }
            } else {
                chargeProgress += delta / chargeDuration;
                if (chargeProgress > 1f) chargeProgress = 1f;
                
                Vector2 targetPos;
                if (chargeProgress < 0.5f) {
                    float t = chargeProgress * 2f;
                    targetPos = chargeStart.cpy().lerp(chargeTarget, t);
                } else {
                    float t = (chargeProgress - 0.5f) * 2f;
                    targetPos = chargeTarget.cpy().lerp(chargeEnd, t);
                }
                
                Vector2 moveDelta = targetPos.cpy().sub(position);
                float moveDistance = moveDelta.len();
                
                if (moveDistance > 0.1f) {
                    moveDelta.nor();
                    float moveStep = chargeSpeed * delta;
                    
                    // 第四关：宠物无视墙体
                    boolean ignoreWalls = (game.getSelectedLevel() == 4);
                    
                    float newX = position.x + moveDelta.x * moveStep;
                    Rectangle testX = new Rectangle(newX, position.y, hitbox.width, hitbox.height);
                    if ((ignoreWalls || !hasWallAt(testX, game)) && isWithinMapBounds(testX, game)) {
                        position.x = newX;
                    }
                    
                    float newY = position.y + moveDelta.y * moveStep;
                    Rectangle testY = new Rectangle(position.x, newY, hitbox.width, hitbox.height);
                    if ((ignoreWalls || !hasWallAt(testY, game)) && isWithinMapBounds(testY, game)) {
                        position.y = newY;
                    }
                } else {
                    position.set(targetPos);
                }
                
                if (!chargeDamageDone && targetBoss != null && 
                    targetBoss.getHitbox().overlaps(hitbox) && chargeProgress >= 0.4f && chargeProgress <= 0.6f) {
                    targetBoss.takeDamage(1, Boss.DamageType.PET_MELEE);
                    chargeDamageDone = true;
                }
                
                if (chargeProgress >= 1f) {
                    currentState = PetState.RETURN;
                    stateTimer = 0f;
                    targetBoss = null;
                    chargeProgress = 0f;
                    chargeDamageDone = false;
                }
            }
            
            hitbox.setPosition(position.x, position.y);
            return;
        }
        
        // 确保hitbox已更新
        hitbox.setPosition(position.x, position.y);
        
        // 在攻击过程中，检查所有敌人的碰撞，只要碰到宠物碰撞体积的怪物都会扣血
        if (enemies != null && attackDamageCooldown <= 0f) {
            boolean damageDealt = false;
            for (Enemy enemy : enemies) {
                if (enemy == null || enemy.isDead()) continue;
                
                // 检查碰撞
                if (hitbox.overlaps(enemy.getHitbox())) {
                    enemy.takeDamage(1);
                    damageDealt = true;
                }
            }
            // 如果对任何敌人造成了伤害，启动冷却时间
            if (damageDealt) {
                attackDamageCooldown = ATTACK_DAMAGE_COOLDOWN_TIME;
            }
        }
        
        // 处理目标敌人的追踪逻辑
        if (targetEnemy == null || targetEnemy.isDead()) {
            targetEnemy = null;
            // 检查是否还有其他敌人
            Enemy nearestEnemy = null;
            float nearestDistance = detectionRadius;
            for (Enemy enemy : enemies) {
                if (enemy == null || enemy.isDead()) continue;
                float distance = position.dst(enemy.getPosition());
                if (distance < nearestDistance) {
                    nearestEnemy = enemy;
                    nearestDistance = distance;
                }
            }
            
            if (nearestEnemy != null) {
                targetEnemy = nearestEnemy;
            } else {
                currentState = PetState.FOLLOW;
                stateTimer = 0f;
                return;
            }
        }

        Vector2 enemyPos = targetEnemy.getPosition();
        float distance = position.dst(enemyPos);
        
        // 继续追踪目标敌人
        if (distance > attackDistance) {
            Vector2 direction = enemyPos.cpy().sub(position);
            direction.nor();
            velocity.set(direction.scl(moveSpeed));
            float petCenterX = position.x + hitbox.width / 2f;
            isFlipped = (enemyPos.x < petCenterX);
        } else {
            // 距离足够近时，停止移动但继续检查碰撞造成伤害
            velocity.setZero();
            float petCenterX = position.x + hitbox.width / 2f;
            isFlipped = (enemyPos.x < petCenterX);
        }
    }

    /**
     * RETURN state: retreat back to owner
     */
    private void updateReturn(float delta, MazeRunnerGame game) {
        Vector2 ownerPos = owner.getPosition();
        float distance = position.dst(ownerPos);

        if (distance < followRadius) {
            currentState = PetState.FOLLOW;
            stateTimer = 0f;
            velocity.setZero();
        } else {
            Vector2 direction = ownerPos.cpy().sub(position);
            direction.nor();
            velocity.set(direction.scl(moveSpeed * 1.5f));
            float petCenterX = position.x + hitbox.width / 2f;
            isFlipped = (ownerPos.x < petCenterX);
        }
    }
    
    /**
     * ROAM state: roam state
     */
    private void updateRoam(float delta, MazeRunnerGame game) {
        // 第四关禁用roam状态，立即切换到DETECT状态
        if (game.getSelectedLevel() == 4) {
            currentState = PetState.DETECT;
            stateTimer = 0f;
            velocity.setZero();
            return;
        }
        
        velocity.set(roamDirection.cpy().scl(moveSpeed));
        isFlipped = (velocity.x < 0);
        
        if (stateTimer >= ROAM_DURATION) {
            currentState = PetState.DETECT;
            stateTimer = 0f;
            velocity.setZero();
        }
    }

    /**
     * Apply movement (with collision detection)
     */
    private void applyMovement(float delta, MazeRunnerGame game) {
        float stepX = velocity.x * delta;
        float stepY = velocity.y * delta;

        // 第四关：宠物无视墙体
        boolean ignoreWalls = (game.getSelectedLevel() == 4);

        Rectangle checkX = new Rectangle(position.x + stepX, position.y, hitbox.width, hitbox.height);
        if ((ignoreWalls || !hasWallAt(checkX, game)) && isWithinMapBounds(checkX, game)) {
            position.x += stepX;
        } else {
            velocity.x = 0;
        }

        Rectangle checkY = new Rectangle(position.x, position.y + stepY, hitbox.width, hitbox.height);
        if ((ignoreWalls || !hasWallAt(checkY, game)) && isWithinMapBounds(checkY, game)) {
            position.y += stepY;
        } else {
            velocity.y = 0;
        }
    }

    /**
     * Check if within map bounds
     */
    private boolean isWithinMapBounds(Rectangle checkRect, MazeRunnerGame game) {
        float tileSize = 32f;
        var map = game.getMazeLayout();

        int maxX = 0;
        int maxY = 0;
        for (var entry : map.keySet()) {
            maxX = Math.max(maxX, entry.getKey());
            maxY = Math.max(maxY, entry.getValue());
        }

        float mapWidth = (maxX + 1) * tileSize;
        float mapHeight = (maxY + 1) * tileSize;

        return checkRect.x >= 0 &&
                checkRect.y >= 0 &&
                checkRect.x + checkRect.width <= mapWidth &&
                checkRect.y + checkRect.height <= mapHeight;
    }


    @Override
    public void draw(Batch batch) {
        TextureRegion frame = null;

        switch (currentState) {
            case ATTACK:
                if (targetBoss != null) {
                    if (stateTimer < WINDUP_DURATION) {
                        if (attack3WindupFrames != null && attack3WindupFrames.length >= 2) {
                            int frameIndex = (int)(stateTimer / (WINDUP_DURATION / 2f));
                            frameIndex = Math.min(frameIndex, 1);
                            frame = attack3WindupFrames[frameIndex];
                        }
                    } else {
                        if (attack3ChargeAnim != null) {
                            frame = attack3ChargeAnim.getKeyFrame(animationTimer, true);
                        }
                    }
                } else {
                    if (attackAnim != null) {
                        frame = attackAnim.getKeyFrame(stateTimer, true);
                    } else if (walkAnim != null) {
                        frame = walkAnim.getKeyFrame(animationTimer, true);
                    }
                }
                break;
            case FOLLOW:
            case DETECT:
            case RETURN:
            case ROAM:
            default:
                if (walkAnim != null) {
                    frame = walkAnim.getKeyFrame(animationTimer, true);
                }
                break;
        }

        if (frame != null) {
            float drawX = position.x;
            float drawY = position.y;
            float renderWidth = 32f;
            float renderHeight = 32f;
            
            if (isFlipped) {
                batch.draw(frame, drawX + renderWidth, drawY, -renderWidth, renderHeight);
            } else {
                batch.draw(frame, drawX, drawY, renderWidth, renderHeight);
            }
        }
    }

    // Getters
    public Character getOwner() {
        return owner;
    }

    public Enemy getTargetEnemy() {
        return targetEnemy;
    }

    public PetState getCurrentState() {
        return currentState;
    }
}

