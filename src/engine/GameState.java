// engine/GameState.java
package engine;

import java.util.HashMap;
import java.util.Map;
import engine.ItemEffect.ItemEffectType;

/**
 * Implements an object that stores the state of the game between levels -
 * supports 2-player co-op with shared lives.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 *
 */
public class GameState {

    private static final java.util.logging.Logger logger = Core.getLogger();

    // 2P mode: number of players used for shared lives in co-op
	public static final int NUM_PLAYERS = 2; // adjust later if needed

	// 2P mode: true if in co-op mode
	private final boolean coop;

	/** Current game level. */
	private int level;

	// 2P mode: if true, lives are shared in a team pool; else per-player lives
	private final boolean sharedLives;

	// team life pool and cap (used when sharedLives == true).
	private int teamLives;
	private int teamLivesCap;

    // check ship damaged for achievement
    private boolean tookdamage;

	/** Current coin count. */ // ADD THIS LINE
    private static int coins = 0; // ADD THIS LINE - edited for 2P mode

    private static class EffectState {
        Cooldown cooldown;
        boolean active;
        Integer effectValue;

        EffectState() {
            this.cooldown = null;
            this.active = false;
            this.effectValue = null;
        }
    }

    /** Each player has all effect types always initialized (inactive at start). */
    private final Map<Integer, Map<ItemEffectType, EffectState>> playerEffects = new HashMap<>();

	// 2P mode: co-op aware constructor used by the updated Core loop - livesEach
	// applies per-player; co-op uses shared pool.
	public GameState(final int level, final int livesEach, final boolean coop, final int coin) {
		this.level = level;
		this.coop = coop;
        this.coins = coin;
        this.tookdamage = false;

		if (coop) {
			this.sharedLives = true;
			this.teamLives = Math.max(0, livesEach * NUM_PLAYERS);
			this.teamLivesCap = this.teamLives;
		} else {
			this.sharedLives = false;
			this.teamLives = 0;
			this.teamLivesCap = 0;
			// legacy: put all lives on P1
			lives[0] = Math.max(0, livesEach);
		}

        initializeEffectStates();
    }

	// 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
	// mode).
	private final int[] score = new int[NUM_PLAYERS];
	private final int[] lives = new int[NUM_PLAYERS];
	private final int[] bulletsShot = new int[NUM_PLAYERS];
	private final int[] shipsDestroyed = new int[NUM_PLAYERS];

	/* ---------- Constructors ---------- */

	/** Legacy 6-arg - kept for old call sites */
	/**
	 * Constructor.
	 *
	 * @param level
	 *                       Current game level.
	 * @param score
	 *                       Current score.
	 * @param livesRemaining
	 *                       Lives currently remaining.
	 * @param bulletsShot
	 *                       Bullets shot until now.
	 * @param shipsDestroyed
	 *                       Ships destroyed until now.
	 * @param coins          // ADD THIS LINE
	 *                       Current coin count. // ADD THIS LINE
	 */
	public GameState(final int level, final int score,
					 final int livesRemaining, final int bulletsShot,
					 final int shipsDestroyed, final int coins) { // MODIFY THIS LINE
		this.level = level;
		this.sharedLives = false;
		this.teamLives = 0;
		this.teamLivesCap = 0;

		this.score[0] = score;
		this.lives[0] = livesRemaining;
		this.bulletsShot[0] = bulletsShot;
		this.shipsDestroyed[0] = shipsDestroyed;

		this.coins = coins; // ADD THIS LINE - edited for 2P mode
		this.coop = false; // 2P: single-player mode

        initializeEffectStates();
    }

	/* ------- 2P mode: aggregate totals used by Core/ScoreScreen/UI------- */
	public int getScore() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += score[p];
		return t;
	}

	public int getLivesRemaining() {
		return sharedLives ? teamLives : (lives[0] + lives[1]);
	}

	public int getBulletsShot() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += bulletsShot[p];
		return t;
	}

	public int getShipsDestroyed() {
		int t = 0;
		for (int p = 0; p < NUM_PLAYERS; p++)
			t += shipsDestroyed[p];
		return t;

	}


	/* ----- Per-player getters (needed by Score.java) ----- */
	public int getScore(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? score[p] : 0;
	}

	public int getBulletsShot(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? bulletsShot[p] : 0;
	}

	public int getShipsDestroyed(final int p) {
		return (p >= 0 && p < NUM_PLAYERS) ? shipsDestroyed[p] : 0;
	}

	public void addScore(final int p, final int delta) {
		int realDelta = delta;
		// If ScoreBoost item active, score gain is doubled.
        Integer multiplier = getEffectValue(p, ItemEffect.ItemEffectType.SCOREBOOST);
        if (multiplier != null) {
            realDelta = delta * multiplier;
            logger.info("[GameState] Player " + (p + 1) + " ScoreBoost active (x" + multiplier + "). Score changed from " + delta + " to " + realDelta);
        }
		score[p] += realDelta;
	}

	public void incBulletsShot(final int p) {
		bulletsShot[p]++;
	}

	public void incShipsDestroyed(final int p) {
		shipsDestroyed[p]++;
	}

    public boolean getCoop() { return this.coop; }

	// 2P mode: per-player coin tracking
    public int getCoins() { return coins; } // legacy total for ScoreScreen

    public void addCoins(final int p, final int delta) {
        if (p >= 0 && p < NUM_PLAYERS && delta > 0)
            coins = Math.max(0, coins + delta);
    }

    public boolean spendCoins(final int p, final int amount) {
        if (p < 0 || p >= NUM_PLAYERS || amount < 0) return false;
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

	// ===== Mode / life-pool helpers expected elsewhere =====
	public boolean isCoop() {
		return coop;
	}

	public boolean isSharedLives() {
		return sharedLives;
	}

	public int getTeamLives() {
		return teamLives;
	}

	public void addTeamLife(final int n) {
		if (sharedLives)
			teamLives = Math.min(teamLivesCap, teamLives + Math.max(0, n));
	}

	private void decTeamLife(final int n) {
		if (sharedLives)
			teamLives = Math.max(0, teamLives - Math.max(0, n));
	}

    // 2P mode: decrement life (shared pool if enabled; otherwise per player). */
    public void decLife(final int p) {
        if (sharedLives) {
            decTeamLife(1);
        } else if (p >= 0 && p < NUM_PLAYERS && lives[p] > 0) {
            lives[p]--;
        }
    }

    // for bonusLife, balance out decLife (+/- life)
    public void addLife(final int p, final int n) {
        if (sharedLives) {
            addTeamLife(n);
        } else if (p >= 0 && p < NUM_PLAYERS) {
            lives[p] = Math.max(0, lives[p] + Math.max(0, n));
        }
    }

    public void isTookdamage(){ tookdamage = true;}
    public boolean checktookdamage(){return tookdamage;}

    public int getLevel() {
		return level;
	}

	public void nextLevel() {
		level++;
	}

	// Team alive if pool > 0 (shared) or any player has lives (separate).
	public boolean teamAlive() {
		return sharedLives ? (teamLives > 0) : (lives[0] > 0 || lives[1] > 0);
	}

	// for ItemEffect.java
	public int getTeamLivesCap() {
		return teamLivesCap;
	}

	// for ItemEffect.java
	public int get1PlayerLives() {

		return lives[0];
	}

    /** ---------- Item effects status methods ---------- **/

    /** Initialize all possible effects for every player (inactive). */
    private void initializeEffectStates() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Map<ItemEffectType, EffectState> effectMap = new HashMap<>();
            for (ItemEffectType type : ItemEffectType.values()) {
                effectMap.put(type, new EffectState());
            }
            playerEffects.put(p, effectMap);
        }
    }

    public void addEffect(int playerIndex, ItemEffectType type, Integer effectValue, int durationSeconds) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) return;

        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) return;

        EffectState state = effects.get(type);
        if (state == null) return;

        String valueStr = (effectValue != null) ? " (value: " + effectValue + ")" : "";

        if (state.active && state.cooldown != null) {
            // Extend existing effect
            state.cooldown.addTime(durationSeconds * 1000);

            state.effectValue = effectValue;

            logger.info("[GameState] Player " + playerIndex + " extended " + type
                    + valueStr + ") by " + durationSeconds + "s to " + state.cooldown.getDuration() );
        } else {
            // Start new effect
            state.cooldown = Core.getCooldown(durationSeconds * 1000);
            state.cooldown.reset();
            state.active = true;

            state.effectValue = effectValue;

            logger.info("[GameState] Player " + playerIndex + " started " + type
                    + valueStr + ") for " + durationSeconds + "s");
        }
    }

    public boolean hasEffect(int playerIndex, ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) return false;

        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) return false;

        EffectState state = effects.get(type);
        if (state == null || !state.active) return false;

        return !state.cooldown.checkFinished();
    }

    /**
     * Gets the effect value for a specific player and effect type
     *
     * @param playerIndex
     *            Index of the player (0 or 1)
     * @param type
     *            Type of effect to check
     * @return
     *            Effect value if active, null otherwise
     */
    public Integer getEffectValue(int playerIndex, ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) return null;

        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) return null;

        EffectState state = effects.get(type);
        if (state == null || !state.active) return null;

        // Check if effect is still valid (not expired)
        if (state.cooldown != null && state.cooldown.checkFinished()) {
            return null;
        }

        return state.effectValue;
    }

    /** Call this each frame to clean up expired effects */
    public void updateEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Map<ItemEffectType, EffectState> effects = playerEffects.get(p);
            if (effects == null) continue;

            for (Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
                EffectState state = entry.getValue();
                if (state.active && state.cooldown != null && state.cooldown.checkFinished()) {
                    logger.info("[GameState] Player " + p + " effect " + entry.getKey() + " expired.");
                    state.active = false;
                    state.cooldown = null;  // Release reference
                    state.effectValue = null;
                }
            }
        }
    }

    /** Clear all active effects for a specific player */
    public void clearEffects(int playerIndex) {
        //
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) return;

        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) return;

        // for - all effect types for this player
        for (Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
            // get effect state
            EffectState state = entry.getValue();
            // if state active then false
            if (state.active) {
                state.active = false;
                state.cooldown = null;
                state.effectValue = null;
            }
        }
        logger.info("[GameState] Player " + playerIndex + ": All effects cleared.");
    }

    /** Clear all active effects for all players */
    public void clearAllEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            clearEffects(p);
        }
    }
}
