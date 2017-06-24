package fr.evolya.javatoolkit.gui.swing.console.v2;



import fr.evolya.javatoolkit.cli.v2.TextShell.BooleanReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.CharInputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.CharReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.FloatReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.IntReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.LineReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.Observable;
import fr.evolya.javatoolkit.cli.v2.TextShell.Reader;
import fr.evolya.javatoolkit.code.MutableMap;

public class TextPaneInputStream implements CharInputStream {

	StringBuffer inputBuffer = new StringBuffer();
	Reader<?> currentReader = null;
	private MutableMap config;
	
	/**
	 * Constructeur
	 */
	public TextPaneInputStream(MutableMap config) {
		this.config = config;
	}
	
	public Reader<?> reset() {
		inputBuffer.setLength(0);
		Reader<?> reader = currentReader;
		currentReader = null;
		return reader;
	}

	public StringBuffer getBuffer() {
		return inputBuffer;
	}

	private TextPaneInputStream setReader(Reader<?> reader) {
		System.out.println("Readding: " + reader);
		if (currentReader != null) {
			// TODO
		}
		currentReader = reader;
		return this;
	}
	
	@Override
	public TextPaneInputStream read(Reader<?> reader) {
		return setReader(reader);
	}

	@Override
	public TextPaneInputStream read(LineReader reader) {
		return setReader(reader);
	}

	@Override
	public TextPaneInputStream read(IntReader reader) {
		return setReader(reader);
	}

	@Override
	public TextPaneInputStream read(FloatReader reader) {
		return setReader(reader);
	}

	@Override
	public TextPaneInputStream read(CharReader reader) {
		return setReader(reader);
	}

	@Override
	public TextPaneInputStream read(BooleanReader reader) {
		reader.setConfig(config);
		return setReader(reader);
	}

	public CharInputStream resetBuffer() {
		inputBuffer.setLength(0);
		return this;
	}
	
	@Override
	public String readLine() {
		throw new IllegalAccessError();
	}

	@Override
	public Integer readInt() {
		throw new IllegalAccessError();
	}

	@Override
	public Float readFloat() {
		throw new IllegalAccessError();
	}

	@Override
	public Boolean readBool() {
		throw new IllegalAccessError();
	}

	@Override
	public Character readChar() {
		throw new IllegalAccessError();
	}

	public boolean write(String input) {
		if (currentReader != null && !(currentReader instanceof CharReader)) {
			currentReader.input(input);
		}
		return false;
	}

	public boolean write(char input) {
		if (currentReader != null && currentReader instanceof CharReader) {
			((CharReader)currentReader).read(input);
		}
		return false;
	}

	@Override
	public boolean isReadding() {
		return currentReader != null;
	}

	@Override
	public Reader<?> getCurrentReader() {
		return currentReader;
	}

	@Override
	public void setEventTarget(Observable target) {
		// TODO Auto-generated method stub
		
	}

}
