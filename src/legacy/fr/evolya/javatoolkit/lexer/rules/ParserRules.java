package fr.evolya.javatoolkit.lexer.rules;

import java.util.regex.Pattern;

import fr.evolya.javatoolkit.lexer.rules.Element.FloatNumber;
import fr.evolya.javatoolkit.lexer.rules.Element.IntegerNumber;
import fr.evolya.javatoolkit.lexer.rules.Rule.ParserRule;

public abstract class ParserRules implements ParserRule {

	public static class NumericParser extends ParserRules {

		@Override
		public boolean accept(String str) {
			return Pattern.compile("^-?[0-9]{1,12}(?:\\.[0-9]{1,13})?$").matcher(str).matches();
		}

		@Override
		public Element parse(String str) {
			if (str.contains(".")) return new FloatNumber(Double.parseDouble(str));
			return new IntegerNumber(Integer.parseInt(str));
		}

	}
	
}
