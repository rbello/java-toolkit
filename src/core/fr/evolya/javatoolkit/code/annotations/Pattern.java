package fr.evolya.javatoolkit.code.annotations;

public enum Pattern {
	/**
	 * Vériable singleton
	 */
	Singleton,
	
	/**
	 * Possède une méthode getInstance() pour récupérer
	 * la dernière instance créée. Néanmoins on s'attend
	 * à n'avoir qu'une instance.
	 */
	DynamicSingleton,
	Observer,
	Decorator,
	Adapter
}
