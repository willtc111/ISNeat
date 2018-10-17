package evolution;
import java.util.ArrayList;

public class Genome {

	private ArrayList<NodeGene> nodes;
	private ArrayList<ConnectionGene> connections;
	
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
	public Genome( ArrayList<NodeGene> nodes, ArrayList<ConnectionGene> connections ) {
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
	 * Getter for node list
	 * @return	The node list
	 */
	public ArrayList<NodeGene> getNodes() {
		return nodes;
	}
	
	/**
	 * Getter for the connection list
	 * @return	The connection list
	 */
	public ArrayList<ConnectionGene> getConnections() {
		return connections;
	}
}
