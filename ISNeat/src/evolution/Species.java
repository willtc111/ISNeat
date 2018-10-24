package evolution;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class Species {

	LinkedList<Genome> organisms;
	double sharedFitness;
	
	
	public Species( Genome firstMember ) {
		organisms = new LinkedList<Genome>(Arrays.asList(firstMember));
	}
	
	public Genome getRepresentative() {
		return organisms.peekFirst();
	}
	
	public double getSharedFitness() {
		return sharedFitness;
	}
	
	public static class BY_SHARED_FITNESS implements Comparator<Species> {
		@Override
		public int compare(Species a, Species b) {
			return Double.compare(a.getSharedFitness(), b.getSharedFitness());
		};
	}
}
