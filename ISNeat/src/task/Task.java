package task;

import neuralnetwork.NeuralNetwork;

public abstract class Task {

	/**
	 * Get the input names for this task
	 * @return	Array of input names
	 */
	public abstract String[] getInputs();
	
	/**
	 * Get the output names for this task
	 * @return	Array of output names
	 */
	public abstract String[] getOutputs();
	
	/**
	 * Perform the task with the given neural network and calculate the fitness score
	 * @param neuralNetwork	The neural network to test
	 * @return				The fitness of the neural network
	 */
	public abstract double calculateFitness(NeuralNetwork neuralNetwork);
}
