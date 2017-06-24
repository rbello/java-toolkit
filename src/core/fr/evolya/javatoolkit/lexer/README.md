# Lexer API

## Description

A tool for implementing an interpreter to create custom languages.

## State

This API is currently in development.

## Usage

```java
// Create builder
ExpressionBuilder builder = new ExpressionBuilder();

// Parse an expression
Expression ex = builder.build("your own(syntax) + you['decides']");

// Get the list of structures
List<Structure> structs = ex.getStructures();

// Or display as XML (debug)
String xml = ex.toXml();
```

## Credits

R. Bello