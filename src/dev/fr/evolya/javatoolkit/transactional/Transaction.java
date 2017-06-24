package fr.evolya.javatoolkit.transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @see http://www.agi.com/resources/help/online/agicomponents/html/T_AGI_Foundation_Transaction.htm
 */
public class Transaction {
	
	private TransactionContext _context;
	
	private Map<TransactedProperty<?>, Object> _modifications;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public Transaction(TransactionContext context) {
		
		// On sauvegarde le contexte
		_context = context;
		
		// On fabrique une liste des modifications faites pendant cette transaction
		_modifications = new HashMap<TransactedProperty<?>, Object>();
		
	}

	/**
	 * Aborts this transactions. Any changes made by this transaction will be discarded.
	 */
	public void abort() {
		
	}
	
	/**
	 * Adds a modification to this transaction. This method is intended to be called
	 * within custom TransactedProperty implementations.
	 * 
	 * @param property
	 * @param value
	 */
	public void addModification(TransactedProperty<?> property, Object value) {
		
		if (property == null) {
			throw new NullPointerException();
		}
		
		_modifications.put(property, value);
		
	}
	
	/**
	 * Commits this transaction so that its changes are visible to other transactions.
	 * This method may throw TransactionConflictException if another transaction modifies
	 * one of the same values as this transaction and the other transaction commits first.
	 * Use DoTransactionally(Action<Transaction>) to automatically retry conflicting exceptions.
	 */
	public void commit() throws TransactionConflictException {
		
		
		
	}
	
	/**
	 * Disposes this transaction. It is safe to call this method on a transaction that has
	 * already been committed or aborted. If it has not already been committed or aborted,
	 * it will be aborted.
	 */
	public void dispose() {
		
	}
	
	/**
	 * Gets the transaction context in which this transaction operates. This transaction
	 * can only be used to access transacted objects in this context.
	 * 
	 * @return
	 */
	public TransactionContext getContext() {
		return _context;
	}
	
	/**
	 * Gets the number of this transaction. Multiple transactions may share a single number
	 * and the number of a transaction may change when it is committed.
	 * 
	 * @return
	 */
	public long getNumber() {
		return 0;
	}
	
	/**
	 * Gets a value indicating whether or not this transaction is still active.
	 * A transaction is active from the time it is constructed until commit(),
	 * abort(), or dispose() is called on it.
	 * 
	 * @return
	 */
	public boolean isActive() {
		return false;
	}

}
