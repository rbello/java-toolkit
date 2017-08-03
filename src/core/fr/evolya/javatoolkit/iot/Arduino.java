package fr.evolya.javatoolkit.iot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.fi.Observable;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class Arduino extends Observable
	implements SerialPortEventListener, AutoCloseable {
	
	/** Milliseconds to block while waiting for port open */
	public static int TIME_OUT = 2000;
	
	/** Default bits per second for COM port */
	public static int DATA_RATE = 9600;
	
	/** Liaison série avec l'arduino */
	public SerialPort serialPort;
	
	/** Flux de lecture sur la liaison série */
	public BufferedReader input;
	
	/** Flux d'écriture sur la liaison série */
	public OutputStream output;

	private CommPortIdentifier commPort;
	
	/** Message d'erreur en cas de buffer vide */
	private static final String EmptyBufferErrorMessage = "Underlying input stream returned zero bytes";

	public Arduino(CommPortIdentifier commPort) {
		this.commPort = commPort;
	}
	
	public CommPortIdentifier getCommPort() {
		return commPort;
	}
	
	public synchronized void open() throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) commPort.open(Arduino.class.getName(), TIME_OUT);
			
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
	
			// Open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			notify(OnStateChanged.class, true, null);
			
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException ex) {
			serialPort = null;
			notify(OnStateChanged.class, false, ex);
			throw ex;
		}
	}
	
	public synchronized boolean isOpen() {
		return serialPort != null;
	}

	public static Stream<CommPortIdentifier> getPortIdentifiers() {
		return Utils
				.list(CommPortIdentifier.getPortIdentifiers(), CommPortIdentifier.class)
				.stream()
				.filter(port -> port.getPortType() == CommPortIdentifier.PORT_SERIAL);
	}
	
	public static Arduino getByName(String portName) {
		CommPortIdentifier id = getPortIdentifiers()
			.filter(port -> portName.toUpperCase().equals(port.getName().toUpperCase()))
			.findFirst()
			.orElse(null);
		if (id == null) return null;
		return getByPort(id);
	}
	
	public static Arduino getByPort(CommPortIdentifier port) {
		return new Arduino(port);
	}
	
	public static Arduino getFirst() {
		return new Arduino(getPortIdentifiers().findFirst().get());
	}
	
	public void write(String data) {
		try {
			output.write(data.getBytes());
			output.flush();
		}
		catch (Exception e) {
			System.err.println(String.format("Could not write data to serial, %s : %s", e.getClass().getSimpleName(), e.getMessage()));
		}
	}
	
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
	
		// Uniquement les évents d'arrivée de données
		if (oEvent.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
			notify(OnSerialEvent.class, oEvent);
			return;
		}
		
		// On tente de lire une trame
		try {
			String inputLine = input.readLine();
			notify(OnRawDataReceived.class, inputLine);
		}
		catch (IOException e) {
			if (e.getMessage().equals(EmptyBufferErrorMessage)) {
				// On laisse passer ce genre d'erreur
			}
			else {
				System.err.println(String.format("[Arduino] Error %s", e.toString()));
			}
		}
		catch (Throwable e) {
			System.err.println(String.format("[Arduino] Error %s", e.toString()));
			//e.printStackTrace();
		}

	}

	@Override
	public synchronized void close() throws Exception {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
			notify(OnStateChanged.class, false, null);
		}
		removeAllListeners();
	}
	
	@FunctionalInterface
	public static interface OnStateChanged {
		void onStateChanged(boolean open, Exception ex);
	}
	
	@FunctionalInterface
	public static interface OnSerialEvent {
		void onSerialEvent(SerialPortEvent event);
	}
	
	@FunctionalInterface
	public static interface OnRawDataReceived {
		void onRawDataReceived(String data);
	}

}
