package fr.evolya.javatoolkit.app;

public interface AppActivity {

	/**
	 * Lancer l'activité.
	 * Une StateChangeException peut être lancée si le démarrage de
	 * l'activité est impossible. 
	 */
	public void start();

	/**
	 * Couper l'activité, en lui laissant le temps de se fermer.
	 * C'est fait souvent par l'appel d'une méthode en interne, onStop,
	 * qui permet à l'implémentation de bien terminer son travail.
	 * La méthode interrupt() n'appel pas cette méthode, et coupe le/les
	 * thread(s) eventuellement actifs.
	 */
	public void stop();

	/**
	 * Couper l'activité de force, sans lui laisser le temps de finir son travail.
	 * Souvent les classes qui implémentent cette interface proposent une méthode
	 * interne (abstraite) pour effectuer un arrêt "propre". Cette méthode n'est
	 * pas lancée avec interrupt(), qui typiquement force l'arrêt des threads.
	 */
	public void interrupt();
	
	/**
	 * Renvoie TRUE si l'activitée est actuellement lancée.
	 */
	public boolean isStarted();

	/**
	 * Renvoie l'activité parente qui a lancé celle-ci, ou NULL si elle
	 * n'a pas de parent. Cette méthode permet de remonter l'arbre de lancement
	 * des activités.
	 */
	public AppActivity getInvoker();

}
