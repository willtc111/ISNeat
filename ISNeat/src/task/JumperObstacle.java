package task;

public class JumperObstacle {

	final boolean isInAir;
	final double width;
	double distance;
	
	public JumperObstacle( boolean isInAir, double width, double distance ) {
		this.isInAir = isInAir;
		this.width = width;
		this.distance = distance;
	}
	
	public double getType() {
		if( isInAir ) {
			return 1.0;
		} else {
			return 0.0;
		}
	}
	
	public void move(double speed) {
		distance -= speed;
	}
	
	public double rearDistance() {
		return distance + width;
	}
	
	public boolean isWithinRange() {
		return (distance <= 0) && ((distance + width) > 0);
	}
	
	public boolean isPassed() {
		if( rearDistance() <= 0 ) {
			return true;
		} else {
			return false;
		}
	}
	
}
