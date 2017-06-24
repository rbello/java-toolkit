package fr.evolya.javatoolkit.gui.swing.viewport;

import java.util.ArrayList;
import java.util.List;

import fr.evolya.javatoolkit.code.editable.EditableProperty;
import fr.evolya.javatoolkit.code.editable.IEditableObject;
import fr.evolya.javatoolkit.math.geodesy.GlobalCoordinates;
import fr.evolya.javatoolkit.math.geodesy.GlobalPosition;

/**
 * Un point définie par ses valeurs angulaires curvilignes, dans un repère cartographique
 * sphérique dont l'origine est le centre.
 * 
 * Coordonnées géographiques d'un lieu sur une planete, c-à-d un système de
 * trois coordonnées qui sont : la latitude, la longitude et l'altitude (ou l'élévation)
 * par rapport au centre de la planete.
 * 
 * ATTENTION = toutes les valeurs sont en radians.
 */
public class Cartographic implements IEditableObject {

	/**
	 * La longitude, en radians.
	 */
	public final EditableProperty<Double> Lon =
		new EditableProperty<Double>("Longitude", Double.class, true, 0d);
	
	/**
	 * La latitude, en radians.
	 */
	public final EditableProperty<Double> Lat =
		new EditableProperty<Double>("Latitude", Double.class, true, 0d);
	
	/**
	 * La longueur par rapport au centre, en mètres.
	 */
	public final EditableProperty<Double> Height =
		new EditableProperty<Double>("Height", Double.class, true, 0d);
	
	/**
	 * Constructeur par défaut.
	 */
	public Cartographic() {
		this(0, 0, 0);
	}
	
	/**
	 * Constructeur de copie.
	 */
	public Cartographic(Cartographic c) {
		this(c.Lon.getValue(), c.Lat.getValue(), c.Height.getValue());
	}
	
	/**
	 * Construteur paramétré.
	 */
	public Cartographic(double lonRad, double latRad, double heightMeters) {
		Lon.setValue(lonRad);
		Lat.setValue(latRad);
		Height.setValue(heightMeters);
	}

	@Override
	public EditableProperty<?>[] getEditableProperties() {
		return new EditableProperty<?>[] {
			Lon, Lat, Height
		};
	}
	
	@Override
	public String toString() {
		return "[Lon="
			+ Lon.getValue() + " Lat=" + Lat.getValue()
			+ " Height=" + Height.getValue() + "]";
	}
	
	/**
	 * Comparer les valeurs du point actuel avec le point donné.
	 */
	public boolean equals(Cartographic c) {
		return isEquals(this, c);
	}
	
	/**
	 * Calcule l'angle de bearing (relèvement) entre ce point et un autre.
	 * 
	 * Détermination de l'angle que fait la ligne d'un observateur vers un
	 * objet par rapport au Nord vrai ou géographique : positif dans le sens horaire.
	 * 
	 * Renvoie une valeur en radian.
	 * 
	 * @see https://fr.wikipedia.org/wiki/Rel%C3%A8vement
	 */
	public double bearing(Cartographic c) {
		return bearing(this, c);
	}
	
    /**
     * Calcule les points d'un cercle autour de ce point, dont les rayon est angularDistance.
     * Les points sont positionnés à la même altitude que ce point.
     * La méthode renvoie une liste dont la taille est stepsCount, contenant tous les points. 
     */
	public List<Cartographic> computeCirclePoints(float angularDistance, int stepsCount) {
		return computeCirclePoints(this, angularDistance, stepsCount);
	}
	
	/**
	 * Renvoie cette position cartographique sous forme de GlobalCoordinates.
	 */
	public GlobalCoordinates toGlobalCoordinates() {
		return new GlobalCoordinates(
				Math.toDegrees(Lat.getValue()),
				Math.toDegrees(Lon.getValue())
		);
	}
	
	/**
	 * Renvoie cette position cartographique sous forme de GlobalPosition.
	 */
	public GlobalPosition toGlobalPosition() {
		return new GlobalPosition(
				Math.toDegrees(Lat.getValue()),
				Math.toDegrees(Lon.getValue()),
				Math.toDegrees(Height.getValue())
		);
	}
	
	/**
	 * Renvoie un point cartographique vide, c-à-d positionné à 0,0,0.
	 */
	public static Cartographic empty() {
		return new Cartographic(0, 0, 0);
	}
	
	/**
	 * Comparer les valeurs de deux points cartographiques.
	 */
	public static boolean isEquals(Cartographic a, Cartographic b) {
		if (a == null || b == null) return false;
		if (a.Lon.getValue() != b.Lon.getValue()) return false;
		if (a.Lat.getValue() != b.Lat.getValue()) return false;
		if (a.Height.getValue() != b.Height.getValue()) return false;
		return true;
	}
	
    /**
     * Calcule l'angle de bearing entre deux points.
     * Les paramétres sont en radian, et la valeur retournée aussi.
     * 
     * @see https://en.wikipedia.org/wiki/Bearing_(navigation)
     * @see https://fr.wikipedia.org/wiki/Rel%C3%A8vement
     * 
     * The general formula for calculating the angle(bearing) between two points
     * is as follows:
     * 	θ = atan2(sin(Δlong)*cos(lat2), cos(lat1)*sin(lat2) − sin(lat1)*cos(lat2)*cos(Δlong))
	 */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double deltaLong = lon1 - lon2;
        double a = Math.sin(deltaLong) * Math.cos(lat2);
        double b = Math.cos(lat1) * Math.sin(lat2);
        double c = Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLong);
        double teta = Math.atan2(a, b - c);
        return teta;
    }
    
    /**
     * Calcule l'angle de bearing entre deux points.
     * Les paramétres sont en radian, et la valeur retournée aussi.
     * 
     * @alias bearing(double, double, double, double)
     */
    public static double bearing(Cartographic a, Cartographic b) {
    	return bearing(a.Lat.getValue(), a.Lon.getValue(), b.Lat.getValue(), b.Lon.getValue());
    }
    
    /**
     * @alias computeCirclePoints(double, double, double, float, int)
     */
    public static List<Cartographic> computeCirclePoints(Cartographic c, float angularDistance, int stepsCount) {
    	return computeCirclePoints(c.Lat.getValue(), c.Lon.getValue(), c.Height.getValue(), angularDistance, stepsCount);
    }
    
    /**
     * Calcule les points d'un cercle autour du point donné, dont les rayon est angularDistance.
     * Les points sont positionnés à l'altitude donnée.
     * La méthode renvoie une liste dont la taille est stepsCount, contenant tous les points. 
     */
    public static List<Cartographic> computeCirclePoints(double centerLatRad, double centerLonRad, double heightMeters, float angularDistance, int stepsCount) {

    	// On fabrique la liste des points
        ArrayList<Cartographic> pointsList = new ArrayList<Cartographic>();

        // On calcule l'angle en radians de chaque pas
        double stepAngleRad = 2 * Math.PI / stepsCount;

        // On va boucler antant de fois qu'il y a de pas
        for (int i = 0; i < stepsCount; i++) {

        	// La vraie course de l'angle en cours
            double trueCourse = i * stepAngleRad;

            // Calcule de la latitude
            double lat = Math.asin(
                Math.sin(centerLatRad) * Math.cos(angularDistance) +
                Math.cos(centerLatRad) * Math.sin(angularDistance) * Math.cos(trueCourse)
            );

            // Calcule de la longitude
            double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(centerLatRad),
                Math.cos(angularDistance) - Math.sin(centerLatRad) * Math.sin(lat)
            );
            double lon = ((centerLonRad + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

            // On ajoute un point dans la liste
            pointsList.add(new Cartographic(lat, lon, heightMeters));

        }

        // A la fin, on renvoie la liste des points
        return pointsList;

    }

}
