package rocks.inspectit.shared.cs.storage.nio.stream;

import java.io.IOException;
import java.util.List;

import rocks.inspectit.shared.cs.indexing.storage.IStorageDescriptor;
import rocks.inspectit.shared.cs.storage.IStorageData;

/**
 * Class that is used for providing the correct instance of {@link ExtendedByteBufferInputStream}
 * via Spring framework.
 *
 * @author Ivan Senic
 *
 */
public abstract class InputStreamProvider {

	/**
	 * @param storageData
	 *            {@link IStorageData} to get the data for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 *
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferInputStream}.
	 * @throws IOException
	 *             if input stream can not be obtained
	 */
	public ExtendedByteBufferInputStream getExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors) throws IOException {
		ExtendedByteBufferInputStream stream = createExtendedByteBufferInputStream();
		stream.setStorageData(storageData);
		stream.setDescriptors(descriptors);
		stream.prepare();
		return stream;
	}

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferInputStream}.
	 */
	protected abstract ExtendedByteBufferInputStream createExtendedByteBufferInputStream();
}
