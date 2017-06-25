package fr.evolya.javatoolkit.cli.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.evolya.javatoolkit.code.MutableMap;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;

public interface TextShell<IN extends TextShell.InputStream, OUT extends TextShell.OutputStream> {

	public MutableMap getConfig();
	
	public IN getInputStream();
	public OUT getOutputStream();
	
	public void start();
	
	public void addListener(Listener listener);
	
	@FunctionalInterface
	interface KeyCatcher {
		@SuppressWarnings("rawtypes")
		public boolean handle(TextShell shell, java.awt.event.KeyEvent evt);
	}
	
	@FunctionalInterface
	interface KeyCatcher2 {
		@SuppressWarnings("rawtypes")
		public void handle(TextShell shell, KeyEvent evt);
	}
	
	/*interface ConsoleInputStream2 extends InputStream {

		public Reader<?> reset();
		
		public void next();
		
		boolean reading(char value);
		boolean reading(String value);
		
	}*/
	
	interface InputStream {
		
//		public State getInputState();
//		public void setInputState(State state);
		
		@AsynchOperation
		public InputStream read(Reader<?> reader);
		
		@AsynchOperation
		public InputStream read(LineReader reader);
		
		public String readLine();
		
		@AsynchOperation
		public InputStream read(IntReader reader);
		
		public Integer readInt();
		
		@AsynchOperation
		public InputStream read(FloatReader reader);
		
		public Float readFloat();
		
		@AsynchOperation
		public InputStream read(BooleanReader reader);
		
		public Boolean readBool();
		
		public boolean isReadding();
		
		public Reader<?> getCurrentReader();
		
		public void setEventTarget(TextShell.Observable target);
		
		
	}
	
	interface CharInputStream extends InputStream {
		
		@AsynchOperation
		public CharInputStream read(CharReader reader);
		
		public Character readChar();

		public StringBuffer getBuffer();
		
		public CharInputStream resetBuffer();
		
	}
	
	interface OutputStream {
		public void setEventTarget(TextShell.Observable target);
		
		public OutputStream print(String str);
		public OutputStream println(String str);
	}
	
	interface ErrorOutputStream extends OutputStream {
		public OutputStream print(String str, boolean error);
		public OutputStream println(String str, boolean error);
	}
	
	interface PromptOutputStream extends ErrorOutputStream {
		public PromptOutputStream prompt();
	}
	
	interface RichTextOutputStream<E> extends PromptOutputStream {
		public RichTextOutputStream<E> write(String str, E style);
	}
	
	enum State {
		INPUT_NORMAL, // Normal
		INPUT_HIDDEN, // Passwords
		INPUT_LOCKED  // Executing
	}
	
	interface Reader<T> {
		void input(String input);
		void read(T value);
	}
	
	abstract class CharReader implements Reader<Character> {
		public void input(String input) {
			read(input.charAt(0));
		}
	}
	abstract class LineReader implements Reader<String> {
		public void input(String input) {
			read(input);
		}
	}
	abstract class IntReader implements Reader<Integer> {
		public void input(String input) {
			read(parse(input));
		}
		public static Integer parse(String input) {
			try {
				return Integer.parseInt(input);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}
	}
	abstract class FloatReader implements Reader<Float> {
		public void input(String input) {
			read(parse(input));
		}
		public static Float parse(String input) {
			try {
				return Float.parseFloat(input);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}
	}
	abstract class BooleanReader implements Reader<Boolean> {
		private MutableMap config = null;
		public void input(String input) {
			read(parse(input, config));
		}
		public static Boolean parse(String input) {
			return parse(input, null);
		}
		public static Boolean parse(String input, MutableMap config) {
			if (config != null) {
				if (config.get("YesKey", List.class).contains(input)) return true;
				if (config.get("NoKey", List.class).contains(input)) return false;
				return null;
			}
			input = input.trim().toLowerCase();
			if (input.equals("1") || input.equals("y")) return true;
			if (input.equals("0") || input.equals("n")) return false;
			return null;
		}
		public void setConfig(MutableMap config) {
			this.config  = config;
		}
	}
	
	static abstract class ShellUtils {
		public static Pattern REGEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
		public static List<String> split(String input) {
			Matcher m = REGEX .matcher(input);
			List<String> tokensList = new ArrayList<String>();
			while (m.find()) tokensList.add(m.group(1));
			return tokensList;
		}
		public static <T> InputStream invokeAsynch(final InputStream in, final Reader<T> reader) {
			new Thread(new Runnable() {
				public void run() {
					// On lit une ligne, on convertit dans le format demandé, et on déclanche le reader
					//reader.read(reader.convert(in.readLine()));
					reader.input(in.readLine());
				}
			}).start();
			return in;
		}
	}
	
	interface Listener {
		void onLineRead(String line);
		void onEnterInput();
	}

	interface Observable {
		String notifyLineRead(String readLine);
		void notifyEnter();
	}

}
