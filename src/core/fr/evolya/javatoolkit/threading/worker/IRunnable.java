package fr.evolya.javatoolkit.threading.worker;

@FunctionalInterface
public interface IRunnable {
	
	public void run() throws InterruptedException;

}
