package fr.evolya.javatoolkit.net.http;


/**
 * Ce thread peut �tre utilis� pour lancer une requ�te HTTP de mani�re
 * asynchrone, en notifiant une HttpCallback.
 */
public class HttpAsynchThread extends Thread {

	public HttpAsynchThread(final HttpRequest request, final HttpContext context, final HttpCallback callback) {
		super(new Runnable() {
			@Override
			public void run() {
				
				// On supprime le thread
				request.setAsynchThread(null);
				
				// On execute la requ�te HTTP
				Http.execute(request, context);
				
				// Failure (for exception)
				if (request.hasFailed()) {
					callback.onFailure(
						request.getReturnCode(),
						request.getReturnMessage(),
						request,
						request.getFailureException()
					);
				}
				
				// Success
				else if (request.getReturnCode() == 200) {
					callback.onSuccess(request);
				}
				
				// Failure
				else {
					callback.onFailure(
						request.getReturnCode(),
						request.getReturnMessage(), 
						request,
						null
					);
				}
				
			}
		});
	}
	
}
