package fr.evolya.javatoolkit.threading.worker;

/**
 * Interface pour les opérations qui peuvent être managées.
 */
public interface IOperation extends IRunnable {
	
	/**
	 * Renvoie TRUE si l'opération est terminée.
	 */
	public boolean isCompleted();
	
	/**
	 * Renvoie TRUE s'il est possible d'annuler l'opération. Cela indique
	 * simplement si la méthode cancel() est supportée, par l'opération
	 * est en cours.
	 * TODO Il y a confusion entre cancel() et stop()
	 * TODO Cancel() ce serait pouvoir revenir en arrière de l'opération si
	 * TODO elle rate, alors que stop() serait pour arrêter en cours.
	 * TODO Actuellement ces fonctions sont utilisées pour stopper.
	 */
	public boolean isCancelable();

	/**
	 * Tente d'annuler l'opération. Si l'opération n'est pas supportée, il
	 * est recommandé de lever une UnsupportedOperationException. Il est
	 * possible de savoir à l'avance si on peut annuler une opération en
	 * appelant la méthode isCancelable().
	 * 
	 * Renvoie TRUE si l'opération a été annulée, FALSE en cas d'erreur ou
	 * si l'opération n'est plus en cours.
	 */
	public boolean cancel() throws UnsupportedOperationException;
	
	/**
	 * Renvoie TRUE si l'opération est terminée avec une erreur.
	 */
	public boolean hasFailed();
	
	/**
	 * Si l'opération a rencontré une erreur, cette méthode permet de
	 * récupérer l'exception à l'origine de l'erreur.
	 */
	public Throwable getFailure();
	
	/**
	 * Renvoie l'objet qui a initié l'action, ou NULL si l'action est orpheline.
	 */
	public Object getInitiator();
	
	/**
	 * Renvoie un pourcentage indiquant l'état d'avancement de l'opération.
	 * Il est recommandé de renvoyer 0 si l'opération n'a pas commencé, et
	 * 100 si elle est terminée.
	 * Pour indiquer qu'il n'est pas possible de déterminer l'avancement
	 * (mode indeterminate) il faut renvoyer Float.NaN.
	 */
	public float getOperationPercent();
	
	/**
	 * Renvoie une estimation du temps restant jusqu'à la fin de l'opération.
	 * Si l'opération n'est pas capable de déterminer ce temps, elle doit
	 * renvoyer une valeur < à 0.
	 * @return Le temps en secondes.
	 */
	public long getTimeRemaining();

	public void setFailure(Throwable ex);

}
