package evolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Genome {

	private final static double MUTATION_SCALAR = 0.25;
	
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
	 * Mutate this genome's connection weights
	 * 
	 * @param mutationChance The probability for mutation to occurr on an individual weight
	 */
	public void mutateWeights( double mutationChance ) {
		Random rand = new Random();
		for( ConnectionGene c : connections ) {
			if( rand.nextDouble() < mutationChance ) {
				// mutate this connection
				double alteration = MUTATION_SCALAR * rand.nextGaussian();
				alteration = Math.max(-1.0, Math.min(1.0, alteration));
				c.setWeight(c.getWeight() + alteration);
				// TODO: should the weight value be -1/1 bound?  Maybe put a min/max check in the setWeight method itself.
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
		
		int to = nodes.get(rand.nextInt(nodes.size())).getId();
		int from = nodes.get(rand.nextInt(nodes.size())).getId();
		ConnectionGene newConnection = new ConnectionGene(innovationNumber, rand.nextDouble(), from, to);
		
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
				newConnection.setWeight(rand.nextDouble());
				connections.add(newConnection);
				
				// return null to signify a new connection was not made
				return null;
			}
		}
		
		connections.add(newConnection);
		return newConnection;
	}
	
	/**
	 * Add a node by selecting a current connection and adding a node in the
	 * middle of it, with two new connections going from the old starting
	 * point to the new node, and from the new node to the old ending point.
	 * 
	 * @param innovationNumber The next connection's innovation number
	 * @param nextNodeNumber The next node ID
	 * @param latestConnections The list of new connections from this generation's mutations 
	 * @return All of the connections added to the genome during this operation
	 */
	public List<ConnectionGene> mutateAddNode(int innovationNumber, int nextNodeNumber, List<ConnectionGene> latestConnections) {
		// choose a connection to split
		ConnectionGene removeMe = connections.get(new Random().nextInt(connections.size()));
		connections.remove(removeMe);
		
		// make the new stuff
		int from = removeMe.getIn();
		int to = removeMe.getOut();
		NodeGene newNode = new NodeGene(nextNodeNumber, NodeType.HIDDEN);
		ConnectionGene firstConnection  = new ConnectionGene(innovationNumber,
															 1.0,
															 from,
															 newNode.getId());
		ConnectionGene secondConnection = new ConnectionGene(innovationNumber+1,
															 removeMe.getWeight(),
															 newNode.getId(),
															 to);
		
		nodes.add(newNode);
		connections.add(firstConnection);
		connections.add(secondConnection);
		
		return Arrays.asList(firstConnection, secondConnection);
		
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
	
	/**
	 * Getter for the connection list
	 * @return	The connection list
	 */
	public List<ConnectionGene> getConnections() {
		return connections;
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
	
	
	// Comparators:
	
	public static class BY_INDIVIDUAL_FITNESS implements Comparator<Genome> {
		@Override
		public int compare(Genome a, Genome b) {
			return Double.compare(a.getIndividualFitness(), b.getIndividualFitness());
		};
	}
	
	public static class BY_SHARED_FITNESS implements Comparator<Genome> {
		@Override
		public int compare(Genome a, Genome b) {
			return Double.compare(a.getSharedFitness(), b.getSharedFitness());
		};
	}
}
