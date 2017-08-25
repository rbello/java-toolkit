package fr.evolya.javatoolkit.iot.arduilink;

public class Node extends Command {
	
	public final String version;
	
	public Node(Arduilink link, String[] tokens) {
		super(link, tokens);
		version = tokens[2];
	}
	
	public String toString() {
		return String.format("Node[ID=%s Version=%s]", nodeId, version);
	}
	
}