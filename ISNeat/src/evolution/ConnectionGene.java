package evolution;

import java.util.Comparator;

public class ConnectionGene {
	
	private int innov;
	private double weight;
	private int in;
	private int out;
	private boolean enabled;
	
	public ConnectionGene( int innov, double weight, int in, int out ) {
		this.innov = innov;
		this.weight = weight;
		this.in = in;
		this.out = out;
		this.enabled = true;
	}
	
	private ConnectionGene( int innov, double weight, int in, int out, boolean enabled ) {
		this.innov = innov;
		this.weight = weight;
		this.in = in;
		this.out = out;
		this.enabled = enabled;
	}
	
	public ConnectionGene clone() {
		return new ConnectionGene(innov, weight, in, out, enabled);
	}
	
	public int getInnov() {
		return innov;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight( Double weight ) {
		// TODO: figure out if this should actually be bounded.
		this.weight = Math.max(-1.0, Math.min(1.0, weight));
	}
	
	public int getIn() {
		return in;
	}
	
	public int getOut() {
		return out;
	}
	
	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	// Comparators:
	public static Comparator<ConnectionGene> BY_INNOVATION_NUMBER() {
		return new Comparator<ConnectionGene>() {
			@Override
			public int compare(ConnectionGene a, ConnectionGene b) {
				return Integer.compare(a.getInnov(), b.getInnov());
			}
		};
	}
	
}
