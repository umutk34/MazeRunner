package de.tum.cit.fop.maze.Various;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Behaviour.Player;

/**
 * Portal direction arrow rendering utility class.
 * Responsibilities:
 * - Load arrow texture
 * - Draw pointing arrow based on player and portal (or door) positions
 */
public class Arrow {
    private Texture arrowTexture;

    public Arrow() {
        try {
            arrowTexture = new Texture(Gdx.files.internal("game_assets/UI/Items/arrow.png"));
        } catch (Exception e) {
            Gdx.app.error("ASSET", "Failed to load arrow texture game_assets/UI/Items/arrow.png", e);
            arrowTexture = null;
        }
    }

    /**
     * Draw arrow pointing to portal/door above player's head.
     *
     * @param batch          SpriteBatch
     * @param player         Player
     * @param portal         Portal rectangle (can be null)
     * @param doorHitbox     Door rectangle (can be null, used as alternative target when portalRect is null)
     * @param showPortalArrow Whether need to show arrow
     * @param tileSize       Map tile size
     */
    public void render(SpriteBatch batch,
                       Player player,
                       Rectangle portal,
                       Rectangle doorHitbox,
                       boolean showPortalArrow,
                       int tileSize) {
        if (!showPortalArrow || arrowTexture == null || player == null) return;

        float targetX;
        float targetY;

        if (portal != null) {
            targetX = portal.x + portal.width / 2f;
            targetY = portal.y + portal.height / 2f;
        } else if (doorHitbox != null) {
            // If no separate portalRect, treat exit door as portal
            targetX = doorHitbox.x + doorHitbox.width / 2f;
            targetY = doorHitbox.y + doorHitbox.height / 2f;
        } else {
            targetX = targetY = 0f;
        }

        if (targetX == 0f && targetY == 0f) return;

        // Player head position
        Vector2 playerPos = player.getPosition();
        Rectangle playerBox = player.getHitbox();
        float playerCenterX = playerPos.x + playerBox.width / 2f;
        float playerHeadY   = playerPos.y + playerBox.height + tileSize * 0.45f;

        float dx = targetX - playerCenterX;
        float dy = targetY - playerHeadY;

        // Calculate direction angle (angle from X axis)
        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        // Texture defaults to upward (Y+), while 0 degrees is rightward (X+), so subtract 90 degrees to align
        float rotation = angleDeg - 90f;

        // Reduce by another 10% on original basis (90% of current size)
        float arrowSize = tileSize * 0.42f * 0.9f;
        float drawW = arrowSize;
        float drawH = arrowSize;
        float drawX = playerCenterX - drawW / 2f;
        float drawY = playerHeadY   - drawH / 2f;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(
                arrowTexture,
                drawX, drawY,
                drawW / 2f, drawH / 2f,      // Rotation origin (center)
                drawW, drawH,
                1f, 1f,
                rotation,
                0, 0,
                arrowTexture.getWidth(), arrowTexture.getHeight(),
                false, false
        );
    }

    public void dispose() {
        if (arrowTexture != null) {
            arrowTexture.dispose();
            arrowTexture = null;
        }
    }
}