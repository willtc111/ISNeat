package neuralnetwork;

import java.util.HashMap;
import java.util.Map;

public class NeuralNetwork {

	private double[] states;
	private double[][] connections; // [from][to]
	
	private Map<String, Integer> inputs;
	private Map<String, Integer> outputs;
	
	public NeuralNetwork(int numNodes, Map<String, Integer> inputs, Map<String, Integer> outputs) {
		connections = new double[numNodes][numNodes];
		states = new double[numNodes];
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	public void update() {
		double[] newStates = new double[states.length];
		
		for( int t = 0; t < states.length; t++ ) {
			// update the newState node t using the values from the old state and connection weights.
			double count = 0;
			for( int f = 0; f < states.length; f++ ) {
				if( connections[f][t] != 0 ) {
					count++;	// keep track of number of connections for normalization
					newStates[t] += states[f] * connections[f][t];
				}
			}
			newStates[t] /= count;	// Normalize
		}
		
		// Overwrite old states
		states = newStates;
	}
	
	public void setInputs(Map<String, Double> values) {
		for( String name : values.keySet() ) {
			states[inputs.get(name)] = values.get(name);
		}
	}
	
	public Map<String, Double> getOutputs() {
		Map<String, Double> values = new HashMap<String, Double>();
		for( String name : outputs.keySet() ) {
			values.put(name, states[outputs.get(name)]);
		}
		return values;
	}
	
	public void setWeight(int from, int to, double weight) {
		connections[from][to] = weight;
	}
	
	public void setState(int node, double state) {
		states[node] = state;
	}
	
	public double getState(int node) {
		return states[node];
	}
}
