package fr.evolya.javatoolkit.transactional;

import fr.evolya.javatoolkit.events.basic.Event;

public class TransactionEvent extends Event {
	
	public static class CommittedEvent extends TransactionEvent {
		
	}
	
	public static class ChangedEvent extends TransactionEvent {
		
	}

}
