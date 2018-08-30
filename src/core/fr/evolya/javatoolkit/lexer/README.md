# Lexer API

## Description

A tool for implementing an interpreter to create custom languages.

## State

This API is currently in development.

```
                ╔═══════════╗
                ║ Structure ║
                ╚═══════════╝
                      ^ Implements
       ┌──────────────┼────────────────┬──────────────┐
       │              │                │              │
╔════════════╗  ╔════════════╗  ╔════════════╗  ╔════════════╗
║ Expression ║  ║  Operator  ║  ║    Token   ║  ║   String   ║
╚════════════╝  ╚════════════╝  ╚════════════╝  ╚════════════╝
                      ^ Extends        ^ Extends
                      │                │
                ╔════════════╗  ╔════════════╗
                ║  Multiply  ║  ║   Symbol   ║ Eg. literals
                ╚════════════╝  ╚════════════╝
                ╔════════════╗  ╔════════════╗
                ║  Division  ║  ║   Double   ║
                ╚════════════╝  ╚════════════╝
                ╔════════════╗  ╔════════════╗
                ║    Plus    ║  ║    Float   ║
                ╚════════════╝  ╚════════════╝
                ╔════════════╗  ╔════════════╗
                ║    Minus   ║  ║     True   ║
                ╚════════════╝  ╚════════════╝
                ╔════════════╗  ╔════════════╗
                ║   Modulo   ║  ║    False   ║
                ╚════════════╝  ╚════════════╝


╔════════════╗ 0..1      ╔════════════╗
║ Expression ║-----------║ Structure  ║
╚════════════╝      0..n ╚════════════╝
```

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