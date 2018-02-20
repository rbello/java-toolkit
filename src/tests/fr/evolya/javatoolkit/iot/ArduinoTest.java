package fr.evolya.javatoolkit.iot;

import fr.evolya.javatoolkit.iot.arduilink.Arduilink;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnLinkEstablished;
import fr.evolya.javatoolkit.iot.arduilink.ArduilinkEvents.OnLinkInvalidated;
import fr.evolya.javatoolkit.iot.arduino.Arduino;
import fr.evolya.javatoolkit.iot.arduino.ArduinoEvents.OnConnected;

public class ArduinoTest {

	public static void main(String[] args) {
		
		System.out.println("COM port identifiers:");
		Arduino.getPortIdentifiers().forEach((identifier) -> {
			System.out.println(identifier.getName());
		});
		
		new Arduino().start().when(OnConnected.class).execute((uno) -> {
			
			Arduilink lnk = new Arduilink(uno);
			
			lnk.when(OnLinkInvalidated.class).execute(() -> {
				
				try {
					if (lnk.reconnect()) {
						System.out.println("Reconnect success");
					}
					else {
						System.err.println("Reconnect useless");
					}
				}
				catch (Exception e) {
					System.err.println("Reconnect failure");
				}
				
			})
			.when(OnLinkEstablished.class).execute((node) -> {
				System.out.println("Link is established !");
			});
			
		});
		
	}
	
}
