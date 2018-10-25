package evolution;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Population {
	
	List<Species> population;
	
	public Population() {
		population = new LinkedList<Species>();
	}
	
	/**
	 * Add a genome to the population, putting it into a species if it fits in one, or making a new species
	 * @param genome
	 */
	public void add(Genome genome) {
		for( Species species : population ) {
			if( species.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
				species.add(genome);
				return;
			}
		}
		population.add(new Species(Arrays.asList(genome)));
	}
	
	
	
}
