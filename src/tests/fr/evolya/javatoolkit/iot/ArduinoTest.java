package fr.evolya.javatoolkit.iot;

import fr.evolya.javatoolkit.iot.arduino.Arduino;

public class ArduinoTest {

	public static void main(String[] args) {
		
		System.out.println("COM port identifiers:");
		Arduino.getPortIdentifiers().forEach((identifier) -> {
			System.out.println(identifier.getName());
		});
		
	}
	
}
