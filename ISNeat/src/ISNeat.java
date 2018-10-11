import evolution.Evolver;
import task.CarClassifyTask;

/**
 * Main class for running the NEAT algorithm
 * 
 * @author William Carver
 */
public class ISNeat {

	public static void main(String[] args) {
		CarClassifyTask cct = new CarClassifyTask();
		Evolver evolver = new Evolver(cct);
	}

}
