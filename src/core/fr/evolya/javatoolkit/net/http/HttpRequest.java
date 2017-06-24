package fr.evolya.javatoolkit.net.http;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

	protected String requestMethod = null;
	protected URL requestURL = null;
	protected Map<String, Object> requestData = new HashMap<String, Object>();
	protected int returnCode = 0;
	protected String returnMessage = "";
	protected String returnBody = null;
	protected boolean keepAlive = false;
	protected HttpURLConnection connection = null;
	protected Thread asynchThread = null;
	protected Map<String, String> _returnHeaders = new HashMap<String, String>();
	private Exception _failureException;

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public URL getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(URL requestURL) {
		this.requestURL = requestURL;
	}
	
	public void setRequestURL(String requestURL) throws MalformedURLException {
		this.requestURL = new URL(requestURL);
	}
	
	public void setRequestURL(String url, Map<String, String> params) throws MalformedURLException {
		throw new IllegalAccessError();
	}
	
	public void setRequestURL(String url, Map<String, String> params, String hash) throws MalformedURLException {
		throw new IllegalAccessError();
	}

	public Map<String, Object> getRequestData() {
		return requestData;
	}

	public void setRequestData(Map<String, Object> requestData) {
		this.requestData = requestData;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	public String getReturnBody() {
		return returnBody;
	}

	public void setReturnBody(String returnBody) {
		this.returnBody = returnBody;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public synchronized HttpURLConnection getConnection() {
		return connection;
	}

	public synchronized void setConnection(HttpURLConnection connection) {
		this.connection = connection;
	}

	public synchronized Thread getAsynchThread() {
		return asynchThread;
	}

	public synchronized void setAsynchThread(Thread asynchThread) {
		this.asynchThread = asynchThread;
	}
	
	public synchronized void abort() {
		
	}

	public String getRequestDataRaw() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : requestData.keySet()) {
			if (i++ > 0) {
				sb.append('&');
			}
			sb.append(Http.encodeURL(key));
			sb.append('=');
			sb.append(Http.encodeURL("" + requestData.get(key)));
		}
		return sb.toString();
	}

	public void setReturnHeaders(Map<String, String> headers) {
		_returnHeaders  = headers;
	}
	
	public Map<String, String> getReturnHeaders() {
		return _returnHeaders;
	}
	
	public void addReturnHeader(String name, String value) {
		_returnHeaders.put(name, value);
	}

	public void failure(Exception ex) {
		_failureException = ex;
	}
	
	public boolean hasFailed() {
		return _failureException != null;
	}

	public Exception getFailureException() {
		return _failureException;
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public boolean isSuccess() {
		return getReturnCode() >= 200 && getReturnCode() < 300;
	}
	
	@Override
	public String toString() {
		return "HttpRequest[url=" + requestURL + ";method=" + requestMethod + ";returnCode="
				+ returnCode + ";returnMsg=" + returnMessage + "]";
	}
	
}
