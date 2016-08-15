package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache;

import com.github.kristofa.brave.http.HttpResponse;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Ivan Senic
 *
 */
class ApacheHttpClientV40HttpResponse implements HttpResponse {

	/**
	 * FQN of the org.apache.http.StatusLine.
	 */
	private static final String ORG_APACHE_HTTP_STATUS_LINE_FQN = "org.apache.http.StatusLine";

	/**
	 * FQN of the org.apache.http.HttpResponse.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_RESPONSE_FQN = "org.apache.http.HttpResponse";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Apache http response, instance of org.apache.http.HttpResponse.
	 */
	final Object apacheHttpResponse;

	/**
	 * @param apacheHttpResponse
	 *            Apache http response, instance of org.apache.http.HttpResponse.
	 * @param cache
	 *            reflection cache to use
	 */
	ApacheHttpClientV40HttpResponse(Object apacheHttpResponse, ReflectionCache cache) {
		this.apacheHttpResponse = apacheHttpResponse;
		this.cache = cache;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getHttpStatusCode() {
		int result = 0;
		Object statusLine = cache.invokeMethod(apacheHttpResponse.getClass(), "getStatusLine", new Class<?>[] {}, apacheHttpResponse, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_RESPONSE_FQN);
		if (null != statusLine) {
			result = (Integer) cache.invokeMethod(statusLine.getClass(), "getStatusCode", new Class<?>[] {}, statusLine, new Object[] {}, Integer.valueOf(0), ORG_APACHE_HTTP_STATUS_LINE_FQN);
		}
		return result;
	}

}