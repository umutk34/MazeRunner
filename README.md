AutoHaus - Maze Runner Game (LibGDX) — Fundamentals of Programming
=======================================================

A 2D maze game built with **LibGDX (Java)** and **Gradle** for the course **Fundamentals of Programming**.

Features
--------

### Gameplay

*   Player movement + collision-based navigation through maze levels

*   Enemies + hazards (including moving hazards)

*   Items: quest parts, health packs, coins, shields, attack upgrades, light pickup

*   Level progression + win/lose screens

*   Portal / door / trapdoor logic for progression and transitions


### UI / Screens

*   Splash / intro flow

*   Main menu with navigation to levels, settings, help, and profiles/leaderboard

*   In-game HUD (health/feedback)

*   Pause menu (resume/restart/settings/quit)

*   Settings screen with persistent audio options (music + SFX volume/mute)

*   How-to-play + hint screen(s)

*   Level selection

*   End screens (You Win / You Lose)

*   Puzzle screen (level-specific)


### Persistence / Data

*   Local settings saved (JSON)

*   Player profiles (name + progress/stats as used by the game)

*   Optional telemetry recording utilities (if enabled)


How to Run
----------

### Requirements

*   **Java 17** (recommended)

*   Gradle wrapper included in the repo


### Run (Desktop)

**Windows (PowerShell)**

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   .\gradlew desktop:run   `

**macOS / Linux**

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ./gradlew desktop:run   `

If gradlew isn’t executable (macOS/Linux):

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   chmod +x gradlew  ./gradlew desktop:run   `

How to Play
-----------

### Controls

*   **Move:** WASD / Arrow keys

*   **Sprint:** Left Shift

*   **Attack:** Space

*   **Switch weapon:** Q

*   **Toggle minimap:** M

*   **Pause:** ESC

*   **Hint / Info screen:** I


### Basic Goal

*   Navigate the maze, avoid hazards, fight enemies where needed

*   Collect items (quest parts / keys / upgrades depending on the level rules)

*   Reach the completion condition (e.g., portal/exit logic) to finish the level


Project Structure
-----------------

The project follows a typical LibGDX multi-module setup (desktop launcher + shared core code).Main code is organized into packages:

### de.tum.cit.fop.maze (Core / glue)

*   **MazeRunnerGame**: main game class (screen switching + shared resources)

*   **DesktopLauncher**: desktop entrypoint

*   **GameConstants**: central constants (speeds, sizes, gameplay tuning)

*   **GameConfig / GameConfigManager**: persistent settings (audio, preferences)

*   **Profile / telemetry utilities**: helper services used across screens (if enabled)


### de.tum.cit.fop.maze.Screens (UI flow)

Each screen implements LibGDX Screen and owns its own UI + input for that state:

*   Splash / Intro

*   Menu

*   Game screen (main loop)

*   Pause

*   Settings

*   Level selector

*   How-to-play / Hint

*   Leaderboard + Name input

*   Puzzle screen

*   You Win / You Lose

*   UI component: **HealthBar** (Scene2D Table)


### de.tum.cit.fop.maze.Behaviour (Gameplay objects)

*   **Character** base class + subtypes:

    *   Player, Enemy, Boss, Pet

*   **Pickup** base class + subtypes:

    *   Coin, HealthPack, Shield, Attackup, Light

*   World/logic objects:

    *   QuestItem, Door, Portal, Trapdoor

*   Hazards & combat:

    *   Trap, MovingBomb, Bullet

*   Lightweight base type used by some world objects:

    *   MazeObjects

## UML Class Diagrams

### Screen Flow
![Screen Flow](docs/uml/screen-flow.png)

### Gameplay Model
![Gameplay UML](docs/uml/gameplay.png)

### Package Overview
![Package Diagram](docs/uml/package-diagram.png)


### de.tum.cit.fop.maze.Various (Helpers)

*   LevelLoader (load map/level content and spawn objects)

*   Minimap (toggleable minimap overlay)

*   FloatingText (temporary UI feedback)

*   Other small utility classes


### de.tum.cit.fop.maze.Data (Profiles / saves)

*   PlayerProfile (player save/profile data)

*   ProfileManager (load/save profiles)


Class Hierarchy
---------------

### Characters

*   Character (abstract)

    *   Player

    *   Enemy

    *   Boss

    *   Pet


### Pickups

*   Pickup (abstract)

    *   Coin

    *   HealthPack

    *   Shield

    *   Attackup

    *   Light


### Screens

*   LibGDX Screen interface implemented by:

    *   SplashScreen, IntroScreen, MenuScreen, GameScreen, PauseScreen,SettingsScreen, HowToPlayScreen, HintScreen, LevelSelectorScreen,LeaderboardScreen, NameInputScreen, PuzzleScreen, YouWin, YouLose, …


### Game Mechanics Beyond Minimal Requirements

### Sprint / Energy

*   Sprinting (Left Shift) increases movement speed

*   Sprint is limited by an energy/stamina-like mechanic (consumption + regeneration)


### Combat + Weapon Switching

*   Player can attack and switch weapon mode

*   Includes projectile support via a dedicated bullet entity (where enabled by level rules)


### Minimap

*   Toggleable minimap overlay for navigation in larger/complex levels


### Hazards & Timing Challenges

*   Static traps + moving hazards introduce timing/avoidance gameplay beyond simple navigation


### Persistent Settings & Profiles

*   Settings (audio, preferences) persist locally via JSON

*   Profiles/leaderboard flow supports storing and displaying player information


Reviewer Guide (Where to Look First)
------------------------------------

*   **Main gameplay loop:** GameScreen

*   **Level parsing / spawning:** LevelLoader

*   **Player & enemies:** Player, Character, Enemy, Boss

*   **Items & effects:** Pickup subclasses, QuestItem

*   **UI flow:** MenuScreen, PauseScreen, SettingsScreen, YouWin/YouLose

*   **Minimap & feedback:** Minimap, FloatingText, HealthBar


Credits
-------

*   Team members:
* 
    *   Rui, Josh, Guantse, Umut