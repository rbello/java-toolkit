package fr.evolya.javatoolkit.cli.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import fr.evolya.javatoolkit.cli.v2.TextShell.BooleanReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.FloatReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.InputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.IntReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.LineReader;
import fr.evolya.javatoolkit.cli.v2.TextShell.Observable;
import fr.evolya.javatoolkit.cli.v2.TextShell.OutputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.PromptOutputStream;
import fr.evolya.javatoolkit.cli.v2.TextShell.Reader;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.code.utils.StringUtils;

public class SystemInputOutputStream implements OutputStream, InputStream, PromptOutputStream {

	protected BufferedReader in;
	protected PrintStream out;
	protected PrintStream err;
	private boolean readding;
	private Observable target;

	/**
	 * Constructeur qui utilise par défaut les entrées/sorties standards.
	 */
	public SystemInputOutputStream() {
		this(System.out, System.err, System.in);
	}

	/**
	 * Constructeur en surchargant les entrées/sorties.
	 * 
	 * @param out
	 * @param err
	 * @param in
	 */
	public SystemInputOutputStream(java.io.PrintStream out, java.io.PrintStream err, java.io.InputStream in) {
		this.out = out;
		this.err = err;
		this.in  = new BufferedReader(new InputStreamReader(in));
	}

	@Override
	public OutputStream print(String str) {
		return print(str, false);
	}

	@Override
	public OutputStream println(String str) {
		return println(str, false);
	}

	@Override
	public OutputStream print(String str, boolean error) {
		(error ? err : out).print(str);
		return this;
	}

	@Override
	public OutputStream println(String str, boolean error) {
		return print(str + StringUtils.NL_CHAR, error);
	}

	@Override
	public String readLine() {
		try {
			readding = true;
			String line = target.notifyLineRead(in.readLine());
			target.notifyEnter();
			return line;
		} catch (IOException e) {
			return null;
		}
		finally {
			readding = false;
		}
	}

	@Override
	public Integer readInt() {
		return IntReader.parse(readLine());
	}

	@Override
	public Float readFloat() {
		return Float.parseFloat(readLine());
	}

	@Override
	public Boolean readBool() {
		return BooleanReader.parse(readLine());
	}

	@Override
	public InputStream read(Reader<?> reader) {
		return TextShell.ShellUtils.invokeAsynch(this, reader);
	}
	
	@Override
	@AsynchOperation
	public InputStream read(LineReader reader) {
		return TextShell.ShellUtils.invokeAsynch(this, reader);
	}

	@Override
	@AsynchOperation
	public InputStream read(IntReader reader) {
		return TextShell.ShellUtils.invokeAsynch(this, reader);
	}

	@Override
	@AsynchOperation
	public InputStream read(FloatReader reader) {
		return TextShell.ShellUtils.invokeAsynch(this, reader);
	}

	@Override
	@AsynchOperation
	public InputStream read(BooleanReader reader) {
		return TextShell.ShellUtils.invokeAsynch(this, reader);
	}

	public BufferedReader getInputStream() {
		return in;
	}

	public PrintStream getOutputStream() {
		return out;
	}

	public PrintStream getErrorStream() {
		return err;
	}

	@Override
	public PromptOutputStream prompt() {
		getOutputStream().print("$ ");
		return this;
	}
	
	@Override
	public String toString() {
		return "std io";
	}

	@Override
	public boolean isReadding() {
		return readding;
	}

	@Override
	public Reader<?> getCurrentReader() {
		throw new IllegalAccessError();
	}

	@Override
	public void setEventTarget(Observable target) {
		this.target = target;
	}

}
