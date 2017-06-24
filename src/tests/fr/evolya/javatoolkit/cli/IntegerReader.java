package fr.evolya.javatoolkit.cli;

import fr.evolya.javatoolkit.cli.v2.TextShell;
import fr.evolya.javatoolkit.cli.v2.TextShell.Reader;

public abstract class IntegerReader implements TextShell.Reader<Integer> {
	
	private int value;

	@Override
	public void input(String input) {
		this.value = Integer.parseInt(input);
	}
	
	public int getValue() {
		return this.value;
	}

}
