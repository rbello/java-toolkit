# Observer Design Pattern Implementations

## Description

This package contains several implementations of Observer DP.

- [fi implementation](https://github.com/rbello/java-toolkit/tree/master/src/core/fr/evolya/javatoolkit/events/fi) is stable. This implementation works on the principle of annotations that will allow to subscribe to the events. Compatible with dispatch on HMI but mono-threaded.
- [attr implementation](https://github.com/rbello/java-toolkit/tree/master/src/core/fr/evolya/javatoolkit/events/attr) is in development. This implementation imitates the C# style where events are exposed as class attributes.
- [alpha implementation](https://github.com/rbello/java-toolkit/tree/master/src/core/fr/evolya/javatoolkit/events/alpha) is stable. This implementation is very complete, it allows to give a priority to the listeners during the propagation, to pass method names as callback (reflection), to make redirections of event between several sources, to use asynch-propagation, to configure the management of Exceptions raises and interrupt the events propagations.
- [basic implementation](https://github.com/rbello/java-toolkit/tree/master/src/core/fr/evolya/javatoolkit/events/basic) is stable. This very simple implementation can be used for microprograms that do not require advenced features.

## State

This API is mostly Stable.

## Usage of FI implementation

Using annotations :

```java
public class MyClass {

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

Using inline-code :

```java
	// Subsribe to an event
	observable.when(GuiIsReady.class) // Event class
	          .onlyOn(ConsoleView.class) // Filter on source
	          .execute((gui, appli) -> {
	          	// Job...
	          });
```

## Credits

R. Bello