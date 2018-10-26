package evolution;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	private int id;
	private int startGen;
	
	private List<Genome> organisms;
	private Genome representative = null;
	
	
	public Species( int startGen, int id, List<Genome> firstMembers ) {
		this.id = id;
		organisms = firstMembers;
	}
	
	private Species( Species old ) {
		this.id = old.getId();
		organisms = new LinkedList<Genome>();
		this.representative = old.getRepresentative();
	}

	/**
	 * Clone the species, keeping no members, but
	 * taking a representative from this generation.
	 * @return an empty species
	 */
	public Species getNextGenSpecies() {
		updateRepresentative();
		return new Species(this);
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
	
	public int getStartGen() {
		return startGen;
	}
	
	public int getId() {
		return id;
	}
}
