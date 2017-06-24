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

## Credits

R. Bello