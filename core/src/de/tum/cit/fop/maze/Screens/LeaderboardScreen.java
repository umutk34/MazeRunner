package de.tum.cit.fop.maze.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.Data.PlayerProfile;
import de.tum.cit.fop.maze.Data.ProfileManager;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * LeaderboardScreen - Leaderboard interface
 * Displays high score records of all players
 */
public class LeaderboardScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final ProfileManager profileManager;
    private Table leaderboardTable;
    private ScrollPane scrollPane;

    public LeaderboardScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        this.profileManager = new ProfileManager();
        
        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(
                game.getSkin().getFont("new-font"),
                com.badlogic.gdx.graphics.Color.WHITE
        );
        Label titleLabel = new Label("HALL OF FAME", titleStyle);
        titleLabel.setFontScale(2.5f);
        titleLabel.setAlignment(Align.center);
        root.add(titleLabel).padTop(40f).padBottom(30f).row();

        Table headerTable = new Table();
        headerTable.defaults().pad(5f).expandX();
        
        Label.LabelStyle headerStyle = new Label.LabelStyle(
                game.getSkin().getFont("new-font"),
                com.badlogic.gdx.graphics.Color.WHITE
        );
        headerStyle.font.getData().setScale(1.5f);
        
        Label rankHeader = new Label("Rank", headerStyle);
        Label nameHeader = new Label("Name", headerStyle);
        Label scoreHeader = new Label("Score", headerStyle);
        
        rankHeader.setAlignment(Align.center);
        nameHeader.setAlignment(Align.left);
        scoreHeader.setAlignment(Align.center);
        
        headerTable.add(rankHeader).width(100f).center();
        headerTable.add(nameHeader).expandX().left();
        headerTable.add(scoreHeader).width(200f).center();
        
        root.add(headerTable).width(Gdx.graphics.getWidth() * 0.8f).padBottom(10f).row();

        leaderboardTable = new Table();
        leaderboardTable.defaults().pad(2f).expandX();
        
        scrollPane = new ScrollPane(leaderboardTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        
        root.add(scrollPane).width(Gdx.graphics.getWidth() * 0.8f).height(Gdx.graphics.getHeight() * 0.5f).padBottom(20f).row();

        TextButton backButton = new TextButton("Back to Menu", game.getSkin(), "button-style");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.playClickSound();
                game.goToMenu();
            }
        });
        
        root.add(backButton).width(300).height(50).padTop(20f).row();
        
        populateLeaderboard();
    }

    /**
     * Populate leaderboard data
     * Reloads all user data from file system on each call to ensure latest data
     */
    private void populateLeaderboard() {
        leaderboardTable.clear();
        
        List<PlayerProfile> profiles = profileManager.loadAllProfiles();
        Gdx.app.log("LeaderboardScreen", "Loaded " + profiles.size() + " user profiles from file system");
        
        PlayerProfile anton = new PlayerProfile("ANTON");
        anton.setScore(250);
        profiles.add(anton);
        
        PlayerProfile josh = new PlayerProfile("JOSH");
        josh.setScore(-250);
        profiles.add(josh);
        
        PlayerProfile potatoman = new PlayerProfile("POTATOMAN");
        potatoman.setScore(420);
        profiles.add(potatoman);
        
        PlayerProfile ziway = new PlayerProfile("ZIWAY");
        ziway.setScore(1200);
        profiles.add(ziway);
        
        PlayerProfile gong = new PlayerProfile("GONG");
        gong.setScore(670);
        profiles.add(gong);
        
        PlayerProfile athena = new PlayerProfile("ATHENA");
        athena.setScore(1000);
        profiles.add(athena);
        
        if (profiles.isEmpty()) {
            Label.LabelStyle emptyStyle = new Label.LabelStyle(
                    game.getSkin().getFont("new-font"),
                    com.badlogic.gdx.graphics.Color.GRAY
            );
            Label emptyLabel = new Label("No records yet. Play the game to set a score!", emptyStyle);
            emptyLabel.setAlignment(Align.center);
            leaderboardTable.add(emptyLabel).colspan(3).padTop(50f).row();
            return;
        }
        
        Collections.sort(profiles, new Comparator<PlayerProfile>() {
            @Override
            public int compare(PlayerProfile p1, PlayerProfile p2) {
                return Integer.compare(p2.getScore(), p1.getScore());
            }
        });
        
        int displayCount = Math.min(10, profiles.size());
        
        for (int i = 0; i < displayCount; i++) {
            PlayerProfile profile = profiles.get(i);
            int rank = i + 1;
            
            com.badlogic.gdx.graphics.Color textColor;
            if (rank == 1) {
                textColor = com.badlogic.gdx.graphics.Color.GOLD;
            } else {
                textColor = com.badlogic.gdx.graphics.Color.WHITE;
            }
            
            Label.LabelStyle rowStyle = new Label.LabelStyle(
                    game.getSkin().getFont("new-font"),
                    textColor
            );
            
            Label rankLabel = new Label(String.valueOf(rank), rowStyle);
            rankLabel.setAlignment(Align.center);
            rankLabel.setFontScale(1.5f);
            
            String playerName = profile.getPlayerName();
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Unknown";
            }
            Label nameLabel = new Label(playerName, rowStyle);
            nameLabel.setAlignment(Align.left);
            nameLabel.setFontScale(1.5f);
            
            Label scoreLabel = new Label(String.valueOf(profile.getScore()), rowStyle);
            scoreLabel.setAlignment(Align.center);
            scoreLabel.setFontScale(1.5f);
            
            leaderboardTable.add(rankLabel).width(100f).center();
            leaderboardTable.add(nameLabel).expandX().left();
            leaderboardTable.add(scoreLabel).width(200f).center();
            leaderboardTable.row();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        populateLeaderboard();
        Gdx.app.log("LeaderboardScreen", "Reloaded all user data");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.renderBackground(delta);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        buildUI();
        populateLeaderboard();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {
        populateLeaderboard();
        Gdx.app.log("LeaderboardScreen", "Reloaded all user data on resume");
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
