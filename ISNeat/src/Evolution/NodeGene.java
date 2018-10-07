package Evolution;

public class NodeGene {

	private static int nextId = 0;
	
	private int id;
	private NodeType type;
	
	public NodeGene( NodeType type) {
		this.id = nextId++;
		this.type = type;
	}
	
	private NodeGene( int id, NodeType type ) {
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
