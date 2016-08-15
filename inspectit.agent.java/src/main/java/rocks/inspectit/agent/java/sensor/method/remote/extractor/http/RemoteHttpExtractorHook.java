package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.kristofa.brave.ServerSpan;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerRequest;
import com.github.kristofa.brave.http.HttpServerRequestAdapter;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import zipkin.Span;

/**
 * The hook implements the {@link RemoteHttpExtractorSensor} class. It extracts the inspectIT header
 * from a remote Call.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpExtractorHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteHttpExtractorHook.class);

	/**
	 * Default span name provider.
	 */
	private static final SpanNameProvider SPAN_NAME_PROVIDER = new DefaultSpanNameProvider();

	/**
	 * Brave instance.
	 */
	private final Brave brave;

	/**
	 * The ID manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The extractor.
	 */
	private final RemoteHttpParameterExtractor extractor;

	/**
	 * Helps us to ensure that we only store on remote call per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * Cache for the <code> Method </code> elements.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param extractor
	 *            The Extractor
	 * @param brave
	 *            Brave instance
	 */
	public RemoteHttpExtractorHook(IPlatformManager platformManager, RemoteHttpParameterExtractor extractor, Brave brave) {
		this.extractor = extractor;
		this.platformManager = platformManager;
		this.brave = brave;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!refMarker.isMarkerSet()) {
			// request is parameter one
			Object httpServletRequest = parameters[0];
			// brave processing
			HttpServerRequest serverRequest = new JavaHttpServerRequest(httpServletRequest, cache);
			ServerRequestAdapter requestAdapter = new HttpServerRequestAdapter(serverRequest, SPAN_NAME_PROVIDER);
			brave.serverRequestInterceptor().handle(requestAdapter);
		}

		refMarker.markCall();
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// no invocation marked -> skip
		if (!refMarker.isMarkerSet()) {
			return;
		}

		// remove mark from sub call
		refMarker.markEndCall();

	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// check if in the right(first) invocation
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {

			// call ended, remove the marker.
			refMarker.remove();

			// extract InspectItHeader Informations
			Object httpServletRequest = parameters[0];
			Object httpServletResponse = parameters[1];

			// get current span
			ServerSpan currentSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
			Span span = currentSpan.getSpan().toZipkin();
			// TODO add span to the closing invocation or core service problem it's not default
			// data?

			// brave processing
			HttpResponse response = new JavaHttpResponse(httpServletResponse, cache);
			ServerResponseAdapter responseAdapter = new HttpServerResponseAdapter(response);
			brave.serverResponseInterceptor().handle(responseAdapter);

			RemoteCallData data = extractor.getRemoteCallData(httpServletRequest);

			// just save data if insptectItHeader is available, it makes no sense without the header
			if (data != null) {
				try {

					long platformId = platformManager.getPlatformId();

					data.setPlatformIdent(platformId);
					data.setMethodIdent(methodId);
					data.setSensorTypeIdent(sensorTypeId);
					data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

					// returning gathered information
					coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.nanoTime()), data);
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the remote call data because of an unavailable id. " + e.getMessage());
					}
				}
			}

		}
	}

}