package evolution;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Population {
	
	private int nextSpeciesId = 0;
	private int generationNumber;
	
	List<Species> population;
	
	public Population() {
		generationNumber = 0;
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
		population.add(new Species(generationNumber, nextSpeciesId++, Arrays.asList(genome)));
	}
	
	public int size() {
		int size = 0;
		for( Species species : population ) {
			size += species.size();
		}
		return size;
	}
	
	public List<Genome> getTotalPopulation() {
		List<Genome> totalPop = new LinkedList<Genome>();
		for( Species species : population ) {
			species.shareFitnesses();
			totalPop.addAll( species.getOrganisms() );
		}
		return totalPop;
	}
	
	public List<Species> getEmptySpecies() {
		List<Species> emptySpecies = new LinkedList<Species>();
		for( Species species : population ) {
			emptySpecies.add( species.getNextGenSpecies() );
		}
		return emptySpecies;
	}
	
	public List<Species> speciate( List<Species> oldGenSpecies, List<Genome> organisms ) {
		List<Species> nextGenSpecies = new LinkedList<Species>();
		for( Genome genome : organisms ) {
			// figure out which species it can fit in
			boolean hasSpecies = false;
			
			// try one of the old empty species
			ListIterator<Species> oldSpecies = oldGenSpecies.listIterator();
			while( oldSpecies.hasNext() ) {
				Species currentSpecies = oldSpecies.next();
				if( currentSpecies.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
					currentSpecies.add(genome);
					// remove this species from the old list since it is no longer empty
					nextGenSpecies.add(currentSpecies);
					oldSpecies.remove();
					
					hasSpecies = true;
					break;
				}
			}
			// try one of the non-empty species
			if( !hasSpecies ) {
				for( Species newSpecies : nextGenSpecies ) {
					if( newSpecies.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
						newSpecies.add(genome);
						hasSpecies = true;
						break;
					}
				}
			}
			// make new species
			if( !hasSpecies ) {
				nextGenSpecies.add(new Species(generationNumber, nextSpeciesId++, Arrays.asList(genome)));
			}
		}
		
		// empty species get left behind
		return nextGenSpecies;
	}
	
}
