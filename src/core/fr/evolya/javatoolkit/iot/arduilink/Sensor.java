package fr.evolya.javatoolkit.iot.arduilink;

import java.util.ArrayList;
import java.util.List;

public class Sensor extends Command {
	
	public static final int S_INFO = 1;
	public static final int S_HIT = 2;
	public static final int S_ACTION = 4;
	public static final int S_BATTERY = 8;
	
	public final int sensorId;
	public final int flags;
	public final String unit;
	public final boolean verbose;
	public final String name;
	public final String type;
	public String value = null;
	
	public Sensor(Arduilink link, String[] tokens) {
		super(link, tokens);
		sensorId = Integer.parseInt(tokens[2]);
		flags = Integer.parseInt(tokens[3]);
		unit = tokens[4];
		verbose = Integer.parseInt(tokens[5]) == 1;
		name = tokens[6];
		type = getSensorType(flags);
	}
	
	public String getSensorType(int flags) {
		List<String> tags = new ArrayList<>();
		if (is(S_INFO)) tags.add("Info");
		if (is(S_HIT)) tags.add("Hit");
		if (is(S_ACTION)) tags.add("Action");
		if (is(S_BATTERY)) tags.add("Battery");
		return String.join(", ", tags);
	}
	
	public boolean is(int type) {
		return (flags & type) == type;
	}
	
	public String toString() {
		return String.format("Sensor[Name=%s Node=%s Type=%s]", name, nodeId, type);
	}

	boolean setValue(String value) {
		// TODO C'est une stratégie qui ne devrait pas marcher pour les HIT 
		//if (Utils.equals(this.value, value)) return false;
		this.value = value;
		// TODO Auto-generated method stub
		return true;
	}

	public boolean set(String data, boolean ack) {
		return link.setSensorValue(this, data, ack);
	}

}
