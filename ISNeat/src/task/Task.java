package task;

import neuralnetwork.NeuralNetwork;

public abstract class Task {

	public abstract String[] getInputs();
	public abstract String[] getOutputs();
	
	public abstract double calculateFitness(NeuralNetwork neuralNetwork);
}
