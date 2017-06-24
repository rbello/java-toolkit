package fr.evolya.javatoolkit.gui.animation.swing;

import javax.swing.JSplitPane;

import fr.evolya.javatoolkit.gui.animation.Timeline;
import fr.evolya.javatoolkit.gui.animation.Timeline.TimelineState;
import fr.evolya.javatoolkit.gui.animation.callback.TimelineCallback;

public class JSplitAnimation implements TimelineCallback {

	private JSplitPane _pane;
	private int _nextPosition;
	private Runnable _callback;
	private Timeline _animation;
	private int _lastPosition;

	public JSplitAnimation(JSplitPane pane) {
		this(pane, null);
	}
	
	public JSplitAnimation(JSplitPane pane, Runnable onAnimationEnded) {
		_pane = pane;
		_callback = onAnimationEnded;
	}

	public synchronized void setSeparatorPosition(int dividerPosition) {
		
		// On ne fait rien
		if (
				// Si la position donn�e est invalide
				dividerPosition < 1 ||
				// Si la position donn�e est actuellement celle du diviseur
				dividerPosition == _pane.getDividerLocation()
				// Si on tente de r�p�ter la m�me op�ration
				|| dividerPosition == _nextPosition) {
			return;
		}
		
		// On m�morise la position actuelle et la position � atteindre
		_lastPosition = _pane.getDividerLocation();
		_nextPosition = dividerPosition;

		// Si une ancienne animation �tait en cours, on la retire
		if (_animation != null) {
			_animation.abort();
		}
		
		// On lance une nouvelle animation
		_animation = new Timeline();
		_animation.setDuration(200);
		_animation.addCallback(this);
		_animation.play();
		
	}

	@Override
	public void onTimelinePulse(float durationFraction, float timelinePosition) {
		
		// On calcule la distance entre la position de d�part et la position d'arriv�e
		final int distance = _nextPosition - _lastPosition;
		
		// On calcule la nouvelle position 
		int currentPosition = (int)(_lastPosition + (distance * durationFraction));
		
		// M�fiance...
		currentPosition = Math.max(currentPosition, 0);
		
		// On modifie la taille 
		_pane.setDividerLocation(currentPosition);
		
	}

	@Override
	public synchronized void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
		
		// Sur la fin d'animation
		if (newState == TimelineState.DONE) {
			
			// On s'assure de bien arriver au bout de l'animation. 
			_pane.setDividerLocation(_nextPosition);
			
			// On retire l'animation
			_animation = null;
			
			// On reset les positions
			_lastPosition = 0;
			_nextPosition = 0;
			
			// Et on lance la callback
			if (_callback != null) {
				_callback.run();
			}
			
		}
		
	}
	
}
