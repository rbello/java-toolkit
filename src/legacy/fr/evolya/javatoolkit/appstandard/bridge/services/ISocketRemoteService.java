package fr.evolya.javatoolkit.appstandard.bridge.services;

import java.net.InetAddress;

public interface ISocketRemoteService extends IRemoteService {

	public InetAddress getServiceAddress();

	public int getServicePort();

}
