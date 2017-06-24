# Command Line Interface API

## Description

This package contains a toolkit to implements a Command Line Interface (CLI)

## State

This API is mostly Stable.

## Usage of FI implementation

Using annotations :

```java
// Fix console before
EclipseTools.fixConsole();
		
// Bind I/O
SystemInputOutputStream io = new SystemInputOutputStream(System.out, System.err, System.in) {
	@Override
	public PromptOutputStream prompt() {
		getOutputStream().print("anonymous@local $ ");
		return this;
	}
};

// Create a shell
final BasicShell shell = new BasicShell(io, io);

// Add a command
shell.addCommand("parseint", new Command() {
	public void handle(List<String> args, String plain, String cmd) {
		io.println("Enter a numeric value: ");
		io.read(new IntegerReader() {
			public void read(Integer value) {
				System.out.println("Read: " + value);
			}
		});
	}
});

// Display a debug if needed
shell.debug();

// Run shell
shell.start();
```

## Credits

R. Bello