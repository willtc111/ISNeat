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
	
	public void updateOnce() {
		// copy states array
		double[] newStates = new double[states.length];
		for( int t = 0; t < states.length; t++ ) {
			newStates[t] = 0.0;
		}
		
		for( int t = 0; t < states.length; t++ ) {
			// update the newState node t using the values from the old state and connection weights.
			double sum = 0;
			boolean isUpdated = false;
			for( int f = 0; f < states.length; f++ ) {
				if( connections[f][t] != 0 ) {
					isUpdated = true;
					sum += states[f] * connections[f][t];
				}
			}
			if( isUpdated ) {
				newStates[t] = 1 / (1 + Math.pow(Math.E, -4.9 * sum));
			} else {
				newStates[t] = states[t];
			}
		}
		
		// Overwrite old states
		states = newStates;
	}
	
	public void updateAll() {
		// no matter how many nodes there are, this is guaranteed to update all of them at least once
		updateUntilSteady(states.length);
	}
	
	public void updateUntilSteady(int max) {
		int count = 0;
		double[] oldStates = null;
		do {
			oldStates = states.clone();
			updateOnce();
			count++;
		} while( stateHasChanged(oldStates) && count < max );
	}
	
	private boolean stateHasChanged( double[] old ) {
		for( int i = 0; i < states.length; i++ ) {
			if( states[i] != old[i] ) {
				return true;
			}
		}
		return false;
	}
	public void setInputs(Map<String, Double> values) {
		for( String name : values.keySet() ) {
			states[inputs.get(name)] = values.get(name);
		}
	}
	
	public Map<String, Double> getOutputs() {
		Map<String, Double> values = new HashMap<String, Double>();
		for( String name : outputs.keySet() ) {
			int index = outputs.get(name);
			values.put(name, states[index]);
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
