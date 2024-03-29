package org.webcurator.core.store;

import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import javax.activation.DataHandler;
import java.util.Map;

public interface DigitalAssetStoreService {
    void save(String targetInstanceName, String[] filenames, DataHandler[] files) throws DigitalAssetStoreException;

    void save(String targetInstanceName, String filename, DataHandler file) throws DigitalAssetStoreException;

    void save(String targetInstanceName, String directory, String filename, DataHandler file)
            throws DigitalAssetStoreException;

    void save(String targetInstanceName, String directory, String[] filenames, DataHandler[] files)
            throws DigitalAssetStoreException;

    /**
     * Purge all the data from the digital asset store for the target instances
     * specified in the list of target instance names.
     *
     * @param targetInstanceNames the target instances to purge
     */
    void purge(String[] targetInstanceNames) throws DigitalAssetStoreException;

    /**
     * Purge all the data from the digital asset store for the target instances
     * specified in the list of **aborted** target instance names.
     *
     * @param targetInstanceNames the target instances to purge
     */
    void purgeAbortedTargetInstances(String[] targetInstanceNames) throws DigitalAssetStoreException;

    void submitToArchive(String targetInstanceOid, String SIP, Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException;

    /**
     * Initiate the indexing of the given HarvestResult.
     *
     * @param harvestResult The HarvestResult to index.
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void initiateIndexing(HarvestResultDTO harvestResult) throws DigitalAssetStoreException;

    /**
     * Initiate the removal of indexes of the given HarvestResult.
     *
     * @param harvestResult The HarvestResult to remove indexes for.
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void initiateRemoveIndexes(HarvestResultDTO harvestResult) throws DigitalAssetStoreException;

    /**
     * Check the given HarvestResult is still indexing.
     *
     * @param harvestResultOid The HarvestResult to abort.
     * @return true if the harvestResult is still indexing.
     * @throws DigitalAssetStoreException if any errors occur.
     */
    Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException;

    /**
     * Determine whether a custom deposit form is required to be shown before
     * submitting a harvest to a specific digital asset store.
     *
     * @param criteria Provides parameters that are required for DAS to make this decision.
     * @return The response DTO that indicates whether a custom form is required,
     * and if so, the location/content of the custom form.
     * @throws Exception thrown if there is an error
     */
    CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException;

    /**
     * Create screenshots for live and harvested seeds
     *
     * @param identifiers
     * @throws DigitalAssetStoreException
     */
    Boolean createScreenshots(Map identifiers) throws DigitalAssetStoreException;
}
