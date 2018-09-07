package fr.evolya.javatoolkit.lexer;

import java.util.LinkedList;
import java.util.List;

public class Expression implements IElement<List<IElement<?>>> {

	private List<IElement<?>> elements = new LinkedList<>();
	private String rawInput;
	
	public Expression(String rawInput) {
		this.rawInput = rawInput;
	}

	protected void add(IElement<?> element) {
		if (element == null) throw new NullPointerException();
		this.elements.add(element);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		elements.forEach(e -> {
			if (e instanceof Expression) {
				sb.append(e.toString());
			}
			else {
				sb.append("[" + e.getTokenValue() + "]");
			}
		});
		sb.append(')');
		return sb.toString();
	}
	
	public String toString(boolean displayTypes) {
		if (!displayTypes) return toString();
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		elements.forEach(e -> {
			if (e instanceof Expression) {
				sb.append(((Expression)e).toString(true));
			}
			else {
				sb.append("[" + e.getTokenName() + "]");
			}
		});
		sb.append(')');
		return sb.toString();
	}

	public int getCount() {
		return getCount(false);
	}
	
	public int getCount(boolean countNestedExpressions) {
		if (!countNestedExpressions) return this.elements.size();
		int c = 0;
		for (IElement<?> el : elements) {
			if (el instanceof Expression) {
				c += ((Expression)el).getCount(true);
			}
			else {
				c++;
			}
		}
		return c;
	}
	
	public int getCount(String tokenName) {
		return getCount(tokenName, true);
	}

	public int getCount(String tokenName, boolean countNestedExpressions) {
		if (!countNestedExpressions) {
			return (int) this.elements.stream()
					.filter(el -> tokenName.equals(el.getTokenName()))
					.count();
		}
		return getCount(tokenName, elements);
	}

	@SuppressWarnings("unchecked")
	private int getCount(String tokenName, List<IElement<?>> list) {
		int c = 0;
		for (IElement<?> el : list) {
			if (el.getTokenValue() instanceof List<?>) {
				c += getCount(tokenName, (List<IElement<?>>) el.getTokenValue());
			}
			else if (tokenName.equals(el.getTokenName())) {
				c += 1;
			}
		}
		return c;
	}

	public void removeWhitespaces() {
		elements.removeIf(e -> "T_WHITESPACE".equals(e.getTokenName()));
	}

	public String getRawInput() {
		return this.rawInput;
	}

	@Override
	public String getTokenName() {
		return "T_EXPRESSION";
	}

	@Override
	public List<IElement<?>> getTokenValue() {
		return this.elements;
	}
	
	public List<IElement<?>> getElements() {
		return this.elements;
	}

}
