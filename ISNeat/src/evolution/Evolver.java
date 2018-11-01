package evolution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import neuralnetwork.NeuralNetwork;
import neuralnetwork.NeuralNetworkBuilder;
import task.Task;

/**
 * Primary class to perform the genetic algorithm
 */
public class Evolver {
	public static final double COMPATABILITY_THRESHOLD = 3.0;
	public static final double C1 = 1.0;	// constant multiplier for excess gene differences
	public static final double C2 = 1.0;	// constant multiplier for disjoint gene differences
	public static final double C3 = 0.4;	// constant multiplier for gene weight differences
	
	public static final double INTERSPECIES_MATING_RATE = 0.001;
	public static final double ENABLE_GENE_CHANCE = 0.75;
	
	public static final double CONNECTION_MUTATION_CHANCE = 0.05;	// 0.3 if using large population (1000+)
	public static final double NODE_MUTATION_CHANCE = 0.03;
	public static final double MUTATION_SCALAR = 0.25;
	public static final double RANDOM_RESET_MUTATION_CHANCE = 0.1;
	
	private int nextInnovNum;
	private int nextNodeNum;
	
	private final int populationSize;
	private int nextSpeciesId = 0;
	private int generationNumber;
	
	List<Species> population;
	
	// The mapping from task inpout/output names to node id's.
	private Map<String, Integer> inputNodeMap;
	private Map<String, Integer> outputNodeMap;
	private List<NodeGene> requiredNodes;
	private Task task;
	
	public Evolver(Task task, int populationSize) {
		this.populationSize = populationSize;
		
		generationNumber = 0;
		
		this.task = task;
		
		String[] inputArray = task.getInputs().toArray( new String[0] );
		String[] outputArray = task.getOutputs().toArray( new String[0] );
		
		requiredNodes = new LinkedList<NodeGene>();
		
		// Make the initial input nodes and the input map
		inputNodeMap = new HashMap<String, Integer>();
		for( int i = 0; i < inputArray.length; i++ ) {
			inputNodeMap.put( inputArray[i], i );
			requiredNodes.add( new NodeGene(i, NodeType.INPUT) );
		}
		
		// Make the initial output nodes and the output map
		outputNodeMap = new HashMap<String, Integer>();
		for( int i = 0; i < outputArray.length; i++ ) {
			int outputId = i + inputArray.length;
			outputNodeMap.put( outputArray[i], outputId );
			requiredNodes.add( new NodeGene(outputId, NodeType.OUTPUT) );
		}
		
		nextNodeNum = outputArray.length + inputArray.length;
		nextInnovNum = 0;
		
		// Initialize the population.
		population = new LinkedList<Species>();
		for( int i = 0; i < populationSize; i++ ) {
			// start with no connections, bare minimum!
			addToPopulation( new Genome(requiredNodes, null) );
		}
	}
	
	public Genome evolve() {
		Genome best = null;
		Random rand = new Random();
		double mutationChance = 0.8;
		boolean isDone = false;	
		while( !isDone ) {
			generationNumber++;
			System.out.println("Generation " + generationNumber + " starting!  Now using approximately " + (nextNodeNum-1) + " nodes.");	// TODO: debug
			List<Species> nextGenPopulation = new LinkedList<Species>();
			
			// evaluate fitnesses
			double totalFitness = 0;
			for( Species s : population ) {
				for( Genome g : s.getOrganisms() ) {
					// build the organism's neural network
					NeuralNetwork network = NeuralNetworkBuilder.build(g, inputNodeMap, outputNodeMap);
					// test the network on the task
					double fitness = task.calculateFitness(network);
					// update the fitness value
					g.setIndividualFitness(fitness);
				}
				s.shareFitnesses();
				s.updateFitnessHistory();
				
				if( !s.canBeTerminated(generationNumber) ) {
					totalFitness += s.sumOfFitnesses();
					nextGenPopulation.add(s);
				}
			}
			System.out.println("\tTotalFitness =  " + totalFitness + "!");	// TODO: debug
			
			// Kill off the weak and perform crossover
			List<Genome> newbies = new LinkedList<Genome>();
			ListIterator<Species> ngpop = nextGenPopulation.listIterator();
			while( ngpop.hasNext() ) {
				Species s = ngpop.next();
				// remove the weakest organisms
				double thisSpeciesFitSum = s.sumOfFitnesses();
				int newSpeciesSize = (int) Math.round(populationSize * (thisSpeciesFitSum / totalFitness));
				if( newSpeciesSize <= 0 ) {
					// Species has died.  Too bad, so sad...
					// just kidding, this might actually be too aggressive!
					// TODO: Might need to account for whether or not a species can be terminated (gotta protext the newbies).
					ngpop.remove();
					// remove this species from the total fitness count
					totalFitness -= thisSpeciesFitSum;
					continue;
				}
				s.cullTheWeak(newSpeciesSize / 2);
				
				// Do crossover if there is more than one member of the species
				if( s.size() >= 2 ) {
					List<Genome> parents = new LinkedList<Genome>(s.getOrganisms());
					parents.sort(Genome.BY_INDIVIDUAL_FITNESS());
					
					
					ListIterator<Genome> parIt = parents.listIterator();
					// mate the best organism with a random lucky member of the species other than itself
					Genome parentA = parIt.next();
					Genome parentB;
					if( rand.nextDouble() <= INTERSPECIES_MATING_RATE && nextGenPopulation.size() > 1 ) {
						// PSYCH, mate with a random member of a random other species instead
						List<Species> otherSpecies = new LinkedList<Species>(nextGenPopulation);
						otherSpecies.remove(s);
						List<Genome> otherOrganisms = otherSpecies.get(rand.nextInt(otherSpecies.size())).getOrganisms();
						parentB = otherOrganisms.get(rand.nextInt(otherOrganisms.size()));
						
					} else {
						parentB = parents.get( rand.nextInt(parents.size()-1)+1 );
					}
					newbies.add(crossover(parentA, parentB));
					
					// mate every other organism with a random member of the species better than itself
					while( parIt.hasNext() ) {
						parentA = parIt.next();
						if( rand.nextDouble() <= INTERSPECIES_MATING_RATE && nextGenPopulation.size() > 1 ) {
							// mate with a random member of a random other species instead
							List<Species> otherSpecies = new LinkedList<Species>(nextGenPopulation);
							otherSpecies.remove(s);
							List<Genome> otherOrganisms = otherSpecies.get(rand.nextInt(otherSpecies.size())).getOrganisms();
							parentB = otherOrganisms.get(rand.nextInt(otherOrganisms.size()));
							
						} else {
							int index = parIt.previousIndex()+1;
							parentB = parents.get(rand.nextInt(index));
						}
						newbies.add(crossover(parentA, parentB));
					}
				}
			}
			System.out.println("\tCrossover completed!");	// TODO: debug
			
			// do mutations
			List<ConnectionGene> latestConnections = new LinkedList<ConnectionGene>();
			List<Integer> latestNodeIds = new LinkedList<Integer>();
			for( Species s : nextGenPopulation ) {
				// mutate everyone except the champion from each species from the past generation
				List<Genome> genesToMutate = s.getNonBestGenomes();
				for( Genome g : genesToMutate ) {
					// mutate weights
					g.mutateWeights(mutationChance, MUTATION_SCALAR, RANDOM_RESET_MUTATION_CHANCE );
					// structural mutations
					if( rand.nextDouble() < CONNECTION_MUTATION_CHANCE ) {
						// mutate connections
						ConnectionGene change = g.mutateAddConnection(nextInnovNum, latestConnections);
						if( change != null ) {
							nextInnovNum++;
							latestConnections.add(change);
						}
					} else if( rand.nextDouble() < NODE_MUTATION_CHANCE ) {
						// mutate nodes
						List<ConnectionGene> changes = g.mutateAddNode(nextInnovNum, nextNodeNum, latestNodeIds, latestConnections);
						if( changes != null ) {
							nextInnovNum += 2;
							latestConnections.addAll(changes);
							latestNodeIds.add(nextNodeNum);
							nextNodeNum++;
						}
					}
				}
			}
			System.out.println("\tMutation completed!");	// TODO: debug
			
			// add the newbies to the species
			population = speciate(nextGenPopulation, newbies);
		}
		return best;
	}
	
	private Genome crossover( Genome parentA, Genome parentB ) {
		// Make sure parentA is the more fit of the two organisms
		boolean equalFits = false;
		if( parentA.getSharedFitness() < parentB.getSharedFitness() ) {
			// parent B is better, switch them
			Genome temp = parentA;
			parentA = parentB;
			parentB = temp;
		} else if( parentA.getSharedFitness() == parentB.getSharedFitness() ) {
			equalFits = true;
		}
		
		// get each parent's genes
		Map<Integer, ConnectionGene> aGenes = parentA.getConnectionMap();
		Map<Integer, ConnectionGene> bGenes = parentB.getConnectionMap();
		// get the innovation number of every possible gene in either parent
		Set<Integer> allInnovationNumbers = new HashSet<Integer>(aGenes.keySet());
		allInnovationNumbers.addAll(bGenes.keySet());
		
		// get all of the nodes in either parent
		Map<Integer, NodeGene> allNodes = parentA.getNodeMap();
		allNodes.putAll(parentB.getNodeMap());
		
		// the set for all of the child's nodes
		Set<NodeGene> childNodes = new HashSet<NodeGene>();
		childNodes.addAll(requiredNodes);
		List<ConnectionGene> childGenes = new LinkedList<ConnectionGene>();
		for( int i : allInnovationNumbers ) {
			ConnectionGene geneToPass = null;
			if( aGenes.containsKey(i) && bGenes.containsKey(i) ) {
				// both parents have this gene, add randomly from either parent
				if( Math.random() < 0.5 ) {
					geneToPass = aGenes.get(i);
				} else {
					geneToPass = bGenes.get(i);
				}
			} else {
				// only one parent has this connection (disjoint/excess)
				if( aGenes.containsKey(i) ) {
					// add this gene to child from parent A since A is more fit
					geneToPass = aGenes.get(i);
				} else if( equalFits ) {
					// add this gene to child from parent B, since fitnesses are equal
					geneToPass = bGenes.get(i);
				} else {
					// parent B has the gene, but parent B sucks, so don't add it.
				}
			}
			// Add the new gene & corresponding nodes
			if( geneToPass != null ) {
				childNodes.add(allNodes.get(geneToPass.getIn()));
				childNodes.add(allNodes.get(geneToPass.getOut()));
				// chance to enable any disabled genes
				if( !geneToPass.isEnabled() && (Math.random() < ENABLE_GENE_CHANCE) ) {
					geneToPass.enable();
				}
				childGenes.add(geneToPass);
			}
		}
		
		return new Genome(new LinkedList<NodeGene>(childNodes), childGenes);
		
	}
	
	/**
	 * For measuring how close the algorithm is to finding a solution as a whole
	 * @return The best genome out of the entire populaiton
	 */
	private Genome getBest() {
		Genome best = population.get(0).getBestGenome();
		for( Species s : population ) {
			Genome sBest = s.getBestGenome();
			if( sBest.getIndividualFitness() < best.getIndividualFitness() ) {
				best = sBest;
			}
		}
		return best;
	}
	
	private void addToPopulation(Genome genome) {
		for( Species s : population ) {
			if( s.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
				s.add(genome);
				return;
			}
		}
		population.add(new Species(generationNumber, nextSpeciesId++, new LinkedList<Genome>(Arrays.asList(genome))));
	}
	
	private int populationSize() {
		int size = 0;
		for( Species species : population ) {
			size += species.size();
		}
		return size;
	}
	
	private List<Species> speciate( List<Species> species, List<Genome> organisms ) {
		for( Genome genome : organisms ) {
			boolean hasSpecies = false;
			// try fitting it into one of the existing species
			for( Species s : species ) {
				if( s.getRepresentative().calculateDistance(genome, Evolver.C1, Evolver.C2, Evolver.C3) < Evolver.COMPATABILITY_THRESHOLD ) {
					s.add(genome);
					hasSpecies = true;
					break;
				}
			}
			// make new species
			if( !hasSpecies ) {
				species.add(new Species(generationNumber, nextSpeciesId++, new LinkedList<Genome>(Arrays.asList(genome))));
			}
		}
		return species;
	}
}
