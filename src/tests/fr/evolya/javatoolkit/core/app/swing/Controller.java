package fr.evolya.javatoolkit.core.app.swing;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.event.ApplicationReady;
import fr.evolya.javatoolkit.app.event.ApplicationStopping;
import fr.evolya.javatoolkit.code.annotations.GuiTask;
import fr.evolya.javatoolkit.code.annotations.Inject;
import fr.evolya.javatoolkit.core.app.swing.Model.ModelChanged;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;

public class Controller {

	@Inject public App app;
	
	@Inject public Model model;
	
	@Inject public View view;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	@BindOnEvent(ApplicationReady.class)
	@GuiTask
	public void onStart() {
		view.setVisible(true);
		new Thread(model).start();
	}
	
	@BindOnEvent(ModelChanged.class)
	@GuiTask
	public void onModelChanged(Date date) {
		view.getLabel().setText(dateFormat.format(date));
	}
	
	@BindOnEvent(ApplicationStopping.class)
	@GuiTask
	public void onStop() {
		model.stop();
		view.setVisible(false);
	}
	
}
