package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.kristofa.brave.http.HttpServerRequest;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Our implementation of the {@link HttpServerRequest} class defined by brave working with
 * {@link javax.servlet.HttpServletRequest}. We only need to extract data from the original
 * javax.servlet.HttpServletRequest.
 *
 * @author Ivan Senic
 *
 */
class JavaHttpServerRequest implements HttpServerRequest {

	/**
	 * FQN constant of the javax.servlet.HttpServletRequest.
	 */
	private static final String JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS = "javax.servlet.HttpServletRequest";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Object representing http servlet request.
	 */
	private final Object httpServletRequest;

	/**
	 * @param httpServletRequest
	 *            request object
	 * @param cache reflection cache to use
	 */
	JavaHttpServerRequest(Object httpServletRequest, ReflectionCache cache) {
		this.httpServletRequest = httpServletRequest;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getUri() {
		String uri = (String) cache.invokeMethod(httpServletRequest.getClass(), "getRequestURI", new Class<?>[] {}, httpServletRequest, new Object[] {}, null,
				JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
		if (uri != null) {
			try {
				return new URI(uri);
			} catch (URISyntaxException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHttpMethod() {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getMethod", new Class<?>[] {}, httpServletRequest, new Object[] {}, null, JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHttpHeaderValue(String headerName) {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getHeader", new Class<?>[] { String.class }, httpServletRequest, new Object[] { headerName }, null,
				JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
	}

}