package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.evolya.javatoolkit.code.editable.EditableProperty;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartographic;
import fr.evolya.javatoolkit.gui.swing.viewport.ViewportManager;

public class Imagery2DLayer extends Layer {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * L'image brute, conservée dans un buffer.
	 */
	protected BufferedImage _img;
	
	/**
	 * Le cache de l'image redimensionnée.
	 */
	protected Image _cacheImg;
	
	/**
	 * Les bornes du cache
	 */
	protected Rectangle _cacheBounds;
	
	/**
	 * La propriété : nom du fichier image.
	 */
	private EditableProperty<File> _propertyFile =
		new EditableProperty<File>("File", File.class, false);

	/**
	 * La propriété : nom du circuit représenté.
	 */
	private EditableProperty<String> _propertyCircuitName =
		new EditableProperty<String>("CircuitName", String.class, true, "");
	
	/**
	 * La propriété : bord haut/gauche de l'image, en position cartographique.
	 */
	private EditableProperty<Cartographic> _propertyTopLeftPoint =
		new EditableProperty<Cartographic>("TopLeft", Cartographic.class, true, Cartographic.empty());
	
	/**
	 * La propriété : bord bas/droit de l'image, en position cartographique.
	 */
	private EditableProperty<Cartographic> _propertyBottomRightPoint =
		new EditableProperty<Cartographic>("BottomRight", Cartographic.class, true, Cartographic.empty());
	
	/**
	 * Surcharge pour afficher l'image.
	 */
	@Override
    public void paintComponent(Graphics g) {
    	if (_img != null) {
    		g.drawImage(getResizedImage(), 0, 0, null);
    	}
    }

    public Image getResizedImage() {
    	
    	// Actual resized bounds
    	Rectangle bounds = getBounds();
    	
    	// Final picture
    	Image image = null;
    	
    	// Cache
    	if (_cacheImg != null) {
    		if (_cacheBounds.equals(bounds)) {
    			// Use cache
    			image = _cacheImg;
    		}
    	}
    	
    	if (image == null) {
    		
    		// Generate resized image
    		image = ViewportManager.resizeImage(_img, (int)bounds.getWidth(), (int)bounds.getHeight());
    		
    		// Save cache
    		_cacheImg = image;
    		_cacheBounds = bounds;
    		
    	}
    	
		return image;
    }
	
	public void setImage(File file) throws IOException {
		
    	// On vide le cache
    	_cacheImg = null;
    	_cacheBounds = null;
    	
    	// Si un fichier existait, il n'existe plus...
    	_propertyFile.setValue(null);
    	
		// Lecture de l'image. Cette ligne peut lever les IOException.
		_img = ImageIO.read(file);

		// On enregistre les bornes initiales
		setOriginalBounds(0, 0, _img.getWidth(), _img.getHeight());
		
		// Sauvegarde du chemin vers le fichier
		_propertyFile.setValue(file);
	    
	}

	public File getImageFile() {
		return _propertyFile.getValue();
	}

	@Override
	public EditableProperty<?>[] getEditableProperties() {
		return new EditableProperty<?>[] {
			_propertyLayerName,
			_propertyFile,
			_propertyCircuitName,
			_propertyTopLeftPoint,
			_propertyBottomRightPoint
		};
	}

	public EditableProperty<Cartographic> getPropertyBottomRightPosition() {
		return _propertyBottomRightPoint;
	}

	public EditableProperty<String> getPropertyCircuitName() {
		return _propertyCircuitName;
	}

	public EditableProperty<File> getPropertyFile() {
		return _propertyFile;
	}

	public EditableProperty<Cartographic> getPropertyTopLeftPosition() {
		return _propertyTopLeftPoint;
	}

	protected void onSelectionChanged(boolean selected) {
		// TODO
	}
     
}
