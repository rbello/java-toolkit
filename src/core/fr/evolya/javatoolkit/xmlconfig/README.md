# XmlConfig API

## Description

Implementation of the principle of inversion of controls (IOC) by a mechanism of object creation from XML configuration files.

## State

This API is Stable and documented.

## Usage

```java
// Load XML configuration file
XmlConfig cfg = new XmlConfig(new File("myconfig.xml"));

// Get a property by his name
String property = cfg.getProperty("otherProperty");

// Get a bean, with ou without cast
Object bean1 = cfg.getBean("MyBean");
MyClass bean2 = cfg.getBean("MyBean", MyClass.class);
```

The XML file must respect the following format:

```xml

	<root replacevars="true|false">
		
		<!-- Properties -->
		<property name="oneProperty" system="true|false">Property value</property>
		<property name="otherProperty">Replace var: ${oneProperty}</property>
		
		<!-- Include other configuration file -->
		<include>./other-config-file.xml</include>
		
		<!-- Bean declaration -->
		<bean name="MyBean" class="fr.evolya.MyClass">
		
			<!-- Constructor parameters -->
			<constructor>
				<param type="string">This is a string</param>
				<param type="boolean">true</param>
				<param type="bean">NameOfAnOtherBean</param>
			</constructor>
			
			<!-- Modify attributes using setters -->
			<attr name="y" type="java.lang.Double">1.156978</param>
			<attr name="x" type="double">0.153801</param>
			
			<!-- Infer type of attribute -->
			<attr name="width">600</param>
			<attr name="height">800</param>
			
			<!-- Access static fields (and enumerations) -->
			<attr name="color" type="java.awt.Color">RED</param>
			
			<!-- Modify attribute of type list argument -->
			<list name="aList" class="fr.evolya.MyOtherClass">
				<!-- You can configure bean's attributes like this too -->
				<bean attributeName="attributeValue">...</bean>
			</list>
			
			<!-- Create a list of -->
			<list name="anotherList" class="java.lang.String">
				<value>A string</value>
				...
			</list>
			
			<!-- Call a methode after creating -->
			<call name="methodName">
				<param type="string">Pass a string to this method</param>
			</call>
			
		</bean>
		
		
	</root>
```

## Credits

Antti S. Brax
Jeon Jiwon
R. Bello