package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache;

import java.util.Map;

import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.RemoteHttpInserterSensor;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link RemoteApacheHttpClientV40InserterHook} class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteApacheHttpClientV40InserterSensor extends RemoteHttpInserterSensor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameters) {
		hook = new RemoteApacheHttpClientV40InserterHook(platformManager, remoteIdentificationManager, brave);
	}

}
