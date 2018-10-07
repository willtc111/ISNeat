package NeuralNetwork;

import java.util.ArrayList;

import Evolution.ConnectionGene;
import Evolution.Genome;
import Evolution.NodeGene;

/**
 * Builds neural networks
 */
public class NeuralNetworkBuilder {
	
	public NeuralNetworkBuilder() {
		
	}
	
	/**
	 * 
	 * @param genome	The genome representing the network
	 * @return			The constructed neural network
	 */
	public NeuralNetwork build( Genome genome ) {
		ArrayList<NodeGene> nodes = genome.getNodes();
		ArrayList<ConnectionGene> connections = genome.getConnections();
		
		// find highest node id.
		int maxNode = 0;
		for( NodeGene node : nodes ) {
			maxNode = Math.max(node.getId(), maxNode);
		}
		
		NeuralNetwork nn = new NeuralNetwork(maxNode);
		
		for( ConnectionGene c : connections ) {
			nn.setWeight(c.getIn(), c.getOut(), c.getWeight());
		}
		
		return nn;
	}
}
