package rocks.inspectit.agent.java.sensor.method.remote.inserter.http;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.kristofa.brave.Brave;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache.RemoteApacheHttpClientV40InserterHook;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link RemoteApacheHttpClientV40InserterHook} class.
 *
 * @author Thomas Kluge
 *
 */
public abstract class RemoteHttpInserterSensor extends AbstractMethodSensor {

	/**
	 * The hook.
	 */
	protected RemoteHttpInserterHook hook = null;

	/**
	 * Brave instance.
	 */
	@Autowired
	protected Brave brave;

	/**
	 * The ID manager.
	 */
	@Autowired
	protected IPlatformManager platformManager;

	/**
	 * The remoteIdentificationManager provides a unique identification for each remote call.
	 */
	@Autowired
	protected RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
