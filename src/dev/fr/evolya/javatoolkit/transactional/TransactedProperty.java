package fr.evolya.javatoolkit.transactional;

import fr.evolya.javatoolkit.events.basic.Listener;
import fr.evolya.javatoolkit.transactional.TransactionEvent.ChangedEvent;

/**
 * @see http://www.agi.com/resources/help/online/agicomponents/Programmer's%20Guide/Overview/Tracking/Transactions.html
 * @see http://www.agi.com/resources/help/online/agicomponents/html/T_AGI_Foundation_Infrastructure_Threading_TransactedProperty_1.htm
 * @param <T> The type of the property. This type must be immutable (meaning it cannot be changed after it is constructed) or it must be treated as such.
 */
public class TransactedProperty<T> {
	
	private TransactionContext _context;
	private T _value;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public TransactedProperty(TransactionContext context) {
		_context = context;
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param value
	 */
	public TransactedProperty(TransactionContext context, T value) {
		_context = context;
		_value = value;
	}

	/**
	 * Renvoie la valeur de la propri�t� en dehors de toute transaction en cours.
	 * 
	 * @return
	 */
	public T getValue() {
		return _value;
	}
	
	/**
	 * Renvoie la valeur de la propri�t� au moment de la transaction.
	 * 
	 * @param transaction
	 * @return
	 */
	public T getValue(Transaction transaction) {

		//transaction.getModifications(this);
		return null;
		
	}
	
	/**
	 * Modifie la valeur de cette propri�t�.
	 * 
	 * @param transaction
	 * @param value
	 */
	public void setValue(Transaction transaction, T value) {
		// On ajoute l'op�ration de modification dans la transaction
		transaction.addModification(this, value);
	}
	
	/**
	 * Event lanc� quand la propri�t� est modifi�e.
	 * 
	 * @param listener
	 */
	public void onChanged(Listener<ChangedEvent> listener) {
		throw new IllegalAccessError();
	}
	
	/**
	 * @see TransactionContext#doTransactionally(Action)
	 */
	public void doTransactionally(Action<Transaction> callback) {
		_context.doTransactionally(callback);
	}
	
}
