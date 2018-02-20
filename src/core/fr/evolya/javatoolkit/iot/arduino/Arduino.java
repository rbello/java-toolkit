package fr.evolya.javatoolkit.iot.arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
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
 *  
 *  Usage:
 *  	
 */
public class Arduino extends Observable
	implements SerialPortEventListener, AutoCloseable, Runnable {
	
	public static final Logger LOGGER = Logs.getLogger("Arduino");
	
	/**
	 * Milliseconds to block while waiting for the port to open
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
	 * Delay before retrying the open the connection.
	 */
	private long openRetryDelay = 3000;

	/**
	 * Is arduino currently connected
	 */
	private boolean connected = false;
	
	/**
	 * Error message for empty string readding
	 */
	private static final String EmptyBufferErrorMessage = "Underlying input stream returned zero bytes";

	/**
	 * Delay to retry a connection attempt.
	 */
	public static long RECONNECT_SLEEP_DELAY = 1000;
	
	/**
	 * Create an Aduino bound to the first COM port available.
	 */
	public Arduino() {
		this(null);
	}
	
	/**
	 * Create an Arduino bound to the given COM port.
	 * 
	 * @param commPort The given COM port.
	 */
	public Arduino(CommPortIdentifier commPort) {
		this.commPort = commPort;
		this.thread = new Thread(this);
	}
	
	/**
	 * Returns TRUE if this Arduino is bound to a COM port. 
	 */
	public boolean isBound() {
		return commPort != null;
	}

	/**
	 * Returns TRUE if the serial port is open.
	 */
//	public synchronized boolean isOpen() {
//		return serialPort != null;
//	}
	
	/**
	 * Returns TRUE is the Arduino is currently connected.
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Obtains the CommPortIdentifier object corresponding to a port that has already been opened by the application.
	 */
	public CommPortIdentifier getComPort() {
		return commPort;
	}
	
	/**
	 * Get the serial connection created when Arduino was connected.
	 */
	public SerialPort getSerialPort() {
		return serialPort;
	}

	/**
	 * Change the baudrate used by the Arduino's connection.
	 * Default is 9600.
	 * 
	 * If the baudrate passed in by the application is unsupported by the driver, the driver
	 * will throw an UnsupportedCommOperationException.
	 */
	public void setDataRate(int dataRate) {
		// TODO Throw exception if already open 
		this.dataRate = dataRate;
	}
	
	/**
	 * Return the current baudrate configured for this connection.
	 */
	public int getDataRate() {
		return dataRate;
	}
	
	public void setOpenTimeOut(int openTimeOut) {
		this.openTimeOut = openTimeOut;
	}
	
	/**
	 * Time in milliseconds to block waiting for port open. 
	 */
	public int getOpenTimeOut() {
		return openTimeOut;
	}

	/**
	 * Return the input stream used for readding the serial connection with the Arduino.
	 */
	public BufferedReader getInputStream() {
		return input;
	}

	/**
	 * Return the input stream used for writing to the serial connection with the Arduino.
	 */
	public OutputStream getOutputStream() {
		return output;
	}
	
	/**
	 * Start the Arduino in his dedicated thread, then open the serial port
	 * and begin the connection process. Please register to events before
	 * invoking this method.
	 */
	@AsynchOperation
	public Arduino start() {
		// Log
		LOGGER.log(Logs.INFO, "Starting arduino " + Integer.toHexString(hashCode()));
		// Start the thread watching for COM port ready to use.
		this.thread.start();
		return this;
	}
	
	/**
	 * Do NOT run this method manually ! It will be invoked by thread start.
	 */
	@Override
	public void run() {
		
		while (!Thread.interrupted()) {
	
			// Not connected, try to connect
			if (!isConnected()) {
				
				boolean bound = (commPort != null);
				
				// No COM port was already specified
				if (!bound) {
					// Search for an available COM port
					commPort = getFirstPortAvailable();
					// Log
					LOGGER.log(Logs.DEBUG, "No COM port already selected, searching for available port...");
					// Not found
					if (commPort == null) {
						LOGGER.log(Logs.DEBUG, "No port available, retry in " + (openRetryDelay/1000) + "seconds...");
						try {
							Thread.sleep(openRetryDelay);
							continue;
						}
						catch (InterruptedException e) {
							Thread.interrupted();
							return;
						}
					}
				}
			
				try {
					// Open the connection...
					open();
					// Successful
					LOGGER.log(Logs.INFO, "Arduino " + Integer.toHexString(hashCode()) + " is connected to: " + commPort.getName());
					// Exit thread
					thread = null;
					return;
				}
				catch (Throwable ex) {
					LOGGER.log(Logs.INFO, "Unable to connect " + commPort.getName() + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
					if (!bound) {
						commPort = null;
					}
				}
				
			}
		}
	}
	
	/**
	 * Open the current commPort.
	 * 
	 * Multiples exceptions can be thrown here and wrapped in an ArduinoException :
	 * 	- PortInUseException if the port is in use by some other application that is not willing to relinquish ownership
	 *  - UnsupportedCommOperationException if the serial port parameters are not supported by the driver 
	 *  - TooManyListenersException if something wrong happens with serial connection listeners 
	 *  - IOException in general cases of I/O errors
	 * 
	 * @throws NullPointerException If the arduino isn't bound to any COMM port.
	 * @throws ArduinoException If a sublayer exception was thrown.
	 */
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
			
			notify(OnConnected.class, this);

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

	/**
	 * Return a list of available COM ports.
	 * 
	 * @throws RuntimeException If RXTX is not properly installed
	 */
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
	
	/**
	 * Get an Arduino instance bound to a given port name, or null if not available.
	 */
	public static Arduino getByName(String portName) {
		CommPortIdentifier id = getPortIdentifiers()
			.filter(port -> portName.toUpperCase().equals(port.getName().toUpperCase()))
			.findFirst()
			.orElse(null);
		if (id == null) return null;
		return getByPort(id);
	}
	
	/**
	 * Get an arduino instance bound to a specific COM port given.
	 */
	public static Arduino getByPort(CommPortIdentifier port) {
		if (port == null) return null;
		return new Arduino(port);
	}
	
	/**
	 * Get an arduino instance bound to the firt COM port available.
	 */
	public static Arduino getFirst() {
		return getByPort(getFirstPortAvailable());
	}
	
	/**
	 * Get the firt COM port available, or null if none are available.
	 */
	protected static CommPortIdentifier getFirstPortAvailable() {
		return getPortIdentifiers().findFirst().orElse(null);
	}
	
	/**
	 * Write some data to the serial connection. Return FALSE if the operation cannot be
	 * realized successfuly.
	 */
	public boolean write(String data) {
		try {
			writeUnsafe(data);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Write some data to the serial connection. Throws IOException if the operation cannot be
	 * realized successfuly.
	 * 
	 * @param data
	 * @throws IOException If serial is not connected, baudrate not properly defined, etc...
	 */
	public void writeUnsafe(String data) throws IOException {
		if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
			LOGGER.log(Logs.DEBUG_FINE, "Write to serial: " + data);
		}
		output.write(data.getBytes());
		output.flush();
	}
	
	/**
	 * Do NOT run this method manually ! It will be invoked by serial events.
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
	
		// Only for available data events
		if (oEvent.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
			notify(OnSerialEvent.class, oEvent);
			return;
		}
		
		// Try to read a datagram
		try {
			String inputLine = input.readLine();
			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
				LOGGER.log(Logs.DEBUG_FINE, "Read from serial: " + inputLine);
			}
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
				if (LOGGER.isLoggable(Logs.DEBUG)) {
					LOGGER.log(Logs.DEBUG, "IOException in serialEvent(): " + e.getMessage());
				}
				lastException = e;
				notify(OnReceiveError.class, e);
			}
		}

	}

	/**
	 * Internal method to close the current connection and run the thread
	 * watching for available 
	 */
	private void closeAndContinue() {
		try {
			close();
		}
		catch (Exception ex) {
			// Nothing to do...
		}
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public synchronized void close() throws Exception {
		Exception ex = null;
		if (serialPort != null) {
			notify(OnDisconnected.class, this, null);
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
		//commPort = null; // Don't reset the COM port bound
		input = null;
		output = null;
		connected = false;
		serialPort = null;
		if (ex != null) throw ex;
	}
	
	public void dispose() {
		try {
			close();
		} catch (Exception e) { }
		removeAllListeners();
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}
	
	public static class ArduinoException extends Exception {
		private static final long serialVersionUID = 8331013725124493915L;
		private ArduinoException(Exception ex) {
			super(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
		}
	}

}
