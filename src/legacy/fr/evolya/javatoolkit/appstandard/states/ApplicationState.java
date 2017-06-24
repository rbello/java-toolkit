package fr.evolya.javatoolkit.appstandard.states;

public abstract class ApplicationState {

	private Object _stateID;

	/**
	 * Constructeur
	 * @visibility package only, il faut utiliser les impl�mentations directes
	 */
	ApplicationState(Object stateID) {
		
		// V�rification de l'argument
		if (stateID == null) {
			throw new NullPointerException();
		}
		
		// On enregistre l'ID
		_stateID = stateID;
		
	}
	
	/**
	 * Renvoie l'identifiant de cet �tat
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
	 * Renvoie l'�tat par d�faut d'une application lanc�e
	 */
	public static StartedState getDefaultStartedState() {
		return new StartedState();
	}

	/**
	 * Renvoie l'�tat par d�faut pour une application stopp�e
	 */
	public static StoppedState getDefaultStoppedState() {
		return new StoppedState();
	}

	/**
	 * Renvoie l'�tat par d�faut pour une application en pause
	 */
	public static PausedState getDefaultPausedState() {
		return new PausedState();
	}

}
