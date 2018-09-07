# Lexer API

## Description

A tool for implementing an interpreter to create custom languages.

## State

This API is currently in development.

## Usage

```java
// Create builder
ExpressionBuilder builder = ExpressionBuilder.createDefault();

// Parse an expression
Expression ex = builder.parse("your own(syntax) + you['decides']");

// Get the list of structures
List<IElement<?>> structs = ex.getElements();

// Or display as string (debug)
System.out.println(ex); // tokens values
System.out.println(ex.toString(true)); // tokens types
```

## Credits

R. Bello