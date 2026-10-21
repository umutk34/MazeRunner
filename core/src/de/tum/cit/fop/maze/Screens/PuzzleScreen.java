package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.Behaviour.Player;
import de.tum.cit.fop.maze.Data.PlayerProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 洗牌追踪小游戏界面
 * 类似暂停界面的视觉效果，包含难度选择和游戏逻辑
 */
public class PuzzleScreen implements Screen {
    
    private final MazeRunnerGame game;
    private final Screen returnScreen;
    private final Stage stage;
    private SpriteBatch batch;
    
    // Game state
    private enum GamePhase {
        DIFFICULTY_SELECT,  // Difficulty selection
        SHOWING,            // Showing phase (display X position)
        SHUFFLING,          // Shuffling phase
        SELECTING,          // Selection phase
        RESULT              // Result display
    }
    
    private GamePhase currentPhase = GamePhase.DIFFICULTY_SELECT;
    
    // Difficulty configuration
    private enum Difficulty {
        EASY(3, 5, 1.0f, 0.3f, 100, false),
        NORMAL(4, 7, 0.7f, 0.2f, 100, true),
        HARD(5, 8, 0.5f, 0.15f, 500, true);
        
        final int cardCount;      // Number of cards
        final int swapCount;      // Number of swaps
        final float swapSpeed;    // Swap speed (multiplier, smaller is faster)
        final float swapInterval; // Swap interval (seconds)
        final int scoreReward;    // Score reward
        final boolean hasMysteryReward; // Whether has mystery reward
        
        Difficulty(int cardCount, int swapCount, float swapSpeed, float swapInterval, 
                   int scoreReward, boolean hasMysteryReward) {
            this.cardCount = cardCount;
            this.swapCount = swapCount;
            this.swapSpeed = swapSpeed;
            this.swapInterval = swapInterval;
            this.scoreReward = scoreReward;
            this.hasMysteryReward = hasMysteryReward;
        }
    }
    
    private Difficulty selectedDifficulty = null;
    
    // Card data structure
    private static class Card {
        int id;              // Logical ID (fixed)
        int position;        // Current position index (0-based)
        boolean isX;         // Whether is X card
        Vector2 screenPos;   // Screen position
        Rectangle hitbox;     // Click detection area
        
        Card(int id, int position, boolean isX) {
            this.id = id;
            this.position = position;
            this.isX = isX;
            this.screenPos = new Vector2();
            this.hitbox = new Rectangle();
        }
    }
    
    private List<Card> cards = new ArrayList<>();
    private int xCardId = -1; // X card ID
    
    // Shuffling related
    private List<int[]> swapSequence = new ArrayList<>(); // Swap sequence: {{pos1, pos2}, ...}
    private int currentSwapIndex = 0;
    private float swapTimer = 0f;
    private boolean isSwapping = false;
    private int swappingCard1 = -1;
    private int swappingCard2 = -1;
    private float swapProgress = 0f; // 0-1, swap animation progress
    
    // Showing phase
    private float showTimer = 0f;
    private static final float SHOW_DURATION = 2.0f; // Show for 2 seconds
    
    // Selection result
    private boolean gameWon = false;
    private int selectedCardId = -1;
    
    // UI elements
    private Table mainTable;
    private Label resultLabel;
    
    // Card appearance (using simple geometric shapes instead)
    private Texture cardBackTexture;
    private Texture cardXTexture;
    private Texture cardOTexture;
    
    public PuzzleScreen(MazeRunnerGame game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.batch = game.getSpriteBatch();
        
        // Create simple card texture (if no resources, use color blocks)
        try {
            cardBackTexture = new Texture(Gdx.files.internal("game_assets/UI/Items/Card.png"));
        } catch (Exception e) {
            cardBackTexture = null;
        }
        
        buildDifficultySelectUI();
    }
    
    private void buildDifficultySelectUI() {
        stage.clear();
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Table panel = new Table();
        panel.defaults().pad(8);
        
        if (game.getSkin().has("window", Drawable.class)) {
            panel.setBackground(game.getSkin().getDrawable("window"));
        } else if (game.getSkin().has("default-round", Drawable.class)) {
            panel.setBackground(game.getSkin().getDrawable("default-round"));
        }
        
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();
        // Increase panel width to accommodate longer button text
        float panelW = Math.max(640f, Math.min(640f, vw * 0.8f));
        float panelPad = 18f;
        
        panel.pad(panelPad);
        root.add(panel).width(panelW).center();
        
        // Title
        Label title = new Label("Select Difficulty", game.getSkin(), "label-style");
        title.setAlignment(Align.center);
        panel.add(title).growX().padBottom(20).row();
        
        // Difficulty buttons
        float btnH = Math.min(70f, vh * 0.08f);
        // Increase button width to accommodate long text
        float btnW = panelW - (panelPad * 2f);
        
        TextButton easyBtn = new TextButton("EASY (Score +100)", game.getSkin(), "button-style");
        TextButton normalBtn = new TextButton("NORMAL (Score +100 + Mystery Reward)", game.getSkin(), "button-style");
        TextButton hardBtn = new TextButton("HARD (Score +500 + Mystery Reward)", game.getSkin(), "button-style");
        
        float gap = 12f;
        panel.add(easyBtn).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(normalBtn).width(btnW).height(btnH).padBottom(gap).row();
        panel.add(hardBtn).width(btnW).height(btnH).row();
        
        // Button listeners
        easyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                startGame(Difficulty.EASY);
            }
        });
        
        normalBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                startGame(Difficulty.NORMAL);
            }
        });
        
        hardBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                startGame(Difficulty.HARD);
            }
        });
    }
    
    private void startGame(Difficulty difficulty) {
        selectedDifficulty = difficulty;
        currentPhase = GamePhase.SHOWING;
        
        // Initialize cards
        cards.clear();
        Random random = new Random();
        
        // Randomly select X card position
        xCardId = random.nextInt(difficulty.cardCount);
        
        for (int i = 0; i < difficulty.cardCount; i++) {
            cards.add(new Card(i, i, i == xCardId));
        }
        
        // Generate shuffle sequence (random pairwise swaps)
        swapSequence.clear();
        for (int i = 0; i < difficulty.swapCount; i++) {
            int pos1 = random.nextInt(difficulty.cardCount);
            int pos2 = random.nextInt(difficulty.cardCount);
            while (pos2 == pos1) {
                pos2 = random.nextInt(difficulty.cardCount);
            }
            swapSequence.add(new int[]{pos1, pos2});
        }
        
        currentSwapIndex = 0;
        swapTimer = 0f;
        showTimer = 0f;
        gameWon = false;
        selectedCardId = -1;
        
        // Calculate card screen positions (horizontal arrangement)
        updateCardPositions();
        
        // Switch to game interface
        buildGameUI();
    }
    
    private void updateCardPositions() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        float totalWidth = selectedDifficulty.cardCount * 120f; // Each card 120 pixels wide
        float startX = (screenW - totalWidth) / 2f;
        float cardY = screenH * 0.4f;
        
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            // Calculate screen coordinates based on position (current position)
            float x = startX + card.position * 120f;
            card.screenPos.set(x, cardY);
            card.hitbox.set(x, cardY, 100f, 140f);
        }
    }
    
    private void buildGameUI() {
        stage.clear();
        
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        
        // Result label (initially hidden)
        resultLabel = new Label("", game.getSkin(), "label-style");
        resultLabel.setAlignment(Align.center);
        resultLabel.setVisible(false);
    }
    
    @Override
    public void render(float delta) {
        // Draw background (similar to pause screen)
        if (returnScreen != null) {
            returnScreen.render(delta);
        }
        
        // Semi-transparent black overlay
        game.renderDarkOverlay(0.55f);
        
        // Update game logic
        update(delta);
        
        // Draw game content
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        
        // Draw cards
        for (Card card : cards) {
            drawCard(card);
        }
        
        // Draw phase hint text
        drawPhaseText();
        
        batch.end();
        
        // Draw UI
        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
        
        // ESC key to return
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && currentPhase != GamePhase.DIFFICULTY_SELECT) {
            // Return to difficulty selection
            currentPhase = GamePhase.DIFFICULTY_SELECT;
            buildDifficultySelectUI();
        }
    }
    
    private void update(float delta) {
        switch (currentPhase) {
            case SHOWING:
                showTimer += delta;
                if (showTimer >= SHOW_DURATION) {
                    currentPhase = GamePhase.SHUFFLING;
                    swapTimer = 0f;
                }
                break;
                
            case SHUFFLING:
                updateShuffling(delta);
                break;
                
            case SELECTING:
                // Wait for player click
                handleCardClick();
                break;
                
            case RESULT:
                // Show result, wait for return
                break;
        }
    }
    
    private void updateShuffling(float delta) {
        if (currentSwapIndex >= swapSequence.size()) {
            // Shuffling complete, enter selection phase
            currentPhase = GamePhase.SELECTING;
            isSwapping = false;
            updateCardPositions(); // Ensure positions are correct
            return;
        }
        
        if (!isSwapping) {
            // Wait for swap interval
            swapTimer += delta;
            if (swapTimer >= selectedDifficulty.swapInterval) {
                // Start new swap
                int[] swap = swapSequence.get(currentSwapIndex);
                swappingCard1 = swap[0];
                swappingCard2 = swap[1];
                isSwapping = true;
                swapProgress = 0f;
                swapTimer = 0f;
            }
        } else {
            // Execute swap animation
            float swapDuration = 0.3f * selectedDifficulty.swapSpeed; // Swap animation duration
            swapProgress += delta / swapDuration;
            
            if (swapProgress >= 1.0f) {
                // Swap complete, update logical positions
                swapProgress = 1.0f;
                performSwap(swappingCard1, swappingCard2);
                isSwapping = false;
                currentSwapIndex++;
                swapTimer = 0f;
                updateCardPositions(); // Update to new positions
            }
        }
    }
    
    private void performSwap(int pos1, int pos2) {
        // Find cards at these two positions
        Card card1 = null;
        Card card2 = null;
        for (Card card : cards) {
            if (card.position == pos1) card1 = card;
            if (card.position == pos2) card2 = card;
        }
        
        if (card1 != null && card2 != null) {
            // Swap positions
            int temp = card1.position;
            card1.position = card2.position;
            card2.position = temp;
        }
    }
    
    private void handleCardClick() {
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            
            for (Card card : cards) {
                if (card.hitbox.contains(mousePos)) {
                    selectedCardId = card.id;
                    gameWon = (card.id == xCardId);
                    currentPhase = GamePhase.RESULT;
                    showResult();
                    break;
                }
            }
        }
    }
    
    private void showResult() {
        if (gameWon) {
            // Success: add score
            if (game.getPlayerProfile() != null) {
                PlayerProfile profile = game.getPlayerProfile();
                profile.setScore(profile.getScore() + selectedDifficulty.scoreReward);
                game.setScore(profile.getScore());
            } else {
                game.addScore(selectedDifficulty.scoreReward);
            }
            
            // Mystery reward: unlock staff and Portal
            if (selectedDifficulty.hasMysteryReward) {
                if (game.getPlayerProfile() != null) {
                    PlayerProfile profile = game.getPlayerProfile();
                    profile.unlockWeapon("staff");
                    // If currently no weapon or only hand, switch to staff
                    if (profile.getCurrentWeapon().equals("hand") || 
                        !profile.getUnlockedWeapons().contains("sword")) {
                        profile.setCurrentWeapon("staff");
                    }
                }
                // Unlock Portal (NORMAL or HARD difficulty) - unlock regardless of whether PlayerProfile exists
                game.setPortalUnlocked(true);
            }
            
            resultLabel.setText("Success!\nScore +" + selectedDifficulty.scoreReward + 
                              (selectedDifficulty.hasMysteryReward ? "\nPortal unlocked!" : ""));
            resultLabel.setColor(Color.GREEN);
        } else {
            resultLabel.setText("Failed!\nTry again!");
            resultLabel.setColor(Color.RED);
        }
        
        resultLabel.setVisible(true);
        mainTable.clear();
        mainTable.add(resultLabel).center().pad(20);
        
        // Add buttons
        if (gameWon) {
            // Success: only show return button
            TextButton backBtn = new TextButton("Return to Game", game.getSkin(), "button-style");
            backBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.playClickSound();
                    returnToGame();
                }
            });
            mainTable.row();
            // Reduce button width but ensure text can be fully displayed
            float btnWidth = 250f; // Fixed width, enough to display text
            mainTable.add(backBtn).width(btnWidth).height(50).padTop(20);
        } else {
            // Failed: show Retry and return buttons
            TextButton retryBtn = new TextButton("Retry", game.getSkin(), "button-style");
            retryBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.playClickSound();
                    // Return to difficulty selection screen
                    currentPhase = GamePhase.DIFFICULTY_SELECT;
                    buildDifficultySelectUI();
                }
            });
            
            TextButton backBtn = new TextButton("Return to Game", game.getSkin(), "button-style");
            backBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.playClickSound();
                    returnToGame();
                }
            });
            
            mainTable.row();
            // Create button container, horizontal arrangement
            Table buttonTable = new Table();
            buttonTable.defaults().pad(8);
            float btnWidth = 200f; // Button width
            buttonTable.add(retryBtn).width(btnWidth).height(50);
            buttonTable.add(backBtn).width(btnWidth).height(50);
            mainTable.add(buttonTable).padTop(20);
        }
    }
    
    private void returnToGame() {
        if (returnScreen instanceof GameScreen) {
            GameScreen gs = (GameScreen) returnScreen;
            gs.setPaused(false);
            // Mark just returned from PuzzleScreen, used to reset Portal timer
            gs.setJustReturnedFromPuzzle(true);
        }
        game.setScreen(returnScreen);
    }
    
    private void drawCard(Card card) {
        // Calculate current screen position
        float screenW = Gdx.graphics.getWidth();
        float totalWidth = selectedDifficulty.cardCount * 120f;
        float startX = (screenW - totalWidth) / 2f;
        float cardY = Gdx.graphics.getHeight() * 0.4f;
        
        float x = startX + card.position * 120f;
        float y = cardY;
        
        // If currently swapping, calculate interpolated position
        if (isSwapping && currentPhase == GamePhase.SHUFFLING) {
            Card otherCard = null;
            for (Card c : cards) {
                if (c != card && 
                    ((card.position == swappingCard1 && c.position == swappingCard2) ||
                     (card.position == swappingCard2 && c.position == swappingCard1))) {
                    otherCard = c;
                    break;
                }
            }
            
            if (otherCard != null) {
                float otherX = startX + otherCard.position * 120f;
                // Interpolate to calculate intermediate position
                if (card.position == swappingCard1) {
                    x = x + (otherX - x) * swapProgress;
                } else {
                    x = otherX + (x - otherX) * swapProgress;
                }
            }
        }
        
        // Update hitbox (for click detection)
        card.screenPos.set(x, y);
        card.hitbox.set(x, y, 100f, 140f);
        
        // Draw card: black border white filled rectangle
        Texture white = game.getWhitePixel();
        if (white != null) {
            TextureRegion whiteRegion = new TextureRegion(white);
            
            // Draw white fill
            batch.setColor(Color.WHITE);
            batch.draw(whiteRegion, x + 2f, y + 2f, 96f, 136f);
            
            // Draw black border (using 4 lines)
            batch.setColor(Color.BLACK);
            // Top border
            batch.draw(whiteRegion, x, y + 138f, 100f, 2f);
            // Bottom border
            batch.draw(whiteRegion, x, y, 100f, 2f);
            // Left border
            batch.draw(whiteRegion, x, y, 2f, 140f);
            // Right border
            batch.draw(whiteRegion, x + 98f, y, 2f, 140f);
            
            batch.setColor(Color.WHITE);
        }
        
        // Draw card face content (X or O identifier)
        if (currentPhase == GamePhase.SHOWING) {
            // Showing phase: display X or O (let player remember X position)
            BitmapFont font = game.getSkin().getFont("new-font");
            if (card.isX) {
                // Draw X letter
                font.getData().setScale(2.0f); // Enlarge font
                font.setColor(Color.RED);
                // Calculate text center position
                GlyphLayout layout = new GlyphLayout();
                layout.setText(font, "X");
                float textX = x + 50f - layout.width / 2f;
                float textY = y + 70f + layout.height / 2f;
                font.draw(batch, "X", textX, textY);
                font.getData().setScale(1.0f); // Restore font size
            } else {
                // Draw O
                batch.setColor(Color.BLACK);
                drawO(batch, x + 50f, y + 70f, 40f);
            }
            batch.setColor(Color.WHITE);
            if (font != null) {
                font.setColor(Color.WHITE);
            }
        } else if (currentPhase == GamePhase.SHUFFLING) {
            // Shuffling phase: hide all X and O identifiers
            // Don't draw any content
        } else if (currentPhase == GamePhase.SELECTING) {
            // Selection phase: also don't show card content
            // Don't draw any content
        } else if (currentPhase == GamePhase.RESULT) {
            // Result phase: show all card content (flip all cards)
            BitmapFont font = game.getSkin().getFont("new-font");
            if (card.isX) {
                // Draw X letter
                font.getData().setScale(2.0f); // Enlarge font
                font.setColor(Color.RED);
                // Calculate text center position
                GlyphLayout layout = new GlyphLayout();
                layout.setText(font, "X");
                float textX = x + 50f - layout.width / 2f;
                float textY = y + 70f + layout.height / 2f;
                font.draw(batch, "X", textX, textY);
                font.getData().setScale(1.0f); // Restore font size
            } else {
                // Draw O
                batch.setColor(Color.BLACK);
                drawO(batch, x + 50f, y + 70f, 40f);
            }
            batch.setColor(Color.WHITE);
            if (font != null) {
                font.setColor(Color.WHITE);
            }
        }
    }
    
    private void drawX(SpriteBatch batch, float centerX, float centerY, float size) {
        float halfSize = size / 2f;
        float thickness = 8f;
        // Draw two diagonal lines
        Texture white = game.getWhitePixel();
        if (white == null) return;
        TextureRegion whiteRegion = new TextureRegion(white);
        float angle = 45f;
        float len = (float) Math.sqrt(2) * size;
        batch.draw(whiteRegion, centerX - halfSize, centerY - halfSize, 
                  halfSize, halfSize, len, thickness, 1f, 1f, angle);
        batch.draw(whiteRegion, centerX - halfSize, centerY + halfSize, 
                  halfSize, -halfSize, len, thickness, 1f, 1f, -angle);
    }
    
    private void drawO(SpriteBatch batch, float centerX, float centerY, float size) {
        // Draw circle (using multiple small rectangles to approximate)
        Texture white = game.getWhitePixel();
        if (white == null) return;
        TextureRegion whiteRegion = new TextureRegion(white);
        float radius = size / 2f;
        int segments = 32;
        float thickness = 6f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);
            float x1 = centerX + (float) Math.cos(angle1) * radius;
            float y1 = centerY + (float) Math.sin(angle1) * radius;
            float x2 = centerX + (float) Math.cos(angle2) * radius;
            float y2 = centerY + (float) Math.sin(angle2) * radius;
            
            // Draw line segment (simplified: using small rectangle)
            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            batch.draw(whiteRegion, x1, y1 - thickness / 2f, 0f, thickness / 2f, len, thickness, 1f, 1f, angle);
        }
    }
    
    private void drawPhaseText() {
        BitmapFont font = game.getSkin().getFont("new-font");
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        String text = "";
        switch (currentPhase) {
            case SHOWING:
                text = "Remember the X position!";
                break;
            case SHUFFLING:
                text = "Shuffling...";
                break;
            case SELECTING:
                text = "Select the X card!";
                break;
            case RESULT:
                // Result already displayed in resultLabel
                return;
        }
        
        if (!text.isEmpty()) {
            font.draw(batch, text, screenW / 2f - 100f, screenH * 0.8f);
        }
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (returnScreen instanceof GameScreen) {
            ((GameScreen) returnScreen).setPaused(true);
        }
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (currentPhase == GamePhase.DIFFICULTY_SELECT) {
            buildDifficultySelectUI();
        } else if (cards.size() > 0) {
            updateCardPositions();
        }
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        stage.dispose();
        if (cardBackTexture != null) {
            cardBackTexture.dispose();
        }
    }
}
