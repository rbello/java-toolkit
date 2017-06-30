# Application Framework API

## Description

A framework for simply creating applications on an event-driven model and by injecting dependencies.

## State

This API is currently in development.

## Usage

```java
public static void main(String[] args) {

	// Init Look&Feel and loggers
	App.init();
	
	// Create a new app (here based on Swing)
	App app = new SwingApp();
	
	// Set common properties
	app.get(AppConfiguration.class)
		.setProperty("App.Name", "MyApplication")
		.setProperty("App.Version", "3.0.0");
	
	// Add some components
	app.add(ConsoleView.class);
	app.add(ModuleConsole.class);
	app.add(ModuleNetworkWatcher.class);
	
	// Subsribe to an event
	app.when(GuiIsReady.class)
	   .onlyOn(ConsoleView.class)
	   .execute((gui, appli) -> ((ConsoleView)gui).getConsolePanel()
			   .getInputField().requestFocus());
	
	// Start application
	app.start();
	
}
```

The default lifecycle of an application will trigger this sequence of events : 
`BeforeApplicationStarted -> ApplicationBuilding -> ApplicationStarting -> ApplicationStarted -> ApplicationReady`
All objects are created during `ApplicationBuilding` and dependencies injections are solved during this step.

Then in each module, you can attach methods on application events and dependencies injections:

```java
public class ModuleConsole {

		@Inject
		App application; // Injection
		
		@BindOnEvent(GuiIsReady.class)
		@EventArgClassFilter(ConsoleView.class)
		@GuiTask
		public void buildMenuSession(ConsoleView view, App app) {
			// Do some stuff
		}

}
```

Annotation used:
- Annotation `@BindOnEvent(Class<Event>)` is used to define witch event to listener.
- Annotation `@EventArgClassFilter(Class<Event>)` will filter only events triggered by the given source.
- Annotation `@GuiTask` will force execution on Event Dispatch Thread (EDT).
- Annotation `@Inject` will inject the dependency according to field type.

A bootstrap for [creating a Swing application using MVC](https://github.com/rbello/java-toolkit/tree/master/src/tests/fr/evolya/javatoolkit/core/app/swing) is available.

## Credits

R. Bello