package engine;

/**
 * Implements an object that stores the state of the game between levels.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameState {

	/** Current game level. */
	private int level;
	/** Current score. */
	private int score;
	/** Lives currently remaining. */
	private int livesRemaining;
	/** Bullets shot until now. */
	private int bulletsShot;
	/** Ships destroyed until now. */
	private int shipsDestroyed;
	/** [CHANGE][2025-10-01][PR #1] 코인 상태 필드 추가 */
	/** Total coins collected by the player. */
	private int coins;

	/**
	 * Constructor.
	 *
	 * @param level
	 *            Current game level.
	 * @param score
	 *            Current score.
	 * @param livesRemaining
	 *            Lives currently remaining.
	 * @param bulletsShot
	 *            Bullets shot until now.
	 * @param shipsDestroyed
	 *            Ships destroyed until now.
	 * @param coins
	 * 			  Coins collected by the player.
	 */
	/** [CHANGE][2025-10-01][PR #1] 스냅샷 생성자에 coins 인자 포함 */
	public GameState(final int level, final int score,
					 final int livesRemaining, final int bulletsShot,
					 final int shipsDestroyed, final int coins) {
		this.level = level;
		this.score = score;
		this.livesRemaining = livesRemaining;
		this.bulletsShot = bulletsShot;
		this.shipsDestroyed = shipsDestroyed;
		this.coins = coins;
	}

	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * @return the score
	 */
	public final int getScore() {
		return score;
	}

	/**
	 * @return the livesRemaining
	 */
	public final int getLivesRemaining() {
		return livesRemaining;
	}

	/**
	 * @return the bulletsShot
	 */
	public final int getBulletsShot() {
		return bulletsShot;
	}

	/**
	 * @return the shipsDestroyed
	 */
	public final int getShipsDestroyed() {
		return shipsDestroyed;
	}

	/** [CHANGE][2025-10-01][PR #1] 코인 API: get/add/spend(옵션 set) */
	public int getCoins() { return coins; }
	public void addCoins(int delta) { if (delta > 0) coins += delta; }
	public boolean spendCoins(int amount) {
		if (amount <= 0) return false;
		if (coins < amount) return false;
		coins -= amount;
		return true;
	}
}
