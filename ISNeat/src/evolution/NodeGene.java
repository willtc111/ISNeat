package evolution;

public class NodeGene {
	
	private int id;
	private NodeType type;
	
	public NodeGene( int id, NodeType type ) {
		this.id = id;
		this.type = type;
	}
	
	public NodeGene clone() {
		return new NodeGene(id, type);
	}
	
	public int getId() {
		return id;
	}
	
	public NodeType getType() {
		return type;
	}
}
