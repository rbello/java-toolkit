package fr.evolya.javatoolkit.appstandard.states;

public abstract class ApplicationState {

	private Object _stateID;

	/**
	 * Constructeur
	 * @visibility package only, il faut utiliser les implémentations directes
	 */
	ApplicationState(Object stateID) {
		
		// Vérification de l'argument
		if (stateID == null) {
			throw new NullPointerException();
		}
		
		// On enregistre l'ID
		_stateID = stateID;
		
	}
	
	/**
	 * Renvoie l'identifiant de cet état
	 */
	public Object getStateID() {
		return _stateID;
	}
	
	/**
	 * 
	 * @param state
	 * @return
	 */
	public final boolean equals(ApplicationState state) {
		return state._stateID.equals(_stateID);
	}
	
	/**
	 * Renvoie l'état par défaut d'une application lancée
	 */
	public static StartedState getDefaultStartedState() {
		return new StartedState();
	}

	/**
	 * Renvoie l'état par défaut pour une application stoppée
	 */
	public static StoppedState getDefaultStoppedState() {
		return new StoppedState();
	}

	/**
	 * Renvoie l'état par défaut pour une application en pause
	 */
	public static PausedState getDefaultPausedState() {
		return new PausedState();
	}

}
