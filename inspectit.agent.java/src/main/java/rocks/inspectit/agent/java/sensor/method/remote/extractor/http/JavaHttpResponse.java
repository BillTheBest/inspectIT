package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerRequest;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Our implementation of the {@link HttpServerRequest} class defined by brave working with
 * {@link javax.servlet.HttpServletResponse}. We only need to extract data from the original
 * javax.servlet.HttpServletResponse.
 *
 * @author Ivan Senic
 *
 */
class JavaHttpResponse implements HttpResponse {

	/**
	 * FQN of the javax.servlet.HttpServletResponse.
	 */
	private static final String JAVAX_SERVLET_HTTP_SERVLET_RESPONSE_FQN = "javax.servlet.HttpServletResponse";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Cache for the <code> Method </code> elements. One {@link ReflectionCache} for all the
	 * instances of this class.
	 */
	private final Object httpServletResponse;

	/**
	 * @param httpServletResponse
	 *            response object
	 * @param cache reflection cache to use
	 */
	JavaHttpResponse(Object httpServletResponse, ReflectionCache cache) {
		this.httpServletResponse = httpServletResponse;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHttpStatusCode() {
		return (Integer) cache.invokeMethod(httpServletResponse.getClass(), "getStatus", new Class<?>[] {}, httpServletResponse, new Object[] {}, 0, JAVAX_SERVLET_HTTP_SERVLET_RESPONSE_FQN);
	}

}