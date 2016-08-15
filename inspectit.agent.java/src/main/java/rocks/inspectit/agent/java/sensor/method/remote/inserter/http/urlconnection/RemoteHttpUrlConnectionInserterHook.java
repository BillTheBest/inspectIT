package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.urlconnection;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpClientRequest;
import com.github.kristofa.brave.http.HttpClientRequestAdapter;
import com.github.kristofa.brave.http.HttpClientResponseAdapter;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.SpanNameProvider;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteDefaultInserterHook;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.RemoteHttpInserterHook;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;

/**
 * The hook implements the {@link RemoteDefaultInserterHook} class for the . It puts the InspectIT
 * header as additional header/attribute to the remote call. The hook invokes the methode
 * {@link #METHOD_NAME} to add the header attribute.
 *
 *
 * The methodes {@link #METHOD_NAME_URL} and {@link #METHOD_NAME_RESPONSE_CODE} are used to extraced
 * the called URL and the Response Code of the Request.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpUrlConnectionInserterHook extends RemoteHttpInserterHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteHttpUrlConnectionInserterHook.class);

	/**
	 * Default span name provider.
	 */
	private static final SpanNameProvider SPAN_NAME_PROVIDER = new DefaultSpanNameProvider();

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * HttpUrlConnection specific method name to add inspectIT header.
	 */
	private static final String METHOD_NAME = "addRequestProperty";

	/**
	 * HttpUrlConnection specific method to get uri.
	 */
	private static final String METHOD_NAME_URL = "getURL";

	/**
	 * HttpUrlConnection specific method to get status code.
	 */
	private static final String METHOD_NAME_RESPONSE_CODE = "getResponseCode";

	/**
	 * HttpUrlConnection specific method name to check if inspectIT header is already in place.
	 */
	private static final String METHOD_NAME_GET_REQUEST_PROPERTY = "getRequestProperty";

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteHttpUrlConnectionInserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager,Brave brave) {
		super(platformManager, remoteIdentificationManager, brave);
	}

	@Override
	protected boolean needToInsertInspectItHeader(Object object, Object[] parameters) {
		try {

			String inspectITHeader = (String) cache.invokeMethod(object.getClass(), METHOD_NAME_GET_REQUEST_PROPERTY, METHOD_PARAMETER_ONE_STRING_FIELD, object,
					new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER }, null);

			return inspectITHeader == null;

		} catch (Exception e) {
			LOG.warn("Check of InspectITHeader was not possible.", e);
			return true;
		}
	}

	@Override
	protected void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters) {
		long identification = remoteIdentificationManager.getNextIdentification();

		String inspectItHeader = getInspectItHeader(identification);

		try {
			cache.invokeMethod(object.getClass(), METHOD_NAME, METHOD_PARAMETER_TWO_STRING_FIELD, object, new Object[] { RemoteConstants.INSPECTIT_HTTP_HEADER, inspectItHeader }, null);

			RemoteHttpCallData remoteCallData = new RemoteHttpCallData();
			remoteCallData.setIdentification(identification);
			remoteCallData.setRemotePlatformIdent(0);
			this.threadRemoteCallData.set(remoteCallData);

			if (LOG.isDebugEnabled()) {
				LOG.debug("InspectITHeader inserted: " + inspectItHeader);
			}
		} catch (Exception e) {
			LOG.warn("Insertion of InspectITHeader was not possible. No Header Extention.", e);
		}

	}

	@Override
	protected int readResponseCode(Object object, Object[] parameters, Object result) {
		int returnValue = 0;
		try {

			Object statusCode = cache.invokeMethod(object.getClass(), METHOD_NAME_RESPONSE_CODE, METHOD_PARAMETER_EMPTY, object, null, null);

			returnValue = (Integer) statusCode;

			if (LOG.isDebugEnabled()) {
				LOG.debug("ResponseCode: " + returnValue);
			}

		} catch (Exception e) {
			LOG.warn("Could not read response code from Webrequest.");
		}

		return returnValue;
	}

	@Override
	protected String readURL(Object object, Object[] parameters, Object result) {
		String returnValue = "";
		try {

			URL uri = (URL) cache.invokeMethod(object.getClass(), METHOD_NAME_URL, METHOD_PARAMETER_EMPTY, object, null, null);

			returnValue = uri.toString();

			if (LOG.isDebugEnabled()) {
				LOG.debug("URL: " + returnValue);
			}

		} catch (Exception e) {
			LOG.warn("Could not read URL Object from Webrequest. No URL Information available.");
		}

		return returnValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientRequestAdapter getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		HttpClientRequest request = new UrlConnectionHttpRequest(urlConnection, cache);
		return new HttpClientRequestAdapter(request, SPAN_NAME_PROVIDER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		HttpResponse response = new UrlConnectionHttpResponse(urlConnection, cache);
		return new HttpClientResponseAdapter(response);
	}

}
