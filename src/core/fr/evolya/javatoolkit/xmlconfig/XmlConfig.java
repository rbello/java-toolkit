package fr.evolya.javatoolkit.xmlconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.evolya.javatoolkit.code.utils.XmlUtils;

/**
 * This class constructs objects from an XML file.
 * 
 * @author Antti S. Brax
 * @author Jeon Jiwon
 * @author R. Bello
 * 
 * @version 3.0.1-Beta-ev
 */
public class XmlConfig {

	/**
	 * The program version.
	 */
	public static final String VERSION = "3.0.1-Beta-ev";

	/**
	 * This attribute contains the current beans.
	 */
	protected Map<String, Object> beans = null;
	
	/**
	 * This attribute contains the current properties.
	 */
	protected Map<String, String> properties = null;

	/**
	 * The document builder.
	 */
	protected DocumentBuilder db;

	/**
	 * Replace ${name} type variables?
	 */
	protected boolean replaceVariables = false;

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
		
		if (db == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setCoalescing(true);

			try {
				db = dbf.newDocumentBuilder();
			} catch (Throwable t) {
				throw new XmlConfigException("Could not create parser", t);
			}
		}

		Document doc;
		try {
			doc = db.parse(in);
		} catch (SAXException ex) {
			throw new XmlConfigException("Could not parse configuration", ex);
		} catch (IOException ex) {
			throw new XmlConfigException("Error while reading configuration", ex);
		}

		HashMap<String, String> tmpProperties = new HashMap<String, String>();
		HashMap<String, Object> tmpBeans = new HashMap<String, Object>(beans);
		
		handleDocument(src, doc, tmpProperties, tmpBeans);
		
		mergeMaps(tmpProperties, tmpBeans);

	}
	
	protected void mergeMaps(Map<String, String> mapProperties, Map<String, Object> mapBeans) {
		
		properties.putAll(mapProperties);
		mapProperties.clear();
		
		beans.putAll(mapBeans);
		mapBeans.clear();
		
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
	public <T> T getBean(String name, Class<T> clazz) {
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

	/**
	 * Handle the JDOM document.
	 * 
	 * @param src
	 * 			the orginal file source, or null if the source is directly a stream
	 * 
	 * @param mapProperties
	 * 			the map of properties to fill
	 * 		
	 * @param mapBeans
	 * 			the map of beans to fill
	 */
	protected void handleDocument(File src, Document doc, HashMap<String, String> mapProperties,
			HashMap<String, Object> mapBeans) throws XmlConfigException {
		
		handleConf(src, doc.getDocumentElement(), mapProperties, mapBeans);
	}

	/**
	 * Handle &lt;conf&gt; elements.
	 * 
	 * @param src 
	 * 			  the orginal file source, or null if the source is directly a stream
	 * 
	 * @param conf
	 *            the conf element
	 *            
	 * @param mapBeans 
	 * 			  the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 			  the map of properties to fill
	 */
	protected void handleConf(File src, Element conf, HashMap<String, String> mapProperties,
			HashMap<String, Object> mapBeans) throws XmlConfigException {
		
		try {
			
			handleConf0(src, conf, mapProperties, mapBeans);
			
		} catch (Throwable t) {
			throw new XmlConfigException("Error during configuration, in " + src, t);
		}

	}

	/**
	 * Handle &lt;conf&gt; elements.
	 * 
	 * @param src 
	 * 			  the orginal file source, or null if the source is directly a stream
	 * 
	 * @param conf
	 *            the conf element
	 *            
	 * @param mapBeans 
	 * 			  the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 			  the map of properties to fill
	 */
	protected void handleConf0(File src, Element conf, HashMap<String, String> mapProperties,
			HashMap<String, Object> mapBeans) throws Exception {

		boolean oldReplaceVariables = replaceVariables;

		// See if the element contains the replacevars attribute.
		String str = getAttributeValue(conf, "replacevars", mapProperties);
		if (str == null || str.equals("inherit")) {
			// Do nothing.
		} else if (str.equals("true")) {
			replaceVariables = true;
		} else if (str.equals("false")) {
			replaceVariables = false;
		} else {
			throw new XmlConfigException("Unknown variable replacement policy \"" + str + "\", in " + src);
		}

		NodeList list = conf.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);

			if (child instanceof Element) {

				// <include>url</include>
				// <include>file:path</include>
				if (child.getNodeName().equals("include")) {
					handleInclude(src, (Element) child, mapProperties, mapBeans);
				}
				
				// <bean name="Logical name" class="path.Class">
				else if (child.getNodeName().equals("bean")) {
					handleBean(this, (Element) child, null, mapProperties, mapBeans);
				}
				
				// <property name="Propety name">value</property>
				else if (child.getNodeName().equals("property")) {
					
					String name = getAttributeValue(child, "name", mapProperties);
					String value = getTextContent(child, mapProperties);
					boolean system = false;

					// If <property> element has a system-attribute and it's
					// value equals "true"
					String tmp = getAttributeValue(child, "system", mapProperties);
					if (tmp == null) {
						system = false;
					} else if (tmp.equalsIgnoreCase("true")) {
						system = true;
					} else if (tmp.equalsIgnoreCase("false")) {
						system = false;
					} else {
						throw new XmlConfigException("Unknown system-attribute value \"" + tmp
								+ "\", in " + src);
					}

					if (system) {
						System.setProperty(name, value);
					} else {
						mapProperties.put(name, value);
					}

				}
				
				else {
					throw new XmlConfigException("Unknown element <" + child.getNodeName()
							+ "> inside <conf> element, in " + src);
				}
			}
		}

		replaceVariables = oldReplaceVariables;
	}

	/**
	 * Handle &lt;include&gt; elements.
	 * 
	 * @param mapBeans 
	 * 				the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 				the map of properties to fill
	 */
	protected void handleInclude(File src, Element elem, HashMap<String, String> mapProperties,
			HashMap<String, Object> mapBeans) throws Exception {

		String source = getTextContent(elem, mapProperties);
		
		if (source.trim().isEmpty()) {
			throw new XmlConfigException("Include target must be in the text content " +
					"of the <include> element, in " + src);
		}
		
		Document doc = null;
		
		if (source.substring(0, 5).equals("file:")) {
			File file = new File(source.substring(5, source.length()));
			try {
				
				doc = db.parse(new FileInputStream(file));
				
				src = file;
				
			} catch (FileNotFoundException ex) {
				throw new XmlConfigException("Include target file not found : " + source
						+ ", in " + src, ex);
			} catch (Throwable t) {
				throw new XmlConfigException("Unable to include target file : " + source
						+ ", in " + src, t);
			}
		}
		
		else {

			URL url = null;
			try {
				url = new URL(source);
			} catch (Throwable t) {
				url = XmlConfig.class.getResource(source);
			}
			
			if (url == null) {
				throw new XmlConfigException("Include target URL not found : " + source
						+ ", in " + src);
			}
	
			try {
				doc = db.parse(url.openStream());
			} catch (Throwable t) {
				throw new XmlConfigException("Unable to include URL : " + source + ", in " + src, t);
			}
			
		}
		
		handleDocument(src, doc, mapProperties, mapBeans);
		
	}

	/**
	 * Handle &lt;bean&gt; elements.
	 * 
	 * @param beans
	 * 			  the used configuration
	 * 
	 * @param elem
	 *            the bean element
	 *            
	 * @param beanClass 
	 * 			  the bean class (if in a list declaration)
	 * 
	 * @param mapBeans
	 * 			the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 			the map of properties to fill
	 */
	protected static Object handleBean(XmlConfig config, Element elem, Class<?> beanClass,
			Map<String, String> mapProperties, Map<String, Object> mapBeans) throws Exception {

		// If the bean has a logical name then see if a previously configured
		// instance is available.
		Object bean = null;
		String name = config.getAttributeValue(elem, "name", mapProperties);
		if (name != null) {
			// TODO: This might cause problems during reconfiguration.
			bean = config.beans.get(name);
			if (bean == null) {
				bean = mapBeans.get(name);
			}
		}

		// Get the defined constructor (if any).
		List<Node> constructors = XmlUtils.getChildrenByTagName(elem, "constructor");
		if (constructors.size() > 1) {
			throw new XmlConfigException("Multiple constructors defined for "
					+ name);
		}

		// If there is no previously configured instance available and
		// the class attribute is specified then create a new object.
		Class<?> clazz = null;
		String clazzName = config.getAttributeValue(elem, "class", mapProperties);
		
		if (clazzName == null && beanClass != null) {
			clazzName = beanClass.getName();
		}
		
		if (bean == null) {
			if (clazzName != null && clazzName.length() != 0) {
				clazz = Class.forName(clazzName);
				
				if (clazz.isInterface()) {
					throw new XmlConfigException("Interface defined for bean class (bean "
							+ name + ", interface " + clazz.getName() + ")");
				}
				
				if (constructors.size() > 0) {
					bean = initializeBean(config, clazz, constructors.get(0), mapProperties, mapBeans);
				} else {
					try {
						bean = clazz.newInstance();
					} catch (Throwable t) {
						throw new XmlConfigException("Unable to make a new instance of " +
								"class " + clazz.getName() + " (bean " + name + ")", t);
					}
				}
			} else {
				String msg;
				if (name != null) {
					msg = "No class given to a new bean (" + name + ")";
				} else {
					msg = "No class given to an unnamed bean";
				}
				throw new XmlConfigException(msg);
			}
		} else if (clazzName != null && clazzName.length() != 0
				&& clazzName.equals(bean.getClass().getName()) == false) {
			String msg = "Bean's (" + name + ") class has changed from "
					+ clazzName + " to " + bean.getClass().getName();
			throw new XmlConfigException(msg);
		}

		// Handle bean's attributes.
		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			if (child instanceof Element) {
				
				if (child.getNodeName().equals("attr")) {
					handleAttr(config, bean, child, mapProperties, mapBeans);
				}
				
				else if (child.getNodeName().equals("call")) {
					handleCall(config, bean, child, mapProperties, mapBeans);
				}
				
				else if (child.getNodeName().equals("list")) {
					handleList(config, bean, name, child, mapProperties, mapBeans);
				}
				
				else if (child.getNodeName().equals("constructor")) {
					// Already processed.
				}
				
				else {
					String msg = "Unknown element <" + child.getNodeName()
							+ "> in <bean>";
					throw new XmlConfigException(msg);
				}
			}
		}

		// Save the bean if a logical name was given.
		if (name != null && name.length() != 0) {
			mapBeans.put(name, bean);
		}

		return bean;
	}

	/**
	 * Handle &lt;attr&gt; elements.
	 * 
	 * @param conf
	 * 			  the used configuration
	 * 
	 * @param bean
	 *            the object that contains the attribute
	 *            
	 * @param attr
	 *            the attr element
	 *            
	 * @param mapBeans 
	 * 				the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 				the map of properties to fill

	 */
	protected static void handleAttr(XmlConfig conf, Object bean, Node node,
			Map<String, String> mapProperties, Map<String, Object> mapBeans) throws Exception {

		Attr attr = new Attr(conf, node, mapProperties, mapBeans);

		// Get the right method and invoke it.
		Class<?> b_class = bean.getClass();
		String m_name = getSetterMethodName(attr.getName());
		Class<?>[] p_classes = new Class[] { attr.getClazz() };
		Method method = getDeclaredMethod(b_class, m_name, p_classes);
		if (method == null) {
			throw new XmlConfigException("Class " + b_class + " does "
					+ "not contain method " + getMethodName(m_name, p_classes));
		}

		method.invoke(bean, new Object[] { attr.getValue() });
	}

	/**
	 * Handle &lt;call&gt; elements.
	 *
	 * @param conf
	 * 			  the used configuration
	 * 
	 * @param bean
	 *            the object that contains the method
	 *            
	 * @param call
	 *            the call element
	 *            
	 * @param mapBeans 
	 * 				the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 				the map of properties to fill
	 */
	protected static void handleCall(XmlConfig conf, Object bean, Node call,
			Map<String, String> mapProperties, Map<String, Object> mapBeans)
		throws Exception {

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

		Method method = getDeclaredMethod(b_class, m_name, p_classes);
		if (method == null) {
			throw new XmlConfigException("Class " + b_class.getName() + " does "
					+ "not contain method " + getMethodName(m_name, p_classes));
		}

		method.invoke(bean, p_values);
		
	}

	/**
	 * Handle &lt;list&gt; elements.
	 * 
	 * @param conf
	 * 			  the used configuration
	 * 
	 * @param bean
	 *            the object that contains the list
	 *            
	 * @param beanName
	 * 			  the name of the current bean
	 *            
	 * @param list
	 *            the list element
	 *            
	 * @param mapBeans 
	 * 				the map of beans to fill
	 * 
	 * @param mapProperties 
	 * 				the map of properties to fill 
	 */
	@SuppressWarnings("unchecked")
	protected static void handleList(XmlConfig conf, Object bean, String beanName, Node list,
			Map<String, String> mapProperties, Map<String, Object> mapBeans) throws Exception {
		
		String l_name = conf.getAttributeValue(list, "name", mapProperties);
		if (l_name == null) {
			throw new XmlConfigException("No name attribute in <list>");
		}
		
		String l_classname = conf.getAttributeValue(list, "class", mapProperties);
		if (l_classname == null) {
			throw new XmlConfigException("No class attribute in <list name=\""
					+ l_name + "\">");
		}
		Class<?> l_class = Class.forName(l_classname);
		Class<?> b_class = bean.getClass();
		
		NodeList nodeList = list.getChildNodes();
		
		/// Searching the ADDED method (use the add method)
		
		String m_name = getAdderMethodName(l_name);
		
		Class<?>[] p_classes = new Class[] { l_class };
		Object m_param[] = new Object[1];
		m_param = new Object[1];
		Method method = getDeclaredMethod(b_class, m_name, p_classes);
		
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
		method = getDeclaredMethod(b_class, m_name, p_classes);
		
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
		method = getDeclaredMethod(b_class, m_name, p_classes);
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
		
		throw new XmlConfigException("No added/getter/setter method for <list name=\""
					+ l_name + "\"> in bean '" + beanName + "'");
	}

	// ==================================================================== //

	/**
	 * Initialize a bean with a specified constructor.
	 * 
	 * @revision Inca Framework : static
	 */
	protected static Object initializeBean(XmlConfig conf, Class<?> clazz, Node node,
			Map<String, String> mapProperties, Map<String, Object> mapBeans) throws Exception {

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
					+ getMethodName("<init>", p_classes), t);
		}
	}

	/**
	 * Replace ${name} type variables from the string.
	 */
	protected String replaceVariables(String orig,  Map<String, String> mapProperties) {

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
				String val = mapProperties.get(key);
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
	protected String getTextContent(Node elem, Map<String, String> mapProperties) {
		Node n = elem.getFirstChild();
		if (n == null) return "";
		return replaceVariables(n.getNodeValue().trim(), mapProperties);
	}

	/**
	 * Get the name of an attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 */
	protected String getAttributeValue(Node elem, String name, Map<String, String> mapProperties) {
		Node node = elem.getAttributes().getNamedItem(name);
		if (node != null) {
			return replaceVariables(node.getNodeValue(), mapProperties);
		} else {
			return null;
		}
	}

	/**
	 * Generate a setter method name for the specified attribute name.
	 * 
	 * @revision Inca Framework : static
	 * @revision Inca Framework : StringBuilder au lieu du StringBuffer
	 */
	protected static String getSetterMethodName(String attr) {
		StringBuilder sb = new StringBuilder("set");
		sb.append(Character.toUpperCase(attr.charAt(0)));
		sb.append(attr.substring(1));
		return sb.toString();
	}
	
	/**
	 * Generate a getter method name for the specified attribute name.
	 * 
	 * @revision Inca Framework : method added
	 */
	protected static String getGetterMethodName(String attr) {
		StringBuilder sb = new StringBuilder("get");
		sb.append(Character.toUpperCase(attr.charAt(0)));
		sb.append(attr.substring(1));
		return sb.toString();
	}

	/**
	 * Generate a setter method name for the specified attribute name.
	 * 
	 * @revision Inca Framework : static
	 * @revision Inca Framework : StringBuilder au lieu du StringBuffer
	 */
	protected static String getAdderMethodName(String attr) {
		StringBuilder sb = new StringBuilder("add");
		sb.append(Character.toUpperCase(attr.charAt(0)));
		sb.append(attr.substring(1));
		return sb.toString();
	}

	/**
	 * Recursively search through the class hierarchy of <code>c</code> for
	 * the specified method.
	 * 
	 * @revision Inca Framework : static
	 */
	protected static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>[] args) {

		Method out = null;
		
		for (Method m : clazz.getDeclaredMethods()) {
			
			// Le nom de la méthode ne correspond pas
			if (!m.getName().equals(name)) {
				continue;
			}
			
			// On recherche les types des arguments de la méthode
			Class<?>[] types = m.getParameterTypes();
			
			// Si le nombre d'argument ne match pas
			if (args.length != types.length) {
				continue;
			}

			StringBuilder sb = new StringBuilder();

			// On parcours les types
			int i = 0;
			boolean ok = true;
			for (Class<?> type : m.getParameterTypes()) {
				
				// R�cup�ration de l'argument correspondant au param�tre
				Class<?> arg = args[i++];
				
				sb.append(type.getCanonicalName() + "/" + arg.getCanonicalName() + ",");
				
				// Type générique
				// La plus-value de cette méthode est ici.
				if (type.getCanonicalName().equals("java.lang.Object")) {
					continue;
				}

				// Mauvais type d'argument
				if (!type.getCanonicalName().equals(arg.getCanonicalName())) {
					ok = false;
					break;
				}

			}
			
			// Method found
			if (ok) {
				out = m;
				break;
			}
			
		}
		
		// Recursivity
		if (out == null) {
			clazz = clazz.getSuperclass();
			if (clazz != null) {
				out = getDeclaredMethod(clazz, name, args);
			}
		}
		
		return out;
	}

	/**
	 * Helper method for generating method names.
	 * 
	 * @revision Inca Framework : static
	 */
	protected static String getMethodName(String n, Class<?>[] p) {
		StringBuffer sb = new StringBuffer(n).append("(");
		for (int i = 0; i < p.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(p[i].getName());
		}
		sb.append(")");
		return sb.toString();
	}
	
}
