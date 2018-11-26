package task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import neuralnetwork.NeuralNetwork;

public class JumperTask implements Task {

	private static final double GRAVITY = 1.0;
	private static final double INITIAL_JUMP_VELOCITY = 10.0;
	private static final double MAX_WIDTH = 150.0;
	private static final double MAX_SPEED = 20.0;
	private static final double DELTA_SPEED = 0.001;
	private static final double VIEW_DISTANCE = 1000.0;
	
	private final Random rand;
	private final double goal;
	
	public JumperTask( long distanceGoal ) {
		rand = new Random();
		goal = distanceGoal;
	}
	
	public JumperTask( long seed, long distanceGoal ) {
		rand = new Random( seed );
		goal = distanceGoal;
	}
	
	
	@Override
	public List<String> getInputs() {
		return Arrays.asList("bias", "speed", "obstacleType", "obstacleDistance", "obstacleWidth");
	}

	@Override
	public List<String> getOutputs() {
		return Arrays.asList("jump");
	}

	public double calculateFitness( NeuralNetwork neuralNetwork ) {
		long traveled = 0;
		double speed = 10.0;
		int timeToNextObstacle = 60;
		double vertVelocity = 0.0;
		double vertPos = 0.0;
		boolean onGround = true;
		LinkedList<JumperObstacle> obstacles = new LinkedList<JumperObstacle>();
		
		HashMap<String,Double> inputs = new HashMap<String,Double>();
		inputs.put("bias", 1.0);
		while( traveled <= goal ) {
			// update the current speed
			speed = Math.min(speed + DELTA_SPEED, MAX_SPEED);
						
			// get output of network for current inputs
			inputs.put("speed", (speed / MAX_SPEED));
			if( obstacles.isEmpty() ) {
				inputs.put( "obstacleType", 0.0 );
				inputs.put( "obstacleDistance", 1.0 );
				inputs.put( "obstacleWidth", 0.0 );
			} else {
				inputs.put( "obstacleType", obstacles.getFirst().getType() );
				inputs.put( "obstacleDistance", obstacles.getFirst().distance / VIEW_DISTANCE );
				inputs.put( "obstacleWidth", obstacles.getFirst().width / MAX_WIDTH );
			}
			neuralNetwork.setInputs(inputs);
			neuralNetwork.updateOnce();
			double jumpAmount = neuralNetwork.getOutputs().get("jump");
			
			//// update game ////

			// check if the player wants to jump (and do so if they can)
			if( jumpAmount > 0.5 && onGround ) {
				vertVelocity = jumpAmount * INITIAL_JUMP_VELOCITY;
				onGround = false;
			}
			
			
			// make physical changes
			vertVelocity -= GRAVITY;
			vertPos += vertVelocity;
			traveled++;
			
			// deal with the ground
			if( vertPos <= 0.0 ) {
				vertPos = 0.0;
				vertVelocity = 0.0;
				onGround = true;
			}
			
			// move the obstacles
			ListIterator<JumperObstacle> o = obstacles.listIterator();
			while( o.hasNext() ) {
				JumperObstacle cur = o.next();
				cur.distance = cur.distance - speed;
				if( cur.isPassed() ) {
					o.remove();
				}
			}
			
			//// check for failure ////
			if( !obstacles.isEmpty() && obstacles.getFirst().isWithinRange() ) {
				if( obstacles.getFirst().isInAir ) {
					// is the player jumping into an aerial obstacle?
					if( !onGround ) {
						break;
					}
				} else {
					if( onGround ) {
						break;
					}
				}
			}
			
			timeToNextObstacle--;
			//// add new obstacles ////
			if( timeToNextObstacle <= 0 ) {
				// Check if the player is far enough to qualify for air obstacles
				boolean type = traveled > 750 ? rand.nextBoolean() : false;
				// make a new obstacle at the limit of the view
				JumperObstacle newObstacle = new JumperObstacle(
						type,
						rand.nextDouble()*MAX_WIDTH,
						VIEW_DISTANCE
				);
				obstacles.addLast( newObstacle );
				timeToNextObstacle = 60 + rand.nextInt(60);
			}
			
		}
		
		double fitness = traveled / goal;
		return Math.min( fitness, 1 );
	}
	
	@Override
	public double calculateTestFitness( NeuralNetwork neuralNetwork ) {
		return calculateFitness( neuralNetwork );
	}

	@Override
	public double calculateTrainFitness( NeuralNetwork neuralNetwork ) {
		return calculateFitness( neuralNetwork );
	}

}
