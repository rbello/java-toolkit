package fr.evolya.javatoolkit.threading;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Parallel {
	
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public static <T> void For(final Iterable<T> elements, final Operation<T> operation) {
    	For(elements, operation, null);
    }
    
    public static <T> void For(final Iterable<T> elements, final Operation<T> operation, Runnable end) {
        try {
        	
        	// Create pool
        	ExecutorService forPool = Executors.newFixedThreadPool(NUM_CORES * 2, new ThreadFactory() {
        		int i = 0;
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "Parallel.For(" + i++ + ")");
				}
			});
        	
            // invokeAll blocks for us until all submitted tasks in the call complete
            forPool.invokeAll(createCallables(elements, operation));
            
            // Callback
            if (end != null) {
            	end.run();
            }
            
        }
        catch (InterruptedException e) {
            return;
        }
    }

    protected static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        for (final T elem : elements) {
            callables.add(new Callable<Void>() {
                @Override
                public Void call() {
                    operation.perform(elem);
                    return null;
                }
            });
        }

        return callables;
    }

    public static interface Operation<T> {
        public void perform(T pParameter);
    }
    
}