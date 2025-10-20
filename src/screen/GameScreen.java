package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import Animations.Explosion;
import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.*;
import engine.SoundManager;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

// NEW Item code
import entity.Item;
import entity.ItemPool;

/**
 * Implements the game screen, where the action happens.(supports co-op with
 * shared team lives)
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen {

    /** Milliseconds until the screen accepts user input. */
    private static final int INPUT_DELAY = 6000;
    /** Bonus score for each life remaining at the end of the level. */
    private static final int LIFE_SCORE = 100;
    /** Minimum time between bonus ship's appearances. */
    private static final int BONUS_SHIP_INTERVAL = 20000;
    /** Maximum variance in the time between bonus ship's appearances. */
    private static final int BONUS_SHIP_VARIANCE = 10000;
    /** Time until bonus ship explosion disappears. */
    private static final int BONUS_SHIP_EXPLOSION = 500;
    /** Time from finishing the level to screen change. */
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    /** Height of the interface separation line. */
    private static final int SEPARATION_LINE_HEIGHT = 68;
      private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static boolean sessionHighScoreNotified = false;

    /** For Check Achievement
     * 2015-10-02 add new */
    private AchievementManager achievementManager;
    /** Current game difficulty settings. */
    private GameSettings gameSettings;
    /** Current difficulty level number. */
    private int level;
    /** Formation of enemy ships. */
    private EnemyShipFormation enemyShipFormation;
    private EnemyShip enemyShipSpecial;
    /** Formation of player ships. */
    private Ship[] ships = new Ship[GameState.NUM_PLAYERS];
    /** Minimum time between bonus ship appearances. */
    private Cooldown enemyShipSpecialCooldown;
    /** Time until bonus ship explosion disappears. */
    private Cooldown enemyShipSpecialExplosionCooldown;
    /** Time from finishing the level to screen change. */
    private Cooldown screenFinishedCooldown;
    /** Set of all bullets fired by on screen ships. */
    private Set<Bullet> bullets;
    /** Set of all items spawned. */
    private Set<Item> items;
    private long gameStartTime;
    /** Checks if the level is finished. */
    private boolean levelFinished;
    /** Checks if a bonus life is received. */
    private boolean bonusLife;
    private int topScore;
    private boolean highScoreNotified;
    private long highScoreNoticeStartTime;

    private boolean isPaused;
    private Cooldown pauseCooldown;
    private Cooldown returnMenuCooldown;

    private int score;
    private int lives;
    private int bulletsShot;
    private int shipsDestroyed;
    private Ship ship;

    /** checks if player took damage
     * 2025-10-02 add new variable
     * */
    private boolean countdownSoundPlayed = false;

    private final GameState state;

    private Ship.ShipType shipTypeP1;
    private Ship.ShipType shipTypeP2;
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *                     Current game state.
     * @param gameSettings
     *                     Current game settings.
     * @param bonusLife
     *                     Checks if a bonus life is awarded this level.
     * @param width
     *                     Screen width.
     * @param height
     *                     Screen height.
     * @param fps
     *                     Frames per second, frame rate at which the game is run.
     * @param shipTypeP1
     *                     Player 1's ship type.
     * @param shipTypeP2
     *                     Player 2's ship type.
     * @param achievementManager
     * 			               Achievement manager instance used to track and save player achievements.
     * 			  2025-10-03 add generator parameter and comment
     */
    public GameScreen(final GameState gameState,
                      final GameSettings gameSettings, final boolean bonusLife,
                      final int width, final int height, final int fps, final Ship.ShipType shipTypeP1, final Ship.ShipType shipTypeP2, final AchievementManager achievementManager) {
        super(width, height, fps);

        this.state = gameState;
        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.shipTypeP1 = shipTypeP1;
        this.shipTypeP2 = shipTypeP2;
        this.level = gameState.getLevel();
        this.score = gameState.getScore();
        this.lives = gameState.getLivesRemaining();
        if (this.bonusLife)
            this.lives++;
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();

        // for check Achievement 2025-10-02 add
        this.achievementManager = achievementManager;

//        try {
//            List<Score> highScores = Core.getFileManager().loadHighScores();
//            this.topScore = highScores.isEmpty() ? 0 : highScores.get(0).getScore();
//        } catch (IOException e) {
//            logger.warning("Couldn't load high scores for checking!");
//            this.topScore = 0;
//        }
        this.highScoreNotified = false;
        this.highScoreNoticeStartTime = 0;

        // 2P: bonus life adds to team pool + singleplayer mode
        if (this.bonusLife) {
            if (state.isSharedLives()) {
                state.addTeamLife(1); // two player
            } else {
                // 1P legacy: grant to P1
                state.addLife(0, 1);  // singleplayer
            }
        }
      // [ADD] ensure achievementManager is not null for popup system
		if (this.achievementManager == null) this.achievementManager = new AchievementManager();
    }

      /**
     * Resets the session high score notification flag.
     * Should be called when a new game starts from the main menu.
     */
    public static void resetSessionHighScoreNotified() {
        sessionHighScoreNotified = false;
    }

    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();

        state.clearAllEffects();

        // Start background music for gameplay
        SoundManager.startBackgroundMusic("sound/SpaceInvader-GameTheme.wav");

        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);

        // 2P mode: create both ships, tagged to their respective teams
        this.ships[0] = new Ship(this.width / 2 - 60, this.height - 30, Entity.Team.PLAYER1, shipTypeP1, this.state); // P1
        this.ships[0].setPlayerId(1);

        // only allowing second ship to spawn when 2P mode is chosen
        if (state.isCoop()) {
            this.ships[1] = new Ship(this.width / 2 + 60, this.height - 30, Entity.Team.PLAYER2, shipTypeP2, this.state); // P2

            this.ships[1].setPlayerId(2);
        } else {
            this.ships[1] = null; // ensuring there's no P2 ship in 1P mode
        }

        this.enemyShipSpecialCooldown = Core.getVariableCooldown(BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
        this.enemyShipSpecialCooldown.reset();
        this.enemyShipSpecialExplosionCooldown = Core.getCooldown(BONUS_SHIP_EXPLOSION);
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.bullets = new HashSet<Bullet>();

        // New Item Code
        this.items = new HashSet<Item>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
        drawManager.setDeath(false);

        this.isPaused = false;
        this.pauseCooldown = Core.getCooldown(300);
        this.returnMenuCooldown = Core.getCooldown(300);
    }



    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        // 2P mode: award bonus score for remaining TEAM lives
        state.addScore(0, LIFE_SCORE * state.getLivesRemaining());

        // Stop all music on exiting this screen
        SoundManager.stopAllMusic();

        this.logger.info("Screen cleared with a score of " + state.getScore());
        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        // Countdown beep once during pre-start
        if (!this.inputDelay.checkFinished() && !countdownSoundPlayed) {
            long elapsed = System.currentTimeMillis() - this.gameStartTime;
            if (elapsed > 1750) {
                SoundManager.playOnce("sound/CountDownSound.wav");
                countdownSoundPlayed = true;
            }
        }

        if (this.inputDelay.checkFinished() && inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.pauseCooldown.checkFinished()) {
            this.isPaused = !this.isPaused;
            this.pauseCooldown.reset();

            if (this.isPaused) {
                // Pause game music when pausing - no sound during pause
                SoundManager.stopBackgroundMusic();
            } else {
                // Resume game music when unpausing
                SoundManager.startBackgroundMusic("sound/SpaceInvader-GameTheme.wav");
            }
        }
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE) && this.returnMenuCooldown.checkFinished()) {
            SoundManager.playOnce("sound/select.wav");
            SoundManager.stopAllMusic(); // Stop all music before returning to menu
            returnCode = 1;
            this.isRunning = false;
        }

        if (!this.isPaused) {
            if (this.inputDelay.checkFinished() && !this.levelFinished) {

                // Per-player input/move/shoot
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    Ship ship = this.ships[p];

                    if (ship == null || ship.isDestroyed())
                        continue;

                    boolean moveRight, moveLeft, fire;
                    // Get player key input status
                    if (p == 0) {
                        moveRight = inputManager.isP1RightPressed();
                        moveLeft = inputManager.isP1LeftPressed();
                        fire = inputManager.isP1ShootPressed();
                    } else {
                        moveRight = inputManager.isP2RightPressed();
                        moveLeft = inputManager.isP2LeftPressed();
                        fire = inputManager.isP2ShootPressed();
                    }

                    boolean isRightBorder = ship.getPositionX() + ship.getWidth() + ship.getSpeed() > this.width - 1;

                    boolean isLeftBorder = ship.getPositionX() - ship.getSpeed() < 1;

                    if (moveRight && !isRightBorder)
                        ship.moveRight();
                    if (moveLeft && !isLeftBorder)
                        ship.moveLeft();

                    fire = (p == 0)
                            ? inputManager.isKeyDown(KeyEvent.VK_SPACE)
                            : inputManager.isKeyDown(KeyEvent.VK_ENTER);

                        if (fire && ship.shoot(this.bullets)) {
                            SoundManager.playOnce("sound/shoot.wav");

                        state.incBulletsShot(p); // 2P mode: increments per-player bullet shots

                    }
                }

                // Special ship lifecycle
                if (this.enemyShipSpecial != null) {
                    if (!this.enemyShipSpecial.isDestroyed())
                        this.enemyShipSpecial.move(2, 0);
                    else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
                        this.enemyShipSpecial = null;
                }
                if (this.enemyShipSpecial == null && this.enemyShipSpecialCooldown.checkFinished()) {
                    this.enemyShipSpecial = new EnemyShip();
                    this.enemyShipSpecialCooldown.reset();
                    SoundManager.playLoop("sound/special_ship_sound.wav");
                    this.logger.info("A special ship appears");
                }
                if (this.enemyShipSpecial != null && this.enemyShipSpecial.getPositionX() > this.width) {
                    this.enemyShipSpecial = null;
                    SoundManager.stop();
                    this.logger.info("The special ship has escaped");
                }

                // Update ships & enemies
                for (Ship s : this.ships)
                    if (s != null)
                        s.update();

                this.enemyShipFormation.update();
                int bulletsBefore = this.bullets.size();
                this.enemyShipFormation.shoot(this.bullets);
                if (this.bullets.size() > bulletsBefore) {
                    // At least one enemy bullet added
                    SoundManager.playOnce("sound/shoot_enemies.wav");
                }
            }


            manageCollisions();
            cleanBullets();

            // Item Entity Code
            cleanItems();
            manageItemPickups();

            // check active item affects
            state.updateEffects();
            drawManager.setLastLife(state.getLivesRemaining() == 1);
		    draw();

            if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
                sessionHighScoreNotified = true;
                this.highScoreNotified = true;
                this.highScoreNoticeStartTime = System.currentTimeMillis();
            }

            // End condition: formation cleared or TEAM lives exhausted.
            if ((this.enemyShipFormation.isEmpty() || !state.teamAlive()) && !this.levelFinished) {
                // The object managed by the object pool pattern must be recycled at the end of the level.
                BulletPool.recycle(this.bullets);
                this.bullets.removeAll(this.bullets);
                ItemPool.recycle(items);
                this.items.removeAll(this.items);

                this.levelFinished = true;
                this.screenFinishedCooldown.reset();

		    }

            if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
                if (!achievementManager.hasPendingToasts()) {
                    this.isRunning = false;
                }
            }

		    if (this.achievementManager != null) this.achievementManager.update();
	    }

        checkAchievement();
        draw();
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawExplosions();
        drawManager.updateGameSpace();

        for (Ship s : this.ships)
            if (s != null)
                drawManager.drawEntity(s, s.getPositionX(), s.getPositionY());

        if (this.enemyShipSpecial != null)
            drawManager.drawEntity(this.enemyShipSpecial,
                    this.enemyShipSpecial.getPositionX(),
                    this.enemyShipSpecial.getPositionY());

        enemyShipFormation.draw();

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(),
                    bullet.getPositionY());

        // draw items
        for (Item item : this.items)
            drawManager.drawEntity(item, item.getPositionX(),
                    item.getPositionY());

		// Aggregate UI (team score & team lives)
		drawManager.drawScore(this, state.getScore());
    drawManager.drawLives(this, state.getLivesRemaining(),state.isCoop() );
		drawManager.drawCoins(this,  state.getCoins()); // ADD THIS LINE - 2P mode: team total
        // 2P mode: setting per-player coin count
//        if (state.isCoop()) {
//            // left: P1
//            String p1 = String.format("P1  S:%d  K:%d  B:%d",
//                    state.getScore(0), state.getShipsDestroyed(0),
//                    state.getBulletsShot(0));
//            // right: P2
//            String p2 = String.format("P2  S:%d  K:%d  B:%d",
//                    state.getScore(1), state.getShipsDestroyed(1),
//                    state.getBulletsShot(1));
//            drawManager.drawCenteredRegularString(this, p1, 40);
//            drawManager.drawCenteredRegularString(this, p2, 60);
//            // remove the unnecessary "P1 S: K: B: C:" and "P2 S: K: B: C:" lines from the game screen
//        }
        drawManager.drawLevel(this, this.state.getLevel());
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        drawManager.drawShipCount(this, enemyShipFormation.getShipCount());

		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.state.getLevel(), countdown, this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
		}
        if (this.highScoreNotified &&
                System.currentTimeMillis() - this.highScoreNoticeStartTime < HIGH_SCORE_NOTICE_DURATION) {
            drawManager.drawNewHighScoreNotice(this);
        }

		// [ADD] draw achievement popups right before completing the frame
		drawManager.drawAchievementToasts(
				this,
				(this.achievementManager != null)
						? this.achievementManager.getActiveToasts()
						: java.util.Collections.emptyList()
		);
		if(this.isPaused){
			drawManager.drawPauseOverlay(this);
		}

        drawManager.completeDrawing(this);
    }

    /**
     * Cleans bullets that go off screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
                    || bullet.getPositionY() > this.height)
                recyclable.add(bullet);
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Cleans items that go off screen.
     */
    private void cleanItems() {
        Set<Item> recyclableItems = new HashSet<Item>();
        for (Item item : this.items) {
            item.update();
            if (item.getPositionY() > this.height)
                recyclableItems.add(item);
        }
        this.items.removeAll(recyclableItems);
        ItemPool.recycle(recyclableItems);
    }

    /**
     * Manages pickups between player and items.
     */
    private void manageItemPickups() {
        Set<Item> collected = new HashSet<Item>();
        for (Item item : this.items) {

            for(Ship ship: this.ships) {
                if(ship == null) continue;
                if (checkCollision(item, ship) && !collected.contains(item)) {
                    collected.add(item);
                    this.logger.info("Player " + ship.getPlayerId() + " picked up item: " + item.getType());
                    SoundManager.playOnce("sound/hover.wav");
                    item.applyEffect(getGameState(), ship.getPlayerId());
                }
            }
        }
        this.items.removeAll(collected);
        ItemPool.recycle(collected);
    }

    /**
     * Enemy bullets hit players → decrement TEAM lives; player bullets hit enemies
     * → add score.
     */
    private void manageCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            if (bullet.getSpeed() > 0) {
                // Enemy bullet vs both players

                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    Ship ship = this.ships[p];
                    if (ship != null && !ship.isDestroyed()
                            && checkCollision(bullet, ship) && !this.levelFinished) {
                        recyclable.add(bullet);


                        drawManager.triggerExplosion(ship.getPositionX(), ship.getPositionY(), false, state.getLivesRemaining() == 1);
                        ship.addHit();

                        ship.destroy(); // explosion/respawn handled by Ship.update()
                        SoundManager.playOnce("sound/explosion.wav");
                        state.decLife(p); // decrement shared/team lives by 1

                        // Record damage for Survivor achievement check
                        state.isTookdamage();

                        drawManager.setLastLife(state.getLivesRemaining() == 1);
                        drawManager.setDeath(state.getLivesRemaining() == 0);

						this.logger.info("Hit on player " + (p + 1) + ", team lives now: " + state.getLivesRemaining());
						break;
					}
				}
			} else {
				// Player bullet vs enemies
				// map Bullet owner id (1 or 2) to per-player index (0 or 1)
				final int ownerId = bullet.getOwnerPlayerId(); // 1 or 2 (0 if unset)
				final int pIdx = (ownerId == 2) ? 1 : 0; // default to P1 when unset

                boolean finalShip = this.enemyShipFormation.lastShip();

                // Check collision with formation enemies
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
                        recyclable.add(bullet);
                        enemyShip.hit();

                        if (enemyShip.isDestroyed()) {
                            int points = enemyShip.getPointValue();
                            state.addCoins(pIdx, enemyShip.getCoinValue()); // 2P mode: modified to per-player coins

                            drawManager.triggerExplosion(enemyShip.getPositionX(), enemyShip.getPositionY(), true, finalShip);
                            state.addScore(pIdx, points); // 2P mode: modified to add to P1 score for now
                            state.incShipsDestroyed(pIdx);

                            // obtain drop from ItemManager (may return null)
                            Item drop = engine.ItemManager.getInstance().obtainDrop(enemyShip);
                            if (drop != null) {
                                this.items.add(drop);
                                this.logger.info("Spawned " + drop.getType() + " at " + drop.getPositionX() + "," + drop.getPositionY());
                            }

                            this.enemyShipFormation.destroy(enemyShip);
                            SoundManager.playOnce("sound/invaderkilled.wav");
                            this.logger.info("Hit on enemy ship.");
                        }
                        break;
                    }
                }

                if (this.enemyShipSpecial != null
                        && !this.enemyShipSpecial.isDestroyed()
                        && checkCollision(bullet, this.enemyShipSpecial)) {
                    int points = this.enemyShipSpecial.getPointValue();

                    state.addCoins(pIdx, this.enemyShipSpecial.getCoinValue()); // 2P mode: modified to per-player coins

                    state.addScore(pIdx, points);
                    state.incShipsDestroyed(pIdx); // 2P mode: modified incrementing ships destroyed

					this.enemyShipSpecial.destroy();
                    SoundManager.stop();
                    SoundManager.playOnce("sound/explosion.wav");
                    drawManager.triggerExplosion(this.enemyShipSpecial.getPositionX(), this.enemyShipSpecial.getPositionY(), true, true);
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Checks if two entities are colliding.
     *
     * @param a
     *            First entity, the bullet.
     * @param b
     *            Second entity, the ship.
     * @return Result of the collision test.
     */
    private boolean checkCollision(final Entity a, final Entity b) {
        int centerAX = a.getPositionX() + a.getWidth() / 2;
        int centerAY = a.getPositionY() + a.getHeight() / 2;
        int centerBX = b.getPositionX() + b.getWidth() / 2;
        int centerBY = b.getPositionY() + b.getHeight() / 2;
        int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
        int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
        int distanceX = Math.abs(centerAX - centerBX);
        int distanceY = Math.abs(centerAY - centerBY);
        return distanceX < maxDistanceX && distanceY < maxDistanceY;
    }

    /**
     * Returns a GameState object representing the status of the game.
     *
     * @return Current game state.
     */
    public final GameState getGameState() {
        return this.state;
    }

    /**
     * check Achievement released;
     */
    public void checkAchievement(){
        // First Blood
        if(state.getShipsDestroyed() == 1) {
            achievementManager.unlock("First Blood");
        }
        // Clear
        if (levelFinished && this.enemyShipFormation.isEmpty() && state.getLevel()==5) {
            achievementManager.unlock("Clear");
            float p1Acc = state.getBulletsShot(0) > 0 ? (float) state.getShipsDestroyed(0) / state.getBulletsShot(0)*100 : 0f;
            float p2Acc = state.getBulletsShot(1) > 0 ? (float) state.getShipsDestroyed(1) / state.getBulletsShot(1)*100 : 0f;
            // Survivor

            if(!state.checktookdamage()){
                achievementManager.unlock("Survivor");
            }
            //Sharpshooter
            if(p1Acc>=80){
                //1p
                achievementManager.unlock("Sharpshooter");
                //coop
                if(p2Acc>=80){
                    achievementManager.unlock("Sharpshooter");
                }
            }
        }

        //50 Bullets
        if(state.getBulletsShot() >= 50){
            achievementManager.unlock("50 Bullets");
        }
        //Get 3000 Score
        if(state.getScore()>=3000){
            achievementManager.unlock("Get 3000 Score");
        }
    }
}
