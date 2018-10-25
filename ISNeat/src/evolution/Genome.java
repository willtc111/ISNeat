package evolution;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
