package fr.evolya.javatoolkit.code.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import fr.evolya.javatoolkit.code.annotations.Bug;

/**
 * Toolkit de méthodes PHP.
 */
public final class Utils {

	

	/***
	 * Fusionne les éléments d'un tableau en une chaîne
	 * 
	 * @param delim
	 *            : la chaîne de séparation
	 * @param args
	 *            : la tableau
	 * @return la chaîne fusionnée
	 */
	public static String implode(String delim, String[] args) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0, l = args.length; i < l; i++) {
			if (i > 0)
				sb.append(delim);
			sb.append(args[i]);
		}
		return sb.toString();
	}

	/***
	 * Fusionne les éléments d'un tableau en une chaîne
	 * 
	 * @param delim
	 *            : la chaîne de séparation
	 * @param args
	 *            : la tableau
	 * @return la chaîne fusionnée
	 */
	public static String implode(String delim, Collection<String> args) {
		final StringBuffer sb = new StringBuffer();
		final Iterator<String> it = args.iterator();
		int i = 0;
		while (it.hasNext()) {
			if (i++ > 0)
				sb.append(delim);
			sb.append(it.next());
		}
		return sb.toString();
	}

	public static Date toGMT(Date date, TimeZone tz) {
		if (date == null || tz == null)
			return null;
		Date ret = new Date(date.getTime() - tz.getRawOffset());
		// if we are now in DST, back off by the delta. Note that we are
		// checking
		// the GMT date, this is the KEY.
		if (tz.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before
			// the change for PDT)
			if (tz.inDaylightTime(dstDate)) {
				ret = dstDate;
			}
		}
		return ret;
	}

	public static Date toTimeZone(Date date, TimeZone tz) {
		if (date == null || tz == null)
			return null;
		SimpleDateFormat sdfgmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdfgmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat sdfmad = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdfmad.setTimeZone(tz);
		try {
			return sdfgmt.parse(sdfmad.format(date));
		} catch (ParseException e) {
			return null;
		}
	}

	public static String clean_path(String path) {
		while (path.contains("//"))
			path = path.replace("//", StringUtils.SLASH);
		return path;
	}

	public static String dirname(String path) {
		if (path == null)
			return StringUtils.SLASH;
		if (path.equals(StringUtils.EMPTYSTRING))
			return StringUtils.SLASH;
		if (path.equals(StringUtils.SLASH))
			return StringUtils.SLASH;
		String[] tokens = path.split(StringUtils.SLASH);
		StringBuilder sb = new StringBuilder();
		for (String s : Arrays.copyOfRange(tokens, 1, tokens.length - 1)) {
			sb.append(StringUtils.SLASH);
			sb.append(s);
		}
		if (sb.length() == 0)
			return StringUtils.SLASH;
		return sb.toString();
	}

	public static String get_extension(String path) {
		String extension = "";
		int i = path.lastIndexOf('.');
		int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (i > p) {
			extension = path.substring(i + 1);
		}
		return extension;
	}

	public static Field getFieldByName(Class<?> classe, String name) throws SecurityException {
		// On cherche dans cette classe
		try {
			return classe.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
		}
		// Puis dans celle du dessus
		if (classe.getSuperclass() != null) {
			return getFieldByName(classe.getSuperclass(), name);
		}
		// On a rien trouvé
		return null;
	}

	public static boolean filePutContents(File file, String data) {
		BufferedWriter buffer = null;
		try {
			buffer = new BufferedWriter(new FileWriter(file, true));
			buffer.write(data);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
		finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) { }
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Bug
	public static <E> E[] toArray(List<E> args) {
		Object[] out = new Object[args.size()];
		int i = 0;
		for (E e : args)
			out[i++] = e;
		return (E[]) out;
	}

	public static final String readFirstLine(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String inputLine = null;
		try {
			while ((inputLine = in.readLine()) != null)
				break;
		} finally {
			in.close();
		}
		return inputLine;
	}

	public static final String readAll(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();
		String buffer = null;
		try {
			while ((buffer = in.readLine()) != null) {
				sb.append(buffer);
				sb.append(StringUtils.NL_CHAR);
			}
		} finally {
			in.close();
		}
		return sb.toString();
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toUpperCase();
		return os.contains("WINDOWS");
	}

	public static boolean isLinux() {
		String os = System.getProperty("os.name").toUpperCase();
		return os.contains("LINUX");
	}

//	public static boolean isRunningAsAdmin() {
//		// Windows
//		if (isWindows()) {
//			return isUserWindowsAdmin();
//		}
//		// Linux
//		// TODO
//		return false;
//	}

//	protected static boolean isUserWindowsAdmin() {
//		String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
//		for (String group : groups) {
//			if (group.equals("S-1-5-32-544"))
//				return true;
//		}
//		return false;
//	}

//	protected interface Shell32 extends StdCallLibrary {
//		boolean IsUserAnAdmin() throws LastErrorException;
//	}
//
//	protected static boolean isUserWindowsAdmin2() {
//		Shell32 shell = (Shell32) Native.loadLibrary("shell32", Shell32.class);
//		return shell.IsUserAnAdmin();
//	}

	public static boolean isPrintableChar( char c ) {
	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
	    return (!Character.isISOControl(c)) && c != 65535 && block != null && block != Character.UnicodeBlock.SPECIALS;
	}

}
