package fr.evolya.javatoolkit.iot.arduilink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.code.InstanceContainer;
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
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnConnected;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnDisconnected;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnRawDataReceived;

@EventProvider({OnLinkEstablished.class, OnSensorConnected.class, 
	OnDataReceived.class, OnUnknownPacket.class})

public class Arduilink implements 
	IObservable, AutoCloseable, OnRawDataReceived {

	private Arduino uno;
	
	private boolean debug;
	
	private Map<Integer, Node> nodes = new HashMap<>();
	private List<Sensor> sensors = new ArrayList<>();

	public Arduilink(Arduino uno) {
		if (uno == null)
			throw new NullPointerException();

		this.uno = uno;
		this.debug = uno.debug;
		
		final InstanceContainer<Boolean> bound = new InstanceContainer<>(false);
		
		// Setup
		uno.when(OnConnected.class).execute((port) -> {
			if (debug) System.out.println("[Arduilink] Arduino connected to "+port.getName());
			new Thread(() -> {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
					return;
				}
				if (uno.isConnected() && !bound.get()) {
					// The arduino haven't respected the ARDUILINK protocol
					if (debug) System.err.println("[Arduilink] Invalid Arduilink node (don't speak the same langage !)");
					System.out.println("[Arduilink] Invalid Arduilink node (don't speak the same langage !)");
				}
			}).start();
		});
		
		// Present sensors on link established
		this.when(OnLinkEstablished.class).execute(lnk -> {
			if (debug) System.out.println("[Arduilink] Link established with ARDUILINK node, sending PRESENT SENSOR request...");
			bound.set(true);
			presentSensors();
		});
		
		// Cleanup sensors and nodes on disconnected
		uno.when(OnDisconnected.class).execute((port, ex) -> {
			if (debug) System.out.println("[Arduilink] Arduino disconnected from " + port.getName());
			bound.set(false);
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
	
	public Sensor getSensorByName(int nodeId, String sensorName) {
		return sensors.stream()
				.filter(sensor -> sensor.nodeId == nodeId && sensor.name.equals(sensorName))
				.findFirst()
				.orElse(null);
	}
	
	public Sensor getSensorByName(String sensorName) {
		return sensors.stream()
				.filter(sensor -> sensor.name.equals(sensorName))
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
		if (debug) System.out.println("[Arduilink] Received: " + data);
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
			if (debug) System.out.println("[Arduilink] New sensor: " + sensor);
			sensors.add(sensor);
			notify(OnSensorConnected.class, sensor);
			break;

		default:
			notify(OnUnknownPacket.class, opcode, tokens);
		}
	}
	
	public boolean setSensorVerboseEnabled(Sensor sensor, boolean enabled) {
		StringBuilder sb = new StringBuilder("SET;");
		sb.append(sensor.nodeId);
		sb.append(';');
		sb.append(sensor.sensorId);
		sb.append(";VERBOSE;0;");
		sb.append(enabled ? '1' : '0');
		return uno.write(sb.toString());
	}

	public boolean setSensorValue(Sensor sensor, String data, boolean ack) {
		StringBuilder sb = new StringBuilder("SET;");
		sb.append(sensor.nodeId);
		sb.append(';');
		sb.append(sensor.sensorId);
		sb.append(";VAL;");
		sb.append(ack ? '1' : '0');
		sb.append(';');
		sb.append(data);
		// TODO Handle ack response
		return uno.write(sb.toString());
	}

}
