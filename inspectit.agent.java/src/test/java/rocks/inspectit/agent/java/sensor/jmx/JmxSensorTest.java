package rocks.inspectit.agent.java.sensor.jmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * All JMX sensor tests disabled until we support JXM via the Configuration interface.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "unchecked", "PMD", "all" })
public class JmxSensorTest extends TestBase {

	@InjectMocks
	JmxSensor jmxSensor;

	@Mock
	JmxSensorTypeConfig sensorTypeConfig;

	@Mock
	IConnection connection;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	IPlatformManager platformManager;

	@Mock
	MBeanServer mBeanServer;

	@Mock
	ICoreService coreService;

	@Mock
	MBeanInfo mBeanInfo;

	@Mock
	Logger logger;

	public static class Init extends JmxSensorTest {

		@Test
		public void listenerRegistered() throws Exception {
			jmxSensor.init(sensorTypeConfig);

			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
		}

		@Test
		public void sensorSet() {
			JmxSensorTypeConfig sensorTypeConfig1 = mock(JmxSensorTypeConfig.class);

			jmxSensor.init(sensorTypeConfig1);

			assertThat(jmxSensor.getSensorTypeConfig(), is(sensorTypeConfig1));
		}

		@Test
		public void connectionOff() throws Exception {
			when(connection.isConnected()).thenReturn(false);

			jmxSensor.init(sensorTypeConfig);

			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);
		}

		@Test
		public void connectionOnRegister() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), captor.capture())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});

			jmxSensor.init(sensorTypeConfig);

			JmxAttributeDescriptor sensorConfig = (JmxAttributeDescriptor) captor.getValue().iterator().next();
			assertThat(sensorConfig.getId(), is(equalTo(definitionDataIdentId)));
			assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
			assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
			assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
			assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
			assertThat(sensorConfig.ismBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
			assertThat(sensorConfig.ismBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
			assertThat(sensorConfig.ismBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);
		}
	}

	public static class Update extends JmxSensorTest {

		@Test
		public void notInitialzed() {
			jmxSensor.update(coreService);

			verifyZeroInteractions(coreService, mBeanServer);
		}

		/**
		 * Tests the registration of a mBean.
		 *
		 * @throws Exception
		 *             any occurring exception
		 */
		@Test
		public void registerAndCollect() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String value = "value";
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(value);
			jmxSensor.init(sensorTypeConfig);

			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);

			ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
			verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

			assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
			assertThat(valueCaptor.getValue().getJmxSensorDefinitionDataIdentId(), is(equalTo(definitionDataIdentId)));
			assertThat(valueCaptor.getValue().getValue(), is(equalTo(value)));
		}

		/**
		 * Tests the registration of a mBean.
		 *
		 * @throws Exception
		 *             any occurring exception
		 */
		@Test
		public void registerMonitorNothing() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			String value = "value";
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenReturn(Collections.<JmxAttributeDescriptor> emptyList());
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(value);
			jmxSensor.init(sensorTypeConfig);

			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);
			verifyZeroInteractions(coreService);
		}

		@Test(dataProvider = "throwableProvider")
		public void collectDataException(Class<? extends Throwable> throwableClass) throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenThrow(throwableClass);
			jmxSensor.init(sensorTypeConfig);

			// update twice
			jmxSensor.update(coreService);
			jmxSensor.lastDataCollectionTimestamp = 0;
			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);
		}

		@Test
		public void collectDataNullValue() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(null);
			jmxSensor.init(sensorTypeConfig);

			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);

			ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
			verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

			assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
			assertThat(valueCaptor.getValue().getJmxSensorDefinitionDataIdentId(), is(equalTo(definitionDataIdentId)));
			assertThat(valueCaptor.getValue().getValue(), is("null"));
		}

		@Test
		public void collectDataPrimitiveArray() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(new int[] { 1, 2, 3 });
			jmxSensor.init(sensorTypeConfig);

			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);

			ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
			verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

			assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
			assertThat(valueCaptor.getValue().getJmxSensorDefinitionDataIdentId(), is(equalTo(definitionDataIdentId)));
			assertThat(valueCaptor.getValue().getValue(), is("[1, 2, 3]"));
		}

		@Test
		public void collectDataObjectArray() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(new String[] { "1", "2", "3" });
			jmxSensor.init(sensorTypeConfig);

			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);

			ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
			verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

			assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
			assertThat(valueCaptor.getValue().getJmxSensorDefinitionDataIdentId(), is(equalTo(definitionDataIdentId)));
			assertThat(valueCaptor.getValue().getValue(), is("[1, 2, 3]"));
		}

		@DataProvider(name = "throwableProvider")
		public Object[][] getThrowables() {
			return new Object[][] { { AttributeNotFoundException.class }, { InstanceNotFoundException.class }, { MBeanException.class }, { ReflectionException.class },
				{ RuntimeMBeanException.class } };
		}

	}

	public static class HandleNotification extends JmxSensorTest {

		@Test
		public void registrationNotification() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String value = "value";
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			jmxSensor.init(sensorTypeConfig);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), captor.capture())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});
			when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(value);
			MBeanServerNotification notification = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, this, 1, objectName);

			jmxSensor.handleNotification(notification, null);
			jmxSensor.update(coreService);

			JmxAttributeDescriptor sensorConfig = (JmxAttributeDescriptor) captor.getValue().iterator().next();
			assertThat(sensorConfig.getId(), is(equalTo(definitionDataIdentId)));
			assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
			assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
			assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
			assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
			assertThat(sensorConfig.ismBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
			assertThat(sensorConfig.ismBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
			assertThat(sensorConfig.ismBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).getAttribute(objectName, testAttributeName);
			verifyNoMoreInteractions(mBeanServer);

			ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
			verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

			assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
			assertThat(valueCaptor.getValue().getJmxSensorDefinitionDataIdentId(), is(equalTo(definitionDataIdentId)));
			assertThat(valueCaptor.getValue().getValue(), is(value));
		}

		@Test
		public void unregistrationNotification() throws Exception {
			long sensorType = 13L;
			long platformIdent = 11L;
			final long definitionDataIdentId = 17L;
			String testObjectName = "Testdomain:Test=TestObjectName,name=test";
			String testAttributeName = "TestAttributename";
			String testAttrDescription = "test-description";
			String testAttrType = "test-type";
			boolean testAttrIsReadable = true;
			boolean testAttrIsWriteable = false;
			boolean testAttrIsIs = false;
			MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
			MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
			ObjectName objectName = new ObjectName(testObjectName);

			when(sensorTypeConfig.getId()).thenReturn(sensorType);
			when(mBeanServer.queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
			when(mBeanServer.getMBeanInfo(Matchers.<ObjectName> any())).thenReturn(mBeanInfo);
			when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeJmxAttributes(eq(platformIdent), Matchers.<Collection<JmxAttributeDescriptor>> any())).thenAnswer(new Answer<Collection<JmxAttributeDescriptor>>() {
				public Collection<JmxAttributeDescriptor> answer(InvocationOnMock invocation) throws Throwable {
					Collection<JmxAttributeDescriptor> descriptors = (Collection<JmxAttributeDescriptor>) invocation.getArguments()[1];
					for (JmxAttributeDescriptor d : descriptors) {
						d.setId(definitionDataIdentId);
					}
					return descriptors;
				}
			});

			MBeanServerNotification notification = new MBeanServerNotification(MBeanServerNotification.UNREGISTRATION_NOTIFICATION, this, 1, objectName);
			jmxSensor.init(sensorTypeConfig);
			jmxSensor.handleNotification(notification, null);
			jmxSensor.update(coreService);

			verify(mBeanServer).queryNames(Matchers.<ObjectName> any(), (QueryExp) eq(null));
			verify(mBeanServer).getMBeanInfo(objectName);
			verify(mBeanServer).addNotificationListener(Matchers.<ObjectName> any(), eq(jmxSensor), Matchers.<NotificationFilter> any(), eq(null));
			verifyNoMoreInteractions(mBeanServer);
			verifyZeroInteractions(coreService);
		}
	}

}