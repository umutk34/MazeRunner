package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class HealthBar extends Table {
    private TextureRegionDrawable fullDrawable;
    private TextureRegionDrawable emptyDrawable;

    // List storing the 4 small segment actors
    private Array<Image> segments;

    private int maxHealth; // Maximum health (e.g., 4)


    public HealthBar(MazeRunnerGame game, int maxHealth) {
        this.fullDrawable = new TextureRegionDrawable(game.getHpFullTexture());
        this.emptyDrawable = new TextureRegionDrawable(game.getHpEmptyTexture());
        this.maxHealth = maxHealth;
        this.segments = new Array<>();

        // Initialize: generate corresponding number of segments based on max health, all set to full first
        for (int i = 0; i < maxHealth; i++) {
            Image image = new Image(fullDrawable);

            // Add segment to list for later modification
            segments.add(image);

            // Add segment to Table layout for display
            // padRight(5) is to add some gap between segments, so they don't crowd together
            this.add(image).padRight(5);
        }

        // Default to compact layout
        this.pack();
    }

    /**
     * Core method: update display based on current health
     * @param currentHealth Current health (e.g., 3)
     */
    public void update(int currentHealth) {
        // Iterate through each segment
        for (int i = 0; i < maxHealth; i++) {
            Image image = segments.get(i);

            // Logic: if current segment index is less than current health, this segment should be lit
            // Example: health 3. i=0(full), i=1(full), i=2(full), i=3(empty)
            if (i < currentHealth) {
                image.setDrawable(fullDrawable);
            } else {
                image.setDrawable(emptyDrawable);
            }
        }
    }

    /**
     * Increase maximum health limit: append new empty segments at the end
     */
    public void setMaxHealth(int newMaxHealth) {
        if (newMaxHealth <= this.maxHealth) return;

        for (int i = this.maxHealth; i < newMaxHealth; i++) {
            Image image = new Image(emptyDrawable);
            segments.add(image);
            this.add(image).padRight(5);
        }
        this.maxHealth = newMaxHealth;
        this.pack();
    }
}
