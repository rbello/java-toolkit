# Soho Connector API

## Description

A small framework to implements unit tests.

## State

This API is currently Stable.

## Usage

Sample using Assert class:

```java

public class MyTest {

	private Point point;

	@BeforeTests
	public void before() {
		// Prepare before tests
		this.point = new Point();
	}
	
	@TestMethod
	public void unorderedTestMethod() {
		Assert.notNull(this.point.x);
		Assert.notNull(this.point.y);
	}
	
	@TestMethod(2)
	public void orderedTestMethod() {
		Assert.equals(this.point.x, 0);
		Assert.equals(this.point.y, 0);
	}
	
	@AfterTests
	public void after() {
		// Clean after tests
		this.point = null;
	}

	public static void main(String[] args) {
		Assert.runTests(MyTest.class);
	}

}

```

## Credits

R. Bello