package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.urlconnection;

import com.github.kristofa.brave.http.HttpResponse;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Ivan Senic
 *
 */
class UrlConnectionHttpResponse implements HttpResponse {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Http url connection, instance of java.net.HttpURLConnection.
	 */
	private final Object urlConnection;

	/**
	 * @param urlConnection
	 *            Http url connection, instance of java.net.HttpURLConnection.
	 * @param cache
	 *            reflection cache to use
	 */
	UrlConnectionHttpResponse(Object urlConnection, ReflectionCache cache) {
		this.urlConnection = urlConnection;
		this.cache = cache;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getHttpStatusCode() {
		return (Integer) cache.invokeMethod(urlConnection.getClass(), "getResponseCode", new Class<?>[] {}, urlConnection, new Object[] {}, Integer.valueOf(0));
	}

}