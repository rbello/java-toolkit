/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.code;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * 
 * Adapter pour utiliser les loggers du jdk
 * 
 * Basé sur java.util.logging
 * 
 * Levels de inca :
 *  ERROR
 *  WARNING
 *  INFO
 *  DEBUG
 * 
 * Levels de java.util.logging :
 *  SEVERE (highest value)
 *  WARNING
 *  INFO
 *  CONFIG
 *  FINE
 *  FINER
 *  FINEST (lowest value)
 * 
 * @version 1.0 13/10/2008
 * @version 2.0 28/08/2017
 * @author rbello
 * 
 */
public class Logs {

	public static final Level NONE			 = Level.OFF;
	public static final Level ALL			 = Level.ALL;
	public static final Level ERROR			 = new LLevel("ERROR", Level.SEVERE.intValue());
	public static final Level WARNING		 = new LLevel("WARNING", Level.WARNING.intValue());
	public static final Level INFO			 = new LLevel("INFO", Level.INFO.intValue()-1);
	public static final Level DEBUG			 = new LLevel("DEBUG", Level.FINE.intValue());
	public static final Level DEBUG_FINE	 = new LLevel("DEBUG_FINE", Level.FINEST.intValue());
	
	public static final Level EVENT			  = Level.FINEST;
	public static final Level EVENT_REDIRECT  = Level.FINEST;
	public static final Level EVENT_BIND	  = Level.FINEST;
	public static final Level EVENT_INTERRUPT = Level.FINE;
	public static final Level EVENT_NOTIFY	  = Level.FINEST;
	
	public static final DateFormat FULL_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
	public static final DateFormat TIME_DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss:SSS");
	
	private static final boolean DEBUG_MODE = false;
	
	/**
	 * Niveau par défaut pour les loggers.
	 */
	private static Level _defaultLevel;
	
	
	/**
	 * Les loggers créés.
	 */
	private static List<Logger> _loggers;
	
	/**
	 * Default output to CLI.
	 */
	private static ConsoleHandler _consoleHandler;
	
	/**
	 * Optional output to a file.
	 */
	private static FileHandler _fileHandler;
	
	/**
	 * Optional stream output.
	 */
	private static StreamHandler _streamHandler;
	
	/**
	 * Constructeur privé, pour forcer l'utilisation des factory statiques.
	 */
	private Logs() {
	}
	
	/**
	 * Constructeur statique
	 */
	static {
		
		_defaultLevel = INFO;
		_loggers = new ArrayList<Logger>();
		
		_consoleHandler = new ConsoleHandler();
		_consoleHandler.setLevel(_defaultLevel);
		_consoleHandler.setFormatter(new DefaultFormatter(TIME_DATE_FORMATTER));
		
	}
	
	/**
	 * Construction du logger pour une classe donnée.
	 */
	public static Logger getLogger(Class<?> loggerClass) {
		return getLogger(loggerClass.getName());
	}

	/**
	 * Construction du logger pour un nom donnée.
	 */
	public static Logger getLogger(String loggerName) {

		// On recupère le logger
		Logger logger = Logger.getLogger(loggerName, null);
		
		// Le logger est déjà configuré, on le renvoie directement
		if (_loggers.contains(logger)) {
			return logger;
		}
		
		if (DEBUG_MODE) System.out.println("[Logs] Create logger '" + loggerName
				+ "' with default level '" + _defaultLevel.getName() + "'");
		
		// On configure le logger
		logger.setLevel(_defaultLevel);
		
		// On ajoute un handler de console
		logger.addHandler(_consoleHandler);
		if (_fileHandler != null) {
			logger.addHandler(_fileHandler);
		}

		// On le sauvegarde
		synchronized (_loggers) {
			_loggers.add(logger);
		}
		
		// On renvoie le logger
		return logger;

	}
	
//	protected static class PassAllFilter implements Filter {
//		@Override
//		public boolean isLoggable(final LogRecord log) {
//			return true;
//		}
//	}
//	
//	protected static class LoggerLevelFilter implements Filter {
//		private Logger logger;
//		public LoggerLevelFilter(final Logger logger) {
//			this.logger = logger;
//		}
//		@Override
//		public boolean isLoggable(final LogRecord log) {
//			return logger.getLevel().intValue() <= log.getLevel().intValue();
//		}
//	}
//	
//	protected static class LevelFilter implements Filter {
//		private Level level;
//		public LevelFilter(final Level level) {
//			this.level = level;
//		}
//		@Override
//		public boolean isLoggable(final LogRecord log) {
//			return this.level.intValue() <= log.getLevel().intValue();
//		}
//		public void setLevel(Level level) {
//			this.level = level;
//		}
//	}
//	
//	protected static class DefaultLevelFilter implements Filter {
//		@Override
//		public boolean isLoggable(final LogRecord log) {
//			return _defaultLevel.intValue() <= log.getLevel().intValue();
//		}
//	}

	protected static final String tab(final Object obj, final int tab) {
		return tab(obj+"", tab);
	}
	
	protected static final String tab(String txt, final int tab) {
		while (txt.length() < tab) txt += ' ';
		return txt;
	}

	@Deprecated
	public static void setDefaultLevel(final Level level) {
	}

	public static void setGlobalLevel(final Level level) {
		
		if (_defaultLevel.equals(level)) return;
		
		if (DEBUG_MODE) System.out.println("[Logs] Change global level '" + level.getName() + "'");

		_consoleHandler.setLevel(level);
		
		if (_fileHandler != null && _fileHandler.getLevel().intValue() < level.intValue())
			return;

		_defaultLevel = level;
		
		synchronized (_loggers) {
			for (Logger logger : _loggers) {
				logger.setLevel(level);
			}
		}
	}
	
	public static void setLoggerLevel(Logger logger, Level level) {
		if (DEBUG_MODE) System.out.println("[Logs] Change individual logger level '" 
				+ logger.getName() + "' -> '" + level.getName() + "'");
		logger.setLevel(level);
	}
	
	public static class LLevel extends Level {

		private static final long serialVersionUID = 1L;

		public LLevel(String string, int intValue) {
			super(string, intValue);
		}
		
	}
	
	public static void dispose() {
		if (_loggers != null) {
			if (DEBUG_MODE) System.out.println("[Logs] Dispose loggers");
			_loggers.clear();
			_loggers = null;
		}
	}
	
	public static void setOutputStream(PrintStream stream, Level level) {
		if (_streamHandler != null) {
			throw new IllegalStateException("An output stream is already defined");
		}
		// Save target for futur loggers
		_streamHandler = new StreamHandler(stream, new DefaultFormatter(TIME_DATE_FORMATTER));
		_streamHandler.setLevel(level);
		if (DEBUG_MODE) System.out.println("[Logs] Add output stream logger '" + stream + "' -> '" + level.getName() + "'");
		addHandler(_streamHandler, level);
	}

	public static void addFileOutputHandler(String path, Level level)
			throws SecurityException, IOException {
		if (_fileHandler != null) {
			throw new IllegalStateException("A file handler is already defined");
		}
		Calendar cal = Calendar.getInstance();
		path = path.replace("%d", "" + cal.get(Calendar.DAY_OF_MONTH));
		path = path.replace("%M", "" + (cal.get(Calendar.MONTH) + 1));
		path = path.replace("%y", "" + cal.get(Calendar.YEAR));
		path = path.replace("%h", "" + cal.get(Calendar.HOUR_OF_DAY));
		path = path.replace("%m", "" + cal.get(Calendar.MINUTE));
		path = path.replace("%s", "" + cal.get(Calendar.SECOND));
		File target = new File(path);
		// Save target for futur loggers
		_fileHandler = new FileHandler(target.getAbsolutePath());
		_fileHandler.setLevel(level);
		_fileHandler.setFormatter(new DefaultFormatter(FULL_DATE_FORMATTER));
		if (DEBUG_MODE) System.out.println("[Logs] Add file logger '" + path + "' -> '" + level.getName() + "'");
		addHandler(_fileHandler, level);
	}
	
	private static void addHandler(StreamHandler handler, Level level) {
		// Add handler to all old loggers
		synchronized (_loggers) {
			for (Logger logger : _loggers) {
				logger.addHandler(handler);
			}
		}
		// Elevate global level if required
		if (level.intValue() < _defaultLevel.intValue()) {
			Level consoleLevel = _consoleHandler.getLevel();
			setGlobalLevel(level);
			_consoleHandler.setLevel(consoleLevel);
		}
	}
	
	private static class DefaultFormatter extends Formatter {

		private DateFormat df;

		public DefaultFormatter(DateFormat df) {
			this.df = df;
		}
		
		@Override
		public String format(LogRecord log) {
			StringBuffer sb = new StringBuffer();
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(log.getMillis());
			sb.append(df.format(date.getTime()));
			sb.append(" | ");
			sb.append(tab(log.getLevel().getName(), 10));
			sb.append("| ");
			sb.append(tab(log.getLoggerName(), 13));
			sb.append(" | ");
			//sb.append(tab(log.getSourceClassName()+'.'+log.getSourceMethodName()+"()", 50));
			//sb.append(" | ");
			sb.append(log.getMessage());
			sb.append('\n');
			return sb.toString();
		}

	};

	@Deprecated
	public static class IncaLogger extends Logs { }

	public static void describe(Logger logger) {
		System.out.println("Loggers:");
		System.out.println(" - " + _loggers.size() + " logger(s) created");
		System.out.println(" - Default level: " + _defaultLevel.getName());
		System.out.println("Logger: " + logger.getName());
		System.out.println(" - Level:  " + logger.getLevel());
		System.out.println(" - Filter: " + logger.getFilter());
		System.out.println("Handlers:");
		for (Handler handler : logger.getHandlers()) {
			System.out.println(" - " + handler.getClass().getSimpleName() + " level " + handler.getLevel());
		}
	}

}
