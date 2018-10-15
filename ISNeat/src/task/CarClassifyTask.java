package task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import neuralnetwork.NeuralNetwork;

public class CarClassifyTask extends Task {
	
	LinkedList<CarClassification> carList;
	
	public CarClassifyTask( String filename ) throws FileNotFoundException {
		// open file and a scanner for it
		File file = new File( filename );
		Scanner scan = new Scanner( file );
		
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
	public String[] getInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double calculateFitness(NeuralNetwork neuralNetwork) {
		// TODO Auto-generated method stub
		return 0;
	}

}
