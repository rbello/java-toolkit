# Threading API

## Description

Several tools to take advantage of multi-threading capabilities in data processing and application execution.

This package mainly contains:
- [Parallel class](https://github.com/rbello/java-toolkit/blob/master/src/core/fr/evolya/javatoolkit/threading/Parallel.java) which provides functional methods for collections' multi-threaded operations.
- [Worker package](https://github.com/rbello/java-toolkit/tree/master/src/core/fr/evolya/javatoolkit/threading/worker) 
Which offers a processing queue to link the execution of jobs.

## State

This API is currently Stable.

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