package fr.evolya.javatoolkit.gui.swing.viewport.layers;

import java.awt.AWTEvent;

import fr.evolya.javatoolkit.appstandard.events.ViewListenerAdapter;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartesian;
import fr.evolya.javatoolkit.gui.swing.viewport.Cartographic;

public class LayerPanelListenerAdapter extends ViewListenerAdapter<AWTEvent>
	implements LayerPanelListener {

	@Override
	public void onZoomFactorChanged(double newValue, double oldValue) {
	}

	@Override
	public void onImageryLayerMouseMotion(Imagery2DLayer layer, Cartesian ca, Cartographic cg) {
	}

}
