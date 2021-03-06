package evolution;

import java.util.Arrays;
import java.util.Collections;
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
	public static final double C3 = 0.3;	// constant multiplier for gene weight differences
	
	public static final double INTERSPECIES_MATING_RATE = 0.001;
	public static final double ENABLE_GENE_CHANCE = 0.75;
	
	public static final double WEIGHT_MUTATION_CHANCE = 0.8;
	public static final double CONNECTION_MUTATION_CHANCE = 0.05;	// 0.3 if using large population (1000+)
	public static final double NODE_MUTATION_CHANCE = 0.03;
	public static final double MUTATION_SCALAR = 0.05;
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
		int gensSinceImprovement = 0;
		List<ConnectionGene> latestConnections = new LinkedList<ConnectionGene>();
		
		while( true ) {
			
			try {Thread.sleep(500);} catch (InterruptedException e) {}	// TODO: debug
			
			generationNumber++;
			System.out.println("Generation " + generationNumber + " starting!  " + population.size() + " species.");	// TODO: debug
//			System.out.println("\t" + task.getInputs() + " " + task.getOutputs());
			List<Species> nextGenPopulation = new LinkedList<Species>();
			
			// evaluate fitnesses
			boolean improved = false;
			double totalFitness = 0;
			for( Species s : population ) {
				for( Genome g : s.getOrganisms() ) {
					// build the organism's neural network
					NeuralNetwork network = NeuralNetworkBuilder.build(g, inputNodeMap, outputNodeMap);
					// test the network on the task
					double fitness = task.calculateTrainFitness(network);
					if( fitness > 0.85 ) {
						if( task.calculateTestFitness(network) > 0.95 ) {
							return g;
						}
					}
					// update the fitness value
					g.setIndividualFitness(fitness);
					
					if( best == null || g.getIndividualFitness() > best.getIndividualFitness() ) {
						best = new Genome(g);
						improved = true;
					}
					totalFitness += fitness; 
				}
				s.shareFitnesses();
				s.updateRepresentative();
				System.out.println(s);	// TODO: debug
			}

			if( improved ) {
				gensSinceImprovement = 0;
			} else {
				gensSinceImprovement++;
			}
			
			double averageFitness = totalFitness / populationSize();
			System.out.println("\tBest Fitness = " + best.getIndividualFitness() + "!");	// TODO: debug
			
			// kill off the weak species and get the total shared fitness
			totalFitness = 0;
			if( population.size() > 2 ) {
				if( gensSinceImprovement > 20 ) {
					// MASS EXTINCTION EVENT!!!!!  Keep only the two best species.
					System.out.println("MASS EXTINCTION EVENT!!!!!");
					gensSinceImprovement = 0;
					// Get top two species
					massExtinction();
					for( Species s : population ) {
						totalFitness += s.sumOfSharedFitnesses();
						nextGenPopulation.add(s);
					}
				} else {
					for( Species s : population ) {
						// disregarding a species here is the only place it is allowed to be exterminated for poor fitness.
						if( !s.canBeTerminated(generationNumber, averageFitness) ) {
							totalFitness += s.sumOfSharedFitnesses();
							nextGenPopulation.add(s);
						} else {
							System.out.println("KILLING SPECIES " + s.getId() + "!");
						}
					}
				}
			} else {
				for( Species s : population ) {
					totalFitness += s.sumOfSharedFitnesses();
					nextGenPopulation.add(s);
				}
			}
			
			// Kill off the weak genomes of each remaining species and perform crossover
			List<Genome> newbies = new LinkedList<Genome>();
			List<Genome> champs = new LinkedList<Genome>();
			ListIterator<Species> ngpop = nextGenPopulation.listIterator();
			while( ngpop.hasNext() ) {
				Species s = ngpop.next();
				// remove the weakest organisms
				double thisSpeciesFitSum = s.sumOfSharedFitnesses();
				s.intendedSize = (int) Math.round(populationSize * (thisSpeciesFitSum / totalFitness));
				if( s.intendedSize / 2 <= 2 ) {
					// not enough organisms to do crossover within the species, but kill off any excess either way.
					// Allow two to stay, since it is supposed to be allowed to persist.  
					s.cullTheWeak(2);
				} else {
					s.cullTheWeak(s.intendedSize / 2);
				}

				// Best in the species gets to carry on unaltered
				champs.add(new Genome(s.getBestGenome()));
				
				// Do crossover if there is more than one member of the species remaining
				if( s.size() >= 1 ) {
					List<Genome> parents = new LinkedList<Genome>(s.getOrganisms());
					parents.sort(Genome.BY_INDIVIDUAL_FITNESS());
					
					// mate every other organism with a random member of the species better than itself
					Genome parentA, parentB;
					for(int i = 0; i < s.intendedSize - 1; i++ ) {
						int index1 = rand.nextInt(parents.size());
						parentA = parents.get(index1);
						int index2 = rand.nextInt(index1+1); //+1 to include parentA (essentially cloning)
						parentB = parents.get(index2);
						if( rand.nextDouble() <= INTERSPECIES_MATING_RATE && nextGenPopulation.size() > 1 ) {
							// mate with a random member of a random other species instead
							List<Species> otherSpecies = new LinkedList<Species>(nextGenPopulation);
							otherSpecies.remove(s);
							// Pick a random non-empty species
							Collections.shuffle(otherSpecies, rand);
							for( Species randOtherSpecies : otherSpecies ) {
								if( randOtherSpecies.size() > 0 ) {
									List<Genome> otherOrganisms = randOtherSpecies.getOrganisms();
									parentB = otherOrganisms.get(rand.nextInt(otherOrganisms.size()));
									break;
								}
							}
						}
						newbies.add(crossover(parentA, parentB));
					}
				}
				s.clear();
			}
			System.out.println("\tCrossover completed!  " + (newbies.size() + champs.size()) + " individuals.");	// TODO: debug
			
			// do mutations on all the new children
			System.out.print("\t\t"); // TODO: debug
			
			for( Genome g : newbies ) {
				// mutate weights
				if( rand.nextDouble() < WEIGHT_MUTATION_CHANCE ) {
					g.mutateWeights( MUTATION_SCALAR, RANDOM_RESET_MUTATION_CHANCE );
				}
				// structural mutations
				if( rand.nextDouble() < CONNECTION_MUTATION_CHANCE ) {
					// mutate connections
					ConnectionGene change = g.mutateAddConnection(nextInnovNum, latestConnections);
					if( change != null ) {
						nextInnovNum++;
						latestConnections.add(change);
					}
					System.out.print("-");	// TODO: debug
				}
				if( rand.nextDouble() < NODE_MUTATION_CHANCE ) {
					// mutate nodes
					List<ConnectionGene> changes = g.mutateAddNode(nextInnovNum, nextNodeNum, latestConnections);
					if( changes != null ) {
						nextInnovNum += 2;
						latestConnections.addAll(changes);
						nextNodeNum++;
					}
					System.out.print("=");	// TODO: debug
				}
			}
			System.out.println("\n\tMutation completed!  " + (newbies.size() + champs.size()) + " individuals.");	// TODO: debug
			
			// add the newbies to the population
			newbies.addAll(champs);
			population = speciate(nextGenPopulation, newbies);
		}
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
	
	private void massExtinction() {
		Species number1 = null;
		Species number2 = null;
		for( Species s : population ) {
			if( number1 == null ) {
				number1 = s;
			} else if( number2 == null ) {
				if( Species.BY_BEST_INDIVIDUAL_FITNESS().compare(number1, s) > 0 ) {
					number2 = s;
				} else {
					number2 = number1;
					number1 = s;
				}
			} else {
				if( Species.BY_BEST_INDIVIDUAL_FITNESS().compare(s, number2) > 0 ) {
					number2 = s;
				} else if( Species.BY_BEST_INDIVIDUAL_FITNESS().compare(s, number1) > 0 ) {
					number2 = number1;
					number1 = s;
				}
			}
		}
		
		LinkedList<Species> newPopulation = new LinkedList<Species>();
		// Maintain the ordering by Id
		if( number1.getId() > number2.getId() ) {
			newPopulation.addFirst(number1);
			newPopulation.addLast(number2);
		} else {
			newPopulation.addFirst(number2);
			newPopulation.addLast(number1);
		}
		population = newPopulation;
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
		
		// remove any species that have no members
		ListIterator<Species> si = species.listIterator();
		while( si.hasNext() ) {
			Species s = si.next();
			if( s.size() <= 0 ) {
				si.remove();
			}
		}
		
		return species;
	}
}
