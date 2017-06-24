package fr.evolya.javatoolkit.appstandard.bridge;

public interface IBridgeable {

	public boolean isBridged();
	
	public IBridge getBridge();
	
	public void setBridge(IBridge newBridge) throws SecurityException;

}
