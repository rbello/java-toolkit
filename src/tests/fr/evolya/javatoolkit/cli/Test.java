package fr.evolya.javatoolkit.cli;

import java.util.List;

import fr.evolya.javatoolkit.cli.v2.BasicShell;
import fr.evolya.javatoolkit.cli.v2.BasicShell.Command;
import fr.evolya.javatoolkit.cli.v2.SystemInputOutputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.PromptOutputStream;

public class Test {


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		
		EclipseTools.fixConsole();
		
		// On bind les I/O du système
		final SystemInputOutputStream io = new SystemInputOutputStream(System.out, System.err, System.in) {
			@Override
			public PromptOutputStream prompt() {
				getOutputStream().print("anonymous@local $ ");
				return this;
			}
		};
		
		// On fabrique le shell
		final BasicShell shell = new BasicShell(io, io);
		
		// On ajoute une commande
		shell.addCommand("unit", new Command() {
			@Override
			public void handle(List<String> args, String plain, String cmd) {
				io.println("Enter a numeric value: ");
				io.read(new IntegerReader() {
					@Override
					public void read(Integer value) {
						System.out.println("Read: " + value);
					}
				});
			}
		});
		
		// On affiche les infos de debug
		shell.debug();

		// On démarre le shell
		shell.start();
		
	}

}
