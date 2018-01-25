package fr.evolya.javatoolkit.iot.arduilink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.events.fi.EventProvider;
import fr.evolya.javatoolkit.events.fi.IObservable;
import fr.evolya.javatoolkit.events.fi.Listener;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnDataReceived;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnLinkBroken;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnLinkEstablished;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnSensorConnected;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnSensorDisconnected;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnUnknownPacket;
import fr.evolya.javatoolkit.iot.arduino.Arduino;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnDisconnected;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnRawDataReceived;

@EventProvider({OnLinkEstablished.class, OnSensorConnected.class, 
	OnDataReceived.class, OnUnknownPacket.class})

public class Arduilink implements 
	IObservable, AutoCloseable, OnRawDataReceived {

	private Arduino uno;
	
	private Map<Integer, Node> nodes = new HashMap<>();
	private List<Sensor> sensors = new ArrayList<>();

	public Arduilink(Arduino uno) {
		if (uno == null)
			throw new NullPointerException();
		this.uno = uno;
		
		// Present sensors on link established
		uno.when(OnLinkEstablished.class).execute(lnk -> {
			presentSensors();
		});
		
		// Cleanup sensors and nodes on disconnected
		uno.when(OnDisconnected.class).execute((port, ex) -> {
			synchronized (Arduilink.class) {
				for (Sensor sensor : sensors)
					notify(OnSensorDisconnected.class, sensor);
				for (Node node : nodes.values())
					notify(OnLinkBroken.class, node);
				sensors.clear();
				nodes.clear();
			}
		});
		
		// Handle data received
		uno.when(OnRawDataReceived.class).execute(this);
		
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
	
	public <T> Listener<T> on(Class<T> eventType) {
		return when(eventType);
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
		return uno.getComPort().getName();
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
			Node node = new Node(this, tokens);
			nodes.put(node.nodeId, node);
			notify(OnLinkEstablished.class, node);
			break;

		// 200 Data exchange
		case 200:
			DataCommand cmd = new DataCommand(this, tokens);
			if (cmd.sensor != null) {
				if (cmd.sensor.setValue(cmd.value)) {
					notify(OnDataReceived.class, cmd);
				}
			}
			else notify(OnDataReceived.class, cmd);
			break;

		// 300 Sensors presentation
		case 300:
			Sensor sensor = new Sensor(this, tokens);
			sensors.add(sensor);
			notify(OnSensorConnected.class, sensor);
			break;

		default:
			notify(OnUnknownPacket.class, opcode, tokens);
		}
	}
	
}
