package fr.evolya.javatoolkit.iot.arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.fi.EventProvider;
import fr.evolya.javatoolkit.events.fi.Observable;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnConnected;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnDisconnected;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnRawDataReceived;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnReceiveError;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnSerialEvent;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

@EventProvider({
	// When an arduino was connected
	OnConnected.class,
	// When the current arduino is disconnected
	OnDisconnected.class,
	// When a data was received
	OnRawDataReceived.class,
	// When other serial events occures
	OnSerialEvent.class,
	// When I/O error was received between open and close
	OnReceiveError.class
})

/**
 *  Link to an Arduino.
 *  Works with RXTX library, be sure to install it before use.
 */
public class Arduino extends Observable
	implements SerialPortEventListener, AutoCloseable, Runnable {
	
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
	 * Last exception thrown
	 */
	private Throwable lastException = null;

	/**
	 * Link maintainer thread
	 */
	private Thread thread;

	/**
	 * Is arduino currently connected
	 */
	private boolean connected = false;
	
	/**
	 * Error message for empty string readding
	 */
	private static final String EmptyBufferErrorMessage = "Underlying input stream returned zero bytes";

	public Arduino() {
		this(null);
	}
	
	public Arduino(CommPortIdentifier commPort) {
		this.commPort = commPort;
		this.thread = new Thread(this);
	}
	
	public void start() {
		this.thread.start();
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

	public synchronized void open() throws ArduinoException {
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
			
			serialPort.addEventListener(this);
			
			connected  = true;
			
			notify(OnConnected.class, commPort);

			serialPort.notifyOnDataAvailable(true);
			
		}
		catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException ex) {
			serialPort = null;
			input = null;
			output = null;
			connected = false;
			throw new ArduinoException(ex);
		}
	}

	public static Stream<CommPortIdentifier> getPortIdentifiers() {
		try {
			return Utils
					.list(CommPortIdentifier.getPortIdentifiers(), CommPortIdentifier.class)
					.stream()
					.filter(port -> port.getPortType() == CommPortIdentifier.PORT_SERIAL);
		}
		catch (UnsatisfiedLinkError e) {
			throw new RuntimeException("RXTX library is not installed: no rxtxSerial DLL or SO in java.library.path", e);
		}
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
		return getByPort(getFirstPortAvailable());
	}
	
	protected static CommPortIdentifier getFirstPortAvailable() {
		return getPortIdentifiers().findFirst().orElse(null);
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
		catch (Throwable e) {
			if (e.getMessage().equals(EmptyBufferErrorMessage)) {
				// Normal
			}
			else {
				// Deux exceptions identiques d'affilée
				if (lastException != null && lastException.getClass() == e.getClass()
						&& lastException.getMessage().equals(e.getMessage())) {
					
					closeAndContinue();
					return;
				}
				lastException = e;
				notify(OnReceiveError.class, e);
			}
		}

	}

	private void closeAndContinue() {
		try {
			close();
		}
		catch (Exception e1) {
			// TODO
			System.err.println("EXCEPTION WHILE CLOSING ARDUINO");
			e1.printStackTrace();
		}
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public synchronized void close() throws Exception {
		Exception ex = null;
		if (serialPort != null) {
			notify(OnDisconnected.class, commPort, null);
			try {
				serialPort.removeEventListener();
				input.close();
				output.close();
				serialPort.close();
			}
			catch (Exception e) {
				ex = e;
			}
		}
		commPort = null;
		input = null;
		output = null;
		connected = false;
		serialPort = null;
		if (ex != null) throw ex;
	}
	
	public void dispose() {
		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		removeAllListeners();
	}
	
	@Override
	public void run() {
		
		while (!Thread.interrupted()) {
	
			// Not connected, try to connect
			if (!isConnected()) {
				//System.out.println("Try to connect arduino...");
				commPort = getFirstPortAvailable();
				// Not found
				if (commPort == null) {
					int sleep = 5;
					//System.out.println("Nothing, try in " + sleep + " seconds...");
					try {
						Thread.sleep(sleep * 1000);
						continue;
					}
					catch (InterruptedException e) {
						Thread.interrupted();
						return;
					}
				}
				// Port found
				else {
					try {
						open();
						// Exit thread
						thread = null;
						return;
					}
					catch (Throwable ex) {
						//System.out.println("Error, unable to connect");
					}
				}
			}
		
		}
		
	}

	/**
	 * Returns TRUE is the Arduino is currently connected.
	 */
	public boolean isConnected() {
		return connected;
	}

	
	/**
	 * Returns TRUE if this Arduino has a bound COM port. 
	 */
	public boolean isBound() {
		return commPort != null;
	}
	
	public static class ArduinoException extends Exception {
		private static final long serialVersionUID = 8331013725124493915L;
		public ArduinoException(Exception ex) {
			super(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
		}
	}

}
