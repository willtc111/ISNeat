package NeuralNetwork;

public class NeuralNetwork {

	private double[] states;
	private double[][] connections; // [from][to]
	
	public NeuralNetwork(int numNodes) {
		connections = new double[numNodes][numNodes];
		states = new double[numNodes];
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
	
	public void update() {
		double[] newStates = new double[states.length];
		for( int t = 0; t < states.length; t++ ) {
			// update the newState node t using the values from the old state and connection weights.
			double count = 0;
			for( int f = 0; f < states.length; f++ ) {
				if( connections[f][t] != 0 ) {
					count++;
				}
				newStates[t] += states[f] * connections[f][t];
			}
			newStates[t] /= count;	// Normalize
		}
		states = newStates;
	}
}
