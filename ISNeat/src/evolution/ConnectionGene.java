package evolution;

public class ConnectionGene implements Comparable<ConnectionGene> {
	
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
	
	@Override
	public int compareTo(ConnectionGene other) {
		return innov - other.getInnov();
	}
	
	public int getInnov() {
		return innov;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight( Double weight ) {
		this.weight = weight;
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

}
