import java.util.ArrayList;

public class Genome {

	private ArrayList<NodeGene> nodes;
	private ArrayList<ConnectionGene> connections;
	
	public Genome( int numInputs, int numOutputs ) {
		// initialize nodes
		nodes = new ArrayList<NodeGene>();
		connections = new ArrayList<ConnectionGene>( numInputs * numOutputs );
		
		// initialize the connections (may want to do this somewhere higher up and pass it in to each organism)
		int innov = 0;
		for( int input = 0; input < numInputs; input++ ) {
			for( int output = 0; output < numOutputs; output++ ) {
				connections.add( new ConnectionGene(innov++, 0.0, input, output) );
			}
		}
		
	}
	
	public Genome( ArrayList<NodeGene> nodes, ArrayList<ConnectionGene> connections ) {
		
	}
	
}
