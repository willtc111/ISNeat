package task;

import java.util.List;

import neuralnetwork.NeuralNetwork;

public interface Task {

	/**
	 * Get the input names for this task.  Needed in order to know how to build the initial neural networks.
	 * @return	List of input names
	 */
	public List<String> getInputs();
	
	/**
	 * Get the output names for this task.  Needed in order to know how to build the initial neural networks.
	 * @return	List of output names
	 */
	public List<String> getOutputs();
	
	/**
	 * Perform the task with the given neural network and calculate the fitness score for the test data set
	 * Score is in the range of 0.0  to 1.0
	 * @param neuralNetwork	The neural network to test
	 * @return				The fitness of the neural network
	 */
	public double calculateTestFitness(NeuralNetwork neuralNetwork);
	
	/**
	 * Perform the task with the given neural network and calculate the fitness score for the training data set
	 * Score is in the range of 0.0  to 1.0
	 * @param neuralNetwork	The neural network to test
	 * @return				The fitness of the neural network
	 */
	public double calculateTrainFitness(NeuralNetwork neuralNetwork);
}
