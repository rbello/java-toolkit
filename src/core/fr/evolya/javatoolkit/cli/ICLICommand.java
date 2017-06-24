package fr.evolya.javatoolkit.cli;

/**
 * Interface des commandes de l'interface CLI.
 */
public interface ICLICommand {

	/**
	 * Renvoie TRUE si la commande estime que l'opération est un succès.
	 * Il est possible de renvoyer un code d'erreur ici, en l'enregistrant dans l'objet de
	 * demande/réponse. Mais si ce n'est pas fait, alors les codes par défauts seront
	 * appliqués en fonction de boolean renvoyé : 200 pour TRUE, 501 pour FALSE.
	 */
	public boolean execute(Handle query) throws InterruptedException;
	
	/**
	 * Renvoie l'objet définissant les paramétres de commande acceptés.
	 * Il s'agit d'un objet qui sera setté par reflexion. Il doit donc mettre
	 * à disposition les champs qu'il souhaite voire associé.
	 */
	public Object getTargetObject();

}
