package de.tum.cit.fop.maze.Behaviour;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Level 5 specific Boss.
 * Implements complete state machine: IDLE -> TRACKING -> ATTACK/SUMMON/CHARGING -> COOLDOWN -> IDLE
 */
public class Boss extends Character {

    /**
     * Boss state enumeration
     */
    public enum BossState {
        IDLE,       // Idle state
        ATTACK,     // Attack state (slash)
        TRACKING,   // Tracking state
        CHARGING,   // Charge state
        COOLDOWN,   // Cooldown state
        SUMMON      // Summon state
    }

    private float maxHealth = 30f;
    private float currentHealth = 30f;

    private boolean isDead = false;
    private boolean isRemoved = false;   // Removed from rendering after death animation finishes

    private BossState currentState = BossState.IDLE;
    private float stateTimer = 0f;
    private Vector2 chargeTarget = new Vector2(); // Charge target point
    private Vector2 velocity = new Vector2(0, 0);  // Movement velocity

    // State duration constants
    private static final float IDLE_DURATION = 0.3f;
    private static final float TRACKING_MAX_TIME = 3.0f;
    private static final float TRACKING_MIN_DISTANCE = 150f;
    private static final float ATTACK_ANIMATION_DURATION = 8f * 0.12f;
    private static final float CHARGING_WINDUP = 8f * 0.12f;
    private static final float CHARGING_DURATION = 1.5f; // Maximum charge time
    private static final float COOLDOWN_DURATION = 2.3f; // Cooldown duration (normal state)
    private static final float COOLDOWN_DURATION_BERSERK = 1.4f; // Cooldown duration (berserk state)
    
    private static final float SUMMON_WINDUP_DURATION = 4f * 0.12f;
    private static final float SUMMON_DURATION = 3.0f; // Summon duration
    private static final float SUMMON_INTERVAL = 0.5f; // Summon interval
    private float summonTimer = 0f; // Summon timer
    private float lastSummonTime = 0f; // Last summon time
    private int summonCount = 0; // Number of minions summoned in this summon session
    private static final int SUMMON_COUNT_NORMAL = 4; // Summon count (normal state)
    private static final int SUMMON_COUNT_BERSERK = 6; // Summon count (berserk state)

    // Movement speed
    private static final float MOVE_SPEED = 120f; // 100f * 1.2 = 120f
    private static final float CHARGE_SPEED = 240f; // 400f * 0.6 = 240f (reduced by 40%)

    // Attack detection range
    private static final float ATTACK_DETECTION_RANGE = 150f;
    
    // Attack hitbox related
    private static final float ATTACK_HITBOX_WIDTH = 120f;  // Attack wave width
    private static final float ATTACK_HITBOX_HEIGHT = 20f; // Attack wave height
    private static final float ATTACK_HITBOX_OFFSET = 100f; // Attack wave offset from Boss
    private float attackDamageCooldown = 0f; // Attack damage cooldown (avoid continuous damage)
    private static final float ATTACK_DAMAGE_COOLDOWN_TIME = 0.5f; // Attack damage cooldown time

    // Animations
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;      // Walk.png (used when tracking)
    private Animation<TextureRegion> attackAnim;   // Attack2.png (used when attacking)
    private Animation<TextureRegion> specialAnim;   // Special.png (used when charging)
    private Animation<TextureRegion> walkAttackAnim; // Walk_Attack.png (used when charging)
    private Animation<TextureRegion> deathAnim;
    private Animation<TextureRegion> jumpAnim;      // Jump.png (plays at half health)
    private TextureRegion[] attackWindupFrames;
    private TextureRegion[] attackSummonFrames;
    
    // Berserk mode
    private boolean isBerserk = false; // Whether in berserk mode
    private boolean hasPlayedJumpAnim = false; // Whether Jump animation has been played
    private float jumpAnimTimer = 0f; // Jump animation timer

    private float hitCooldown = 0f;
    private static final float HIT_COOLDOWN_TIME = 0.5f; // Unit: seconds

    // Tracking timer
    private float trackingTimer = 0f;

    // Attack related
    private boolean isFlipped = false; // Whether horizontally flipped (for determining left/right direction)
    
    // Rendering and collision volume size constants
    private static final float BASE_SIZE = 64f; // Base size
    private static final float SIZE_MULTIPLIER = 3f; // Size multiplier
    private static final float RENDER_SIZE = BASE_SIZE * SIZE_MULTIPLIER; // Render size: 192x192

    /**
     * Damage types:
     * MELEE      - Melee (player lightsaber/hand slash, etc.), subject to damage cooldown;
     * RANGED     - Ranged (bullets, laser segments, etc.), each hit deals damage, not subject to cooldown;
     * PET_MELEE  - Pet's melee charge attack, subject to damage cooldown.
     */
    public enum DamageType {
        MELEE,
        RANGED,
        PET_MELEE
    }

    public Boss(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.nextPosition = new Vector2(0, 0);
        this.hitbox = new Rectangle(x, y, width, height);
        this.speed = MOVE_SPEED;
        this.currentState = BossState.IDLE;
        this.currentHealth = maxHealth;
        this.stateTimer = 0f;
    }

    public void setAnimations(Animation<TextureRegion> idle,
                              Animation<TextureRegion> walk,
                              Animation<TextureRegion> attack,
                              Animation<TextureRegion> special,
                              Animation<TextureRegion> walkAttack,
                              Animation<TextureRegion> death,
                              Animation<TextureRegion> jump,
                              TextureRegion[] attackWindupFrames,
                              TextureRegion[] attackSummonFrames) {
        this.idleAnim = idle;
        this.walkAnim = walk;
        this.attackAnim = attack;
        this.specialAnim = special;
        this.walkAttackAnim = walkAttack;
        this.deathAnim = death;
        this.jumpAnim = jump;
        this.attackWindupFrames = attackWindupFrames;
        this.attackSummonFrames = attackSummonFrames;
    }

    /**
     * Update Boss state machine
     * @param delta Time delta
     * @param game Game object
     * @param player Player object (for calculating distance and direction)
     */
    public void update(float delta, MazeRunnerGame game, Character player) {
        // Update attack damage cooldown
        if (attackDamageCooldown > 0f) {
            attackDamageCooldown -= delta;
            if (attackDamageCooldown < 0f) attackDamageCooldown = 0f;
        }
        if (isDead) {
            stateTimer += delta;
            animationTimer += delta;
            if (deathAnim != null && deathAnim.isAnimationFinished(stateTimer)) {
                isRemoved = true;
            }
            return;
        }
        
        if (isRemoved) return;

        stateTimer += delta;
        animationTimer += delta;

        if (hitCooldown > 0f) {
            hitCooldown -= delta;
            if (hitCooldown < 0f) hitCooldown = 0f;
        }
        
        if (!isBerserk && currentHealth <= maxHealth * 0.5f) {
            isBerserk = true;
            hasPlayedJumpAnim = false;
            jumpAnimTimer = 0f;
        }
        
        if (isBerserk && !hasPlayedJumpAnim) {
            jumpAnimTimer += delta;
            if (jumpAnim != null && jumpAnim.isAnimationFinished(jumpAnimTimer)) {
                hasPlayedJumpAnim = true;
            }
            if (!hasPlayedJumpAnim) {
                return;
            }
        }

        // State machine logic
        switch (currentState) {
            case IDLE:
                updateIdle(delta, player);
                break;
            case ATTACK:
                updateAttack(delta, player);
                break;
            case TRACKING:
                updateTracking(delta, game, player);
                break;
            case CHARGING:
                updateCharging(delta, game, player);
                break;
            case COOLDOWN:
                updateCooldown(delta);
                break;
            case SUMMON:
                updateSummon(delta, game);
                break;
        }

        // X axis movement detection
        Rectangle testX = new Rectangle(position.x + velocity.x * delta, position.y, hitbox.width, hitbox.height);
        if (!hasWallAt(testX, game) && isWithinMapBounds(testX, game)) {
            position.x += velocity.x * delta;
        } else {
            velocity.x = 0;
        }
        
        // Y axis movement detection
        Rectangle testY = new Rectangle(position.x, position.y + velocity.y * delta, hitbox.width, hitbox.height);
        if (!hasWallAt(testY, game) && isWithinMapBounds(testY, game)) {
            position.y += velocity.y * delta;
        } else {
            velocity.y = 0;
        }
        
        hitbox.setPosition(position.x, position.y);
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

    /**
     * IDLE state: after idling 1.5 seconds, decide whether to Tracking or Attack
     */
    private void updateIdle(float delta, Character player) {
        velocity.setZero();
            if (player != null) {
            float bossCenterX = position.x + RENDER_SIZE / 2f;
            isFlipped = (player.getPosition().x < bossCenterX);
        }
        if (stateTimer >= IDLE_DURATION) {
        currentState = BossState.TRACKING;
            stateTimer = 0f;
            trackingTimer = 0f;
        }
    }

    /**
     * ATTACK state: slash attack
     */
    private void updateAttack(float delta, Character player) {
        velocity.setZero();
        
        if (player != null) {
            float bossCenterX = position.x + RENDER_SIZE / 2f;
            isFlipped = (player.getPosition().x < bossCenterX);
        }
        
        if (stateTimer >= ATTACK_ANIMATION_DURATION) {
            currentState = BossState.COOLDOWN;
            stateTimer = 0f;
        }
    }
    
    /**
     * Detect attack hit (attack wave range) - called by GameScreen
     * @return Whether hit player
     */
    public boolean checkAttackHit(Rectangle playerHitbox) {
        if (currentState != BossState.ATTACK) return false;
        if (attackDamageCooldown > 0f) return false;
        if (stateTimer < 0.24f || stateTimer > 0.72f) return false;
        
        // Calculate attack wave hit area
        Rectangle attackHitbox = getAttackHitbox();
        
        // Detect collision
        if (attackHitbox.overlaps(playerHitbox)) {
            attackDamageCooldown = ATTACK_DAMAGE_COOLDOWN_TIME; // Set damage cooldown
            return true;
        }
        return false;
    }
    
    /**
     * Detect charge collision - called by GameScreen
     * @return Whether hit player
     */
    public boolean checkChargeHit(Rectangle playerHitbox) {
        if (currentState != BossState.CHARGING) return false;
        if (stateTimer < CHARGING_WINDUP) return false;
        if (attackDamageCooldown > 0f) return false;
        
        // Detect if Boss and player collision boxes overlap
        if (getAttackHitbox().overlaps(playerHitbox)) {
            attackDamageCooldown = ATTACK_DAMAGE_COOLDOWN_TIME; // Set damage cooldown
            return true;
        }
        return false;
    }
    
    /**
     * Get attack hit area (attack wave range)
     */
    public Rectangle getAttackHitbox() {
        float centerX = hitbox.x + 24;
        float centerY = hitbox.y + 48;
        float hitboxX, hitboxY;

        if (isFlipped) {
            hitboxX = centerX - ATTACK_HITBOX_WIDTH;
        } else {
            hitboxX = centerX;
        }

        hitboxY = centerY - ATTACK_HITBOX_HEIGHT/2f;

        return new Rectangle(hitboxX, hitboxY, ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_HEIGHT);
    }
    /**
     * TRACKING state: track player
     */
    private void updateTracking(float delta, MazeRunnerGame game, Character player) {
        trackingTimer += delta;
        Vector2 playerPos = player.getPosition();
        float distance = position.dst(playerPos);

        float bossCenterX = position.x + RENDER_SIZE / 2f;
        float bossCenterY = position.y + RENDER_SIZE / 2f;
        Vector2 bossCenter = new Vector2(bossCenterX, bossCenterY);
        Vector2 direction = playerPos.cpy().sub(bossCenter);
        
        if (direction.len() > 0.1f) {
            direction.nor();
            velocity.set(direction.scl(MOVE_SPEED));
        isFlipped = (velocity.x < 0);
        } else {
            velocity.setZero();
        }

        if (trackingTimer >= TRACKING_MAX_TIME || distance <= TRACKING_MIN_DISTANCE) {
            float rand = (float)Math.random();
            boolean playerNearby = distance <= ATTACK_DETECTION_RANGE;
            
            if (rand < 0.33f) {
                if (playerNearby) {
                    currentState = BossState.ATTACK;
                    stateTimer = 0f;
                    attackDamageCooldown = 0f;
                    // Reuse already defined bossCenterX
                    isFlipped = (playerPos.x < bossCenterX);
                } else {
                    currentState = BossState.SUMMON;
                    stateTimer = 0f;
                    summonTimer = 0f;
                    lastSummonTime = 0f;
                    summonCount = 0;
                    velocity.setZero();
                }
            } else if (rand < 0.66f) {
                if (isBerserk || currentHealth <= maxHealth * 0.5f) {
                    chargeTarget.set(playerPos);
                    currentState = BossState.CHARGING;
                    stateTimer = 0f;
                    velocity.setZero();
                    isFlipped = (playerPos.x < bossCenterX);
                } else {
                    currentState = BossState.SUMMON;
                    stateTimer = 0f;
                    summonTimer = 0f;
                    lastSummonTime = 0f;
                    summonCount = 0;
                    velocity.setZero();
                }
            } else {
                currentState = BossState.SUMMON;
                stateTimer = 0f;
                summonTimer = 0f;
                lastSummonTime = 0f;
                summonCount = 0;
                velocity.setZero();
            }
        }
    }

    /**
     * CHARGING state: charge
     */
    private void updateCharging(float delta, MazeRunnerGame game, Character player) {
        if (stateTimer < CHARGING_WINDUP) {
            velocity.setZero();
            Vector2 direction = chargeTarget.cpy().sub(position);
            if (direction.len() > 0.1f) {
                isFlipped = (direction.x < 0);
            }
        } else {
            Vector2 direction = chargeTarget.cpy().sub(position);
            float distance = direction.len();
            
            if (distance > 5f && stateTimer < CHARGING_WINDUP + CHARGING_DURATION) {
                direction.nor();
                velocity.set(direction.scl(CHARGE_SPEED));
                isFlipped = (velocity.x < 0);
                
                if (distance <= 10f) {
                    currentState = BossState.COOLDOWN;
                    stateTimer = 0f;
                    velocity.setZero();
                }
            } else {
                currentState = BossState.COOLDOWN;
                stateTimer = 0f;
                velocity.setZero();
            }
        }
    }

    /**
     * SUMMON state: summon minions
     */
    private void updateSummon(float delta, MazeRunnerGame game) {
        velocity.setZero();
        
        int totalSummonCount = isBerserk ? SUMMON_COUNT_BERSERK : SUMMON_COUNT_NORMAL;
        
        if (stateTimer < SUMMON_WINDUP_DURATION) {
            summonTimer = 0f;
            lastSummonTime = 0f;
        } else {
            float summonPhaseTime = stateTimer - SUMMON_WINDUP_DURATION;
            summonTimer = summonPhaseTime;
            
            if (summonPhaseTime - lastSummonTime >= SUMMON_INTERVAL && summonCount < totalSummonCount) {
                float spawnX = 0f, spawnY = 0f;
                boolean validPosition = false;
                int attempts = 0;
                int maxAttempts = 10;
                
                while (!validPosition && attempts < maxAttempts) {
                    float angle = (float)(Math.random() * Math.PI * 2);
                    float distance = 100f + (float)(Math.random() * 100f); // 100-200 pixels
                    spawnX = position.x + RENDER_SIZE / 2f + (float)(Math.cos(angle) * distance) - 6f;
                    spawnY = position.y + RENDER_SIZE / 2f + (float)(Math.sin(angle) * distance) - 9f;
                    
                    Rectangle testRect = new Rectangle(spawnX, spawnY, 12, 18);
                    if (isWithinMapBounds(testRect, game) && !hasWallAt(testRect, game)) {
                        validPosition = true;
                    } else {
                        attempts++;
                    }
                }
                
                if (!validPosition) {
                    spawnX = position.x + RENDER_SIZE / 2f - 6f;
                    spawnY = position.y + RENDER_SIZE + 20f;
                    Rectangle testRect = new Rectangle(spawnX, spawnY, 12, 18);
                    if (hasWallAt(testRect, game)) {
                        spawnY = position.y - 20f;
                        testRect.setPosition(spawnX, spawnY);
                        if (hasWallAt(testRect, game)) {
                            spawnX = position.x + RENDER_SIZE + 20f;
                            spawnY = position.y + RENDER_SIZE / 2f - 9f;
                        }
                    }
                }
                
                Enemy newEnemy = new Enemy(spawnX, spawnY, 12, 18);
                game.loadEnemyAnimations(newEnemy);
                game.getCharacters().add(newEnemy);
                game.getEnemies().add(newEnemy);
                
                summonCount++;
                lastSummonTime = summonPhaseTime;
            }
            
            if (stateTimer >= SUMMON_WINDUP_DURATION + SUMMON_DURATION) {
                currentState = BossState.COOLDOWN;
                stateTimer = 0f;
                summonCount = 0;
            }
        }
    }
    
    /**
     * COOLDOWN state: cooldown
     */
    private void updateCooldown(float delta) {
        velocity.setZero();
        float cooldownDuration = isBerserk ? COOLDOWN_DURATION_BERSERK : COOLDOWN_DURATION;
        if (stateTimer >= cooldownDuration) {
            currentState = BossState.IDLE;
            stateTimer = 0f;
        }
    }

    /**
     * Default damage interface: treated as melee attack (consistent with Enemy behavior)
     */
    public void takeDamage(int amount) {
        takeDamage(amount, DamageType.MELEE);
    }

    /**
     * Damage interface with type:
     * - Melee / PET_MELEE: limited by hitCooldown, ignored if cooldown not finished;
     * - Ranged: no cooldown check, each hit deals damage
     */
    public void takeDamage(int amount, DamageType type) {
        if (isDead || isRemoved || amount <= 0) return;

        boolean useCooldown = (type == DamageType.MELEE || type == DamageType.PET_MELEE);

        if (useCooldown && hitCooldown > 0f) return;

        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;

        if (useCooldown) {
            hitCooldown = HIT_COOLDOWN_TIME;
        }

        if (currentHealth <= 0) {
            die();
        }
    }

    private void die() {
        if (isDead) return;
        isDead = true;
        stateTimer = 0f;
        animationTimer = 0f;
        velocity.setZero();
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public int getHealth() {
        return (int)currentHealth;
    }

    public int getMaxHealth() {
        return (int)maxHealth;
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void draw(Batch batch) {
        if (isRemoved) return;

        TextureRegion frame = null;

        if (isBerserk && !hasPlayedJumpAnim && jumpAnim != null) {
            frame = jumpAnim.getKeyFrame(jumpAnimTimer, false);
        } else if (isDead) {
            if (deathAnim != null) {
                frame = deathAnim.getKeyFrame(stateTimer, false);
            }
        } else {
            switch (currentState) {
                case IDLE:
                    if (idleAnim != null) {
                        frame = idleAnim.getKeyFrame(animationTimer, true);
                    }
                    break;
                case ATTACK:
                    if (attackAnim != null) {
                        frame = attackAnim.getKeyFrame(stateTimer, false);
                    }
                    break;
                case TRACKING:
                    if (walkAnim != null) {
                        frame = walkAnim.getKeyFrame(animationTimer, true);
                    }
                    break;
                case CHARGING:
                    if (stateTimer < CHARGING_WINDUP) {
                        if (specialAnim != null) {
                            frame = specialAnim.getKeyFrame(stateTimer, false);
                        }
                    } else {
                        if (walkAttackAnim != null) {
                            frame = walkAttackAnim.getKeyFrame(animationTimer, true);
                        }
                    }
                    break;
                case SUMMON:
                    if (stateTimer < SUMMON_WINDUP_DURATION) {
                        if (attackWindupFrames != null && attackWindupFrames.length > 0) {
                            int frameIndex = (int)(stateTimer / 0.12f) % attackWindupFrames.length;
                            frame = attackWindupFrames[frameIndex];
                        }
                    } else {
                        if (attackSummonFrames != null && attackSummonFrames.length > 0) {
                            float summonAnimTime = stateTimer - SUMMON_WINDUP_DURATION;
                            int frameIndex = (int)(summonAnimTime / 0.12f) % attackSummonFrames.length;
                            frame = attackSummonFrames[frameIndex];
                        }
                    }
                    break;
                case COOLDOWN:
                    if (idleAnim != null) {
                        frame = idleAnim.getKeyFrame(animationTimer, true);
                    }
                    break;
            }
        }

        if (frame != null) {
            Color stateColor = Color.WHITE;
            
            if (hitCooldown > 0f && !isDead) {
                int flashIndex = (int)(hitCooldown * 10f);
                if (flashIndex % 2 == 0) {
                    stateColor = new Color(1f, 0.3f, 0.3f, 1f);
                } else {
                    stateColor = Color.WHITE;
                }
            } else {
                switch (currentState) {
                    case TRACKING:
                        stateColor = new Color(1f, 0.8f, 0.8f, 1f);
                        break;
                    case CHARGING:
                        stateColor = new Color(1f, 0f, 0f, 1f);
                        break;
                    case COOLDOWN:
                        stateColor = new Color(0.5f, 0.5f, 0.5f, 1f);
                        break;
                    default:
                        stateColor = Color.WHITE;
                        break;
                }
            }

            batch.setColor(stateColor);

            float drawX = hitbox.x - 100;
            float drawY = hitbox.y;
            float renderWidth = 256;
            float renderHeight = 256;

            if (isFlipped) {
                batch.draw(frame, drawX + renderWidth, drawY, -renderWidth, renderHeight);
            } else {
                batch.draw(frame, drawX, drawY, renderWidth, renderHeight);
            }

            batch.setColor(Color.WHITE);
        }
    }
}