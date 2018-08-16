package fr.evolya.javatoolkit.cli;

import java.util.LinkedList;
import java.util.List;

import fr.evolya.javatoolkit.code.utils.Utils;

public class AsciiTable {
	
	private List<Column> columns = new LinkedList<>();

	public AsciiTable(Object... columns) {
		String name = null;
		int length;
		for (Object o : columns) {
			if (name != null) {
				length = (int)o;
				this.columns.add(new Column(name, length));
				name = null;
			}
			else name = (String) o;
		}
	}

	public String header() {
		StringBuilder sb = new StringBuilder();
		sb.append("╔══════════════════════════════════╦═════════════╦════════════════════════════════════════════════════════════════════════════════════════════╗\n");
		sb.append("║               Test               ║  Duration   ║                                            Results                                         ║\n");
		sb.append("╠══════════════════════════════════╬═════════════╬════════════════════════════════════════════════════════════════════════════════════════════╣");
		return sb.toString();
	}
	
	public String footer() {
		return "╚══════════════════════════════════╩═════════════╩════════════════════════════════════════════════════════════════════════════════════════════╝";
	}
	
	public String nl() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, l = columns.size() - 1; i <= l; i++) {
			if (i == 0) sb.append("╠");
			else sb.append("╬");
			int length = columns.get(i).length + 1;
			while (length-- > 0) sb.append("─");
			if (i == l) sb.append("─╣");
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
			if (summary.contains("\n")) {
				rowCount = Math.max(rowCount, str.length() - str.replace("\n", "").length() + 1);
			}
		}
		// Create table rows
		StringBuilder sb = new StringBuilder();
		int row = 0;
		while (row < rowCount) {
			for (int i = 0, l = data.length - 1; i <= l; i++) {
				sb.append("║ ");
				String str = data[i];
				String summary = Utils.padOrTrim(str, columns.get(i).length);
				// Multi-lines
				if (summary.contains("\n")) {
					String[] split = str.split("\n");
					str = split[row];
					summary = Utils.padOrTrim(str, columns.get(i).length);
					sb.append(summary);
				}
				else {
					if (row == 0) sb.append(summary);
					else sb.append(Utils.padOrTrim("", columns.get(i).length));
				}
				if (i == l) sb.append(" ║");
			}
			row++;
			if (row < rowCount) sb.append("\n");
		}
		return sb.toString();
	}
	
	static class Column {

		protected final String name;
		protected final int length;

		public Column(String name, int length) {
			this.name = name;
			this.length = length;
		}
		
	}
	
}
