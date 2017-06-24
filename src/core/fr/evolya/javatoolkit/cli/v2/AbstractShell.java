package fr.evolya.javatoolkit.cli.v2;

import java.util.ArrayList;
import java.util.List;

import fr.evolya.javatoolkit.cli.v2.TextShell.*;
import fr.evolya.javatoolkit.code.MutableMap;

public abstract class AbstractShell<IN extends InputStream, OUT extends OutputStream>
	implements TextShell<IN, OUT>, TextShell.Observable {

	protected MutableMap config;
	private IN in;
	private OUT out;
	private boolean ready;
	private Runnable onReady;
	private List<TextShell.Listener> listeners;
	
	public AbstractShell() {
		this.config = new MutableMap();
		this.listeners = new ArrayList<>();
	}
	
	public AbstractShell(IN in, OUT out) {
		this();
		this.in = in;
		this.out = out;
		in.setEventTarget(this);
		out.setEventTarget(this);
	}
	
	@Override
	public void addListener(TextShell.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public MutableMap getConfig() {
		return config;
	}

	@Override
	public IN getInputStream() {
		return in;
	}
	
	public void setInputStream(IN stream) {
		in = stream;
	}

	@Override
	public OUT getOutputStream() {
		return out;
	}
	
	public void setOutputStream(OUT stream) {
		out = stream;
	}
	
	public void onReady(Runnable job) {
		if (ready) {
			job.run();
		}
		else {
			onReady = job;
		}
	}
	
	public boolean isReady() {
		return ready;
	}
	
	protected void setReady() {
		ready = true;
		if (onReady != null) {
			onReady.run();
		}
	}
	
	@Override
	public String notifyLineRead(String line) {
		listeners.forEach((l) -> l.onLineRead(line));
		return line;
	}
	
	@Override
	public void notifyEnter() {
		listeners.forEach((l) -> l.onEnterInput());
	}

}
