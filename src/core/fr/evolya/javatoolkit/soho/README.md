# Soho Connector API

## Description

An API to connect to a Soho web server and trigger operations.

## State

This API is currently in Stable.

## Usage

```java

// Create connector
SohoConnector soho = new SohoConnector("https://mySohoServer.com/");

// Open a session and login
ISohoSession session = soho.open();
session.auth("login", "password");
boolean logged = session.isLogged();

// Send a CLI command to server
session.sendCLI("help");

// Send a POST request to server
session.post("/page.php", data);

```

## Credits

R. Bello