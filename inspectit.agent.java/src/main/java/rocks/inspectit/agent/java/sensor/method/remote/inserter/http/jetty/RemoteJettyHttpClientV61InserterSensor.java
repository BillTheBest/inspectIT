package rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty;

import java.util.Map;

import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.RemoteHttpInserterSensor;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link RemoteJettyHttpClientV61InserterHook} class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteJettyHttpClientV61InserterSensor extends RemoteHttpInserterSensor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameters) {
		hook = new RemoteJettyHttpClientV61InserterHook(platformManager, remoteIdentificationManager, brave);
	}

}
