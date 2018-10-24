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
	
	LinkedList<CarClassification> carList;
	
	public CarClassifyTask( String filename ) throws FileNotFoundException {
		// open data file and a scanner for it
		Scanner scan = new Scanner( new File(filename) );
		
		carList = new LinkedList<CarClassification>();
		
		// Read in lines
		while( scan.hasNextLine() ) {
			String line = scan.nextLine();
			
			// Split it by comma
			String[] attributes = line.split(",", 7);
			
			// Create a new CarClassification and add it to the list
			try {
				carList.add( new CarClassification(attributes) );
			} catch (Exception e) {
				e.printStackTrace();	// no bueno...
			}
		}
		
		scan.close();
	}
	
	@Override
	public List<String> getInputs() {
		Set<String> inputSet = carList.getFirst().getInputs().keySet();
		List<String> inputList = new ArrayList<String>( inputSet );
		Collections.sort( inputList );
		return inputList;
	}

	@Override
	public List<String> getOutputs() {
		return Arrays.asList("classification");
	}

	@Override
	public double calculateFitness(NeuralNetwork neuralNetwork) {
		double numCorrect = 0;
		for( CarClassification c : carList ) {
			neuralNetwork.setInputs(c.getInputs());
			neuralNetwork.updateAll();
			numCorrect += c.outputCorrectness( neuralNetwork.getOutputs().get("classification") );
		}
		return numCorrect / carList.size();
	}
}
