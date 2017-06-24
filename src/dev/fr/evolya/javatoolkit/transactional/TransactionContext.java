package fr.evolya.javatoolkit.transactional;

import fr.evolya.javatoolkit.events.basic.Listener;
import fr.evolya.javatoolkit.transactional.TransactionEvent.CommittedEvent;

/**
 * @see http://www.agi.com/resources/help/online/agicomponents/html/T_AGI_Foundation_TransactionContext.htm
 */
public class TransactionContext {

	/**
	 * Raised each time that a transaction is committed in this context.
	 * 
	 * @param listener
	 */
	public void onCommitted(Listener<CommittedEvent> listener) {
		
	}
	
	/**
	 * Executes the provided callback within a transaction, and automatically commits the
	 * transaction when the callback returns. If the transaction conflicts (it raises a
	 * TransactionConflictException), the callback is executed again with a new transaction.
	 * This process continues until the transaction commits successfully.
	 * 
	 * @param callback
	 */
	public void doTransactionally(Action<Transaction> callback) {
		
		// Create the transaction
		Transaction transaction = new Transaction(this);
		
		// Lopp while transaction was commited
		while (true)
		{
		    try
		    {
		    	
		    	// Execute the action
		        callback.execute(transaction);
		        
		        // Commit the transaction
		        transaction.commit();
		        
		        // Stop looping
		        return;
		        
		    }
		    catch (TransactionConflictException ex)
		    {
		        // Ignore exception until we succeed.
		    }
		}
		
	}
	
}
