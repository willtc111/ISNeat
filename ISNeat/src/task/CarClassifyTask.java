package task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import neuralnetwork.NeuralNetwork;

public class CarClassifyTask implements Task {
	
	LinkedList<CarClassification> trainCarList;
	LinkedList<CarClassification> testCarList;
	public CarClassifyTask( String filename ) throws FileNotFoundException {
		// open data file and a scanner for it
		Scanner scan = new Scanner( new File(filename) );
		
		trainCarList = new LinkedList<CarClassification>();
		testCarList = new LinkedList<CarClassification>();
		
		// Read in lines
		int count = 0;
		while( scan.hasNextLine() ) {
			String line = scan.nextLine();
			
			// Split it by comma
			String[] attributes = line.split(",", 7);
			
			// Create a new CarClassification and add it to the list
			try {
				CarClassification cc = new CarClassification(attributes);
				if( count++ % 10 == 0 ) {
					testCarList.add( cc );
				} else {
					trainCarList.add( cc );
				}
			} catch (Exception e) {
				e.printStackTrace();	// no bueno...
			}
		}
		
		scan.close();
	}
	
	@Override
	public List<String> getInputs() {
		Set<String> inputSet = trainCarList.getFirst().getInputs().keySet();
		List<String> inputList = new ArrayList<String>( inputSet );
		Collections.sort( inputList );
		return inputList;
	}

	@Override
	public List<String> getOutputs() {
		return Arrays.asList("classification");
	}

	@Override
	public double calculateTrainFitness(NeuralNetwork neuralNetwork) {
		return calcFit(neuralNetwork, trainCarList);
	}
	
	@Override
	public double calculateTestFitness(NeuralNetwork neuralNetwork) {
		return calcFit(neuralNetwork, testCarList);
	}
	
	private double calcFit(NeuralNetwork neuralNetwork, List<CarClassification> carList) {
		double numCorrect = 0;
		for( CarClassification c : carList ) {
			neuralNetwork.setInputs(c.getInputs());
			neuralNetwork.updateUntilSteady(5);
			numCorrect += c.outputCorrectness( neuralNetwork.getOutputs().get("classification") );
		}
		return numCorrect / carList.size();
	}
}
