package balancebot.me;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

/**
 * Links all the behaviours and BalanceBot together. 
 * It runs all the other behaviour classes in an Arbitrator.
 */
public class Driver {
	
	/** The gyro sensor. */
	private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		BalanceBot bb = new BalanceBot(gyroSensor);
		bb.setName("Balance");
		
		Behavior forward = new Forward(bb);
		Behavior detectWall = new DetectWall(bb);
		Behavior fallen = new Fallen(bb);
		Behavior lowBattery = new LowBattery();
		
		Arbitrator ab = new Arbitrator(new Behavior[] {forward, detectWall, fallen, lowBattery});
		ab.go();
		
		gyroSensor.close();
		bb.closeMotors();
	}
}