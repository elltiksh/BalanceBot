package balancebot.me;
import lejos.robotics.subsumption.Behavior;

/**
 * Starts the BalanceBot thread and makes sure to never run again.
 * This is so that illegal thread exceptions do not occur.
 */
public class Forward implements Behavior {
	
	/**  BalanceBot object. */
	private BalanceBot bb;
	
	/** The started. */
	private boolean started = false;
	
	/**
	 * Instantiates a new forward.
	 *
	 * @param bb the bb
	 */
	Forward(BalanceBot bb) {
		this.bb = bb;
	}
	
	/**
	 * Take control.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean takeControl() {
		if(started) return false;
		return true;
	}

	/**
	 * Action.
	 */
	@Override
	public void action() {
		
		// starts the balance thread
		try {
			bb.start();
		} catch (IllegalThreadStateException e) {}
		
		// moves the robot 'straight ahead'
		bb.setDefaultSpeed();
		bb.setDefaultTurn();
		started = true;
	}

	/**
	 * Suppress.
	 */
	@Override
	public void suppress() {
		
	}
}