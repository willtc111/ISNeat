import evolution.Evolver;
import evolution.Genome;
import task.JumperTask;

/**
 * Main class for running the NEAT algorithm
 * 
 * @author William Carver
 */
public class ISNeat {

	public static void main(String[] args) {
		JumperTask jt = new JumperTask( 123456789, 20000 );

		Evolver evolver = new Evolver(jt, 150);
		Genome best = evolver.evolve();
		System.out.println(best);
		
		// Winners for length 5000
		// ??? gens: #{0.000  {0:(5,5) 0.89} {1:(0,5) -0.46} {2:(1,5) 0.29} {3:(3,5) -0.60} {4:(2,5) -0.98} }#
		// ??? gens: #{0.000  {0:(5,5) -0.09} {1:(1,5) 0.84} {2:(3,5) -0.90} {3:(0,5) -0.04} {4:(2,5) -0.74} }#
		
		// Winners for length 10000
		// 178 gens: #{0.000  {0:(0,5) -0.34} {1:(3,5) -0.77} {2:(1,5) 0.53} {4:(2,5) -0.95} {7:(5,5) 0.61} }#
		
		// Winners for length 20000
		// 
	}

}
