package task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CarClassification {
	
	private static final Map<String, Double> classes;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("unacc", 0.0);
		map.put("acc",   0.25);
		map.put("good",  0.5);
		map.put("vgood", 0.75);
		classes = Collections.unmodifiableMap(map);
	}
	public final String classification;
	
	private static final Map<String, Double> prices;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("low",   0.0);
		map.put("med",   0.25);
		map.put("high",  0.5);
		map.put("vhigh", 0.75);
		prices = Collections.unmodifiableMap(map);
	}
	public final String buying;
	public final String maint;
	
	private static final Map<String, Double> doorCounts;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("2",     0.0);
		map.put("3",     0.25);
		map.put("4",     0.5);
		map.put("5more", 0.75);
		doorCounts = Collections.unmodifiableMap(map);
	}
	public final String doors;
	
	private static final Map<String, Double> personCounts;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("2",    0.0);
		map.put("4",    (1.0/3.0));
		map.put("more", (2.0/3.0));
		personCounts = Collections.unmodifiableMap(map);
	}
	public final String persons;
	
	private static final Map<String, Double> lugSizes;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("small", 0.0);
		map.put("med",   (1.0/3.0));
		map.put("big",   (2.0/3.0));
		lugSizes = Collections.unmodifiableMap(map);
	}
	public final String lugBoot;
	
	private static final Map<String, Double> safties;
	static {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("low",  0.0);
		map.put("med",  (1.0/3.0));
		map.put("high", (2.0/3.0));
		safties = Collections.unmodifiableMap(map);
	}
	public final String safety;
	
	private final double[] inputValues;
	private final double outputValue;
	
	public CarClassification( String[] attributes ) throws Exception {
		if( attributes.length != 7 ) {
			throw( new Exception("Invalid number of car classificaiton attributes.") );
		}
		
		// Store the attributes and calculate the input/output decimal values.
		inputValues = new double[6];
		
		buying = attributes[0];
		inputValues[0] = prices.get(buying);
		
		maint = attributes[1];
		inputValues[1] = prices.get(maint);
		
		doors = attributes[2];
		inputValues[2] = doorCounts.get(doors);
		
		persons = attributes[3];
		inputValues[3] = personCounts.get(persons);
		
		lugBoot = attributes[4];
		inputValues[4] = lugSizes.get(lugBoot);
		
		safety = attributes[5];
		inputValues[5] = safties.get(safety);
		
		classification = attributes[6];
		outputValue = classes.get(classification);
	}
	
	public double[] getInputs() {
		return inputValues;
	}
	
	public double getOutput() {
		return outputValue;
	}
	
}
