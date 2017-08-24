package fr.evolya.javatoolkit.iot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.events.fi.EventProvider;
import fr.evolya.javatoolkit.events.fi.IObservable;
import fr.evolya.javatoolkit.events.fi.Listener;
import fr.evolya.javatoolkit.iot.Arduilink.OnDataReceived;
import fr.evolya.javatoolkit.iot.Arduilink.OnLinkEstablished;
import fr.evolya.javatoolkit.iot.Arduilink.OnSensorConnected;
import fr.evolya.javatoolkit.iot.Arduilink.OnUnknownPacket;

@EventProvider({OnLinkEstablished.class, OnSensorConnected.class, 
	OnDataReceived.class, OnUnknownPacket.class})

public class Arduilink implements 
	IObservable, AutoCloseable, Arduino.OnRawDataReceived {

	private Arduino uno;
	
	private Map<Integer, Node> nodes = new HashMap<>();
	private List<Sensor> sensors = new ArrayList<>();

	public Arduilink(Arduino uno) {
		if (uno == null)
			throw new NullPointerException();
		this.uno = uno;
		uno.when(OnLinkEstablished.class).execute(lnk -> {
			presentSensors();
		});
		uno.when(Arduino.OnRawDataReceived.class).execute(this);
		uno.when(Arduino.OnDisconnected.class).execute((port, ex) -> {
			synchronized (Arduilink.class) {
				// TODO Send events
				sensors.clear(); // OnSensorDisconnected
				nodes.clear(); // OnLinkBroken
			}
		});
	}
	
	public void presentSensors() {
		uno.write("PRESENT");
	}
	
	public Map<Integer, Node> getNodes() {
		return nodes;
	}
	
	public List<Sensor> getSensors() {
		return sensors;
	}

	@Override
	public <T> Listener<T> when(Class<T> eventType) {
		return uno.when(eventType);
	}

	@Override
	public void notify(Class<?> eventType, Object... args) {
		uno.notify(eventType, args);
	}

	@Override
	public void notify(Instance<?> target, Class<?> eventType, Object... args) {
		uno.notify(target, eventType, args);
	}
	
	@Override
	public void removeAllListeners() {
		uno.removeAllListeners();
	}

	@Override
	public void close() throws Exception {
		uno.close();
	}
	
	public Arduino get() {
		return uno;
	}
	
	public boolean isOpen() {
		return uno.isOpen();
	}
	
	public String getPortName() {
		return uno.commPort.getName();
	}

	public Node getNode(int nodeId) {
		return nodes.get(nodeId);
	}
	
	public Sensor getSensor(int nodeId, int sensorId) {
		return sensors.stream()
				.filter(sensor -> sensor.nodeId == nodeId && sensor.sensorId == sensorId)
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public void onRawDataReceived(String data) {
		if (data == null) return;
		int opcode = 0;
		try {
			opcode = Integer.parseInt(data.substring(0, data.indexOf(';')));
		}
		catch (Exception ex) {
			// Malformed opcode
			return;
		}
		String[] tokens = data.split(";");
		switch (opcode) {

		// 100 Welcome message
		case 100:
			Node node = new Node(tokens);
			nodes.put(node.nodeId, node);
			notify(OnLinkEstablished.class, node);
			break;

		// 200 Data exchange
		case 200:
			DataCommand cmd = new DataCommand(tokens);
			if (cmd.sensor != null) {
//				if (!Utils.equals(cmd.sensor.value, cmd.value)) {
					cmd.sensor.value = cmd.value;
					notify(OnDataReceived.class, cmd);
//				}
			}
			else notify(OnDataReceived.class, cmd);
			break;

		// 300 Sensors presentation
		case 300:
			Sensor sensor = new Sensor(tokens);
			sensors.add(sensor);
			notify(OnSensorConnected.class, sensor);
			break;

		default:
			notify(OnUnknownPacket.class, opcode, tokens);
		}
	}
	
	abstract class Command {
		public final Arduilink link;
		public final int nodeId;
		public final Node node;
		public Command(String[] tokens) {
			this.link = Arduilink.this;
			nodeId = Integer.parseInt(tokens[1]);
			node = getNode(nodeId);
		}
	}
	
	public class Node extends Command {
		public final String version;
		public Node(String[] tokens) {
			super(tokens);
			version = tokens[2];
		}
		public String toString() {
			return String.format("Node[ID=%s Version=%s]", nodeId, version);
		}
	}
	
	public class DataCommand extends Command {
		public final int sensorId;
		public final Sensor sensor;
		public final String value;
		public DataCommand(String[] tokens) {
			super(tokens);
			sensorId = Integer.parseInt(tokens[2]);
			sensor = getSensor(nodeId, sensorId);
			value = tokens[3];
		}
		public String toString() {
			return String.format("Data[From=%s Value=%s]", sensor, value);
		}
	}
	
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
		public Sensor(String[] tokens) {
			super(tokens);
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
	}
	
	@FunctionalInterface
	public static interface OnLinkEstablished {
		void onLinkEstablished(Node node);
	}
	
	@FunctionalInterface
	public static interface OnLinkBroken {
		void onLinkBroken(Node node);
	}
	
	@FunctionalInterface
	public static interface OnDataReceived {
		void onLinkEstablished(DataCommand data);
	}
	
	@FunctionalInterface
	public static interface OnSensorConnected {
		void onSensorConnected(Sensor sensor);
	}
	
	@FunctionalInterface
	public static interface OnSensorDisconnected {
		void onSensorDisconnected(Sensor sensor);
	}
	
	@FunctionalInterface
	public static interface OnUnknownPacket {
		void onUnknownPacket(int opcode, String[] tokens);
	}
	
}
