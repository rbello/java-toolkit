package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.appstandard.events.ViewListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.ViewBounds;

/**
 * Les vues sont des composants d'applications qui offrent une visualisation
 * graphique, comme des fenêtres, des panels ou des popups.
 * 
 * Ce ne sont pas directement des ApplicationPlugin : ils doivent étre
 * gérés par des ViewController pour pouvoir étre ajouté é une application. 
 *
 * @param <W> Le type de composant graphique que la vue offre (JPanel, JFrame, ...)
 * @param <E> Le type d'event qui est propagé par le framework (AWT event pour swing)
 * @param <C> Le type de composant contenu dans la vue
 */
@Deprecated
public interface AppView<W, E, C> {

	/**
	 * Masquer ou afficher l'interface graphique.
	 */
	public void setVisible(boolean visible);

	/**
	 * Renvoie l'état d'affichage de l'interface graphique.
	 * @return L'état de non affichage
	 */
	public boolean isVisible();

	/**
	 * Supprimer l'interface graphique et tous ses composants,
	 * ainsi que toutes les associations faites avec d'autres objets.
	 */
	public void dispose();
	
	/**
	 * Le bus des events des vues.
	 * 
	 * /!\ Penser à remplacer <? extends ViewListener<E>> par le véritable
	 * nom de classe, pour pouvoir utiliser bind() simplement.
	 */
	public EventSource<? extends ViewListener<E>> getEventsView();

	/**
	 * La dimension de la vue et sa position en x;y.
	 */
	public ViewBounds getViewBounds();

	/**
	 * Modifier la dimension de la vue et sa position en x;y.
	 */
	public void setViewBounds(ViewBounds viewBounds);

	/**
	 * Renvoie la liste des composants
	 */
	public C[] getComponents();

	/**
	 * Renvoie le controlleur
	 */
	public AppViewController<?, W, E, C> getViewController();
	
	/**
	 * Associe le controlleur
	 */
	public void setViewController(AppViewController<?, W, E, C> ctrl);
	
}
