package evolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Genome {
	
	private List<NodeGene> nodes;
	private List<ConnectionGene> connections;
	
	private double fitness = 0;
	private double sharedFitness = 0;
	
	/**
	 * Cloning constructor
	 * @param genome Genome to clone
	 */
	public Genome( Genome genome ) {
		this(genome.getNodes(), genome.getConnections());
	}
	
	/**
	 * Constructor
	 * @param nodes Nodes to clone
	 * @param connections Connections to clone
	 */
	public Genome( List<NodeGene> nodes, List<ConnectionGene> connections ) {
		// Deep copy of the nodes
		this.nodes = new ArrayList<NodeGene>(nodes.size());
		for( NodeGene n : nodes ) {
			this.nodes.add(n.clone());
		}
		
		if( connections != null ) {
			// Deep copy of the connections
			this.connections = new ArrayList<ConnectionGene>(connections.size());
			for( ConnectionGene c : connections ) {
				this.connections.add(c.clone());
			}
		} else {
			this.connections = new ArrayList<ConnectionGene>();
		}
	}

	/**
	 * Mutate this genome's connection weights.
	 * 
	 * @param mutationChance The chance of a mutation occurring for any given weight
	 * @param mutationScalar Scalar for the magnitude of any weight adjustment 
	 * @param randomResetChance The chance to simply assign a new random weight
	 */
	public void mutateWeights( double mutationChance, double mutationScalar, double randomResetChance ) {
		Random rand = new Random();
		for( ConnectionGene c : connections ) {
			if( rand.nextDouble() < mutationChance ) {
				if( rand.nextDouble() < randomResetChance ) {
					// mutate by setting weight randomly
					c.setWeight(randomWeight());
				} else {
					// mutate this connection by a normally distributed amount (mean = 0)
					double alteration = mutationScalar * rand.nextGaussian();
					// but be sure to keep it within the bounds
					c.setWeight( c.getWeight() + alteration );
				}
			}
		}
	}
	
	/**
	 * Perform a mutation to add a connection.
	 * 
	 * @param innovationNumber The id of the next connection gene
	 * @param latestConnections The list of new connections from this generation's mutations
	 * @return The connection added by this mutation, or null if it already existed
	 */
	public ConnectionGene mutateAddConnection(int innovationNumber, final List<ConnectionGene> latestConnections ) {
		Random rand = new Random();
		List<Integer> toOptions = new LinkedList<Integer>();
		List<Integer> fromOptions = new LinkedList<Integer>();
		for( NodeGene n : nodes ) {
			switch(n.getType()) {
			case INPUT:
				fromOptions.add(n.getId());
				break;
			case OUTPUT:
			case HIDDEN:
				toOptions.add(n.getId());
				fromOptions.add(n.getId());
				break;
			}
		}
		int to = toOptions.get(rand.nextInt(toOptions.size()));
		int from = fromOptions.get(rand.nextInt(fromOptions.size()));
		ConnectionGene newConnection = new ConnectionGene(innovationNumber, randomWeight(), from, to);
		
		// check to make sure this connection doesn't already exist in the network.
		for( ConnectionGene c : connections ) {
			if( c.getIn() == from && c.getOut() == to ) {
				// connection already exists in this very genome, don't make a new one.
				return null;
			}
		}
		
		for( ConnectionGene c : latestConnections ) {
			if( c.getIn() == from && c.getOut() == to ) {
				// forget about that newly created connection, use the existing one with a random weight
				newConnection = c.clone();
				newConnection.setWeight(randomWeight());
				connections.add(newConnection);
				
				// return null to signify a new connection was not made
				return null;
			}
		}
		
		connections.add(newConnection);
		return newConnection;
	}
	
	/**
	 * Perform a mutation to add a new node by selecting a current connection
	 * and adding a node in the middle of it, with two new connections going
	 * from the old starting point to the new node, and from the new node to
	 * the old ending point.
	 * 
	 * @param innovationNumber The next connection's innovation number
	 * @param nextNodeNumber The next node ID
	 * @param latestConnections The list of new connections from this generation's mutations 
	 * @return All of the connections added to the genome during this operation
	 */
	public List<ConnectionGene> mutateAddNode(int innovationNumber, int nextNodeNumber, List<ConnectionGene> latestConnections) {
		if( connections.size() <= 0 ) {
			return null;
		} else {
			// choose a connection to split
			ConnectionGene disableMe = connections.get(new Random().nextInt(connections.size()));
			disableMe.disable();
			
			// make the new stuff
			int from = disableMe.getIn();
			int to = disableMe.getOut();
			NodeGene newNode = new NodeGene(nextNodeNumber, NodeType.HIDDEN);
			ConnectionGene firstConnection  = new ConnectionGene(innovationNumber,
																 1.0,
																 from,
																 newNode.getId());
			ConnectionGene secondConnection = new ConnectionGene(innovationNumber+1,
																 disableMe.getWeight(),
																 newNode.getId(),
																 to);
			
			// Check to make sure this hasn't been done before for innovation tracking purposes
			for( ConnectionGene c1 : latestConnections ) {
				if( c1.getIn() == from ) {
					for( ConnectionGene c2 : latestConnections ) {
						if( c1.getOut() == c2.getIn() && c2.getOut() == to ) {
							// This same node was added previously, duplicate everything instead.
							newNode = new NodeGene(c1.getOut(), NodeType.HIDDEN);
							nodes.add(newNode);
							
							firstConnection = c1.clone();
							firstConnection.setWeight(1.0);
							connections.add(firstConnection);
							
							secondConnection = c2.clone();
							secondConnection.setWeight(Math.random());
							connections.add(secondConnection);
							
							// Return null to signify that new connections and a new node were not made
							return null;
						}
					}
				}
			}
			
			// add the new stuff and return the new connections
			nodes.add(newNode);
			connections.add(firstConnection);
			connections.add(secondConnection);
			return Arrays.asList(firstConnection, secondConnection);
		}
	}
	
	public double calculateDistance( Genome other, double c1, double c2, double c3 ) {
		List<ConnectionGene> otherGenes = other.getConnections();
		
		// Calculate the normalization factor (size of larger of two genomes)
		double N = Math.max(connections.size(), otherGenes.size());
		// If significantly small, don't normalize -> N=1
		if( N < 20 ) N = 1;

		if( connections.size() == 0 ) {
			if( otherGenes.size() == 0 ) {
				// no connections to compare from either genome...
				return 0;
			} else {
				// no connections in this genome, just calculate number of excess in the other genome (normalized)
				return c1 * otherGenes.size() / N; 
			}
		} else if( otherGenes.size() == 0 ) {
			// no connections in other genome, just calculate number of excess in this genome (normalized)
			return c1 * connections.size() / N;
		} else {
			// both genomes contain connection genes, so do a gene-for-gene comparison...
			double numExcess = 0;
			double numDisjoint = 0;
			double weightDiff = 0;
			
			// remember the max innovation number for each genome (to determine excess vs disjoint)
			int aMax = 0;
			int bMax = 0;
			
			// make a map of each genome's gene weights for quick comparison
			HashMap<Integer,Double> aGeneWeights = new HashMap<Integer,Double>();
			for( ConnectionGene g : connections ) {
				aMax = Math.max( aMax, g.getInnov() );
				aGeneWeights.put( g.getInnov(), g.getWeight() );
			}
			HashMap<Integer,Double> bGeneWeights = new HashMap<Integer,Double>();
			for( ConnectionGene g : otherGenes ) {
				bMax = Math.max( bMax, g.getInnov() );
				bGeneWeights.put( g.getInnov(), g.getWeight() );
			}
			
			// go through this genome's genes and compare with the other genome's genes
			for( ConnectionGene g : connections ) {
				if( bGeneWeights.get( g.getInnov() ) == null ) {
					if( g.getInnov() > bMax ) {
						// this is an excess gene
						numExcess++;
					} else {
						// this is a disjoint gene
						numDisjoint++;
					}
				} else {
					// add the weight difference.
					weightDiff += Math.abs( g.getWeight() - bGeneWeights.get( g.getInnov() ) );
				}
			}
			
			// go through the other genome's genes and compare with this genome's genes
			for( ConnectionGene g : otherGenes ) {
				if( aGeneWeights.get(g.getInnov()) == null ) {
					if( g.getInnov() > aMax ) {
						// this is an excess gene
						numExcess++;
					} else {
						// this is a disjoint gene
						numDisjoint++;
					}
					// don't add weight differences, since they are all aready counted from the loop above
				}
			}
			
			// return sum of normalized connection differences
			return (c1 * numExcess / N) + (c2 * numDisjoint / N) + (c3 * weightDiff);
		}
	}
	
	/**
	 * Getter for node list
	 * @return	The node list
	 */
	public List<NodeGene> getNodes() {
		return nodes;
	}
	
	public Map<Integer, NodeGene> getNodeMap() {
		HashMap<Integer, NodeGene> nods = new HashMap<Integer, NodeGene>();
		for( NodeGene n : nodes ) {
			nods.put(n.getId(), n);
		}
		return nods;
	}
	
	/**
	 * Getter for the connection list
	 * @return	The connection list
	 */
	public List<ConnectionGene> getConnections() {
		return connections;
	}
	
	public Map<Integer, ConnectionGene> getConnectionMap() {
		HashMap<Integer, ConnectionGene> cons = new HashMap<Integer, ConnectionGene>();
		for( ConnectionGene c : connections ) {
			cons.put(c.getInnov(), c);
		}
		return cons;
	}
	
	public double getIndividualFitness() {
		return fitness;
	}
	
	public void setIndividualFitness( double fitness ) {
		this.fitness = fitness;
	}
	
	public double getSharedFitness() {
		return sharedFitness;
	}
	
	public void setSharedFitness( int speciesSize ) {
		this.sharedFitness = fitness / speciesSize;
	}
	
	public String toString() {
		String s = String.format("#{%3.2f ", fitness);
		for( ConnectionGene g : connections ) {
			s = s + " " + g;
		}
		s = s + " }#";
		return s;
	}
	
	/**
	 * Get a random value from -1 to 1
	 * @return a double value from -1 to 1 (technically excluding 1)
	 */
	private static double randomWeight() {
		return (Math.random() * 2) - 1;
	}
	
	
	// Comparators:
	
	public static Comparator<Genome> BY_INDIVIDUAL_FITNESS() {
		return new Comparator<Genome>() {
			@Override
			public int compare(Genome a, Genome b) {
				return -1 * Double.compare(a.getIndividualFitness(), b.getIndividualFitness());
			}
		};
	}
	
	public static Comparator<Genome> BY_SHARED_FITNESS() {
		return new Comparator<Genome>() {
			@Override
			public int compare(Genome a, Genome b) {
				return -1 * Double.compare(a.getSharedFitness(), b.getSharedFitness());
			}
		};
	}
}
