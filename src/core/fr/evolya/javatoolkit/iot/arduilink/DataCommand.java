package fr.evolya.javatoolkit.iot.arduilink;

public class DataCommand extends Command {
	
	public final int sensorId;
	public final Sensor sensor;
	public final String value;
	
	public DataCommand(Arduilink link, String[] tokens) {
		super(link, tokens);
		sensorId = Integer.parseInt(tokens[2]);
		sensor = link.getSensor(nodeId, sensorId);
		value = tokens[3];
		if (sensor == null)
			throw new NullPointerException("Command has no sensor: " + String.join(" ", tokens));
	}
	
	public String toString() {
		return String.format("Data[From=%s Value=%s]", sensor, value);
	}
	
}
