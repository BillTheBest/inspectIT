package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.kristofa.brave.http.HttpClientRequest;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Ivan Senic
 *
 */
class JettyHttpClientV61HttpRequest implements HttpClientRequest {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Jetty http exchange object, instance of org.mortbay.jetty.client.HttpExchange.
	 */
	private final Object jettyHttpExchange;

	/**
	 * @param jettyHttpExchange
	 *            Jetty http exchange object, instance of org.mortbay.jetty.client.HttpExchange.
	 * @param cache
	 *            reflection cache to use
	 */
	JettyHttpClientV61HttpRequest(Object jettyHttpExchange, ReflectionCache cache) {
		this.jettyHttpExchange = jettyHttpExchange;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getUri() {
		String uri = (String) cache.invokeMethod(jettyHttpExchange.getClass(), "getURI", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
		if (null != uri) {
			try {
				return new URI(uri);
			} catch (URISyntaxException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHttpMethod() {
		return (String) cache.invokeMethod(jettyHttpExchange.getClass(), "getMethod", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHeader(String header, String value) {
		cache.invokeMethod(jettyHttpExchange.getClass(), "addRequestHeader", new Class<?>[] { String.class, String.class }, jettyHttpExchange, new Object[] { header, value }, null);
	}

}