package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.urlconnection;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.github.kristofa.brave.http.HttpClientRequest;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Ivan Senic
 *
 */
class UrlConnectionHttpRequest implements HttpClientRequest {

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
	UrlConnectionHttpRequest(Object urlConnection, ReflectionCache cache) {
		this.urlConnection = urlConnection;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getUri() {
		URL url = (URL) cache.invokeMethod(urlConnection.getClass(), "getURL", new Class<?>[] {}, urlConnection, new Object[] {}, null);
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHttpMethod() {
		return (String) cache.invokeMethod(urlConnection.getClass(), "getRequestMethod", new Class<?>[] {}, urlConnection, new Object[] {}, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHeader(String header, String value) {
		cache.invokeMethod(urlConnection.getClass(), "addRequestProperty", new Class<?>[] { String.class, String.class }, urlConnection, new Object[] { header, value }, null);
	}

}