package fr.evolya.javatoolkit.app.features;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.event.ApplicationStarting;
import fr.evolya.javatoolkit.app.event.ApplicationStopping;
import fr.evolya.javatoolkit.app.event.ApplicationWakeup;
import fr.evolya.javatoolkit.app.event.BeforeApplicationStarted;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;

public class SingletonAppBehavior {

	private static ServerSocket SOCKET;

	private static App APP;

	@BindOnEvent(BeforeApplicationStarted.class)
	public static void checkIfInstanceExists(App app) {
		APP = app;
		long ipcPort = app.get(AppConfiguration.class).getPropertyInt("App.Singleton.Port", 50);
		long soTimeout = app.get(AppConfiguration.class).getPropertyInt("App.Singleton.SoTimeout", 1000);
		try {
			SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", (int)ipcPort);
			SOCKET = new ServerSocket();
			SOCKET.bind(socketAddress);
			SOCKET.setSoTimeout((int)soTimeout);
		}
		catch (Exception e1) {
			SOCKET = null;
			// Impossible d'ouvrir le socket, on teste de connecter le port.
			try {
				Socket socket = new Socket("127.0.0.1", (int)ipcPort);
				App.LOGGER.log(Logs.INFO, "App is already started, wake up other instance...");
				socket.close();
				System.exit(0);
			}
			catch (Exception e2) {
				App.LOGGER.log(Logs.WARNING, "Unable to setup SingletonAppBehavior", e2);
			}
		}
	}

	private Thread thread;
	
	@BindOnEvent(ApplicationStarting.class)
	public void handleServer() {
		if (SOCKET == null) return;
		App.LOGGER.log(Logs.INFO, "App is started with Singleton behavior");
		thread = new Thread(() -> {
			while (!Thread.interrupted()) {
				try {
					Socket socket = SOCKET.accept();
					PrintWriter out = new PrintWriter(socket.getOutputStream());
					out.println("");
					out.flush();
					out.close();
					socket.close();
					APP.notify(ApplicationWakeup.class);
				}
				catch (SocketTimeoutException ex) { }
				catch (IOException e) {
					App.LOGGER.log(Logs.WARNING, "Failure in SingletonAppBehavior", e);
				}
			}
		});
		thread.start();
	}
	
	@BindOnEvent(ApplicationStopping.class)
	public void shutdown() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
		if (SOCKET != null) {
			try {
				SOCKET.close();
			} catch (Exception e) { }
			SOCKET = null;
		}
	}
	
}
