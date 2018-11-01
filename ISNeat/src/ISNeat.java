import java.io.FileNotFoundException;

import evolution.Evolver;
import evolution.Genome;
import task.CarClassifyTask;

/**
 * Main class for running the NEAT algorithm
 * 
 * @author William Carver
 */
public class ISNeat {

	public static void main(String[] args) {
		CarClassifyTask cct;
		try {
			cct = new CarClassifyTask("car.data");

			Evolver evolver = new Evolver(cct, 100);
			Genome best = evolver.evolve();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
