package balancebot.me;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;


/**
 * Maintaines the "Balancing Act". 
 * It runs this code in a max priority thread, and continually reacts to changes in sensor readings.
 * 
 * The main state variables are:
 * 
 * gyroAngle  This is the angle of the robot, it is the results of
 *                    integrating on the gyro value over time.
 *                    Units: degrees
 *
 *         gyroSpeed  The value from the Gyro Sensor.
 *                    Units: degrees/second
 *
 *         motorPos   This is the motor position used for balancing - the rotational angle of motor
 *                    Units: degrees (sum of two motors)
 *
 *         motorSpeed This is the rotational speed of the wheels of the robot found by taking an average of rotational angle of the motors and then take the derivative with respect to time. 
 *                    Units: degrees/second (sum of the two motors)
 *
 *         From these state variables, the power to the motors is determined
 *         by the equation:
 *             power = KSPEED * motorSpeed +  KPOS  * motorPos + KGYROSPEED * gyro +
 *                     KGYROANGLE * gyroAngle
 *
 *     These are the main four balance constants, only the gyro
 *         constants are relative to the wheel size.
 *         KPOS and KSPEED are self-relative to the wheel size.
 *
 */
public class BalanceBot extends Thread {
	
	/** The right (leg) motor. */
	private UnregulatedMotor mRight = new UnregulatedMotor(MotorPort.A);
	
	/** The left (leg) motor. */
	private UnregulatedMotor mLeft = new UnregulatedMotor(MotorPort.B);
	
	/** The gyro sensor. */
	private EV3GyroSensor gyroSensor;
	
	/** The gyro reader. */
	private SampleProvider gyroReader;
	
	/** Where the readings are stored. */
	private float[] sample;
	
	/** The current gyro angle. Initially set to 0. */  
	private double gyroAngle = 0;
	
	/** The default speed. Set to 3. */
	final private int DEFAULT_SPEED = 3;
	
	/** The starting angle. Set to -0.25 */
	final private double STARTING_ANGLE = -0.25;
	
	/** The max speed. Set to 5. */
	final private int MAX_SPEED = 5;
	
	/** The max turn. Set to 5. */
	final private int MAX_TURN = 5;
	
	/** The angle speed constant. */
	final private double KSPEED = 0.08;
	
	/** The gyro speed constant. */
    final private double KGYROSPEED = 0.8;
    
    /** The position constant. */
    final private double KPOS = 0.12;
    
    /** The gyro angle constant. */
    final private double KGYROANGLE = 15;
	
	/** The speed. Initially set to 0.
	 * 
	 * Limited to 10 (forwards) to -10 (backwards).
	 */
	private double speed = 0;
	
	/** The direction. Initially set to 0.
	 *
	 * Limited to -50 (left) to 50 (right).
	 */
	private double direction = 0;

	/** Determines whether the robot suspends its balancing act. */
	private boolean terminate = false;
	
	/**
	 * Instantiates a new balance bot.
	 *
	 * @param gyroSensor the gyro sensor
	 */
	public BalanceBot(EV3GyroSensor gyroSensor) {
		this.gyroSensor = gyroSensor; 
	}
	
	/**
	 * Gets the sample.
	 *
	 * @return the current gyro angle
	 */
	// Constructor
	public double getSample() {
		return gyroAngle;
	}
	
	/**
	 * Sets the default speed.
	 */
	public void setDefaultSpeed() {
		setSpeed(DEFAULT_SPEED);
	}
	
	/**
	 * Sets the default turn.
	 */
	public void setDefaultTurn() {
		turn(0);
	}
	
	/**
	 * Suspend loop.
	 * 
	 * Used in Fallen behaviour.
	 */
	public void suspendLoop() {
		mLeft.setPower(0);
		mRight.setPower(0);
		
		terminate = true;
	}
	
	/**
	 * Resume loop.
	 * 
	 * Used in Fallen behaviour class.
	 */
	public void resumeLoop() {
		terminate = false;
	}
	
	/**
	 * Repositions robot in another direction.
	 * 
	 * Only called when an object is detected within 30cm.
	 */
	// Called when the robot detects an object ahead
	public void turnAround() {
		// Reverse for 2 seconds at speed -3
		setSpeed(-3);
		LCD.drawString("R E V E R S I N G !", 0, 4);
		Delay.msDelay(2000); // 2 seconds
		
		// Reverse at an angle of 5 for 3 seconds
		turn(5);
		Delay.msDelay(3000);
		
		// Set the robot back to a straight path forwards
		setDefaultSpeed();
		setDefaultTurn();
		Button.LEDPattern(0);
	}
	
	/**
	 * Closes motors.
	 */
	public void closeMotors() {
		mRight.close();
		mLeft.close();
	}
	
	/**
	 * Sets the speed.
	 *
	 * @param s the new speed
	 */
	// Limits speed to MAX_SPEED
	public void setSpeed(double s) {
		if (s>MAX_SPEED) s=MAX_SPEED;
		if (s<-MAX_SPEED) s=-MAX_SPEED;
		speed=s;
	}
	
	/**
	 * Turns the robot left(-) or right(+).
	 * 
	 * Limits turn to the value of MAX_TURN.
	 *
	 * @param d the direction to be set.
	 */
	public void turn(double d) {
		if (d>MAX_TURN) d=MAX_TURN;
		if (d<-MAX_TURN)d=-MAX_TURN;
		direction=d;
	}

	/**
	 * Run - aka The Balancing Act.
	 * 
	 * Balances the robot in response to sensor information.
	 * Runs on a thread at max priority.
	 */
	public void run() {

		Sound.beepSequenceUp();
		Thread.currentThread().setPriority(MAX_PRIORITY);

		while(true) {
			
			double gyroSpeed = 0; // Gyro angle speed in degrees/sec
			double motorPos = 0; // Rotation angle of motor in degrees
			double motorSpeed = 0; // Rotation speed of motor in degrees/sec
			double motorPower = 0; // Motor power (-100 to 100).
			double motorSum = 0;
			double motorDirection = 0;
			double motorDP1 = 0;
			double motorDP2 = 0;
			double motorDP3 = 0;

			// Gives the robot time to stabilise
			int loopCount=0;			
			boolean ready=false;
			mRight.resetTachoCount();

			mLeft.resetTachoCount();
			gyroSensor.reset();
			gyroReader = gyroSensor.getRateMode();
			sample = new float[gyroReader.sampleSize()];
			long lastTimeStep = System.nanoTime();

			gyroAngle = STARTING_ANGLE;

			while (!terminate) {
				// Get time in seconds since last reading
				long now = System.nanoTime();
				double dt = (now - lastTimeStep) / 1000000000.0;	// Time step in seconds
				lastTimeStep = now;

				// Get gyro angle and speed
				gyroSensor.fetchSample(sample, 0);
				gyroSpeed = -sample[0]; // invert sign to undo negation in class EV3GyroSensor
				gyroAngle = gyroAngle + (gyroSpeed * dt); // integrate angle speed to get angle

				// Get motor rotation angle and rotational angle speed
				double motorSumOld = motorSum;
				double rightTacho = mRight.getTachoCount();
				double leftTacho = mLeft.getTachoCount();
				motorSum = rightTacho + leftTacho;
				motorDirection = motorSum - motorSumOld;
				motorPos = motorPos + motorDirection;
				motorSpeed = ((motorDirection + motorDP1 + motorDP2 + motorDP3) / 4.0) / dt; // motor rotational speed
				motorDP3 = motorDP2;
				motorDP2 = motorDP1;
				motorDP1 = motorDirection;

				// Calculate new motor power (+ is forwards, - is backwards)
				// Reference on this formula included in README.md
				motorPos -= speed;
				motorPower = KSPEED * motorSpeed + KPOS * motorPos + KGYROSPEED * gyroSpeed + KGYROANGLE * gyroAngle;
				motorPower = 0.08 * motorSpeed + 0.12 * motorPos + 0.8 * gyroSpeed + 15 * gyroAngle;
				if (motorPower > 100) motorPower = 100;
				if (motorPower < -100) motorPower = -100;
				if (ready){
					mRight.setPower((int) (motorPower - direction));
					mLeft.setPower((int) (motorPower + direction));
				}

				Delay.msDelay(10);
				loopCount++;
				
				// Skips the first 10 loops
				if (loopCount==10) ready=true;	
			}
			
		}
		
	}

}