package evolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	private static final int IMMUNITY_TIME = 15;
	private static final int HISTORY_LENGTH = 5;
	
	private int id;
	private int startGen;
	private double[] lastBestFitnesses;
	
	private List<Genome> organisms;
	private Genome representative = null;
	
	
	public Species( int startGen, int id, List<Genome> firstMembers ) {
		this.id = id;
		this.startGen = startGen;
		lastBestFitnesses = new double[HISTORY_LENGTH];
		organisms = firstMembers;
	}
	
	private Species( Species old ) {
		startGen = old.getStartGen();
		this.id = old.getId();
		organisms = new LinkedList<Genome>();
		this.lastBestFitnesses = old.getLastBestFitnesses();
		this.representative = old.getRepresentative();
	}

	/**
	 * Clone the species, keeping no members, but
	 * taking a representative from this generation.
	 * @return an empty species
	 */
	public Species getNextGenSpecies() {
		updateRepresentative();
		
		// update the lastBestFitness
		for( int i = 0; i < lastBestFitnesses.length - 1; i++ ) {
			lastBestFitnesses[i+1] = lastBestFitnesses[i];
		}
		lastBestFitnesses[0] = getBestGenome().getIndividualFitness();
		
		return new Species(this);
	}
	
	public Genome getBestGenome() {
		Genome best = organisms.get(0);
		for( Genome genome : organisms ) {
			if( genome.getIndividualFitness() < best.getIndividualFitness() ) {
				best = genome;
			}
		}
		return best; 
	}
	
	/**
	 * Checks if this species is available for termination due to stagnation.
	 * Species are immune for a set number of generations.
	 * 
	 * @param generationNumber The current generation number
	 * @return True if species can be terminated, otherwise False
	 */
	public boolean canBeTerminated(int generationNumber) {
		return (generationNumber - startGen) > IMMUNITY_TIME && !isStagnating();
	}
	
	/**
	 * Checks to make sure there is an improvement in best fitness over
	 * the course of the most recent several generations.
	 * 
	 * @return True if net improvement is not greater than 0, otherwise false
	 */
	public boolean isStagnating() {
		double improvement = 0;
		for( int i = 0; i < lastBestFitnesses.length - 1; i++ ) {
			improvement += lastBestFitnesses[i] - lastBestFitnesses[i+1];
		}
		return improvement > 0;
	}
	
	public Genome getRepresentative() {
		if( representative == null ) {
			updateRepresentative();
		}
		return representative;
	}
	
	public void updateRepresentative() {
		representative = organisms.get(new Random().nextInt(organisms.size()));
	}
	
	public void shareFitnesses() {
		for( Genome genome : organisms ) {
			genome.setIndividualFitness(this.size());
		}
	}
	
	public List<Genome> getOrganisms() {
		Collections.sort(organisms, Genome.BY_INDIVIDUAL_FITNESS());
		return organisms;
	}
	
	public void add(Genome genome) {
		organisms.add(genome);
	}
	
	public int size() {
		return organisms.size();
	}
	
	public double[] getLastBestFitnesses() {
		return lastBestFitnesses;
	}
	
	public int getStartGen() {
		return startGen;
	}
	
	public int getId() {
		return id;
	}
	
	public static Comparator<Species> BY_BEST_INDIVIDUAL_FITNESS() {
		return new Comparator<Species>() {
			@Override
			public int compare(Species a, Species b) {
				return Genome.BY_INDIVIDUAL_FITNESS().compare(a.getBestGenome(), b.getBestGenome());
			}
		};
	}
	
	public static Comparator<Species> BY_BEST_SHARED_FITNESS() {
		return new Comparator<Species>() {
			@Override
			public int compare(Species a, Species b) {
				return Genome.BY_SHARED_FITNESS().compare(a.getBestGenome(), b.getBestGenome());
			}
		};
	}
}
