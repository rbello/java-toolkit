package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;

import fr.evolya.javatoolkit.code.editable.EditableProperty;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartesian;
import fr.evolya.javatoolkit.gui.swing.viewport.Marker;
import fr.evolya.javatoolkit.gui.swing.viewport.Marker.PointMarker;
import fr.evolya.javatoolkit.gui.swing.viewport.MarkerSelection;
import fr.evolya.javatoolkit.math.vecmath.Point2d;

public class PointBatchLayer extends Layer {

	public static final boolean LAST_POINT_IS_EXTREMITY = true;
	public static final boolean FIRST_POINT_IS_EXTREMITY = false;
	
	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * La liste des points.
	 */
	private ArrayList<PointMarker> _points = new ArrayList<PointMarker>();

	/**
	 * La propriété : nom du calque.
	 */
	protected EditableProperty<Color> _propertyColor =
		new EditableProperty<Color>("Color", Color.class, true, Color.RED);

	/**
	 * Mémorise le mode de sélection du point d'extremité.
	 */
	private boolean _extremity = LAST_POINT_IS_EXTREMITY;

	/**
	 * La sélection de marqueur en cours.
	 */
	private MarkerSelection _selection;
	
	/**
	 * Mémorisation de la couleur de sélection.
	 */
	private static Color SELECTION_COLOR = new Color(0, 123, 215);
	
	/**
	 * 
	 * @param point
	 */
	public void add(Cartesian point) {
		
		// Méfiance...
		if (point == null) {
			throw new NullPointerException();
		}
		
		// On ajoute le point dans la liste
		_points.add(new PointMarker(point));
		
	}

	/**
	 * Surcharge pour afficher les points.
	 */
	@Override
	public void paintComponent(Graphics g) {

		final Graphics2D g2 = (Graphics2D) g;

		// On prépare la couleur
		Color color = _selected ? SELECTION_COLOR : _propertyColor.getValue();
		
		// Le dernier point
		Point2d _last = null;
		
		// On fait une copie de la liste
		final ArrayList<PointMarker> copy = new ArrayList<PointMarker>(_points);
		
		// Configuration pour les lignes
		g.setColor(color);
		if (_selected) {
			g2.setStroke(new BasicStroke(2));
		}
		
		{
		
			// On va avoir besoin de la hauteur pour inverser la polarité de l'axe
			// des ordonnées. En effet, les coordonnées sur écran partent du bord
			// en haut à gauche, alors que les Cartesian prennent pour repère
			// le bas à droite.
			final double height = getBounds().getHeight();
			
			// Ratio de conversion des points
			final double ratio = getResizedRatio();
			
			// On parcours les points pour dessiner les lignes
			for (PointMarker point : copy) {
	
				// Conversion du point en coordonnées X;Y en pixels
				Point2d p = new Point2d(
						point.getPosition().X.getValue() * ratio,
						height - point.getPosition().Y.getValue() * ratio
				);
				
				// Save point
				point.setDisplayPosition(p);
				
				// Drawline
				if (_last != null) {
					g.drawLine((int)_last.getX(), (int)_last.getY(), (int)p.getX(), (int)p.getY());
				}
				
				// On enregistre ce point comme le dernier pour le prochain tour
				_last = p;
				
			}
			
		}
		
		// On va avoir besoin de ça pour déterminer si on est au
		// dernier point
		int i = 0, length = copy.size();
		
		// On parcours les points pour dessiner les sommets
		if (_selected) {
			for (PointMarker point : copy) {
				
				// On détermine si ce point est l'extremité
				boolean isExtremity = false;
				if (i++ == 0 && !_extremity) isExtremity = true;
				else if (_extremity && i == length) isExtremity = true;
				
				// On recup�re l'emplacement du point
				Point2d p = point.getDisplayPosition();
				
				// On affiche
				if (_selection != null && _selection.getMarker() == point) {
					g.setColor(color);
					g.fillRect((int)p.getX() - 5, (int)p.getY() - 5, 10, 10);
					g.setColor(Color.WHITE);
					g.fillRect((int)p.getX() - 3, (int)p.getY() - 3, 6, 6);
				}
				else {
					g.setColor(color);
					g.fillRect((int)p.getX() - 3, (int)p.getY() - 3, 8, 8);
				}
				
			}
		}
		
	}

	/**
	 * Renvoie la liste des marqueurs contenus dans ce calque.
	 */
	public ArrayList<PointMarker> getBatchPoints() {
		return _points;
	}
	
	@Override
	public EditableProperty<?>[] getEditableProperties() {
		return new EditableProperty<?>[] {
				_propertyLayerName,
				_propertyColor
		};
	}

	/**
	 * Cette méthode est utilisée par un controller qui surveille le mouvement
	 * du curseur, et qui cherche à savoir des marqueurs se trouvent à la
	 * position courante.
	 * Renvoyer un MarkerSelection si la position est adéquate pour sélectionner
	 * un marqueur.
	 */
	public MarkerSelection getSelectionAt(Point2d point, Cartesian position) {

		// On parcours les points du calque
		for (Marker m : _points) {
			
			// On recupère la position d'affichage du point
			Point2d p = m.getDisplayPosition();
			
			// Méfiance...
			if (p == null) continue;
			
			// On vérifie qu'on soit dans le périmètre du point
			if (   point.getX() >= p.getX() - 5
				&& point.getX() <= p.getX() + 5
				&& point.getY() >= p.getY() - 5
				&& point.getY() <= p.getY() + 5) {
				
				// On fabrique une sélection
				return new MarkerSelection(m, MarkerSelection.hoverCursor);
				
			}
			
		}
		
		// Le curseur n'est pas au dessus d'un marqueur
		return null;

	}

	/**
	 * Modifie la selection actuelle sur cet objet.
	 * Si selection vaut NULL, aucun point n'est sélectionné.
	 */
	public void setMarkerSelection(MarkerSelection selection) {
		_selection = selection;
	}
	
	/**
	 * Renvoie le polygon formé par les points de ce calque.
	 */
	public Polygon toPolygon() {
		Polygon shape = new Polygon();
		for (PointMarker m : _points) {
			shape.addPoint(
					m.getPosition().X.getValue().intValue(),
					m.getPosition().Y.getValue().intValue()
			);
		}
		return shape;
	}
	
	/**
	 * Renvoie la zone formée par les points de ce calque.
	 */
	public Area toArea() {
		return new Area(toPolygon());
	}
	
}
