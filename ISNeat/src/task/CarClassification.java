package task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CarClassification {
	private static final double ONETHIRD = (1.0/3.0);
	private static final double TWOTHIRDS = (2.0/3.0);
	private static final Map<String, Double> classes;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("unacc", 0.0);
		map.put("acc",   ONETHIRD);
		map.put("good",  TWOTHIRDS);
		map.put("vgood", 1.0);
		classes = Collections.unmodifiableMap(map);
	}
	public final String classification;
	
	private static final Map<String, Double> prices;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("low",   0.0);
		map.put("med",   ONETHIRD);
		map.put("high",  TWOTHIRDS);
		map.put("vhigh", 0.);
		prices = Collections.unmodifiableMap(map);
	}
	public final String buying;
	public final String maint;
	
	private static final Map<String, Double> doorCounts;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("2",     0.0);
		map.put("3",     ONETHIRD);
		map.put("4",     TWOTHIRDS);
		map.put("5more", 1.0);
		doorCounts = Collections.unmodifiableMap(map);
	}
	public final String doors;
	
	private static final Map<String, Double> personCounts;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("2",    0.0);
		map.put("4",    0.5);
		map.put("more", 1.0);
		personCounts = Collections.unmodifiableMap(map);
	}
	public final String persons;
	
	private static final Map<String, Double> lugSizes;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("small", 0.0);
		map.put("med",   0.5);
		map.put("big",   1.0);
		lugSizes = Collections.unmodifiableMap(map);
	}
	public final String lugBoot;
	
	private static final Map<String, Double> safties;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("low",  0.0);
		map.put("med",  0.5);
		map.put("high", 1.0);
		safties = Collections.unmodifiableMap(map);
	}
	public final String safety;
	
	private final Map<String, Double> inputValues;
	private final double outputValue;
	
	public CarClassification( String[] attributes ) throws Exception {
		if( attributes.length != 7 ) {
			throw( new Exception("Invalid number of car classificaiton attributes.") );
		}
		
		// Store the attributes and calculate the input/output decimal values.
		inputValues = new HashMap<String, Double>();
		
		buying = attributes[0];
		inputValues.put("buying", prices.get(buying));
		
		maint = attributes[1];
		inputValues.put("maint", prices.get(maint));
		
		doors = attributes[2];
		inputValues.put("doors", doorCounts.get(doors));
		
		persons = attributes[3];
		inputValues.put("persons", personCounts.get(persons));
		
		lugBoot = attributes[4];
		inputValues.put("lugBoot", lugSizes.get(lugBoot));
		
		safety = attributes[5];
		inputValues.put("safety", safties.get(safety));
		
		classification = attributes[6];
		outputValue = classes.get(classification);
	}
	
	/**
	 * The correctness of the output, on a scale from 0.0 to 1.0.
	 * @param out	The value to check
	 * @return		How correct the value is
	 */
	public double outputCorrectness( double out ) {
		return 1.0 - outputWrongness(out);
	}
	
	/**
	 * The wrongness of the output, on a scale from 0.0 to 1.0 
	 * @param out	The value to check
	 * @return		How wrong the value is
	 */
	public double outputWrongness( double out ) {
		// ensure output is in the range of 0.0 to 1.0
		if( out > 1.0 ) out = 1.0;
		else if( out < 0 ) out = 0;
		
		return Math.abs(outputValue - out);
	}
	
	public Map<String, Double> getInputs() {
		return inputValues;
	}
	
	public double getOutput() {
		return outputValue;
	}
}
