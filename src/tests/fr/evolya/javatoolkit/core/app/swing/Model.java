package fr.evolya.javatoolkit.core.app.swing;

import java.util.Date;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.code.annotations.Inject;

public class Model implements Runnable {

	private Date timeValue;

	@Inject public App app;
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			this.timeValue = new Date();
			app.notify(ModelChanged.class, this.timeValue);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	public Date getTimeValue() {
		return this.timeValue;
	}
	
	public static interface ModelChanged {
		public void onTimeChanged(Date dt);
	}
	
}
