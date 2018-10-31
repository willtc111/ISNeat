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
	
	@Override
	public boolean equals(Object other) {
		if( other instanceof NodeGene ) {
			NodeGene otherNG = (NodeGene) other;
			return (id == otherNG.getId()) && (type == otherNG.getType());
		} else {
			return false;
		}
	}
}
