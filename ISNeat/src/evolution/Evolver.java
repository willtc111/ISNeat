package evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import task.Task;

/**
 * Primary class to perform the genetic algorithm
 */
public class Evolver {
	// These will need to be experimentally determined, may even vary based on problem
	public static final double COMPATABILITY_THRESHOLD = 2.0;
	public static final double C1 = 1.0;	// constant multiplier for excess gene differences
	public static final double C2 = 1.0;	// constant multiplier for disjoint gene differences
	public static final double C3 = 1.0;	// constant multiplier for gene weight differences
	
	private int nextInnovNum;
	private int nextNodeNum;
	
	private int nextSpeciesId = 0;
	private int generationNumber;
	
	List<Species> population;
	
	// The mapping from task inpout/output names to node id's.
	private Map<String, Integer> inputNodeMap;
	private Map<String, Integer> outputNodeMap;
	
	private Task task;
	
	public Evolver(Task task, int populationSize) {
		generationNumber = 0;
		
		this.task = task;
		
		String[] inputArray = task.getInputs().toArray( new String[0] );
		String[] outputArray = task.getOutputs().toArray( new String[0] );
		
		List<NodeGene> initialNodes = new LinkedList<NodeGene>();
		
		// Make the initial input nodes and the input map
		inputNodeMap = new HashMap<String, Integer>();
		for( int i = 0; i < inputArray.length; i++ ) {
			inputNodeMap.put( inputArray[i], i );
			initialNodes.add( new NodeGene(i, NodeType.INPUT) );
		}
		
		// Make the initial output nodes and the output map
		outputNodeMap = new HashMap<String, Integer>();
		for( int i = 0; i < outputArray.length; i++ ) {
			int outputId = i + inputArray.length;
			outputNodeMap.put( outputArray[i], outputId );
			initialNodes.add( new NodeGene(outputId, NodeType.OUTPUT) );
		}
		
		nextNodeNum = outputArray.length + inputArray.length;
		nextInnovNum = 0;
		
		// Initialize the population.
		population = new LinkedList<Species>();
		for( int i = 0; i < populationSize; i++ ) {
			// start with no connections, bare minimum!
			addToPopulation(new Genome(initialNodes, null) );
		}
	}
	
	private void addToPopulation(Genome genome) {
		for( Species species : population ) {
			if( species.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
				species.add(genome);
				return;
			}
		}
		population.add(new Species(generationNumber, nextSpeciesId++, Arrays.asList(genome)));
	}
	
	private int populationSize() {
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
