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
			
			//// Update The Game ////

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
			boolean haveCollided = false;
			while( o.hasNext() ) {
				JumperObstacle cur = o.next();
				haveCollided = haveCollided || cur.move(speed, onGround);
				if( cur.isPassed() ) {
					o.remove();
				}
			}
			if( haveCollided ) {
				break;
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
			
//			textDisplay(obstacles, vertPos, traveled); //////  TODO: DEBUG
			
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
	
	private void textDisplay(List<JumperObstacle> obstacles, double height, double traveled ) {
		int offset = (int) Math.ceil(MAX_WIDTH)+2;
		int viewSize = (int) (Math.ceil(VIEW_DISTANCE) + (2 * offset));
		
		String[] text = new String[viewSize]; 
		
		for( JumperObstacle jo : obstacles ) {
			for( int i = (int) Math.floor(jo.distance); i < Math.floor(jo.rearDistance()); i++ ) {
				if( jo.isInAir ) {
					text[offset + i] = "o";
				} else {
					text[offset + i] = "u";
				}
			}
		}
		text[offset] = String.format("%1.0f", height);
		text[viewSize - offset] = "|";
		
		String disp = String.format("%5.0f\t", traveled);
		for( int i = 0; i < viewSize; i++ ) {
			if( text[i] == null ) {
				disp = disp.concat(".");
			} else {
				disp = disp.concat(text[i]);
			}
		}
		System.out.println(disp);
	}

}
