package evolution;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	private List<Genome> organisms;
	private Genome representative = null;
	
	
	public Species( List<Genome> firstMembers ) {
		organisms = firstMembers;
	}
	
	private Species( Genome representative ) {
		organisms = new LinkedList<Genome>();
		this.representative = representative;
	}

	/**
	 * Get a new species with no members, but with
	 * a representative taken from this generation.
	 * @return an empty species
	 */
	public Species getNextGenSpecies() {
		return new Species(getRepresentative());
	}
	
	public Genome getRepresentative() {
		if( representative == null ) {
			updateRepresentative();
		}
		return representative;
	}
	
	public void updateRepresentative() {
		representative = organisms.get(new Random().nextInt(organisms.size()));;
	}
	
	public List<Genome> getOrganisms() {
		return organisms;
	}
	
	public void add(Genome genome) {
		organisms.add(genome);
	}
	
	public int size() {
		return organisms.size();
	}
}
