
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
	
	public void setIn( Integer in ) {
		this.in = in;
	}
	
	public int getOut() {
		return out;
	}
	
	public void setOut( Integer out ) {
		this.out = out;
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
