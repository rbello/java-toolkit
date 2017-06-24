package fr.evolya.javatoolkit.cli.v2;

import java.util.Arrays;

import fr.evolya.javatoolkit.cli.v2.TextShell.CharInputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.RichTextOutputStream;
import fr.evolya.javatoolkit.code.MutableMap;

public class AdvancedShell<IN extends CharInputStream, OUT extends RichTextOutputStream<?>>
	extends BasicShell<IN, OUT> {

	protected MutableMap config;
	
	public AdvancedShell() {
		super();
	}
	
	public AdvancedShell(IN in, OUT out) {
		super(in, out);
	}
	
	protected void initConfig() {
		config = new MutableMap();
		config.set("YesKey", Arrays.asList(new Character[] { 'Y', 'y', 'O', 'o' }));
		config.set("NoKey",  Arrays.asList(new Character[] { 'N', 'n' }));
	}
	
}