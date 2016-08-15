package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.kristofa.brave.http.HttpClientRequest;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Ivan Senic
 *
 */
class ApacheHttpClientV40HttpRequest implements HttpClientRequest {

	/**
	 * FQN of the org.apache.http.HttpMessage.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_MESSAGE_FQN = "org.apache.http.HttpMessage";

	/**
	 * FQN of the org.apache.http.RequestLine.
	 */
	private static final String ORG_APACHE_HTTP_REQUEST_LINE_FQN = "org.apache.http.RequestLine";

	/**
	 * FQN of the org.apache.http.HttpRequest.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_REQUEST_FQN = "org.apache.http.HttpRequest";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Apache http request, instance of org.apache.http.HttpRequest.
	 */
	private final Object apacheHttpRequest;

	/**
	 * @param apacheHttpRequest
	 *            Apache http request, instance of org.apache.http.HttpRequest.
	 * @param cache
	 *            reflection cache to use
	 */
	ApacheHttpClientV40HttpRequest(Object apacheHttpRequest, ReflectionCache cache) {
		this.apacheHttpRequest = apacheHttpRequest;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getUri() {
		Object requestLine = cache.invokeMethod(apacheHttpRequest.getClass(), "getRequestLine", new Class<?>[] {}, apacheHttpRequest, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_REQUEST_FQN);
		if (null != requestLine) {
			String uri = (String) cache.invokeMethod(requestLine.getClass(), "getUri", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
			if (null != uri) {
				try {
					return new URI(uri);
				} catch (URISyntaxException e) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHttpMethod() {
		Object requestLine = cache.invokeMethod(apacheHttpRequest.getClass(), "getRequestLine", new Class<?>[] {}, apacheHttpRequest, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_REQUEST_FQN);
		if (null != requestLine) {
			return (String) cache.invokeMethod(requestLine.getClass(), "getMethod", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHeader(String header, String value) {
		cache.invokeMethod(apacheHttpRequest.getClass(), "addHeader", new Class<?>[] { String.class, String.class }, apacheHttpRequest, new Object[] { header, value }, null,
				ORG_APACHE_HTTP_HTTP_MESSAGE_FQN);
	}

}