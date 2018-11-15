package evolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Species {

	private static final int IMMUNITY_TIME = 15;
	private static final int HISTORY_LENGTH = 5;
	
	public int intendedSize;
	
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
	
	public void updateFitnessHistory() {
		for( int i = 0; i < lastBestFitnesses.length - 1; i++ ) {
			lastBestFitnesses[i+1] = lastBestFitnesses[i];
		}
		lastBestFitnesses[0] = getBestGenome().getIndividualFitness();
	}
	
	public void cullTheWeak( int numSurvivors ) {
		organisms.sort(Genome.BY_SHARED_FITNESS());
		numSurvivors = Math.min(numSurvivors, organisms.size());
		organisms = organisms.subList(0, numSurvivors);
	}
	
	public void clear() {
		organisms = new LinkedList<Genome>();
	}
	
	public Genome getBestGenome() {
		Genome best = organisms.get(0);
		for( Genome genome : organisms ) {
			if( genome.getIndividualFitness() > best.getIndividualFitness() ) {
				best = genome;
			}
		}
		return best; 
	}
	
	public List<Genome> getNonBestGenomes() {
		List<Genome> lameGenomes = new LinkedList<Genome>(organisms);
		lameGenomes.remove(getBestGenome());
		return lameGenomes;
		
	}
	
	/**
	 * Checks if this species is available for termination due to stagnation,
	 * too low of fitness, and losing all members.
	 * Species are immune for a set number of generations (see IMMUNITY_TIME)
	 * 
	 * @param generationNumber The current generation number
	 * @param minFitness The minimum fitness value for the species to survive
	 * @return True if species can be terminated, otherwise False
	 */
	public boolean canBeTerminated(int generationNumber, double minFitness) {
		return (organisms.size() <= 0) || 
			   ((generationNumber - startGen) > IMMUNITY_TIME && isStagnating() && getLastBestFitness() < minFitness);
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
		return improvement <= 0;
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
			genome.setSharedFitness(this.size());
		}
	}
	
	public double sumOfFitnesses() {
		double sum = 0;
		for( Genome genome : organisms ) {
			sum += genome.getIndividualFitness();
		}
		return sum;
	}
	
	public double sumOfSharedFitnesses() {
		double sum = 0;
		for( Genome genome : organisms ) {
			sum += genome.getSharedFitness();
		}
		return sum;
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
	
	public double getLastBestFitness() {
		return lastBestFitnesses[0];
	}
	
	public int getStartGen() {
		return startGen;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		Collections.sort(organisms, Genome.BY_INDIVIDUAL_FITNESS());
		String output = String.format("<%d:  [%3.3f]  ", getId(), getLastBestFitness());
		for( Genome g : organisms ) {
			output = output + g.toString();
		}
		output = output + " >";
		return output;
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
