# Security

## Description

A set of tools used for credentiel authentication, Time-based One-time Password or other security purpose.

## State

This API is currently Stable.

## Usage

Using HOTP generator :

```java
// Create generator
HOTP generator = new HOTP("LJHL5P65A5QCJ7GB");

// Set key's time to live
generator.setTTL(30000); // Key are valid 30 secondes

// Generate current code
int currentCode = ga.now();
```

## Credits

- R. Bello
- The Apache Software Foundation (ASF licence)