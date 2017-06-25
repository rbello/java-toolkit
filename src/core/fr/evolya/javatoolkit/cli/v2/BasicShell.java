package fr.evolya.javatoolkit.cli.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.evolya.javatoolkit.cli.v2.TextShell.InputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.PromptOutputStream;
import fr.evolya.javatoolkit.code.annotations.ToOverride;

public class BasicShell<IN extends InputStream, OUT extends PromptOutputStream>
	extends AbstractShell<IN, OUT> implements Runnable {

	private Map<String, Command> cmds;

	public BasicShell() {
		this(null, null);
	}
	
	public BasicShell(IN in, OUT out) {
		super(in, out);
		this.cmds = new HashMap<String, Command>();
		initCommands();
		onReady(this);
	}
	
	protected void initCommands() {
		addCommand("echo", new Command() {
			@Override
			public void handle(List<String> args, String plain, String cmd) {
				getOutputStream().println(String.join(" ", args));
			}
		});
		addCommand("help", new Command() {
			@Override
			public void handle(List<String> args, String plain, String cmd) {
				getOutputStream().println("Available commands: " + String.join(", ", cmds.keySet()));
			}
		});
	}

	public static abstract class Command {
		public static List<String> parse(String input) {
			return TextShell.ShellUtils.split(input.trim());
		}
		public abstract void handle(List<String> args, String plain, String cmd);
	}

	@ToOverride
	public String getPromptText() {
		return "anonymous@local $ ";
	}

	public void addCommand(String command, Command handler) {
		cmds.put(command.toLowerCase(), handler);
	}

	/**
	 * Initialisation du shell
	 */
	@Override
	public void run() {
		
		// On affiche un prompt
		getOutputStream().prompt();
		
		Reader<String> reader = new Reader<String>() {
			@Override
			public void read(String value) {
				
				// On execute la commande
				try {
					handle(value);
				}
				
				// Traitement des erreurs
				catch (Exception ex) {
					getOutputStream().println("Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(), true);
					ex.printStackTrace();
				}
				
				// On réaffiche le prompte
				getOutputStream().prompt();
				
				// On repositionne ce reader
				getInputStream().read(this);
				
			}

			@Override
			public void input(String input) {
				// Simple redirection
				read(input);
			}

		};
		
		// On se positionne comme reader
		getInputStream().read(reader);
		
	}

	protected Boolean handle(String line) throws Exception {
		if (line == null) {
			return true;
		}
		List<String> tokens = Command.parse(line);
		if (tokens.size() == 0) return null;
		String cmd = tokens.get(0).toLowerCase();
		if (cmds.containsKey(cmd)) {
			tokens.remove(0);
			cmds.get(cmd).handle(tokens, line, cmd);
			return true;
		}
		else {
			getErrorStream().println("Command not found: " + cmd, true);
			return false;
		}
	}

	public ErrorOutputStream getErrorStream() {
		return getOutputStream();
	}

	@Override
	public void start() {
		setReady();
	}

	public void debug() {
		getOutputStream().println("Shell: " + getClass().getSimpleName());
		getOutputStream().println("Input: " + getInputStream());
		getOutputStream().println("Output: " + getOutputStream());
	}

}
