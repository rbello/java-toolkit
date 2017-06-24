package fr.evolya.javatoolkit.xmlconfig;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML helper methods.
 *
 * @author Antti S. Brax
 * @author R. Bello
 * 
 * @version 1.1
 */
public class XmlTool {

	private XmlTool() {
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

}
