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
		<include>file:./other-config-file.xml</include>
		<include>http://test.com/file.xml</include>
		
		<!-- Bean declaration -->
		<bean name="MyBean" class="fr.evolya.MyClass">
		
			<!-- Constructor parameters -->
			<constructor>
				<param type="string">This is a string</param>
				<param type="boolean">true</param>
				<param type="bean">NameOfAnOtherBean</param>
			</constructor>
			
			<!-- Modify attributes using setters -->
			<attr name="x" type="double">0.153801</param>
			<attr name="y" type="double">1.156978</param>
			
			<!-- Modify attribute using setters with list argument -->
			<list name="aList" type="Object">
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