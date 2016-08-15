package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.urlconnection;

import java.util.Map;

import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.RemoteHttpInserterSensor;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link RemoteHttpUrlConnectionInserterHook} class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpUrlConnectionInserterSensor extends RemoteHttpInserterSensor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameters) {
		hook = new RemoteHttpUrlConnectionInserterHook(platformManager, remoteIdentificationManager, brave);
	}

}
