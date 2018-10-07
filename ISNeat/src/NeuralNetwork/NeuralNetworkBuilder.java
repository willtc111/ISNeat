package NeuralNetwork;

import java.util.Map;

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
	 * @param inputs	Map for naming the input nodes
	 * @param outputs	Map for naming the output nodes
	 * @return			The constructed neural network
	 */
	public NeuralNetwork build( Genome genome, Map<String, Integer> inputs, Map<String, Integer> outputs ) {
		
		// find highest node id and collect input/output nodes
		int maxNode = 0;
		for( NodeGene node : genome.getNodes() ) {
			maxNode = Math.max(node.getId(), maxNode);
		}
		
		// Create the unconnected network containing the necessary nodes
		NeuralNetwork nn = new NeuralNetwork(maxNode, inputs, outputs);
		
		// Add the connections to the network
		for( ConnectionGene c : genome.getConnections() ) {
			nn.setWeight(c.getIn(), c.getOut(), c.getWeight());
		}
		
		return nn;
	}
}
