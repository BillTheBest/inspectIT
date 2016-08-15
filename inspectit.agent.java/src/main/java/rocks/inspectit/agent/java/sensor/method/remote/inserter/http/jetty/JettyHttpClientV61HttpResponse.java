package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty;

import com.github.kristofa.brave.http.HttpResponse;

/**
 * @author Ivan Senic
 *
 */
final class JettyHttpClientV61HttpResponse implements HttpResponse {

	/**
	 * Instance for usage.
	 */
	static final JettyHttpClientV61HttpResponse INSTANCE = new JettyHttpClientV61HttpResponse();

	/**
	 * Private constructor, no initialization.
	 */
	private JettyHttpClientV61HttpResponse() {
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * Cannot read response code since is asynchronous connection.
	 */
	public int getHttpStatusCode() {
		return 0;
	}

}