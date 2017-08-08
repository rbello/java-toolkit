package fr.evolya.javatoolkit.time;

import fr.evolya.javatoolkit.events.basic.Listener2;

public class RunnableTimer extends MonoThreadTimer {

	public RunnableTimer(long frequency, boolean raiseExceptions, final Runnable toRun) {
		
		// Construction du parent : on indique si on doit lever les exceptions
		super(raiseExceptions);
		
		// On configure la fréquence
		setFrequency(frequency);
		
		// Et on inscrit un listener 
		onElapsed(new Listener2<TimerEvent> () {
			public void notifyEvent(TimerEvent event) {
				toRun.run();
			}
		});
		
	}
	
}
