package fr.evolya.javatoolkit.transactional;

public class Main {

	public final TransactionContext context = new TransactionContext();
	public final TransactedProperty<Integer> value = new TransactedProperty<Integer>(context);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Test du Transactional");
		
		final Main object = new Main();
		
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					object.value.doTransactionally(new Action<Transaction>() {
						@Override
						public void execute(Transaction transaction) {
							int v = (int)(Math.random() * 999999);
							System.out.println("Transaction n�" + transaction.getNumber() + " value=" + v);
							object.value.setValue(transaction, v);
						}
					});
				}
			}
		};
		
		Thread t1 = new Thread(runnable);
		Thread t2 = new Thread(runnable);
		Thread t3 = new Thread(runnable);
		
		t1.start();
		t2.start();
		t3.start();
		
		
	}

}
