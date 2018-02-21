package fr.evolya.javatoolkit.iot.arduilink;

public class ArduilinkEvents {
	
	@FunctionalInterface
	public static interface OnLinkEstablished {
		void onLinkEstablished(Node node);
	}
	
	@FunctionalInterface
	public static interface OnLinkInvalidated {
		void onLinkInvalidated();
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
