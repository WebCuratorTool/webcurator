package org.webcurator.core.visualization.browser;

import org.apache.commons.httpclient.Header;
import org.webcurator.core.exceptions.DigitalAssetStoreException;

import java.nio.file.Path;
import java.util.List;

public interface VisWayBackService {
    /**
     * Retrieve a resource from the Digital Asset Store. The resource is
     * returned as a SOAP attachment and written to disk for use. This is ideal
     * for streaming large resources.
     *
     * @param targetInstanceId    The OID of the target instance that the
     *                            resource belongs to.
     * @param harvestResultNumber The index of the harvest result, within the
     *                            target instance, that contains the resource.
     * @param resourceUrl         The resource to retrieve.
     * @return The resource, as a file.
     * @throws DigitalAssetStoreException if there are any errors.
     */
    Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException;

    /**
     * Retrieves a resource transferring it as a byte array, rather than as
     * a SOAP attachment. This method is appropriate for small resources where
     * streaming is not required. It has additional memory requirements, but
     * has no file I/O.
     *
     * @param targetInstanceId    The OID of the target instance that the
     *                            resource belongs to.
     * @param harvestResultNumber The index of the harvest result, within the
     *                            target instance, that contains the resource.
     * @param resourceUrl         The resource to retrieve.
     * @return The resource, as a file.
     * @throws DigitalAssetStoreException if there are any errors.
     */
    byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException;

    /**
     * Retrieve the HTTP headers for a given resource.
     *
     * @param targetInstanceId    The OID of the target instance that the
     *                            resource belongs to.
     * @param harvestResultNumber The index of the harvest result, within the
     *                            target instance, that contains the resource.
     * @param resourceUrl         The resource for which to retrieve the headers.
     * @return An array of HTTP Headers.
     * @throws DigitalAssetStoreException if there are any errors.
     */
    List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException;
}
