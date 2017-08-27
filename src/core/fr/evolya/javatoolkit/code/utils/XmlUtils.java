package fr.evolya.javatoolkit.code.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.evolya.javatoolkit.code.funcint.Consumer;

/**
 * XML helper methods.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 1.1
 */
public final class XmlUtils {

	private XmlUtils() {
	}
	
    /**
     * Get direct child nodes by tag name.
     */
    public static List<Node> getChildrenByTagName(Node node, String name) {
        NodeList nodeList = node.getChildNodes();
        ArrayList<Node> arrayList = new ArrayList<Node>();
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (name.equals(child.getNodeName())) {
                arrayList.add(child);
            }
        }
        
        return arrayList;
    }
    
    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setCoalescing(true);
		return dbf.newDocumentBuilder();
	}

	public static boolean getBooleanAttribute(Element node, String attributeName, boolean defaultValue) {
		String value = getAttributeValue(node, attributeName);
		if (value == null) return defaultValue;
		switch (value.toLowerCase()) {
		case "true": return true;
		case "false": return false;
		default: return defaultValue;
		}
	}
	
	public static String getAttributeValue(Node elem, String attributeName) {
		Node node = elem.getAttributes().getNamedItem(attributeName);
		if (node == null) return null;
		return node.getNodeValue();
	}

	public static <E extends Exception> void forEachAttributesExcept(Element node, 
			Consumer<Attr, E> consumer, String... exceptedAttrNames) throws E {
		List<String> except = Arrays.asList(exceptedAttrNames);
		NamedNodeMap attrs = node.getAttributes();
		for (int i = 0, l = attrs.getLength(); i < l; i++) {
			Node attr = attrs.item(i);
			if (attr == null) continue;
			if (except.contains(attr.getNodeName())) continue;
			consumer.accept((Attr)attr);
		}
	}
	
	public static <E extends Exception> void forEachChildNodes(Element node,
			Consumer<Element, E> consumer)
			throws E {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			if (child instanceof Element) {
				consumer.accept((Element) child);
			}
		}
	}

}
