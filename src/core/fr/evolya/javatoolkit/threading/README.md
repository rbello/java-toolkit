# Threading API

## Description

A framework for simply creating applications on an event-driven model and by injecting dependencies.

This package mainly contains:
- [Parallel class]() which provides functional methods for collections' multi-threaded operations.
- [Worker package]() 

## State

This API is currently in Stable.

## Usage

```java
IWorker worker = new Worker();
worker.start();

IOperation operation = ...; // Create an operation here
worker.invokeLater(operation); // Asynch
worker.invokeAndWait(() -> { // Synch and using functionnal interface
	// Operation...
});
```

## Credits

R. Bello