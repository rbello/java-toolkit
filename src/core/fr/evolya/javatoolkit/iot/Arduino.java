package fr.evolya.javatoolkit.iot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.fi.EventProvider;
import fr.evolya.javatoolkit.events.fi.Observable;
import fr.evolya.javatoolkit.iot.Arduino.OnRawDataReceived;
import fr.evolya.javatoolkit.iot.Arduino.OnReceiveError;
import fr.evolya.javatoolkit.iot.Arduino.OnSerialEvent;
import fr.evolya.javatoolkit.iot.Arduino.OnStateChanged;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

@EventProvider({OnStateChanged.class, OnSerialEvent.class,
	OnRawDataReceived.class, OnReceiveError.class})

public class Arduino extends Observable
	implements SerialPortEventListener, AutoCloseable {
	
	/**
	 * Milliseconds to block while waiting for port open
	 */
	protected int openTimeOut = 2000;
	
	/**
	 * Default bits per second for COM port
	 */
	protected int dataRate = 9600;
	
	/**
	 * COM port used to connect to
	 */
	protected CommPortIdentifier commPort;
	
	/**
	 * Serial link with the arduino
	 */
	protected SerialPort serialPort;
	
	/**
	 * Input stream
	 */
	protected BufferedReader input;
	
	/**
	 * Output stream
	 */
	protected OutputStream output;

	/**
	 * Error message for empty string readding
	 */
	private static final String EmptyBufferErrorMessage = "Underlying input stream returned zero bytes";

	public Arduino(CommPortIdentifier commPort) {
		if (commPort == null) throw new NullPointerException("No COM port provided");
		this.commPort = commPort;
	}
	
	public CommPortIdentifier getComPort() {
		return commPort;
	}
	
	public SerialPort getSerialPort() {
		return serialPort;
	}
	
	public synchronized boolean isOpen() {
		return serialPort != null;
	}
	
	public int getDataRate() {
		return dataRate;
	}

	public void setDataRate(int dataRate) {
		this.dataRate = dataRate;
	}

	public BufferedReader getInputStream() {
		return input;
	}

	public OutputStream getOutputStream() {
		return output;
	}
	
	public int getOpenTimeOut() {
		return openTimeOut;
	}

	public void setOpenTimeOut(int openTimeOut) {
		this.openTimeOut = openTimeOut;
	}

	public synchronized void open() throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) commPort.open(Arduino.class.getName(), openTimeOut);
			
			// set port parameters
			serialPort.setSerialPortParams(dataRate,
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
		CommPortIdentifier id = getPortIdentifiers().findFirst().orElse(null);
		if (id == null) return null;
		return new Arduino(id);
	}
	
	public boolean write(String data) {
		try {
			writeUnsafe(data);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public void writeUnsafe(String data) throws IOException {
		output.write(data.getBytes());
		output.flush();
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
				// Normal
			}
			else {
				notify(OnReceiveError.class, e);
			}
		}
		catch (Throwable e) {
			notify(OnReceiveError.class, e);
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
	
	@FunctionalInterface
	public static interface OnReceiveError {
		void onReceiveError(Exception ex);
	}

}
