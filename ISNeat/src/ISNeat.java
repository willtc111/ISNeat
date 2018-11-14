import java.io.FileNotFoundException;

import evolution.Evolver;
import evolution.Genome;
import task.CarClassifyTask;
import task.JumperTask;

/**
 * Main class for running the NEAT algorithm
 * 
 * @author William Carver
 */
public class ISNeat {

	public static void main(String[] args) {
		JumperTask jt = new JumperTask( 1000 );

		Evolver evolver = new Evolver(jt, 150);
		Genome best = evolver.evolve();
		System.out.println(best);
		
	}

}
