package fr.evolya.javatoolkit.lexer.rules;

import java.util.LinkedList;
import java.util.List;

public class Expression implements Element {

	private List<Element> elements = new LinkedList<>();
	private ExpressionBuilder2 builder;
	
	public Expression(ExpressionBuilder2 builder) {
		this(builder, null);
	}
	
	public Expression(ExpressionBuilder2 builder, Expression parent) {
		// TODO Check builder is null
		this.builder = builder;
	}
	
	public ExpressionBuilder2 getBuilder() {
		return this.builder;
	}
	
	protected void add(Element element) {
		if (element == null) throw new NullPointerException();
		this.elements.add(element);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		elements.forEach(sb::append);
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
				sb.append("[" + e.getClass().getSimpleName() + "]");
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
		for (Element el : elements) {
			if (el instanceof Expression) {
				c += ((Expression)el).getCount(true);
			}
			else {
				c++;
			}
		}
		return c;
	}
	
	public int getCount(Class<?> type) {
		return getCount(type, true);
	}

	public int getCount(Class<?> type, boolean countNestedExpressions) {
		if (!countNestedExpressions) {
			return (int) this.elements.stream()
					.filter(e -> type.isAssignableFrom(e.getClass()))
					.count();
		}
		int c = 0;
		for (Element el : elements) {
			if (el instanceof Expression) {
				c += ((Expression)el).getCount(type, true);
			}
			else if (type.isAssignableFrom(el.getClass())) {
				c++;
			}
		}
		return c;
	}

	public void removeWhitespaces() {
		elements.removeIf(e -> e.getClass() == Whitespace.class);
	}

	public Element handle(StringBuffer buffer) {
		return this.builder.tryToParse(buffer);
	}

	

}
