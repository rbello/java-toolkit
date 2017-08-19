package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;
import fr.evolya.javatoolkit.code.utils.XmlUtils;

/**
 * This class constructs objects from an XML file.
 * 
 * @author Antti S. Brax
 * @author Jeon Jiwon
 * @author R. Bello
 * 
 * @version 2.0
 */
public class XmlConfig {
	
	public static final Logger LOGGER = Logs.getLogger("IOC");

	/**
	 * This attribute contains the current beans.
	 */
	protected Map<String, Object> beans = null;
	
	/**
	 * This attribute contains the current properties.
	 */
	protected Map<String, String> properties = null;

	private boolean replaceVariables;

	/**
	 * Create an empty configuration.
	 */
	public XmlConfig() {
		beans 		= Collections.synchronizedMap(new HashMap<String, Object>());
		properties 	= Collections.synchronizedMap(new HashMap<String, String>());
	}

	/**
	 * Read a configuration from the InputStream.
	 * 
	 * @param in
	 *            the input stream which provides the new configuration.
	 * @exception 
	 *            the configuration process failed.
	 *            
	 * @see #addConfiguration(java.io.InputStream)
	 */
	public XmlConfig(InputStream in) throws XmlConfigException, IOException {
		this();
		addConfiguration(in);
	}

	/**
	 * Read a configuration from the File.
	 * 
	 * @exception 
	 *            the configuration process failed.
	 * 
	 * @see #addConfiguration(java.io.File)
	 */
	public XmlConfig(File file) throws XmlConfigException, IOException {
		this();
		addConfiguration(file);
	}

	// ==================================================================== //

	/**
	 * Add configuration from the specified file.
	 * @throws XmlConfigException 
	 */
	public void addConfiguration(File file) throws IOException, XmlConfigException {
		addConfiguration(file, new FileInputStream(file));
	}

	/**
	 * Add configuration from the specified input stream.
	 * @throws XmlConfigException 
	 */
	public void addConfiguration(InputStream in) throws IOException, XmlConfigException {
		addConfiguration(null, in);
	}
	
	/**
	 * Add configuration from the specified input stream.
	 */
	protected synchronized void addConfiguration(File src, InputStream in)
		throws XmlConfigException, IOException {
		
		// Create document builder factory
		DocumentBuilder db;
		try {
			db = XmlUtils.createDocumentBuilder();
		}
		catch (ParserConfigurationException ex) {
			throw new XmlConfigException("Could not create XML parser", ex);
		}

		// Read and parse document
		Document doc;
		try {
			doc = db.parse(in);
		}
		catch (IOException ex) {
			throw new XmlConfigException(src, "Error while reading configuration. Check the given file is readdable.", ex);
		}
		catch (SAXException ex) {
			throw new XmlConfigException(src, "Could not parse configuration file. Check the file has a well-formed XML format.", ex);
		}
		
		// Log
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "Parse file: " + src.getAbsolutePath());
		}
		
		// Handle document
		handleConf(src, doc.getDocumentElement());
		
	}

	
	
	/**
	 * Get a string property.
	 * 
	 * @param name
	 *            the name of the property.
	 */
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	/**
	 * Get a named bean.
	 * 
	 * @param name
	 *            the name of the bean.
	 */
	public Object getBean(String name) {
		return beans.get(name);
	}
	
	/**
	 * Get a named bean.
	 * 
	 * @param name
	 *            the name of the bean.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name, Class<T> type) {
		if (!beans.containsKey(name)) {
			LOGGER.log(Logs.WARNING, "Bean not found: " + name);
			return null;
		}
		Object bean = beans.get(name);
		if (!type.isInstance(bean)) {
			LOGGER.log(Logs.WARNING, "Bean with name '" + name + "' is not an instance of " + type.getName());
			return null;
		}
		return (T) beans.get(name);
	}
	
	/**
	 * Return the number of properties stored in the current configuration.
	 */
	public int getPropertiesCount() {
		return properties.size();
	}

	/**
	 * Return the number of named beans stored in the current configuration.
	 */
	public int getBeansCount() {
		return beans.size();
	}

	// ==================================================================== //

	protected void handleConf(File src, Element rootElement) throws XmlConfigException {

		// See if the element contains the replacevars attribute.
		replaceVariables = XmlUtils.getBooleanAttribute(rootElement, "replacevars", true);
		
		// Fetch child nodes
		XmlUtils.<XmlConfigException>forEachChildNodes(rootElement, (child) -> {
			switch (child.getNodeName()) {
			// <include>path</include>
			case "include": handleInclude(src, child); break;
			// <bean name="Logical name" class="path.Class">
			case "bean": handleBean(src, child, null); break;
			// <property name="Propety name">value</property>
			case "property":
				String name = getAttributeValue(child, "name");
				String value = getTextContent(child);
				if (XmlUtils.getBooleanAttribute(child, "system", false)) {
					System.setProperty(name, value);
				}
				else {
					properties.put(name, value);
				}
				break;
			default:
				throw new XmlConfigException(src, "Unknown element <%s> inside <conf> element", child.getNodeName());
			}
		});
	}

	protected void handleInclude(File src, Element elem)
			throws XmlConfigException, IOException {
		String source = getTextContent(elem);
		if (source.trim().isEmpty()) {
			throw new XmlConfigException(src, "Include target must be in the text content " +
					"of the <include> element");
		}
		addConfiguration(new File(source));
	}

	protected Object handleBean(File src, Element beanNode, Class<?> beanClass)
			throws XmlConfigException {

		// If the bean has a logical name then see if a previously configured
		// instance is available.
		Object beanInstance = null;
		String beanName = getAttributeValue(beanNode, "name");
		if (beanName != null) {
			beanInstance = beans.get(beanName);
			if (beanInstance != null) {
				beanClass = beanInstance.getClass();
			}
		}

		// If there is no previously configured instance available and
		// the class attribute is specified then create a new object.
		if (beanClass == null) {
			String className = getAttributeValue(beanNode, "class");
			if (className == null) {
				throw new XmlConfigException(src, "Element <bean> must have a 'class' attribute");
			}
			try {
				beanClass = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				throw new XmlConfigException(src, e, "Unable to create <bean> with a not found class: %s", className);
			}
		}
		if (beanInstance == null) {
			beanInstance = createBeanInstance(src, beanClass, beanName, beanNode);
		}
		
		// Fetch childs of bean's node
		XmlUtils.<XmlConfigException>forEachChildNodes(beanNode, (child) -> {
			switch (child.getNodeName()) {
			case "attr": handleAttr(src, beanInstance, beanName, child); break;
			case "call": handleCall(src, beanInstance, beanName, child); break;
			case "list": handleList(src, beanInstance, beanName, child); break;
			case "constructor": break; // Already processed
			default:
				throw new XmlConfigException(src, "Unknown element <%s> in <bean>", child.getNodeName());
			}
		});

		// Save the bean if a logical name was given.
		if (beanName != null && !beanName.isEmpty()) {
			beans.put(beanName, beanInstance);
			LOGGER.log(Logs.DEBUG, "Create bean: " + beanName + " (" + beanClass.getName() + ")");
		}
		else {
			LOGGER.log(Logs.DEBUG_FINE, "Create bean: " + beanClass.getName());
		}

		return beanInstance;
	}

	private <T> T createBeanInstance(File src, Class<T> type, String name, Element node)
			throws XmlConfigException {
		// Check if given type is an interface
		if (type.isInterface()) {
			throw new XmlConfigException(src,
					"An interface was defined as bean class (bean %s, interface %s)",
					name, type.getName());
		}
		// Get the defined constructor (if any).
		List<Node> constructors = XmlUtils.getChildrenByTagName(node, "constructor");
		if (constructors.size() > 1) {
			throw new XmlConfigException(src, "Multiple constructors defined for bean '%s'", name);
		}
		try {
			// Initialize with a constructor
			if (constructors.size() > 0) {
				return initializeBean(type, constructors.get(0));
			}
			// Initialize without constructor
			return type.newInstance();
		}
		catch (Throwable t) {
			throw new XmlConfigException(src, t, "Unable to create a new instance of " +
					"class %s (bean %s)", type.getName(), name);
		}
//	} else {
//		String msg;
//		if (name != null) {
//			msg = "No class given to a new bean (" + name + ")";
//		} else {
//			msg = "No class given to an unnamed bean";
//		}
//		throw new XmlConfigException(msg);
//	}
//} else if (clazzName != null && clazzName.length() != 0
//		&& clazzName.equals(bean.getClass().getName()) == false) {
//	String msg = "Bean's (" + name + ") class has changed from "
//			+ clazzName + " to " + bean.getClass().getName();
//	throw new XmlConfigException(msg);
//}
	}

	protected void handleAttr(File src, Object bean, String beanName, Node node)
			throws XmlConfigException {
		// Create attribute
		Attr attr = new Attr(this, src, node, bean);
		
		Method method = attr.getSetterMethod();
		
		if (method == null) {
			throw new XmlConfigException(src, "Class " + b_class + " does "
					+ "not contain method " + ReflectionUtils.getMethodSignature(m_name, p_classes));
		}
		method.invoke(bean, new Object[] { attr.getValue() });
	}

	protected static void handleCall(File src, Object bean, String beanName, Node call)
		throws XmlConfigException {

		String m_name = conf.getAttributeValue(call, "name", mapProperties);
		if (m_name == null) {
			throw new XmlConfigException("No name attribute in <call>");
		}

		Class<?> b_class = bean.getClass();
		List<Node> params = XmlUtils.getChildrenByTagName((Element) call, "param");

		Class<?>[] p_classes = new Class[params.size()];
		Object[] p_values = new Object[params.size()];
		for (int i = 0; i < params.size(); i++) {
			Param param = new Param(conf, (Node) params.get(i), mapProperties, mapBeans);
			p_classes[i] = param.getClazz();
			p_values[i] = param.getValue();
		}

		Method method = ReflectionUtils.getMethodMatching(b_class, m_name, p_classes);
		if (method == null) {
			throw new XmlConfigException("Class " + b_class.getName() + " does "
					+ "not contain method " + ReflectionUtils.getMethodSignature(m_name, p_classes));
		}

		method.invoke(bean, p_values);
		
	}

	@SuppressWarnings("unchecked")
	protected static void handleList(File src, Object bean, String beanName, Node list)
			throws XmlConfigException {
		
		String l_name = conf.getAttributeValue(list, "name", mapProperties);
		if (l_name == null) {
			throw new XmlConfigException("No name attribute in <list>");
		}
		
		String l_classname = conf.getAttributeValue(list, "class", mapProperties);
		if (l_classname == null) {
        	throw new XmlConfigException("No class attribute in <list name=\"" + l_name + "\">");
		}
		Class<?> l_class = Class.forName(l_classname);
		Class<?> b_class = bean.getClass();
		
		NodeList nodeList = list.getChildNodes();
		
		/// Searching the ADDED method (use the add method)
		
		String m_name = getAdderMethodName(l_name);
		
		Class<?>[] p_classes = new Class[] { l_class };
		Object m_param[] = new Object[1];
		m_param = new Object[1];
		Method method = ReflectionUtils.getMethodMatching(b_class, m_name, p_classes);
		
		if (method != null) {
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node child = nodeList.item(i);
				if (child instanceof Element) {
					m_param[0] = handleBean(conf, (Element) child, null, mapProperties, mapBeans);
					method.invoke(bean, m_param);
				}
			}
			return;
			
		}

		/// Searching the GETTER method (get the list, then use the list's add method)
		
		m_name = getGetterMethodName(l_name);
		p_classes = new Class[] { };
		method = ReflectionUtils.getMethodMatching(b_class, m_name, p_classes);
		
		if (method != null) {
			if (method.getReturnType().equals(List.class)) {
				
				List<Object> b_list = (List<Object>) method.invoke(bean, new Object[0]);
				
				if (b_list != null) {
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node child = nodeList.item(i);
						if (child instanceof Element) {
							b_list.add(handleBean(conf, (Element) child, l_class, mapProperties, mapBeans));
						}
						
					}
					return;
				}
				// else let the setter method search to create a list
			}
			// else nevermind, the getter method should be for other things
		}

		/// Searching the SETTER method (set a new list, then use the list's add method)
		
		m_name = getSetterMethodName(l_name);
		p_classes = new Class[] { List.class };
		method = ReflectionUtils.getMethodMatching(b_class, m_name, p_classes);
		if (method != null) {
			
			List<Object> b_list = new LinkedList<Object>();
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node child = nodeList.item(i);
				if (child instanceof Element) {
					b_list.add(handleBean(conf, (Element) child, l_class, mapProperties, mapBeans));
				}
				
			}
			
			method.invoke(bean, new Object[] { b_list });
			return;
			
		}
		if (beanName == null) beanName = l_classname;
		throw new XmlConfigException("No added/getter/setter method for <list name=\""
					+ l_name + "\"> in bean '" + beanName + "'");
	}

	// ==================================================================== //

	protected <T> T initializeBean(Class<T> clazz, Node node) throws XmlConfigException {

		if (Modifier.isAbstract(clazz.getModifiers())) {
			throw new XmlConfigException("Unable to create instance of abstract class "
					+ clazz.getName());
		}
		
		List<Node> params = XmlUtils.getChildrenByTagName((Element) node, "param");
		Class<?>[] p_classes = new Class[params.size()];
		Object[] p_values = new Object[params.size()];
		for (int i = 0; i < params.size(); i++) {
			Param param = new Param(conf, (Node) params.get(i), mapProperties, mapBeans);
			p_classes[i] = param.getClazz();
			p_values[i] = param.getValue();
		}

		Constructor<?> constructor = null;
		constructor = clazz.getDeclaredConstructor(p_classes);

		try {
			return constructor.newInstance(p_values);
		} catch (Throwable t) {
			throw new XmlConfigException("Failed to invoke method "
					+ clazz.getName() + "."
					+ ReflectionUtils.getMethodSignature("<init>", p_classes), t);
		}
	}

	/**
	 * Replace ${name} type variables from the string.
	 */
	protected String replaceVariables(String orig) {

		if (replaceVariables == false || orig == null) {
			return orig;
		}

		StringBuffer sb = new StringBuffer();
		int start, end, index;

		index = 0;
		while ((start = orig.indexOf("${", index)) != -1) {
			sb.append(orig.substring(index, start));
			index = start;
			if ((end = orig.indexOf("}", index)) != -1) {
				String key = orig.substring(start + 2, end);
				index = end + 1;
				String val = properties.get(key);
				if (val != null) {
					sb.append(val);
				}
			} else {
				break;
			}
		}
		if (index < orig.length()) {
			sb.append(orig.substring(index, orig.length()));
		}
		return sb.toString();
	}

	/**
	 * Get the text content of the specified element as a String.
	 */
	protected String getTextContent(Node elem) {
		Node n = elem.getFirstChild();
		if (n == null) return "";
		return replaceVariables(n.getNodeValue().trim());
	}

	/**
	 * Get the name of an attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 */
	protected String getAttributeValue(Node elem, String name) {
		Node node = elem.getAttributes().getNamedItem(name);
		if (node == null) return null;
		return replaceVariables(node.getNodeValue());
	}

}
