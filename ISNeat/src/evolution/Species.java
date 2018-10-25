package evolution;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	private int id;
	private List<Genome> organisms;
	private Genome representative = null;
	
	
	public Species( int id, List<Genome> firstMembers ) {
		this.id = id;
		organisms = firstMembers;
	}
	
	// don't want anyone except species setting the representative of the species
	private Species( int id, Genome representative ) {
		this.id = id;
		organisms = new LinkedList<Genome>();
		this.representative = representative;
	}

	/**
	 * Clone the species, keeping no members, but
	 * taking a representative from this generation.
	 * @return an empty species
	 */
	public Species getNextGenSpecies() {
		return new Species(id, getRepresentative());
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
	
	public void shareFitnesses() {
		for( Genome genome : organisms ) {
			genome.setIndividualFitness(this.size());
		}
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
