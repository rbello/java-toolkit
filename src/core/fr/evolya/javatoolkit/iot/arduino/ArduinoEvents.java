package fr.evolya.javatoolkit.iot.arduino;

import gnu.io.SerialPortEvent;

public class ArduinoEvents {

	@FunctionalInterface
	public static interface OnConnected {
		void onConnected(Arduino uno);
	}
	
	@FunctionalInterface
	public static interface OnDisconnected {
		void onDisconnected(Arduino uno, Exception error);
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
