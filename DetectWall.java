package balancebot.me;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.subsumption.Behavior;

/**
 * Detects if an object is within 30cm of the robot ahead.
 * It responds by backing up and turning to position the robot into a new direction.
 */
public class DetectWall implements Behavior {
	
	/** The ultrasonic sensor. */
	private EV3UltrasonicSensor ultraSensor = new EV3UltrasonicSensor(SensorPort.S4);
	
	/** The ultrasonic sensor reader. */
	private SampleProvider ultraReader = ultraSensor.getDistanceMode();
	
	/** Where the readings are stored. */
	private float[] sample = new float[ultraReader.sampleSize()];
	
	/** The distance threshold. */
	final private double DISTANCE_THRESHOLD = 0.3; // 30cm distance
	
	/** The BalanceBot object. */
	private BalanceBot bb;
	
	/**
	 * Instantiates a new detect wall.
	 *
	 * @param bb BalanceBot object.
	 */
	DetectWall(BalanceBot bb) {
		this.bb = bb;
	}

	/**
	 * Take control.
	 *
	 * @return true if there's an object within 30cm
	 */
	@Override
	public boolean takeControl() {
		float s1;
	
		ultraReader.fetchSample(sample, 0);
		s1 = sample[0];
		
		// Continually updates to the screen
		LCD.drawString("dist: " + s1, 0, 3);
		
		if(s1 <= DISTANCE_THRESHOLD) { return true; } else { return false; }
	}

	/**
	 * Action.
	 * 
	 * Changes direction of the robot.
	 */
	@Override
	public void action() {
		Button.LEDPattern(3);
		bb.turnAround();	
	}

	/**
	 * Suppress.
	 * 
	 * Resets speed and turn to default values.
	 */
	@Override
	public void suppress() {
		bb.setDefaultSpeed();
		bb.setDefaultTurn();

	}

}
