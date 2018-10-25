package evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import task.Task;

/**
 * Primary class to perform the genetic algorithm
 */
public class Evolver {
	// These will need to be experimentally determined, may even vary based on problem
	public static final double COMPATABILITY_THRESHOLD = 2.0;
	public static final double C1 = 1.0;	// constant multiplier for excess gene differences
	public static final double C2 = 1.0;	// constant multiplier for disjoint gene differences
	public static final double C3 = 1.0;	// constant multiplier for gene weight differences
	
	private int nextInnovNum;
	
	// The mapping from task inpout/output names to node id's.
	private Map<String, Integer> inputNodeMap;
	private Map<String, Integer> outputNodeMap;
	
	// The task to be trained for
	private Task task;
	
	// The population of organisms
	private Population population;
	
	public Evolver(Task task, int populationSize) {
		this.task = task;
		
		String[] inputArray = task.getInputs().toArray( new String[0] );
		String[] outputArray = task.getOutputs().toArray( new String[0] );
		
		List<NodeGene> initialNodes = new LinkedList<NodeGene>();
		
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
		
		nextInnovNum = outputArray.length + inputArray.length;
		
		// Initialize the population.
		population = new Population();
		for( int i = 0; i < populationSize; i++ ) {
			// start with no connections, bare minimum!
			population.add(new Genome(initialNodes, null) );
		}
	}
	
}
