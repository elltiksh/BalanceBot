package balancebot.me;
import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;

/**
 * The highest priority behaviour which checks whether the battery voltage is high enough
 * to continue the function of other behaviours. Otherwise, it displays a message on screen.
 */
public class LowBattery implements Behavior {
	
	/** The minimum voltage. */
	final private double MINIMUM_VOLTAGE = 6.1;
	
	/** The current battery voltage. */
	private float batteryVoltage;
	
    /**
     * Gets the voltage.
     *
     * @return the current battery voltage.
     */
    public float getVoltage() {
        float voltage = Battery.getVoltage();
        return voltage;
    }

    /**
     * Action.
     */
    @Override
    public void action() {
        Sound.beepSequenceUp();
        LCD.clear(); 
        LCD.drawString("LOW BATTERY! CHARGE NOW", 0, 3);
        
        Button.ENTER.waitForPressAndRelease();
    }
    
    /**
     * Take control.
     *
     * @return true, if current battery voltage is 6.1 or less.
     */
    @Override
    public boolean takeControl() {
    	batteryVoltage = getVoltage();
        if (batteryVoltage <= MINIMUM_VOLTAGE) return true;
        return false;
    }
    
    /**
     * Suppress.
     * 
     * Clears the "charge now" line (2).
     */
    @Override
    public void suppress() {
    	LCD.clear(2);
    } 
}