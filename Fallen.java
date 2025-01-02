package balancebot.me;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

/**
 * Detects when the robot has fallen by regularly retrieving and 
 * comparing the gyrosensor SampleProvider object with a constant int 45.
 */
public class Fallen implements Behavior {
   
    /** A BalanceBot object. */
    private BalanceBot bb;
    
    /** The threshold for triggering a 'suspended' state. */
    final private int TILT_THRESHOLD = 45;

    /**
     * The Fallen constructor;
     *
     * @param BalanceBot object.
     */
    Fallen(BalanceBot bb) {
        this.bb = bb;
    }

    /**
     * Action.
     * 
     * Suspends robot's balancing and 
     * waits for Enter button to be pressed
     * so it can resume.
     */
    //Stopping the movement and waiting for button press
    @Override
	public void action() {
		
		// suspending the robot
    	bb.suspendLoop();
    	LCD.drawString("S U S P E N D E D !", 0, 2);
    	Sound.beep();
    	
    	/*	
    	*	Waits for button press
    	*	(during this time, the robot is sat back on it's stand)
    	*/
    	Button.LEDPattern(4);
		Button.ENTER.waitForPressAndRelease();
		Sound.beepSequenceUp();
		
		// Gives time for the robot to stabilise after button press
		Delay.msDelay(1000);
		Sound.beepSequenceUp();
		
		// Resumes the loop i.e. balancing
		bb.resumeLoop();
		Sound.beepSequenceUp();
		
		// Removes the "suspended!" message
		LCD.clear(2);
        
        /* 
        * Set values back to normal
        * (since it was altered by the most recent gyro angle reading)
        */ 
        bb.setDefaultSpeed();
        bb.setDefaultTurn();
        
    }

    /**
     * Take control.
     *
     * @return true if the robot has fallen
     */
    // Checks if robot has fallen
    @Override
    public boolean takeControl() {
    	
    	double s1, s2; // sample 1 and sample 2
        
        s1 = bb.getSample();
        s2 = bb.getSample();
        // Reduces the number of false positives
        s1 = (s1+s2)/2;
        
        // If gyro sensor has fallen i.e. tilted beyond 45 degrees
        if(s1 <= -TILT_THRESHOLD || s1 >= TILT_THRESHOLD) {
        	return true;
        }
        return false;
    }
    
    /**
     *  Suppress.
     */
    @Override
    public void suppress() {
    	
    }
}