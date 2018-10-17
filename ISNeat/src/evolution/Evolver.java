package evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import task.Task;

/**
 * Primary class to perform the genetic algorithm
 */
public class Evolver {
	
	private Map<String, Integer> inputNodeMap;
	private Map<String, Integer> outputNodeMap;
	
	private ArrayList<Genome> population;
	
	public Evolver(Task task, int populationSize) {
		String[] inputArray = task.getInputs().toArray( new String[0] );
		String[] outputArray = task.getOutputs().toArray( new String[0] );
		
		ArrayList<NodeGene> initialNodes = new ArrayList<NodeGene>(inputArray.length + outputArray.length);
		
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
		
		// Initialize the population.
		population = new ArrayList<Genome>(populationSize);
		for( int i = 0; i < populationSize; i++ ) {
			// start with no connections, bare minimum!
			population.add(new Genome(initialNodes, null) );
		}
	}
	
}
