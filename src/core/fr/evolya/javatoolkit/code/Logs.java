/*
 * Framework Inca
 * 
 *  Copyright (C) 2008 Interval
 *  Use is subject to license terms.
 */
package fr.evolya.javatoolkit.code;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.code.annotations.TODO;

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
 * @version 1.0 13/10/08
 * @author rbello
 * 
 */
@TODO("Eviter que les methodes getLogger ne fassent le boulot à chaque fois")
public class Logs {

	public static final Level NONE			 = Level.OFF;
	public static final Level ALL			 = Level.ALL;
	public static final Level ERROR			 = new LLevel("ERROR", Level.SEVERE.intValue());
	public static final Level WARNING		 = new LLevel("WARNING", Level.WARNING.intValue());
	public static final Level INFO			 = new LLevel("INFO", Level.INFO.intValue()-1);
	public static final Level DEBUG			 = new LLevel("DEBUG", Level.FINE.intValue());
	public static final Level DEBUG_FINE	 = new LLevel("DEBUG_FINE", Level.FINEST.intValue());
	
	public static final Level EVENT			 = Level.FINEST;
	public static final Level EVENT_REDIRECT = Level.FINEST;
	public static final Level EVENT_BIND	 = Level.FINEST;
	public static final Level EVENT_INTERRUPT = Level.FINE;
	public static final Level EVENT_NOTIFY	 = Level.FINEST;
	
	public static DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
	public static DateFormat TIME_DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss:SSS");
	
	/**
	 * Niveau par défaut pour les loggers.
	 */
	private static Level _defaultLevel = INFO;
	
	/**
	 * Les loggers créés.
	 */
	private static List<Logger> _loggers = new ArrayList<Logger>();
	
	/**
	 * Constructeur privé, pour forcer l'utilisation des factory statiques.
	 */
	private Logs() {
	}
	
	/**
	 * Construction du logger pour une classe donnée.
	 */
	public static Logger getLogger(Class<?> loggerClass) {
		return getLogger(loggerClass.getName(), _defaultLevel);
	}

	/**
	 * Construction du logger pour un nom donnée.
	 */
	public static Logger getLogger(String loggerName) {
		return getLogger(loggerName, _defaultLevel);
	}

	/**
	 * Construction du logger pour un nom donné, envoyant se données sur un fichier donné.
	 */
	public static Logger getLogger(String loggerName, File target) throws SecurityException, IOException {
		return getLogger(loggerName, _defaultLevel, target);
	}
	
	/**
	 * Renvoyer le logger pour un nom donné.
	 */
	public static synchronized Logger getLogger(String loggerName, Level level) {
		
		// On recupère le logger
		Logger logger = Logger.getLogger(loggerName, null);
		
		// Le logger est déjà configuré, on le renvoie directement
		if (logger.getHandlers().length > 0) {
			return logger;
		}
		
		// Filtre
		DefaultLevelFilter filter = new DefaultLevelFilter(logger);

		// On configure le logger
		logger.setLevel(level);
		logger.setFilter(filter);
		
		// On ajoute un handler de console
		Handler handler = new ConsoleHandler();
		handler.setLevel(level);
		handler.setFilter(filter);
		handler.setFormatter(DEV_LOG_FORMATTER);
		logger.addHandler(handler);

		// On le sauvegarde
		_loggers.add(logger);
		
		// On renvoie le logger
		return logger;

	}

	public static synchronized Logger getLogger(String loggerName, Level level, File target)
			throws SecurityException, IOException {

		// On recup�re le logger
		Logger logger = Logger.getLogger(loggerName, null);
		
		// Le logger est d�j� configur�, on le renvoie directement
		if (logger.getHandlers().length > 0) {
			return logger;
		}
		
		// Filtre
		DefaultLevelFilter filter = new DefaultLevelFilter(logger);

		// On configure le logger
		logger.setLevel(level);
		logger.setFilter(filter);
		
		// On ajoute un handler de fichier
		Handler handler = new FileHandler(target.getAbsolutePath());
		handler.setLevel(level);
		handler.setFilter(filter);
		handler.setFormatter(FULL_LOG_FORMATTER);
		logger.addHandler(handler);
		
		// On le sauvegarde
		_loggers.add(logger);

		// On renvoie le logger, il est déjà sauvegardé
		return logger;

	}
	
	private static class DefaultLevelFilter implements Filter {
		private Logger logger;
		public DefaultLevelFilter(final Logger logger) {
			this.logger = logger;
		}
		@Override
		public boolean isLoggable(final LogRecord log) {
			return logger.getLevel().intValue() <= log.getLevel().intValue();
		}
	}

	private static Formatter FULL_LOG_FORMATTER = new Formatter() {

		@Override
		public String format(LogRecord log) {
			StringBuffer sb = new StringBuffer();

			sb.append('[');
			sb.append(log.getLevel());
			sb.append(']');

			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(log.getMillis());
			sb.append(' ');
			sb.append(DEFAULT_DATE_FORMATTER.format(date.getTime()));
			sb.append("  - ");

			sb.append(log.getLoggerName());
			sb.append('\n');

			sb.append(" FROM : ");
			sb.append(log.getSourceClassName());
			sb.append('.');
			sb.append(log.getSourceMethodName());
			sb.append("()  - Thread:");
			sb.append(log.getThreadID());
			sb.append('\n');

			sb.append("  MSG : ");
			sb.append(log.getMessage());
			sb.append('\n');

			return sb.toString();
		}

	};
	
	public static Formatter DEV_LOG_FORMATTER = new Formatter() {

		@Override
		public String format(LogRecord log) {
			StringBuffer sb = new StringBuffer();

			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(log.getMillis());
			sb.append(TIME_DATE_FORMATTER.format(date.getTime()));
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
	
	protected static final String tab(final Object obj, final int tab) {
		return tab(obj+"", tab);
	}
	
	protected static final String tab(String txt, final int tab) {
		while (txt.length() < tab) txt += ' ';
		return txt;
	}

	public static void setGlobalLevel(final Level level) {
		for (Logger logger : _loggers) {
			setLoggerLevel(logger, level);
		}
		_defaultLevel = level;
	}

	public static void setDefaultLevel(final Level level) {
		_defaultLevel = level;
	}
	
	public static class LLevel extends Level {

		private static final long serialVersionUID = 1L;

		public LLevel(String string, int intValue) {
			super(string, intValue);
		}
		
	}
	
	public static void dispose() {
		if (_loggers != null) {
			_loggers.clear();
			_loggers = null;
		}
	}

	public static void setLoggerLevel(Logger logger, Level level) {
		logger.setLevel(level);
		for (Handler handler : logger.getHandlers()) {
			handler.setLevel(level);
		}
	}

}
