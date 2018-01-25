package fr.evolya.javatoolkit.iot.arduilink;

public abstract class Command {
	
	public final Arduilink link;
	public final int nodeId;
	public final Node node;
	
	public Command(Arduilink link, String[] tokens) {
		this.link = link;
		nodeId = Integer.parseInt(tokens[1]);
		node = link.getNode(nodeId);
		if (node == null && Integer.parseInt(tokens[0]) != 100)
			throw new NullPointerException("Node not found: "+  nodeId);
	}
	
}