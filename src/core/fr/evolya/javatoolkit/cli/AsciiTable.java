package fr.evolya.javatoolkit.cli;

import java.util.LinkedList;
import java.util.List;

import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.code.utils.Utils;

public class AsciiTable {
	
	private List<Column> columns = new LinkedList<>();

	public AsciiTable(Object... columns) {
		// Check arguments
		if (columns == null || columns.length == 0) {
			throw new IllegalArgumentException("Columns list is missing");
		}
		if (columns.length % 2 != 0) {
			throw new IllegalArgumentException("Columns list should be a list of (name:String,length:int)");
		}
		// Create columns
		String name = null;
		for (Object o : columns) {
			// Column name
			if (name == null) {
				name = o.toString().trim();
				if (name.isEmpty()) {
					throw new IllegalArgumentException("Column name cannot be an empty string");
				}
			}
			// Column length
			else {
				int length = (int) o;
				if (length < 1) {
					throw new IllegalArgumentException("Column length cannot be less than 1 char");
				}
				if (length < name.length()) {
					throw new IllegalArgumentException("Column length cannot be less than column's name length");
				}
				this.columns.add(new Column(name, length));
				name = null;
			}
		}
	}

	public String header() {
		StringBuilder sb = new StringBuilder();
		sb.append(Chars.TOP_LEFT_CORNER);
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			sb.append(new String(new char[columns.get(i).length + 1]).replace(Chars.NULL, Chars.STRAIGHT_H_DOUBLE));
			sb.append(i == l ? Chars.STRAIGHT_H_DOUBLE + Chars.TOP_RIGHT_CORNER : Chars.SEP_H_TOP);
		}
		sb.append(StringUtils.NL);
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			Column col = columns.get(i);
			sb.append(Chars.STRAIGHT_V_DOUBLE + StringUtils.WHITESPACE);
			sb.append(Utils.padOrTrim(col.name, columns.get(i).length));
			if (i == l) sb.append(StringUtils.WHITESPACE + Chars.STRAIGHT_V_DOUBLE);
		}
		sb.append(StringUtils.NL);
		sb.append(Chars.SEP_L_LEFT);
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			sb.append(new String(new char[columns.get(i).length + 1]).replace(Chars.NULL, Chars.STRAIGHT_H_DOUBLE));
			sb.append(i == l ? Chars.STRAIGHT_H_DOUBLE + Chars.SEP_L_RIGHT : Chars.CROSS);
		}
		sb.append(StringUtils.NL);
		return sb.toString();
	}
	
	public String footer() {
		StringBuilder sb = new StringBuilder();
		sb.append(Chars.BOTTOM_LEFT_CORNER);
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			sb.append(new String(new char[columns.get(i).length + 1]).replace(Chars.NULL, Chars.STRAIGHT_H_DOUBLE));
			sb.append(i == l ? Chars.STRAIGHT_H_DOUBLE + Chars.BOTTOM_RIGHT_CORNER : Chars.SEP_H_BOTTOM);
		}
		sb.append(StringUtils.NL);
		return sb.toString();
	}
	
	public String nl() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			if (i == 0) sb.append(Chars.SEP_L_LEFT);
			else sb.append(Chars.CROSS);
			int length = columns.get(i).length + 1;
			while (length-- > 0) sb.append(Chars.STRAIGHT_H_SIMPLE);
			if (i == l) sb.append(Chars.STRAIGHT_H_SIMPLE + Chars.SEP_L_RIGHT);
		}
		return sb.toString();
	}

	public String print(String... data) {
		// Check arguments length
		if (data.length != columns.size()) throw new IllegalArgumentException("Columns count mismatch");
		// Count lines
		int rowCount = 1;
		for (int i = 0, l = data.length - 1; i <= l; i++) {
			String str = data[i];
			String summary = Utils.padOrTrim(str, columns.get(i).length);
			// Multi-lines
			if (summary.contains(StringUtils.NL)) {
				rowCount = Math.max(rowCount, str.length() - str.replace(StringUtils.NL, StringUtils.EMPTYSTRING).length() + 1);
			}
		}
		// Create table rows
		StringBuilder sb = new StringBuilder();
		int row = 0;
		while (row < rowCount) {
			for (int i = 0, l = data.length - 1; i <= l; i++) {
				sb.append(Chars.STRAIGHT_V_DOUBLE + StringUtils.WHITESPACE);
				String str = data[i];
				String summary = Utils.padOrTrim(str, columns.get(i).length);
				// Multi-lines
				if (summary.contains(StringUtils.NL)) {
					String[] split = str.split(StringUtils.NL);
					str = split[row];
					summary = Utils.padOrTrim(str, columns.get(i).length);
					sb.append(summary);
				}
				else {
					if (row == 0) sb.append(summary);
					else sb.append(Utils.padOrTrim(StringUtils.EMPTYSTRING, columns.get(i).length));
				}
				if (i == l) sb.append(StringUtils.WHITESPACE + Chars.STRAIGHT_V_DOUBLE);
			}
			row++;
			if (row < rowCount) sb.append(StringUtils.NL);
		}
		return sb.toString();
	}
	
	public static class Column {

		public final String name;
		public final int length;

		public Column(String name, int length) {
			this.name = name;
			this.length = length;
		}
		
	}

	public static class Chars {
		public static String TOP_LEFT_CORNER 		= Character.toString((char)9556);//"╔";
		public static String TOP_RIGHT_CORNER		= Character.toString((char)9559);//"╗";
		public static String STRAIGHT_H_DOUBLE 		= Character.toString((char)9552);//"═";
		public static String STRAIGHT_H_SIMPLE		= Character.toString((char)9472);//"─";
		public static String STRAIGHT_V_DOUBLE 		= Character.toString((char)9553);//"║";
		public static String SEP_H_TOP 				= Character.toString((char)9574);//"╦";
		public static String SEP_H_BOTTOM 			= Character.toString((char)9577);//"╩";
		public static String SEP_L_LEFT 			= Character.toString((char)9568);//"╠";
		public static String SEP_L_RIGHT			= Character.toString((char)9571);//"╣";
		public static String BOTTOM_LEFT_CORNER		= Character.toString((char)9562);//"╚";
		public static String BOTTOM_RIGHT_CORNER 	= Character.toString((char)9565);//"╝";
		public static String CROSS 					= Character.toString((char)9580);//"╬";
		public static String NULL 					= "\0";
	}
	
}
